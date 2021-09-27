package progressed.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class TeamLaserBlastBulletType extends BulletType{
    public float length, width;
    
    public TeamLaserBlastBulletType(float damage, float speed){
        super(damage, speed);
        trailWidth = -1f;
    }

    @Override
    public void init(){
        super.init();

        if(trailWidth < 0) trailWidth = width / 2f;
    }

    @Override
    public void update(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            Tmp.v1.trns(b.rotation() - 180f, length / 2f - width / 2f);
            b.trail.update(b.x + Tmp.v1.x, b.y + Tmp.v1.y);
        }

        //All this just to change the trail a little
        if(homingPower > 0.0001f && b.time >= homingDelay){
            Teamc target;
            //home in on allies if possible
            if(healPercent > 0){
                target = Units.closestTarget(null, b.x, b.y, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team,
                    t -> collidesGround && (t.team != b.team || t.damaged()));
            }else{
                target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
            }

            if(target != null){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
            }
        }

        if(weaveMag > 0){
            b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale/2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
        }

        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);

        Draw.color(b.team.color);
        PMDrawf.pill(b.x, b.y, b.rotation(), length, width);

        Draw.color(Color.white);
        PMDrawf.pill(b.x, b.y, b.rotation(), length / 2f, width / 2f);

        Draw.color();
    }

    @Override
    public void hit(Bullet b, float x, float y){
        b.hit = true;
        hitEffect.at(x, y, b.rotation(), b.team.color); //Yes this line is literally the only difference from base code.
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

        Effect.shake(hitShake, hitShake, b);

        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragCone/2) + fragAngle;
                fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
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
            Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(healPercent > 0f){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
                    other.heal(healPercent / 100f * other.maxHealth());
                });
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }

        for(int i = 0; i < lightning; i++){
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

}