package progressed.entities.bullet.pseudo3d;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.*;
import progressed.util.*;

import static progressed.graphics.Draw3D.*;

public class ArcBasicBulletType extends ArcBulletType{
    public String sprite;
    public boolean bloomSprite = true;
    public boolean drawShadow = false, spinShade = true;
    public TextureRegion region, blRegion, trRegion, shadowRegion;

    public ArcBasicBulletType(float speed, float damage, String sprite){
        super(speed, damage);
        this.sprite = sprite;
    }

    public ArcBasicBulletType(float speed, float damage){
        this(speed, damage, "bullet");
        spinShade = false;
    }

    public ArcBasicBulletType(float speed){
        this(speed, 0f);
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        if(spinShade){
            blRegion = Core.atlas.find(sprite + "-bl");
            trRegion = Core.atlas.find(sprite + "-tr");
        }

        shadowRegion = Core.atlas.find(sprite + "-shadow", region);
    }

    @Override
    public ArcBulletData createData(){
        return new BasicArcBulletData();
    }

    @Override
    public ArcBulletData createData(float z, float zVel, float gravity){
        return new BasicArcBulletData(z, zVel, gravity);
    }

    @Override
    public void draw(Bullet b){
        drawTargetZone(b);

        ArcBulletData data = (ArcBulletData)b.data;
        float lastHX = Draw3D.x(b.lastX, data.lastZ),
            lastHY = Draw3D.y(b.lastY, data.lastZ);
        float hX = Draw3D.x(b.x, data.z),
            hY = Draw3D.y(b.y, data.z);
        float rot = Angles.angle(lastHX, lastHY, hX, hY);
        if(drawShadow && data.z < shadowMax){
            float scl = shadowScale(data.z),
                sX = Angles.trnsx(225f, data.z) + b.x,
                sY = Angles.trnsy(225f, data.z) + b.y,
                sRot = Angles.angle(b.originX, b.originY, b.aimX, b.aimY),
                sAlpha = Draw3D.shadowAlpha(data.z);

            float pitch = Tmp.v1.set(b.vel.len(), data.zVel).angle(); //0 - 90 or 270-360
            if(pitch <= 90){ //Going vertical, aim away from current point.
                sRot = Mathf.lerp(sRot, sRot < 45f ? -135f : 225f, pitch / 90f);
            }else if(pitch >= 270f){ //Falling down, aim towards current point.
                sRot = Mathf.lerp(sRot, sRot > 225f ? 405f : 45f, (360f - pitch) / 90f);
            }
            float fsRot = sRot; //I love Java

            Draw3D.shadow(() -> {
                Draw.scl(scl);
                PMDrawf.shadow(shadowRegion, sX, sY, fsRot, sAlpha);
                Draw.scl();
            });
        }

        Draw.z(layer + data.z / 3000f); //Higher elevation should draw above
        drawTrail(b);
        Draw3D.highBloom(bloomSprite, () -> {
            Draw.scl(1f + hMul(data.z));
            float alpha = Draw3D.scaleAlpha(data.z);
            if(spinShade){
                PMDrawf.spinSprite(region, trRegion, blRegion, hX, hY, rot, alpha);
            }else{
                Draw.alpha(alpha);
                Draw.rect(region, hX, hY, rot);
            }
            Draw.scl();
        });

        drawHomingDebug(b);
    }

    public static class BasicArcBulletData extends ArcBulletData{
        public float accel;

        public BasicArcBulletData(float z, float zVel, float gravity){
            super(z, zVel, gravity);
        }

        public BasicArcBulletData(){
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
        public void updateHoming(Bullet b, Teamc target){
            BulletType type = b.type;

            Tmp.v31.set(b.vel, zVel); //Current velocity

            float v2 = Tmp.v31.len();
            float pitch = Math3D.homingPitch(b.x, b.y, z, target.x(), target.y(), v2, accel, gravity); //Find target pitch between -pi/2 and pi/2

            float polar = Mathf.pi - (pitch + Mathf.halfPi); //0 = up, pi - down. Convert -pi/2-pi/2 -> pi-0

            Tmp.v32.setFromSpherical(b.angleTo(target) * Mathf.degRad, polar).setLength2(v2); //Target velocity

            float angle = (float)Math.acos(Tmp.v31.dot(Tmp.v32) / v2) * Mathf.radDeg;

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
