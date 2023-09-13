package progressed.entities.bullet.pseudo3d;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.gen.*;
import progressed.graphics.*;

import static progressed.graphics.Draw3D.hMul;

public class ArcBasicBulletType extends ArcBulletType{
    public String sprite;
    public boolean spinShade = true;
    public TextureRegion region, blRegion, trRegion;

    public ArcBasicBulletType(float speed, String sprite){
        super(speed);
        this.sprite = sprite;
    }

    public ArcBasicBulletType(float speed){
        this(speed, "bullet");
        spinShade = false;
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        if(spinShade){
            blRegion = Core.atlas.find(sprite + "-bl");
            trRegion = Core.atlas.find(sprite + "-tr");
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);
        ArcBulletData data = (ArcBulletData)b.data;
        float lastHX = Draw3D.xHeight(b.lastX, data.lastZ);
        float lastHY = Draw3D.yHeight(b.lastY, data.lastZ);
        float hX = Draw3D.xHeight(b.x, data.z);
        float hY = Draw3D.yHeight(b.y, data.z);
        float rot = Angles.angle(lastHX, lastHY, hX, hY);
        Draw.scl(1f + hMul(data.z));
        if(spinShade){
            PMDrawf.spinSprite(region, trRegion, blRegion, hX, hY, rot);
        }else{
            Draw.rect(region, hX, hY, rot);
        }
        Draw.scl();
    }
}
