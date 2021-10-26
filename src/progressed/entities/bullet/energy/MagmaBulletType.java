package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.entities.*;

public class MagmaBulletType extends BulletType{
    public float radius, minScl = 0.2f, maxScl = 0.5f;
    public float shake, poolSpeed = 0.25f;
    public Color[] colors = {Color.valueOf("ec745855"), Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};
    
    public MagmaBulletType(float damage, float radius){
        super(0.001f, damage);
        this.radius = radius;

        hitEffect = Fx.fireballsmoke;
        despawnEffect = shootEffect = smokeEffect = Fx.none;
        hitSize = 4;
        lifetime = 16f;
        lightOpacity = 0.7f;
        hitColor = lightColor = colors[2];
        lightRadius = -1f;
        makeFire = true;
        keepVelocity = backMove = false;
        collides = pierce = true;
        hittable = absorbable = false;
        collidesTiles = false;
        collidesAir = collidesGround = true;
        puddleLiquid = PMLiquids.magma;
        puddleAmount = 250f;
    }

    @Override
    public float continuousDamage(){
        return damage / 5f * 60f;
    }

    @Override
    public float estimateDPS(){
        //assume firing duration is about 100 by default, may not be accurate there's no way of knowing in this method
        //assume it pierces 3 blocks/units
        return damage * 100f / 5f * 3f;
    }

    @Override
    public void init(){
        super.init();

        drawSize = radius * 2f;
        if(lightRadius < 0f) lightRadius = radius * 1.25f;
    }

    @Override
    public void update(Bullet b){
        //damage every 5 ticks
        if(b.timer(1, 5f)){
            Damage.damage(b.team, b.x, b.y, radius * b.fout(), damage, true, collidesAir, collidesGround);
            if(status != StatusEffects.none) Damage.status(b.team, b.x, b.y, radius * b.fout(), status, statusDuration, collidesAir, collidesGround);
            PMDamage.trueEachTile(b.x, b.y, radius * b.fout(), tile -> {
                if(puddleLiquid != null) Puddles.deposit(tile, puddleLiquid, puddleAmount);
                if(makeFire) Fires.create(tile);
            });
        }

        if(shake > 0){
            Effect.shake(shake, shake, b);
        }
    }

    @Override
    public void draw(Bullet b){
        for(int i = 0; i < colors.length; i++){
            Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
            float scl = Mathf.absin((Time.time + Mathf.randomSeed(b.id)) * poolSpeed, 1f, 1f);
            float frac = i / (colors.length - 1f);
            float sub = minScl + (maxScl - minScl) * scl;
            float shrink = (1f - sub) * frac * radius;
            Fill.circle(b.x, b.y, (radius - shrink) * b.fout());
        }
        Draw.reset();
    }
}