package progressed.entities.bullet.pseudo3d;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class SkyBeamBulletType extends BulletType{
    public float height = 50f * tilesize;
    public float offset = 0.25f;
    public float radius = tilesize;
    public boolean bloom = true;
    public Color baseColor = PMPal.nexusLaserDark;
    public Color topColor = PMPal.nexusLaser.cpy().a(0);

    public SkyBeamBulletType(float speed, float damage){
        super(speed, damage);

        hittable = absorbable = reflectable = false;
        pierce = pierceBuilding = true;
        layer = PMLayer.skyBloom;
        shootEffect = smokeEffect = Fx.none;
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, range);
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        Draw3D.highBloom(bloom, () -> Draw3D.slantTube(b.x, b.y, b.originX, b.originY, height, radius, baseColor, topColor, offset));
    }
}
