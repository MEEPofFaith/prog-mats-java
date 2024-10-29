package progressed.entities.bullet.pseudo3d;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;
import progressed.util.*;

public class ArcMissileBulletType extends ArcBasicBulletType{
    public float accel = 0.1f;

    public ArcMissileBulletType(float damage, String sprite){
        super(0f, damage, sprite);

        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        ammoMultiplier = 1;
        scaleLife = true;
        scaledSplashDamage = true;
        splashDamagePierce = true;
        status = StatusEffects.blasted;
        drawZone = drawShadow = true;
        absorbEffect = Pseudo3DFx.absorbed;
        bloomSprite = false;
    }

    public ArcMissileBulletType(String sprite){
        this(0f, sprite);
    }

    public ArcMissileBulletType(){
        this("prog-mats-basic-missile");
    }

    @Override
    public ArcBulletData createData(){
        return Pools.obtain(ArcMissileData.class, ArcMissileData::new);
    }

    @Override
    public ArcBulletData createData(float z, float zVel, float gravity){
        return new ArcMissileData(z, zVel, gravity);
    }

    @Override
    public void init(Bullet b){
        if(accel < 0) accel = 1f;
        super.init(b);
    }

    @Override
    public void arcBulletDataInit(Bullet b){
        if(isInheritive) return;
        ArcBulletData a = (ArcBulletData)b.data;
        a.updateAimPos(b);
    }

    @Override
    public void updateHoming(Bullet b){
        //Since the pitch calculation can only find one solution, only home when that one solution is the optimal one.
        if(((ArcBulletData)b.data).zVel > 0) return;
        super.updateHoming(b);
    }

    public static class ArcMissileData extends ArcBulletData{
        public float accel;

        public ArcMissileData(float z, float zVel, float gravity){
            super(z, zVel, gravity);
        }

        public ArcMissileData(){
            super();
        }

        @Override
        public void backMove(Bullet b){
            float vSub = accel * Time.delta;
            if(vSub > b.vel.len()){
                b.vel.setLength(0); //Prevent rotation from being reversed
            }else{
                b.vel.sub(Tmp.v1.trns(b.rotation(), vSub));
            }

            super.backMove(b);
        }

        @Override
        public void updateAccel(Bullet b){
            float life = b.lifetime() - b.time();
            float d = Mathf.dst(b.x, b.y, b.aimX, b.aimY);
            accel = (2 * (d - b.vel.len() * life)) / (life * life);
        }

        @Override
        public void update(Bullet b){
            b.vel.add(Tmp.v1.trns(b.rotation(), accel * Time.delta));
            super.update(b);
        }

        @Override
        public void updateHoming(Bullet b, Position target){
            BulletType type = b.type;

            Tmp.v31.set(b.vel, zVel); //Current velocity

            float v2 = Tmp.v31.len2();
            float pitch = Math3D.homingPitch(b.x, b.y, z, target.getX(), target.getY(), v2, accel, gravity); //Find target pitch between -pi/2 and pi/2

            float polar = Mathf.pi - (pitch + Mathf.halfPi); //0 = up, pi - down. Convert -pi/2-pi/2 -> pi-0

            Tmp.v32.setFromSpherical(b.angleTo(target) * Mathf.degRad, polar).setLength2(v2); //Target velocity

            float angle = (float)Math.acos(Tmp.v31.dot(Tmp.v32) / Mathf.sqrt(v2)) * Mathf.radDeg;

            float h = type.homingPower * Time.delta;
            if(angle <= h){
                Tmp.v31.set(Tmp.v32);
            }else{
                //idk what I'm doing, but https://stackoverflow.com/questions/22099490/calculate-vector-after-rotating-it-towards-another-by-angle-θ-in-3d-space
                Tmp.v33.set(Tmp.v31).crs(Tmp.v32).crs(Tmp.v31).nor();

                float c = Mathf.cosDeg(h);
                float s = Mathf.sinDeg(h);
                Tmp.v31.scl(c).add(Tmp.v33.scl(s));
            }

            b.vel.set(Tmp.v31);
            zVel = Tmp.v31.z;
        }

        @Override
        public ArcBulletData setAccel(float a){
            accel = a;
            return this;
        }

        @Override
        public float xAccel(Bullet b){
            return accel * Mathf.cosDeg(b.rotation());
        }

        @Override
        public float yAccel(Bullet b){
            return accel * Mathf.sinDeg(b.rotation());
        }
    }
}
