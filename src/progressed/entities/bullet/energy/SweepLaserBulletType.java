package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class SweepLaserBulletType extends BulletType{
    public Color color = Color.red;
    public float length = 1f, width = 1f;
    public float radius = 2f;
    public float angleRnd;
    public float extendTime = 0.125f, retractTime = -1f,
        sweepTime = 0.5f, blastTime = 0.625f;
    public int blasts = 2;

    public BulletType blastBullet;

    /** Just like with {@link PointBulletType}, speed = range */
    public SweepLaserBulletType(){
        scaleVelocity = true;
        lifetime = 40f;
        collides = keepVelocity = backMove = false;
        absorbable = hittable = false;
        hitEffect = despawnEffect = shootEffect = smokeEffect = Fx.none;
        layer = Layer.effect + 1f;
    }

    @Override
    public void init(){
        super.init();

        if(retractTime < 0) retractTime = sweepTime + extendTime;
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
        if(b.data instanceof SweepLaserData d){
            if(b.fin() >= blastTime && !d.blasted){
                sweepBlast(b, d);
                d.blasted = true;
            }
        }
        super.update(b);
    }

    public void sweepBlast(Bullet b, SweepLaserData d){
        float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
        Tmp.v1.trns(d.rotation - 90f + a, -length / 2f);
        Tmp.v2.trns(d.rotation - 90f + a, length / 2f);
        float time = b.lifetime - b.time;
        float delay = time / (blasts - 1);
        float x1 = d.x + Tmp.v1.x, y1 = d.y + Tmp.v1.y,
            x2 = d.x + Tmp.v2.x, y2 = d.y + Tmp.v2.y;
        for(int i = 0; i < blasts; i++){
            int ii = i;
            Time.run(delay * i, () -> {
                float tx = Mathf.lerp(x1, x2, ii / (blasts - 1f)),
                    ty = Mathf.lerp(y1, y2, ii / (blasts - 1f));
                blastBullet.create(b, tx, ty, d.rotation);
            });
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof SweepLaserData d){
            float fin = Mathf.curve(b.fin(), extendTime, sweepTime);
            float fout = Mathf.curve(b.fin(), blastTime);
            float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
            Tmp.v1.trns(d.rotation - 90f + a, length * fout - length / 2).add(d.x, d.y);
            Tmp.v2.trns(d.rotation - 90f + a, length * fin - length / 2f).add(d.x, d.y);

            Lines.stroke(width, color);
            PMDrawf.baseTri(
                Tmp.v1.x, Tmp.v1.y,
                width, width * 2f,
                d.rotation + 90 + a
            );
            PMDrawf.baseTri(
                Tmp.v2.x, Tmp.v2.y,
                width, width * 2f,
                d.rotation - 90 + a
            );
            Lines.line(
                Tmp.v1.x, Tmp.v1.y,
                Tmp.v2.x, Tmp.v2.y
            );

            //Line
            float lfin = Mathf.curve(b.fin(), 0f, extendTime);
            float lfout = 1f - Mathf.curve(b.fin(), sweepTime, retractTime);
            float lscl = lfin * lfout;
            if(lscl > 0.01f){
                float lx = Mathf.lerp(b.x, Tmp.v2.x, lscl),
                    ly = Mathf.lerp(b.y, Tmp.v2.y, lscl);
                Lines.line(
                    b.x, b.y,
                    lx, ly
                );
                Fill.circle(
                    lx, ly,
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