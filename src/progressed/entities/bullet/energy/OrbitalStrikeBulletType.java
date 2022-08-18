package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class OrbitalStrikeBulletType extends BulletType{
    public float height = 1f, radius = 8f, growTime = 0.5f;
    public Color bottomColor = PMPal.nexusLaserDark, topColor = PMPal.nexusLaser.cpy().a(0);

    public OrbitalStrikeBulletType(){
        lifetime = 30f;
        layer = Layer.flyingUnit + 0.5f;
        scaleLife = true;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
    }

    @Override
    public void init(){
        if(topColor == null){
            topColor = bottomColor.cpy().a(0f);
        }
        super.init();
    }

    @Override
    public void init(Bullet b){
        float px = b.x + b.lifetime * b.vel.x,
            py = b.y + b.lifetime * b.vel.y;

        b.set(px, py);
        b.lifetime(lifetime);
        b.vel.setZero();

        super.init(b);
    }

    @Override
    public void draw(Bullet b){
        DrawPsudo3D.cylinder(b.x, b.y, radius * Mathf.curve(b.fin(), 0f, growTime), height, bottomColor, topColor);
    }
}
