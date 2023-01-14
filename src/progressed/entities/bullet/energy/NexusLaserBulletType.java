package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class NexusLaserBulletType extends BulletType{
    public float height = 1f, radius = 4f;
    public Color baseColorLight = PMPal.nexusLaser, baseColorDark = PMPal.nexusLaserDark, topColorLight, topColorDark;
    public boolean alwaysBloom;
    public float strikeInaccuracy;

    public NexusLaserBulletType(){
        lifetime = 30f;
        layer = Layer.weather + 0.5f;
        scaleLife = true;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        setDefaults = false;
        despawnHit = false;
        despawnEffect = Fx.none;
    }

    @Override
    public void init(){
        if(topColorLight == null) topColorLight = baseColorLight;
        if(topColorDark == null) topColorDark = baseColorDark;

        drawSize += (radius + strikeInaccuracy) * 2f;
        super.init();
    }

    @Override
    public void init(Bullet b){
        float px = b.x + b.lifetime * b.vel.x,
            py = b.y + b.lifetime * b.vel.y;

        b.aimX = b.x;
        b.aimY = b.y;

        b.set(px, py);
        if(strikeInaccuracy > 0.01f){
            Tmp.v1.setToRandomDirection().setLength(strikeInaccuracy * Mathf.sqrt(Mathf.random()));
            b.move(Tmp.v1);
        }

        b.lifetime(lifetime);
        b.vel.setZero();

        hit(b);

        super.init(b);
    }

    @Override
    public void draw(Bullet b){
        float z = Draw.z();

        Draw.z(layer + DrawPseudo3D.layerOffset(b.aimX, b.aimY, b.x, b.y));

        if(alwaysBloom){
            PMDrawf.bloom(() -> drawBeam(b));
        }else{
            drawBeam(b);
        }

        Draw.z(z);
    }

    public void drawBeam(Bullet b){
        DrawPseudo3D.slantTube(b.x, b.y, b.aimX, b.aimY, radius * b.fout(), height, baseColorLight, baseColorDark, topColorLight, topColorDark);
    }
}
