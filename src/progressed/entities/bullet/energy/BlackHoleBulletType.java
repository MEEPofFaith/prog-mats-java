package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.entities.bullet.explosive.*;
import progressed.graphics.*;

public class BlackHoleBulletType extends BulletType{
    public Color color = Color.black;
    public Effect absorbEffect = EnergyFx.blackHoleAbsorb, swirlEffect = EnergyFx.blackHoleSwirl;
    public float cataclysmRadius = 20f * 8f;
    public float cataclysmForceMul = 5f, cataclysmBulletForceMul = 5f, cataclysmForceRange = 40f * 8f;
    public float suctionRadius = 160f, size = 6f, damageRadius = 17f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 2f;
    public float swirlSize = 6f;
    public boolean repel;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = EnergyFx.blackHoleDespawn;
    }

    @Override
    public float continuousDamage(){
        return damage / 2f * 60f; //Damage every 2 ticks
    }

    @Override
    public void update(Bullet b){
        if(b.timer(1, 2f)){
            PMDamage.completeDamage(b.team, b.x, b.y, damageRadius, b.damage);
            
            if(swirlEffect != Fx.none && b.time <= b.lifetime - swirlEffect.lifetime){
                swirlEffect.at(b.x, b.y, Mathf.random(360f), b);
            }

            Units.nearbyEnemies(b.team, b.x - suctionRadius, b.y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, unit -> {
                if(unit.within(b.x, b.y, suctionRadius)){
                    Vec2 impulse = Tmp.v1.trns(unit.angleTo(b), force + (1f - unit.dst(b) / suctionRadius) * scaledForce);
                    if(repel) impulse.rotate(180f);
                    unit.impulseNet(impulse);
                }
            });

            Groups.bullet.intersect(b.x - suctionRadius, b.y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, other -> {
                if(other != null && Mathf.within(b.x, b.y, other.x, other.y, suctionRadius) && b != other && b.team != other.team && other.type.speed > 0.01f && !checkType(other.type)){
                    Vec2 impulse = Tmp.v1.trns(other.angleTo(b), bulletForce + (1f - other.dst(b) / suctionRadius) * bulletScaledForce);
                    if(repel) impulse.rotate(180f);
                    other.vel().add(impulse);

                    //manually move bullets to simulate velocity for remote players
                    if(other.isRemote()){
                        other.move(impulse.x, impulse.y);
                    }

                    if(Mathf.within(b.x, b.y, other.x, other.y, size * 2f)){
                        if(other.type instanceof BlackHoleBulletType type){
                            float radius = (cataclysmRadius + type.cataclysmRadius) / 2f;
                            if(radius > 0){ //Do not create negative radius cataclysms. I have no idea what this would cause anyways.
                                float thisUMul = cataclysmForceMul * Mathf.sign(!repel), thisBMul = cataclysmBulletForceMul * Mathf.sign(!repel);
                                float otherUMul = type.cataclysmForceMul * Mathf.sign(!type.repel), otherBMul = type.cataclysmBulletForceMul * Mathf.sign(!type.repel);

                                CataclysmData cData = new CataclysmData(){{
                                    r = radius;
                                    f = (force * thisUMul + type.force * otherUMul) / 2f;
                                    sF = (scaledForce * thisUMul + type.scaledForce * otherUMul) / 2f;
                                    bF = (bulletForce * thisBMul + type.bulletForce * otherBMul) / 2f;
                                    bSF = (bulletScaledForce * thisBMul + type.bulletScaledForce * otherBMul) / 2f;
                                    rg = (cataclysmForceRange + type.cataclysmForceRange / 2f);
                                    c1 = b.team.color;
                                    c2 = other.team.color;
                                }};

                                float midX = (b.x + other.x) / 2f;
                                float midY = (b.y + other.y) / 2f;

                                Effect.shake(radius / 1.5f, radius * 1.5f, midX, midY);
                                PMBullets.cataclysm.create(b.owner, b.team, midX, midY, 0f, 0f, 1f, 1f, cData);
                                absorbBullet(b, other, true);
                            }
                        }else{
                            absorbBullet(b, other, false);
                        }
                    }
                }
            });
        }

        super.update(b);
    }

    @Override
    public void draw(Bullet b){
        Draw.z(Layer.max - 0.01f);
        Fill.light(b.x, b.y, 60, size,
            Tmp.c1.set(b.team.color).lerp(color, 0.5f + Mathf.absin(Time.time + Mathf.randomSeed(b.id), 10f, 0.4f)), color);
    }

    @Override
    public void drawLight(Bullet b){
        Drawf.light(b, lightRadius, b.team.color, lightOpacity);
    }

    @Override
    public void despawned(Bullet b){
        despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);

        hitSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);

        if(!b.hit && (fragBullet != null || splashDamageRadius > 0f || lightning > 0)){
            hit(b);
        }
    }

    public static boolean checkType(BulletType type){ //Returns true for bullets immune to suction.
        return (type instanceof BallisticMissileBulletType) ||
            (type instanceof BlackHoleCataclysmType) ||
            (type instanceof MagmaBulletType);
    }

    public void absorbBullet(Bullet b, Bullet other, boolean cataclysm){
        if(!cataclysm){
            if(absorbEffect != Fx.none) absorbEffect.at(other.x, other.y);
        }
        if(other.type.trailLength > 0 && other.trail != null && other.trail.size() > 0){
            if(other.trail instanceof PMTrail t){
                UtilFx.PMTrailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, t.copyPM());
            }else{
                Fx.trailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, other.trail.copy());
            }
        }
        other.type = PMBullets.absorbed;
        other.absorb();
        if(cataclysm){
            if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
                if(b.trail instanceof PMTrail t){
                    UtilFx.PMTrailFade.at(b.x, b.y, b.type.trailWidth, b.type.trailColor, t.copyPM());
                }else{
                    Fx.trailFade.at(b.x, b.y, b.type.trailWidth, b.type.trailColor, b.trail.copy());
                }
            }
            b.type = PMBullets.absorbed;
            b.absorb();
        }
    }

    public static class CataclysmData{
        //radius, uForce, uScaledForce, bForce, bScaledForce, range
        protected float r, f, sF, bF, bSF, rg;
        //color 1, color 2
        protected Color c1, c2;
        //has converted floor to space
        protected boolean space;

        public CataclysmData(){}
    }
}
