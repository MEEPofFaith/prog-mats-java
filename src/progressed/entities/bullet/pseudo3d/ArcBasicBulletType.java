package progressed.entities.bullet.pseudo3d;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import progressed.graphics.*;
import progressed.graphics.perspective.*;

import static progressed.graphics.perspective.Draw3D.*;

public abstract class ArcBasicBulletType extends ArcBulletType{
    public String sprite;
    public boolean bloomSprite = true;
    public boolean drawShadow = false, spinShade = true;
    public TextureRegion region, shadowRegion;
    public TextureRegion[] regions;

    public ArcBasicBulletType(float speed, float damage, String sprite){
        super(speed, damage);
        this.sprite = sprite;
    }

    public ArcBasicBulletType(float speed, float damage){
        this(speed, damage, "bullet");
        spinShade = false;
    }

    public ArcBasicBulletType(float speed){
        this(speed, 0f);
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        if(spinShade){
            regions = new TextureRegion[3];
            regions[0] = region;
            for(int i = 1; i < 3; i++){
                regions[i] = Core.atlas.find(sprite + "-" + i);
            }
        }

        shadowRegion = Core.atlas.find(sprite + "-shadow", region);
    }

    @Override
    public void draw(Bullet b){
        drawTargetZone(b);

        ArcBulletData data = (ArcBulletData)b.data;
        Vec2 last = Perspective.drawPos(b.lastX, b.lastY, data.lastZ);
        float lastHX = last.x,
            lastHY = last.y;
        Vec2 curr = Perspective.drawPos(b.x, b.y, data.z);
        float hX = curr.x,
            hY = curr.y;
        float rot = Angles.angle(lastHX, lastHY, hX, hY);
        if(drawShadow && data.z < shadowMax){
            float scl = shadowScale(data.z),
                sX = Angles.trnsx(225f, data.z) + b.x,
                sY = Angles.trnsy(225f, data.z) + b.y,
                sRot = Angles.angle(b.originX, b.originY, b.aimX, b.aimY),
                sAlpha = Draw3D.shadowAlpha(data.z);

            float pitch = Tmp.v1.set(b.vel.len(), data.zVel).angle(); //0 - 90 or 270-360
            if(pitch <= 90){ //Going vertical, aim away from current point.
                sRot = Mathf.lerp(sRot, sRot < 45f ? -135f : 225f, pitch / 90f);
            }else if(pitch >= 270f){ //Falling down, aim towards current point.
                sRot = Mathf.lerp(sRot, sRot > 225f ? 405f : 45f, (360f - pitch) / 90f);
            }
            float fsRot = sRot; //I love Java

            Draw3D.shadow(() -> {
                Draw.scl(scl);
                PMDrawf.shadow(shadowRegion, sX, sY, fsRot, sAlpha);
                Draw.scl();
            });
        }

        if(Perspective.canDraw(data.z)){
            Draw.z(layer + data.z / 3000f); //Higher elevation should draw above
            drawTrail(b);
            Draw3D.highBloom(bloomSprite, () -> {
                Draw.scl(1f + hMul(data.z));
                float alpha = Draw3D.scaleAlpha(data.z);
                if(spinShade){
                    PMDrawf.spinSprite(regions, hX, hY, rot, alpha);
                }else{
                    Draw.alpha(alpha);
                    Draw.rect(region, hX, hY, rot);
                }
                Draw.scl();
            });
        }
    }
}
