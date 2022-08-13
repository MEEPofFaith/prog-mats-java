package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class OribitalStrikeBulletType extends BulletType{
    public float height = 1f, radius = 8f, growTime = 0.5f;
    public Color bottomColor = Pal.lancerLaser, topColor;

    public OribitalStrikeBulletType(){
        speed = 0f;
        lifetime = 30f;
        layer = Layer.flyingUnit + 0.5f;
        collides = false;
        keepVelocity = false;
        backMove = false;
        hittable = false;
        absorbable = false;
    }

    @Override
    public void init(){
        super.init();
        if(topColor == null){
            topColor = bottomColor.cpy().a(0f);
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        b.x = b.aimX;
        b.y = b.aimY;
    }

    @Override
    public void draw(Bullet b){
        DrawPsudo3D.cylinder(b.x, b.y, radius * Mathf.curve(b.fin(), 0f, growTime), height, bottomColor, topColor);
    }
}
