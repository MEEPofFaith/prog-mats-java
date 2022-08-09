package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class OribitalStrikeBulletType extends BulletType{
    public float height, radius, growTime;
    public Color bottomColor = Pal.lancerLaser, topColor;

    public OribitalStrikeBulletType(){
        speed = 0f;
        lifetime = 120f;
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
        Draw3D.cylinder(b.x, b.y, radius * Mathf.clamp(b.fin() * 2f, 0f, 1f), height, bottomColor, topColor);
    }
}
