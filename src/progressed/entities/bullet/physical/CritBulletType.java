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
        if(b.data == null) b.data = Mathf.chance(critChance);
        if((boolean)b.data) b.damage *= critMultiplier;

        super.init(b);
    }

    @Override
    public void update(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new PMTrail(trailLength);
            }
            b.trail.length = trailLength;
            ((PMTrail)(b.trail)).updateRot(b.x, b.y, b.rotation());
        }

        if(Mathf.chanceDelta(1) && (boolean)b.data){
            critEffect.at(b.x, b.y, b.rotation(), b.team.color);
        }

        if(homingPower > 0.0001f && b.time >= homingDelay){
            Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.collided.contains(e.id), t -> collidesGround && !b.collided.contains(t.id));
            if(target != null){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
            }
        }

        if(weaveMag > 0){
            b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale/2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
        }

        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.x, b.y, trailParam, trailColor);
            }
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
        b.fdata = 1f;
        super.despawned(b);
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            TrailFadeFx.PMTrailFade.at(b.x, b.y, trailWidth, backColor, ((PMTrail)(b.trail)).copyPM());
        }
    }

    @Override
    public void hit(Bullet b, float x, float y){
        b.hit = true;
        if(b.fdata != 1f || despawnHitEffects){
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
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    @Override
    public float damageMultiplier(Bullet b){
        float critMul = 1f;
        if(b.isAdded() && (boolean)b.data) critMul = critMultiplier;

        return super.damageMultiplier(b) * critMul;
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets / 2) * fragSpread);
                Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                if(f.type instanceof CritBulletType) f.data = b.data;
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
}
