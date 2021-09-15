package progressed.entities.bullet;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import progressed.content.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class CritBulletType extends BasicBulletType{
    public float critChance = 0.15f, critMultiplier = 5f;
    public Effect critEffect = PMFx.sniperCrit;
    public boolean bouncing, despawnHitEffects = true;

    public CritBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
        pierce = true;
        pierceBuilding = true;
        impact = true;
        ammoMultiplier = 1;
        shootEffect = Fx.shootBig;
        smokeEffect = Fx.shootBigSmoke;
        hitEffect = PMFx.critPierce;
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
    public void update(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new PMTrail(trailLength);
            }
            b.trail.length = trailLength;
            ((PMTrail)(b.trail)).updateRot(b.x, b.y, b.rotation());
        }

        if(Mathf.chanceDelta(1) && ((CritBulletData)b.data).crit){
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
    public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
        super.hitTile(b, build, initialHealth, direct);

        if(direct){
            bounce(b);
        }
    }

    @Override
    public void despawned(Bullet b){
        if(b.data instanceof CritBulletData data){
            data.despawned = true;
        }
        super.despawned(b);
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            PMFx.PMTrailFade.at(b.x, b.y, trailWidth, backColor, ((PMTrail)(b.trail)).copyPM());
        }
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

        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragCone/2) + fragAngle;
                if(fragBullet instanceof CritBulletType critB){
                    critB.create(b.owner, b.team, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, -1f, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax), new CritBulletData(crit));
                }else{
                    fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                }
            }
        }

        if(puddleLiquid != null && puddles > 0){
            for(int i = 0; i < puddles; i++){
                Tile tile = world.tileWorld(x + Mathf.range(puddleRange), y + Mathf.range(puddleRange));
                Puddles.deposit(tile, puddleLiquid, puddleAmount);
            }
        }

        if(Mathf.chance(incendChance)){
            Damage.createIncend(x, y, incendSpread, incendAmount);
        }

        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier() * critBonus, collidesAir, collidesGround);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(healPercent > 0f){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
                    other.heal(healPercent * critBonus / 100f * other.maxHealth());
                });
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }

        for(int i = 0; i < lightning; i++){
            Lightning.create(b, lightningColor, (lightningDamage < 0 ? damage : lightningDamage) * critBonus, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    public void bounce(Bullet b){
        if(bouncing){
            Teamc target = Units.closestTarget(b.team, b.x, b.y, range() * b.fout(),
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