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
    public float suctionRadius = 160f, size = 6f, damageRadius = 17f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 2f;
    public int swirlTrailLength = 8;
    public boolean repel;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = EnergyFx.blackHoleDespawn;
        layer = Layer.weather + 2;
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
                if(swirlTrailLength > 0){
                    swirlEffect.at(b.x, b.y, suctionRadius, new Object[]{b, new Trail(swirlTrailLength)});
                }else{
                    swirlEffect.at(b.x, b.y, suctionRadius, b);
                }
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
                        absorbBullet(b, other);
                    }
                }
            });
        }

        super.update(b);
    }

    @Override
    public void draw(Bullet b){
        Fill.light(b.x, b.y, Lines.circleVertices(size), size, color, Tmp.c1.set(color).lerp(b.team.color, 0.5f));
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
            (type instanceof OrbitalStrikeBulletType) ||
            (type instanceof BlackHoleBulletType) ||
            (type instanceof MagmaBulletType);
    }

    public void absorbBullet(Bullet b, Bullet other){
        if(absorbEffect != Fx.none) absorbEffect.at(other.x, other.y);
        if(other.type.trailLength > 0 && other.trail != null && other.trail.size() > 0){
            if(other.trail instanceof PMTrail t){
                TrailFadeFx.PMTrailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, t.copyPM());
            }else{
                Fx.trailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, other.trail.copy());
            }
        }
        other.type = PMBullets.absorbed;
        other.absorb();
    }
}
