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
    public Effect beamEffect = Fx.none;

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
        instantDisappear = true;
        fragBullets = 1;

        //BE. Uncomment and remove createFrags override when next release
        //fragOffsetMin = fragOffsetMax = 0;
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
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null && (fragOnAbsorb || !b.absorbed)){
            for(int i = 0; i < fragBullets; i++){
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets/2) * fragSpread);
                fragBullet.create(b, x, y, a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
            }
        }
    }
}
