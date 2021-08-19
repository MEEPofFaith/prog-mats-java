package progressed.entities.bullet;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.world.blocks.distribution.BurstDriver.*;

public class BurstDriverItem extends BulletType{
    float size = 6f;
    float minSpin = 10f, maxSpin = 25f;

    public BurstDriverItem(){
        super(1f, 2f);
        collidesTiles = false;
        lifetime = 1f;
        despawnEffect = Fx.smeltsmoke;
        hitEffect = Fx.hitBulletSmall;
    }

    @Override
    public void draw(Bullet b){
        if(!(b.data() instanceof BurstDriverData data)){
            return;
        }

        Draw.z(Layer.bullet - 0.03f); //Don't bloom

        TextureRegion region = data.item.fullIcon;
        float rot = b.rotation() + Mathf.randomSeed(b.id, 0f, 380f) + Time.time * Mathf.randomSeed(b.id + 1, minSpin, maxSpin) * Mathf.signs[Mathf.randomSeed(b.id + 2, 0, 1)];

        Drawf.shadow(region, b.x - size / 2f, b.y - size / 2f, size, size, rot);

        Draw.z(Layer.bullet - 0.02f); //Don't bloom
        Lines.stroke(size / 12f, data.item.color);
        Lines.circle(b.x, b.y, size / 2f);
        Draw.color();
        Draw.rect(region, b.x, b.y, size * 0.75f, size * 0.75f, rot);
    }

    @Override
    public void update(Bullet b){
        //data MUST be an instance of DriverBulletData
        if(!(b.data() instanceof BurstDriverData data)){
            hit(b);
            return;
        }

        float hitDst = 7f;

        //if the target is dead or nonexistent, just keep flying until the bullet explodes
        if(data.to.dead() || !data.to.isAdded()){
            return;
        }

        float baseDst = data.from.dst(data.to);
        float dst1 = b.dst(data.from);
        float dst2 = b.dst(data.to);

        boolean intersect = false;

        //bullet has gone past the destination point: but did it intersect it?
        if(dst1 > baseDst){
            float angleTo = b.angleTo(data.to);
            float baseAngle = data.to.angleTo(data.from);

            //if angles are nearby, then yes, it did
            if(Angles.near(angleTo, baseAngle, 2f)){
                intersect = true;
                //snap bullet position back; this is used for low-FPS situations
                b.set(data.to.x + Angles.trnsx(baseAngle, hitDst), data.to.y + Angles.trnsy(baseAngle, hitDst));
            }
        }

        //if on course and it's in range of the target
        if(Math.abs(dst1 + dst2 - baseDst) < 4f && dst2 <= hitDst){
            intersect = true;
        } //else, bullet has gone off course, does not get received.

        if(intersect){
            data.to.handleBurstItem(b, data);
        }
    }

    @Override
    public void hit(Bullet b, float hitx, float hity){
        super.hit(b, hitx, hity);
        despawned(b);
    }
}