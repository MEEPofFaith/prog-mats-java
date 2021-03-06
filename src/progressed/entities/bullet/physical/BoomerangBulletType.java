package progressed.entities.bullet.physical;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class BoomerangBulletType extends BasicBulletType{
    public float decelOffset = 0.05f, decelStart = 0.375f, decelEnd = 0.875f;
    public float riseStart, riseEnd, targetLayer = -1;

    public BoomerangBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
        pierce = true;
        pierceCap = 8;
        shrinkY = 0;
        spin = -12f;
    }

    public BoomerangBulletType(float speed, float damage){
        this(speed, damage, "prog-mats-saw");
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        b.data = new BoomerangBulletData(b.rotation(), b.vel.len());
    }

    @Override
    public void update(Bullet b){
        if(pierceCap != -1 && ((BoomerangBulletData)b.data).hitCount >= pierceCap){
            b.hit = true;
            b.remove();
            return;
        }

        super.update(b);

        BoomerangBulletData data = (BoomerangBulletData)b.data;
        float lerp = b.fin() * 2f - decelOffset;
        boolean isReturn = lerp >= 1;
        if(isReturn) lerp = 2 - lerp;
        float scl = Mathf.curve(lerp, decelStart, decelEnd);

        b.vel.trns(data.angle + (isReturn ? 180f : 0), Mathf.lerp(0f, data.speed, 1f - Mathf.sin(scl * Mathf.PI / 2f)));

        if(isReturn && !data.returning){
            data.returning = true;
            b.collided.clear();
        }
    }

    @Override
    public void hit(Bullet b){
        super.hit(b);

        ((BoomerangBulletData)b.data).hitCount++;
    }

    @Override
    public void draw(Bullet b){
        Draw.z(getLayer(b));
        drawTrail(b);

        float height = this.height * ((1f - shrinkY) + shrinkY * b.fout());
        float width = this.width * ((1f - shrinkX) + shrinkX * b.fout());
        float offset = (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        Draw.color(backColor);
        Draw.rect(backRegion, b.x, b.y, width, height, offset);
        Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, offset);

        Draw.reset();
    }

    public float getLayer(Bullet b){
        float progress = Mathf.curve(b.time, riseStart, riseEnd);
        return Mathf.lerp(layer, targetLayer, progress);
    }

    static class BoomerangBulletData{
        float angle, speed;
        int hitCount;
        boolean returning;

        public BoomerangBulletData(float angle, float speed){
            this.angle = angle;
            this.speed = speed;
        }
    }
}