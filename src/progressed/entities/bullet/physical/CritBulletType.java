package progressed.entities.bullet.physical;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class CritBulletType extends BasicBulletType{
    public float critChance = 0.15f, critMultiplier = 5f;
    public Effect critEffect = OtherFx.crit;
    public boolean bouncing, despawnHitEffects = true;

    public CritBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
        pierce = true;
        pierceBuilding = true;
        impact = true;
        ammoMultiplier = 1;
        shootEffect = Fx.shootBig;
        smokeEffect = Fx.shootBigSmoke;
        hitEffect = OtherFx.critPierce;
        hitColor = Pal.lightOrange;
        trailLength = 10;
        trailWidth = -1f;
    }

    public CritBulletType(float speed, float damage){
        this(speed, damage, "bullet");
    }

    @Override
    public void init(){
        super.init();

        if(trailWidth < 0f) trailWidth = width * (10f / 52f); //Should match up with normal bullet sprite
    }

    @Override
    public void init(Bullet b){
        if(b.data == null){
            if(Mathf.chance(critChance)){
                b.data = new CritBulletData(true);
            }else{
                b.data = new CritBulletData(false);
            }
        }
        if(((CritBulletData)b.data).crit) b.damage *= critMultiplier;

        super.init(b);
    }

    @Override
    public void updateTrailEffects(Bullet b){
        super.updateTrailEffects(b);

        if(Mathf.chanceDelta(1) && ((CritBulletData)b.data).crit){
            critEffect.at(b.x, b.y, b.rotation(), b.team.color);
        }
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
        super.hitEntity(b, other, initialHealth);

        bounce(b);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);

        if(direct){
            bounce(b);
        }
    }

    @Override
    public void despawned(Bullet b){
        ((CritBulletData)b.data).despawned = true;
        super.despawned(b);
    }

    @Override
    public void hit(Bullet b, float x, float y){
        CritBulletData data = (CritBulletData)b.data;
        boolean crit = data.crit;
        float critBonus = crit ? this.critMultiplier : 1f;
        b.hit = true;
        if(!data.despawned || despawnHitEffects){
            hitEffect.at(x, y, b.rotation(), hitColor);
            hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        }

        Effect.shake(hitShake, hitShake, b);

        if(fragOnHit){
            createFrags(b, x, y);
        }
        createPuddles(b, x, y);
        createIncend(b, x, y);

        if(suppressionRange > 0){
            //bullets are pooled, require separate Vec2 instance
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
        }

        createSplashDamage(b, x, y);

        for(int i = 0; i < lightning; i++){
            Lightning.create(b, lightningColor, (lightningDamage < 0 ? damage : lightningDamage) * critBonus, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null){
            boolean crit = ((CritBulletData)b.data).crit;
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets / 2) * fragSpread);
                Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                if(f.type instanceof CritBulletType) f.data = new CritBulletData(crit);
            }
        }
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y){
        float critBonus = ((CritBulletData)b.data).crit ? this.critMultiplier : 1f;

        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier() * critBonus, false, collidesAir, collidesGround, scaledSplashDamage, b);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(heals()){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    healEffect.at(other.x, other.y, 0f, healColor, other.block);
                    other.heal((healPercent / 100f * other.maxHealth() + healAmount) * critBonus);
                });
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }

    public void bounce(Bullet b){
        if(bouncing){
            Teamc target = Units.closestTarget(b.team, b.x, b.y, range * b.fout(),
                e -> e.isValid() && e.checkTarget(collidesAir, collidesGround) && !b.collided.contains(e.id),
                t -> t.isValid() && collidesGround && !b.collided.contains(t.id)
            );
            if(target != null){
                b.vel.setAngle(b.angleTo(target));
            }
        }
    }

    public static class CritBulletData{
        public boolean crit, despawned;

        public CritBulletData(boolean crit){
            this.crit = crit;
        }
    }
}
