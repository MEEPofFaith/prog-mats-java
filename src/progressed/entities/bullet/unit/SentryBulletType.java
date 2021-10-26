package progressed.entities.bullet.unit;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class SentryBulletType extends BulletType{
    public UnitType unit;

    public SentryBulletType(UnitType unit){
        super(10f, 0f);
        this.unit = unit;

        lifetime = 35f;
        collidesGround = collidesAir = collidesTiles = collides = false;
        scaleVelocity = true;
        splashDamage = 60f;
        splashDamageRadius = 8f;
        hitEffect = despawnEffect = Fx.none;
        layer = Layer.flyingUnitLow - 1f;
    }

    @Override
    public void draw(Bullet b){
        Draw.rect(unit.fullIcon, b.x, b.y, b.rotation() - 90f);
    }

    @Override
    public void despawned(Bullet b){
        Unit spawned = unit.spawn(b.team, b);
        spawned.rotation = b.rotation();
        spawned.vel.add(b.vel);

        super.despawned(b);
    }
}