package progressed.entities.bullet.physical;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class MagnetBulletType extends BasicBulletType{
    public float force = 0.1f, scaledForce, attractRange, lifeExtention = 0.5f;

    public MagnetBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);

        shootEffect = Fx.shootBig;
        drag = 0.018f;
        shrinkX = shrinkY = 0f;
        width = height = 8f;
    }

    public MagnetBulletType(float speed, float damage){
        this(speed, damage, "prog-mats-magnet");
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(b.timer(1, 2f)){
            //Attract to units/blocks
            Teamc target = Units.closestTarget(b.team, b.x, b.y, attractRange, e -> e.checkTarget(collidesAir, collidesGround), t -> collidesGround);
            if(target != null){
                Tmp.v1.trns(b.angleTo(target), (force + (1f - b.dst(target) / attractRange) * scaledForce));
                if(!Tmp.v1.isZero()){
                    b.vel().add(Tmp.v1);

                    //manually move bullets to simulate velocity for remote players
                    if(b.isRemote()){
                        b.move(Tmp.v1.x, Tmp.v1.y);
                    }

                    b.lifetime(b.lifetime + lifeExtention);
                }
                Tmp.v1.setZero();
            }
            b.data = target;
        }
    }

    @Override
    public void draw(Bullet b){
        float height = this.height * ((1f - shrinkY) + shrinkY * b.fout());
        float width = this.width * ((1f - shrinkX) + shrinkX * b.fout());
        float offset = spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f;
        float a = -90 + (b.data instanceof Teamc t && t.isAdded() ? b.angleTo(t) : b.rotation() + offset);

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        Draw.color(backColor);
        Draw.rect(backRegion, b.x, b.y, width, height, a);
        Draw.color(frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, a);

        Draw.reset();
    }
}