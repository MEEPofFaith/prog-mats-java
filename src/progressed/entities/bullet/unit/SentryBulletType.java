package progressed.entities.bullet.unit;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.gen.entities.*;

public class SentryBulletType extends BulletType{
    public SentryBulletType(UnitType unit){
        super(10f, 0f);
        despawnUnit = unit;
        despawnUnitRadius = 0f;

        lifetime = 35f;
        collidesGround = collidesAir = collidesTiles = collides = false;
        scaleLife = true;
        hitEffect = despawnEffect = Fx.none;
        ammoMultiplier = 1f;
        layer = Layer.turret + 0.01f;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        b.data = despawnUnit.create(b.team);
    }

    @Override
    public void draw(Bullet b){
        Unit u = (Unit)b.data;
        u.set(b);
        u.rotation(b.rotation());
        float z = Draw.z();
        Draw.z(Math.min(Layer.darkness, z - 1f));
        u.type.drawShadow(u);
        Draw.z(z);
        u.draw();
    }

    @Override
    public void createUnits(Bullet b, float x, float y){
        Unit u = (Unit)b.data;
        u.set(b);
        u.rotation(b.rotation());
        u.vel.add(b.vel());
        if(u instanceof SentryUnit s) s.anchorVel.add(b.vel());
        u.add();
    }
}
