package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.renders.*;

public class BlackHoleBulletType extends BulletType{
    static Seq<Class<?>> immuneTypes = Seq.with(
        ArcBulletType.class, //TODO Maybe implement a special case for these 3D bullets? Make suction radius spherical.
        BlackHoleBulletType.class,
        MagmaBulletType.class,
        ContinuousBulletType.class,
        LaserBulletType.class,
        SapBulletType.class,
        ShrapnelBulletType.class
    );

    public Color color = Color.black;
    public Effect absorbEffect = EnergyFx.blackHoleAbsorb, swirlEffect = EnergyFx.blackHoleSwirl;
    public float suctionRadius = 160f, size = 6f, lensEdge = -1f, damageRadius = 17f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 1f;
    public float bulletDamage = 10f;
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
    public void init(){
        super.init();
        if(lensEdge < 0f) lensEdge = suctionRadius / 2f;

        drawSize = Math.max(drawSize, lensEdge * 2f);
    }

    @Override
    public float continuousDamage(){
        return damage / 2f * 60f; //Damage every 2 ticks
    }

    @Override
    public void update(Bullet b){
        if(b.timer(1, 2f)){
            float fout = fout(b);
            PMDamage.completeDamage(b.team, b.x, b.y, damageRadius * fout, b.damage);

            float sR = suctionRadius * fout;
            Units.nearbyEnemies(b.team, b.x - sR, b.y - sR, sR * 2f, sR * 2f, unit -> {
                if(unit.within(b.x, b.y, sR)){
                    Vec2 impulse = Tmp.v1.trns(unit.angleTo(b), force + (1f - unit.dst(b) / sR) * scaledForce);
                    if(repel) impulse.rotate(180f);
                    unit.impulseNet(impulse);
                }
            });

            Groups.bullet.intersect(b.x - sR, b.y - sR, sR * 2f, sR * 2f, other -> {
                if(other != null && !checkType(other.type) && Mathf.within(b.x, b.y, other.x, other.y, sR) && b != other && b.team != other.team && other.type.speed > 0.01f){
                    Vec2 impulse = Tmp.v1.trns(other.angleTo(b), bulletForce + (1f - other.dst(b) / sR) * bulletScaledForce);
                    if(repel) impulse.rotate(180f);
                    other.vel().add(impulse);

                    //manually move bullets to simulate velocity for remote players
                    if(other.isRemote()){
                        other.move(impulse.x, impulse.y);
                    }

                    if(other.type.hittable && Mathf.within(b.x, b.y, other.x, other.y, size * 2f)){
                        float realDamage = bulletDamage * damageMultiplier(b);
                        if(other.damage > realDamage){
                            other.damage(other.damage - realDamage);
                        }else{
                            other.remove();
                        }
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
        float fout = fout(b);
        PMRenders.blackHole(b.x, b.y, size * fout, lensEdge * fout, b.team.color);
    }

    @Override
    public void drawLight(Bullet b){
        //none
    }

    public float fout(Bullet b){
        return Interp.sineOut.apply(1 - Mathf.curve(b.time, b.lifetime - swirlEffect.lifetime, b.lifetime));
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
}
