package progressed.entities.bullet.pseudo3d;

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
import progressed.content.effects.*;
import progressed.graphics.trails.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ArcBulletType extends BulletType{
    public boolean zAbsorbable = true;
    public Color absorbEffectColor = Pal.missileYellowBack;
    public Effect absorbEffect = Pseudo3DFx.absorbedSmall;

    public ArcBulletType(float speed){
        super(speed, 0f);

        collides = hittable = absorbable = reflectable = false;
        trailLength = 8;
        shootEffect = smokeEffect = Fx.none;
        scaledSplashDamage = true; //Doesn't collide, will rely on splash damage.
    }

    @Override
    public void init(Bullet b){
        if(b.data instanceof ArcBulletData){
            arcBulletDataInit(b);
        }else{
            b.remove(); //Invalid data
        }
        b.damage = splashDamage * b.damageMultiplier(); //Doesn't collide, put splash damage in instead. Used for shield absorption calculation.

        super.init(b);
    }

    public void arcBulletDataInit(Bullet b){
        ArcBulletData a = (ArcBulletData)b.data;
        a.updateLifetime(b);
        a.updateAccel(b);
    }

    @Override
    public void update(Bullet b){
        ((ArcBulletData)b.data).update(b);
        super.update(b);
    }

    @Override
    public void despawned(Bullet b){
        if(b.absorbed) return;
        super.despawned(b);
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            TrailFadeFx.heightTrailFade.at(b.x, b.y, trailWidth, trailColor, b.trail.copy());
        }
    }

    @Override
    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            Teamc target;
            //home in on allies if possible
            if(heals()){
                target = Units.closestTarget(null, b.aimX, b.aimY, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                    t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, b.aimX, b.aimY, homingRange,
                        e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                        t -> t != null && collidesGround && !b.hasCollided(t.id));
                }
            }

            if(target != null){
                Tmp.v1.set(b.aimX, b.aimY).approachDelta(Tmp.v2.set(target.x(), target.y()), homingPower);
                b.aimX = Tmp.v1.x;
                b.aimY = Tmp.v1.y;
                ArcBulletData data = (ArcBulletData)b.data;
                data.updateAccel(b);
            }
        }
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

    @Override
    public void drawTrail(Bullet b){
        if(trailLength > 0 && b.trail != null){
            //draw below bullets? TODO
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            b.trail.draw(trailColor, trailWidth);
            Draw.z(z);
        }
    }

    public static class ArcBulletData{
        public float xAccel, yAccel, lastZ, z, zVel, gravity;

        public ArcBulletData(float z, float zVel, float gravity){
            this.z = z;
            this.zVel = zVel;
            this.gravity = gravity;
        }

        public ArcBulletData(float z, float zVel){
            this(z, zVel, 1f);
        }

        /** Sets bullet lifetime based on initial z, initial z velocity, and the given gravity constant. */
        public void updateLifetime(Bullet b){ //Calculus :D
            //Calculate lifetime
            float life = PMMathf.quadPos(-0.5f * gravity, zVel, z);
            b.lifetime(life);
        }

        /** Sets constant acceleration in the x and y directions based on distance to target, initial velocity, and lifetime. */
        public void updateAccel(Bullet b){
            float life = b.lifetime() - b.time();
            //Calculate accels
            float dx = b.aimX - b.x;
            xAccel = (2 * (dx - b.vel.x * life)) / (life * life);
            float dy = b.aimY - b.y;
            yAccel = (2 * (dy - b.vel.y * life)) / (life * life);
        }

        /** Sets the bullet's aim pos based on accel, initial velocity, and lifetime */
        public void updateAimPos(Bullet b){
            float life = b.lifetime() - b.time();
            b.aimX = 0.5f * xAccel * life * life + b.vel.x * life + b.x;
            b.aimY = 0.5f * yAccel * life * life + b.vel.y * life + b.y;
        }

        public void update(Bullet b){
            b.vel.add(xAccel * Time.delta, yAccel * Time.delta);
            lastZ = z;
            z += zVel * Time.delta;
            zVel -= gravity * Time.delta;
        }

        public ArcBulletData setAccel(float angle, float a){
            xAccel = Angles.trnsx(angle, a);
            yAccel = Angles.trnsy(angle, a);
            return this;
        }

        /** Calculates proper initial velocity for the bullet. Normal, horizontal velocity is stored in the input vec2, vertical velocity is returned as a float. */
        public static float calcVel(Vec2 vec, float speed, float angle, float tilt, float inaccuracy){
            PMMathf.randomCirclePoint(vec, inaccuracy);
            float horiInacc = vec.x;
            float vertInacc = vec.y;
            Math3D.rotate(Tmp.v31, speed, angle, horiInacc, tilt + vertInacc);
            vec.set(Tmp.v31.x, Tmp.v31.y);
            return Tmp.v31.z;
        }
    }
}
