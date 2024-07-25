package progressed.entities.bullet.pseudo3d;

import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import progressed.util.*;

public class ArcBoltBulletType extends ArcBasicBulletType{
    public ArcBoltBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
    }

    public ArcBoltBulletType(float speed, float damage){
        this(speed, damage, "bullet");

        spinShade = drawZone = drawTarget = drawProgress = false;
    }

    public ArcBoltBulletType(float speed){
        this(speed, 0f);
    }

    @Override
    public ArcBulletData createData(){
        return new ArcBoltData();
    }

    @Override
    public ArcBulletData createData(float z, float zVel, float gravity){
        return new ArcBoltData(z, zVel, gravity);
    }

    /** Assuming accel and gravity stay constant, the bullet travels in a straight trajectory, */
    public Bullet create3DStraight(Entityc owner, Team team, float x, float y, float z, float angle, float tilt, float vel, float accel){
        Math3D.rotate(Tmp.v31, vel, angle, 0f, tilt);
        Math3D.rotate(Tmp.v32, accel, angle, 0f, tilt);
        Tmp.v1.set(Tmp.v31.x, Tmp.v31.y);

        ArcBoltData data = new ArcBoltData(z, Tmp.v31.z, -Tmp.v32.z);
        data.xAccel = Tmp.v32.x;
        data.yAccel = Tmp.v32.y;

        Bullet bullet = beginBulletCreate(owner, team, x, y);
        bullet.vel.set(Tmp.v1);
        bullet.rotation(Tmp.v1.angle());
        if(backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            data.backMove(bullet);
        }else{
            bullet.set(x, y);
        }
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        data.updateLifetime(bullet);
        data.updateAimPos(bullet);
        bullet.add();
        return bullet;
    }

    public static class ArcBoltData extends ArcBulletData{
        public float xAccel, yAccel;

        public ArcBoltData(float z, float zVel, float gravity){
            super(z, zVel, gravity);
        }

        public ArcBoltData(float z, float zVel){
            this(z, zVel, 1f);
        }

        public ArcBoltData(){
            this(0, 0);
        }

        @Override
        public void backMove(Bullet b){
            b.vel.sub(xAccel * Time.delta, yAccel * Time.delta);
            super.backMove(b);
        }

        @Override
        public void updateAccel(Bullet b){
            float life = b.lifetime() - b.time();
            //Calculate accels
            float dx = b.aimX - b.x;
            xAccel = (2 * (dx - b.vel.x * life)) / (life * life);
            float dy = b.aimY - b.y;
            yAccel = (2 * (dy - b.vel.y * life)) / (life * life);
        }

        @Override
        public void updateHoming(Bullet b, Teamc target){
            BulletType type = b.type;

            Tmp.v1.set(b.aimX, b.aimY).approachDelta(Tmp.v2.set(target), type.homingPower);
            b.aimX = Tmp.v1.x;
            b.aimY = Tmp.v1.y;
            updateAccel(b);
        }

        @Override
        public void update(Bullet b){
            b.vel.add(xAccel * Time.delta, yAccel * Time.delta);
            super.update(b);
        }

        @Override
        public ArcBulletData setAccel(float a){
            Tmp.v1.set(xAccel, yAccel).setLength(a);
            xAccel = Tmp.v1.x;
            yAccel = Tmp.v1.y;
            return this;
        }

        @Override
        public float xAccel(Bullet b){
            return xAccel;
        }

        @Override
        public float yAccel(Bullet b){
            return yAccel;
        }
    }
}
