package progressed.entities.bullet.physical;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class DelayBulletType extends BasicBulletType{
    float aimCone = 5f, aimRadius = 12f, aimHomingPower = 0.35f;
    float launchedSpeed = 4.5f, launchedDrag = -0.005f;

    public DelayBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
    }

    @Override
    public void update(Bullet b){
        updateTrail(b);

        DelayBulletData data = (DelayBulletData)b.data;

        if(!data.fired){
            data.fired = b.time >= data.delay;
            if(data.fired){
                b.time = 0f;
                b.vel.setLength(launchedSpeed);
                b.drag = launchedDrag;
            }
        }

        if(data.fired){
            if(!data.aimed){
                float targetAngle = b.angleTo(Tmp.v1.set(data.x, data.y));

                b.vel.setAngle(Angles.moveToward(b.rotation(), targetAngle, aimHomingPower * Time.delta * 50f));

                boolean nearby = b.within(data.x, data.y, aimRadius);
                if(Angles.angleDist(b.rotation(), targetAngle) <= aimCone || nearby) data.aimed = true;
            }else{
                Teamc target;
                //home in on allies if possible
                if(healPercent > 0){
                    target = Units.closestTarget(null, b.x, b.y, homingRange,
                        e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                        t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                    );
                }else{
                    target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
                }

                if(target != null){
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                }
            }
        }
    }

    public static class DelayBulletData{
        final float x, y, delay;
        boolean fired, aimed;

        public DelayBulletData(float x, float y, float delay){
            this.x = x;
            this.y = y;
            this.delay = delay;
        }
    }
}