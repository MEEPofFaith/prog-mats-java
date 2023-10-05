package progressed.entities.bullet.pseudo3d;

import mindustry.content.*;
import mindustry.gen.*;
import progressed.content.effects.*;

public class ArcMissileBulletType extends ArcBasicBulletType{
    public float accel = 0.1f;

    public ArcMissileBulletType(float damage, String sprite){
        super(0f, damage, sprite);

        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        ammoMultiplier = 1;
        scaleLife = true;
        status = StatusEffects.blasted;
        drawZone = drawShadow = true;
        absorbEffect = Pseudo3DFx.absorbed;
        bloomSprite = false;
    }

    public ArcMissileBulletType(String sprite){
        this(0f, sprite);
    }

    public ArcMissileBulletType(){
        this("prog-mats-basic-missile");
    }

    @Override
    public void init(Bullet b){
        if(accel < 0) accel = 1f;
        super.init(b);
    }

    @Override
    public void arcBulletDataInit(Bullet b){
        ArcBulletData a = (ArcBulletData)b.data;
        if(isInheritive) return;
        a.updateAimPos(b);
    }
}
