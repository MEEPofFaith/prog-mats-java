package progressed.entities.bullet.unit;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class SentryBulletType extends BulletType{
    public SentryBulletType(UnitType unit){
        super(10f, 0f);
        despawnUnit = unit;
        despawnUnitRadius = 0f;

        lifetime = 35f;
        collidesGround = collidesAir = collidesTiles = collides = false;
        backMove = false;
        scaleLife = true;
        splashDamage = 60f;
        splashDamageRadius = 8f;
        hitEffect = despawnEffect = Fx.none;
        layer = Layer.turret + 0.01f;
    }

    @Override
    public void draw(Bullet b){
        Draw.rect(despawnUnit.fullIcon, b.x, b.y, b.rotation() - 90f);
    }

    @Override
    public void createUnits(Bullet b, float x, float y){
        if(despawnUnit != null){
            for(int i = 0; i < despawnUnitCount; i++){
                Unit u = despawnUnit.spawn(b.team, x + Mathf.range(despawnUnitRadius), y + Mathf.range(despawnUnitRadius));
                u.rotation(b.rotation());
                u.vel.add(b.vel());
            }
        }
    }
}
