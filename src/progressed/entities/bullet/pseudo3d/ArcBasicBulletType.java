package progressed.entities.bullet.pseudo3d;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static progressed.graphics.Draw3D.*;

public class ArcBasicBulletType extends ArcBulletType{
    public String sprite;
    public float shadowLayer = Layer.flyingUnit + 1;
    public boolean drawShadow = false, spinShade = true;
    public TextureRegion region, blRegion, trRegion;

    public ArcBasicBulletType(float speed, float damage, float radius, String sprite){
        super(speed, damage, radius);
        this.sprite = sprite;
    }

    public ArcBasicBulletType(float speed, float damage, float radius){
        this(speed, damage, radius, "bullet");
        spinShade = false;
    }

    public ArcBasicBulletType(float speed){
        this(speed, 0f, 0f);
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
        if(drawZone) drawTargetZone(b);

        ArcBulletData data = (ArcBulletData)b.data;
        float lastHX = Draw3D.xHeight(b.lastX, data.lastZ),
            lastHY = Draw3D.yHeight(b.lastY, data.lastZ);
        float hX = Draw3D.xHeight(b.x, data.z),
            hY = Draw3D.yHeight(b.y, data.z);
        float rot = Angles.angle(lastHX, lastHY, hX, hY);
        if(drawShadow){
            Draw.scl(1f + height(data.z));
            Draw.z(shadowLayer);
            float sX = Angles.trnsx(225f, data.z) + b.x,
                sY = Angles.trnsy(225f, data.z) + b.y,
                sRot = Angles.angle(b.originX, b.originY, b.aimX, b.aimY), //TODO better shadow rotation calculation
                sAlpha = Draw3D.zAlpha(data.z);
            PMDrawf.shadow(region, sX, sY, sRot, sAlpha);
        }

        Draw.z(layer);
        drawTrail(b);
        Draw.scl(1f + hMul(data.z));
        float alpha = Draw3D.scaleAlpha(data.z);
        if(spinShade){
            PMDrawf.spinSprite(region, trRegion, blRegion, hX, hY, rot, alpha);
        }else{
            Draw.alpha(alpha);
            Draw.rect(region, hX, hY, rot);
        }
        Draw.scl();
    }
}
