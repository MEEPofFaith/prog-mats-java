package progressed.entities.bullet.energy;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import blackhole.utils.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.entities.*;

public class AbyssBulletType extends BulletType{

    public float length = 10f * 8f;
    public int swirlEffects = 10;
    public float maxSwirlDelay = 5f;
    public float swirlRad = -1f;

    public Effect beamEffect = Fx.none;
    public Effect swirlEffect = Fx.none;
    public Effect growEffect = Fx.none;

    static{
        BlackHoleUtils.immuneBulletTypes.add(AbyssBulletType.class);
    }

    public AbyssBulletType(){
        super(0.01f, 0f);

        hitEffect = despawnEffect = Fx.none;
        shootEffect = Fx.hitLancer;
        smokeEffect = Fx.none;
        keepVelocity = false;
        collides = false;
        hittable = false;
        absorbable = false;
        scaledSplashDamage = true;
    }

    @Override
    public void init(){
        super.init();

        if(swirlRad < 0) swirlRad = splashDamageRadius;
    }

    @Override
    protected float calculateRange(){
        return Math.max(length, maxRange);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        Tmp.v1.set(PMDamage.linecast(collidesGround, collidesAir, b.team, b.x, b.y, b.rotation(), length));

        if(beamEffect != Fx.none){
            beamEffect.at(b.x, b.y, b.angleTo(Tmp.v1), new Vec2(Tmp.v1));
        }

        b.set(Tmp.v1);

        if(swirlEffect != Fx.none){
            for(int i = 0; i < swirlEffects; i++){
                Time.run(Mathf.random() * maxSwirlDelay, () -> {
                    swirlEffect.at(b.x, b.y, swirlRad);
                });
            }
        }

        if(growEffect != Fx.none){
            growEffect.at(b.x, b.y, b.team.color);
        }
    }
}
