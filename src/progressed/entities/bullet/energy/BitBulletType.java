package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.graphics.*;

public class BitBulletType extends BulletType{
    public float size, trailDelay;

    public BitBulletType(float speed, float damage){
        super(speed, damage);

        shootEffect = Fx.none;
        smokeEffect = Fx.none;
        hitEffect = PMFx.bitBurst;
        despawnEffect = PMFx.bitBurst;
        trailEffect = PMFx.bitTrail;
        absorbable = hittable = false;
        hitSound = PMSounds.pixelHit;
    }

    public BitBulletType(){
        this(1f, 1f);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        
        if(b.timer(1, trailDelay)){
            trailEffect.at(b.x, b.y, size/2f);
        }
    }

    @Override
    public void draw(Bullet b){
        float offset = Mathf.randomSeed(b.id);
        Color c = Tmp.c1.set(PMPal.pixelFront).lerp(PMPal.pixelBack, Mathf.absin(Time.time * 0.05f + offset, 1f, 1f));
        Draw.color(c);
        Fill.rect(b.x, b.y, size, size);
    }
}