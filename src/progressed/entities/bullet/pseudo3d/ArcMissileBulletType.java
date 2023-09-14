package progressed.entities.bullet.pseudo3d;

import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;

public class ArcMissileBulletType extends ArcBasicBulletType{
    public float shadowLayer = Layer.flyingUnit + 1; //TODO shadow drawing
    public float accel = 0.1f;

    public ArcMissileBulletType(float damage, float radius, String sprite){
        super(0f, damage, radius, sprite);

        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        ammoMultiplier = 1;
        scaleLife = true;
        status = StatusEffects.blasted;
        drawZone = true;
        layer = Layer.flyingUnit + 2;
        absorbEffect = Pseudo3DFx.absorbed;
    }

    public ArcMissileBulletType(String sprite){
        this(0f, 0f, sprite);
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
