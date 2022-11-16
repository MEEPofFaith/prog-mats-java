package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
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
    static Seq<Class<?>> immuneTypes = Seq.with(
        OrbitalStrikeBulletType.class,
        BallisticMissileBulletType.class,
        BlackHoleBulletType.class,
        MagmaBulletType.class,
        SweepLaserBulletType.class,
        ContinuousBulletType.class,
        LaserBulletType.class,
        SapBulletType.class,
        ShrapnelBulletType.class
    );

    public Color color = Color.black;
    public Effect absorbEffect = EnergyFx.blackHoleAbsorb, swirlEffect = EnergyFx.blackHoleSwirl;
    public float suctionRadius = 160f, size = 6f, damageRadius = 17f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 2f;
    public float swirlInterval = 3f;
    public int swirlEffects = 4;
    public boolean repel;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = EnergyFx.blackHoleDespawn;
        layer = Layer.effect + 0.03f;
    }

    @Override
    public float continuousDamage(){
        return damage / 2f * 60f; //Damage every 2 ticks
    }

    @Override
    public void update(Bullet b){
        if(b.timer(1, 2f)){
            PMDamage.completeDamage(b.team, b.x, b.y, damageRadius, b.damage);

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
                        absorbBullet(other);
                    }
                }
            });
        }

        super.update(b);
    }

    @Override
    public void updateTrailEffects(Bullet b){
        super.updateTrailEffects(b);

        if(swirlInterval > 0f && b.time <= b.lifetime - swirlEffect.lifetime){
            if(b.timer(0, swirlInterval)){
                int sign = Mathf.sign(Mathf.randomSeed(b.id, -1, 0));
                for(int i = 0; i < swirlEffects; i++){
                    swirlEffect.at(b.x, b.y, suctionRadius * sign, b.team.color, b);
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        Fill.light(b.x, b.y, Lines.circleVertices(size), size, color, b.team.color);
        Draw.z(Layer.effect + 0.01f);
        Fill.light(b.x, b.y, Lines.circleVertices(size), size, color, b.team.color);
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
        return immuneTypes.contains(c -> c.isAssignableFrom(type.getClass()));
    }

    public void absorbBullet(Bullet bullet){
        if(absorbEffect != Fx.none) absorbEffect.at(bullet.x, bullet.y);
        if(bullet.type.trailLength > 0 && bullet.trail != null && bullet.trail.size() > 0){
            if(bullet.trail instanceof PMTrail t){
                TrailFadeFx.PMTrailFade.at(bullet.x, bullet.y, bullet.type.trailWidth, bullet.type.trailColor, t.copyPM());
            }else{
                Fx.trailFade.at(bullet.x, bullet.y, bullet.type.trailWidth, bullet.type.trailColor, bullet.trail.copy());
            }
        }
        bullet.type = PMBullets.absorbed;
        bullet.absorb();
    }
}
