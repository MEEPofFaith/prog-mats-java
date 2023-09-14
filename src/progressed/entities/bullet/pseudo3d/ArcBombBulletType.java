package progressed.entities.bullet.pseudo3d;

import mindustry.gen.*;

public class ArcBombBulletType extends ArcMissileBulletType{
    public float zVelOffset;

    public ArcBombBulletType(float damage, float radius, String sprite){
        super(damage, radius, sprite);
        inheritVelDrift = false;
    }

    public ArcBombBulletType(String sprite){
        this(0f, 0f, sprite);
    }

    @Override
    public void arcBulletDataInit(Bullet b){
        ArcBulletData a = (ArcBulletData)b.data;
        a.zVel += zVelOffset;
        a.updateLifetime(b);
        a.xAccel = a.yAccel = 0f;
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        ArcBulletData a = (ArcBulletData)b.data;
        a.updateAimPos(b);
    }
}
