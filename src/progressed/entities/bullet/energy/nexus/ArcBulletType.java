package progressed.entities.bullet.energy.nexus;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ArcBulletType extends BulletType{
    public ArcBulletType(float speed){
        super(speed, 0f);

        collides = false;
        trailLength = 8;
    }

    @Override
    public void init(Bullet b){
        if(b.data instanceof ArcBulletData a){
            a.init(b);
        }else{
            b.remove(); //Invalid data
        }

        super.init(b);
    }

    @Override
    public void update(Bullet b){
        ((ArcBulletData)b.data).update(b);
        super.update(b);
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new HeightTrail(trailLength);
            }
            HeightTrail trail = (HeightTrail)b.trail;
            trail.length = trailLength;
            trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)), ((ArcBulletData)b.data).z);
        }
    }

    public static class ArcBulletData{
        public float xAccel, yAccel, z, zVel, gravity;

        public ArcBulletData(float z, float zVel, float gravity){
            this.z = z;
            this.zVel = zVel;
            this.gravity = gravity;
        }

        public ArcBulletData(float z, float zVel){
            this(z, zVel, 1f);
        }

        public void init(Bullet b){ //Calculus :D
            //Calculate lifetime
            float life = PMMathf.quadratic(-0.5f * gravity, zVel, z);
            b.lifetime(life);

            //Calculate accels
            float dx = b.aimX - b.x;
            xAccel = (2 * (dx - b.vel.x * life)) / (life * life);
            float dy = b.aimY - b.y;
            yAccel = (2 * (dy - b.vel.y * life)) / (life * life);
        }

        public void update(Bullet b){
            b.vel.add(xAccel * Time.delta, yAccel * Time.delta);
            z += zVel * Time.delta;
            zVel -= gravity * Time.delta;
        }

        /** Calculates proper initial velocity for the bullet. Normal, horizontal velocity is stored in the input vec2, vertical velocity is returned as a float. */
        public static float calcVel(Vec2 vec, float speed, float angle, float tilt, float inaccuracy){
            PMMathf.randomCirclePoint(vec, inaccuracy);
            float horiInacc = vec.x;
            float vertInacc = vec.y;
            vec.set(
                Angles.trnsx(angle + horiInacc, speed),
                Angles.trnsy(angle + horiInacc, speed)
            );
            return Mathf.sin(tilt + vertInacc) * speed;
        }
    }
}
