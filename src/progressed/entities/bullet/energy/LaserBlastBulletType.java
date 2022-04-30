package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class LaserBlastBulletType extends BulletType{
    public float length, width;
    
    public LaserBlastBulletType(float damage, float speed){
        super(damage, speed);
        trailWidth = -1f;
        shootEffect = smokeEffect = Fx.none;
        displayAmmoMultiplier = false;
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

        Draw.color(hitColor);
        PMDrawf.pill(b.x, b.y, b.rotation(), length, width);

        Draw.color(Color.white);
        PMDrawf.pill(b.x, b.y, b.rotation(), length / 2f, width / 2f);

        Draw.color();
    }
}
