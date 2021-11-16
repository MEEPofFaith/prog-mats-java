package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.*;

public class SweepLaserBulletType extends BulletType{
    public Color color = Color.red;
    public float length = 1f, width = 1f;
    public float radius = 2f;
    public float angleRnd, sweepTime = 0.375f, blastTime = 0.625f;
    public int blasts = 2;

    public BulletType blastBullet;

    /** Just like with {@link PointBulletType}, speed = range */
    public SweepLaserBulletType(){
        scaleVelocity = true;
        lifetime = 40f;
        collides = keepVelocity = backMove = false;
        absorbable = hittable = false;
        hitEffect = despawnEffect = shootEffect = smokeEffect = Fx.none;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        Tmp.v1.set(b.lifetime * b.vel.x, b.lifetime * b.vel.y).add(b.x, b.y);
        b.data = new SweepLaserData(Tmp.v1.x, Tmp.v1.y, b.rotation());
        b.vel.setZero();
        b.lifetime = lifetime;
    }

    @Override
    public void update(Bullet b){
        sweepBlast(b);
        super.update(b);
    }

    public void sweepBlast(Bullet b){
        if(b.data instanceof SweepLaserData data){
            if(b.fin() >= blastTime && !data.blasted){
                data.blasted = true;
                float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
                Tmp.v1.trns(data.rotation - 90f + a, -length / 2f);
                Tmp.v2.trns(data.rotation - 90f + a, length / 2f);
                float time = b.lifetime - b.time;
                float delay = time / (blasts - 1);
                float x1 = data.x + Tmp.v1.x, y1 = data.y + Tmp.v1.y,
                    x2 = data.x + Tmp.v2.x, y2 = data.y + Tmp.v2.y;
                for(int i = 0; i < blasts; i++){
                    int ii = i;
                    Time.run(delay * i, () -> {
                        float tx = Mathf.lerp(x1, x2, ii / (blasts - 1f)),
                            ty = Mathf.lerp(y1, y2, ii / (blasts - 1f));
                        blastBullet.create(b, tx, ty, data.rotation);
                    });
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        if(b.data instanceof SweepLaserData data){
            float fin = Mathf.curve(b.fin(), 0f, sweepTime);
            float fout = Mathf.curve(b.fin(), blastTime);
            float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
            Tmp.v1.trns(data.rotation - 90f + a, length * fout - length / 2);
            Tmp.v2.trns(data.rotation - 90f + a, length * fin - length / 2f);

            Lines.stroke(width, color);
            PMDrawf.baseTri(
                data.x + Tmp.v1.x, data.y + Tmp.v1.y,
                width, width * 2f,
                data.rotation + 90 + a
            );
            PMDrawf.baseTri(
                data.x + Tmp.v2.x, data.y + Tmp.v2.y,
                width, width * 2f,
                data.rotation - 90 + a
            );

            Lines.line(
                data.x + Tmp.v1.x, data.y + Tmp.v1.y,
                data.x + Tmp.v2.x, data.y + Tmp.v2.y
            );

            if(fin < 1){
                Lines.line(
                    b.x, b.y,
                    data.x + Tmp.v2.x, data.y + Tmp.v2.y
                );
                Fill.circle(
                    data.x + Tmp.v2.x, data.y + Tmp.v2.y,
                    radius
                );
            }
        }
    }

    public static class SweepLaserData{
        public float x, y;
        public float rotation;
        public boolean blasted;

        public SweepLaserData(float x, float y, float rotation){
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }
    }
}