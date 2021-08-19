package progressed.entities.bullet;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.entities.units.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.SignalFlareTurret.*;

public class SignalFlareBulletType extends BulletType{
    public UnitType spawn;
    public float size, spinSpeed;
    
    public SignalFlareBulletType(float speed, float lifetime, UnitType spawn){
        super(speed, 0f);
        this.spawn = spawn;
        this.lifetime = lifetime;
        
        scaleVelocity = true;
        shootEffect = smokeEffect = Fx.none;
        ammoMultiplier = 1;
        collidesGround = collidesAir = collidesTiles = collides = false;
        hittable = absorbable = reflectable = false;
    }

    @Override
    public void despawned(Bullet b){
        FlareUnitEntity flare = (FlareUnitEntity)spawn.spawn(b.team, b.x, b.y);
        if(b.owner instanceof SignalFlareTurretBuild build){
            build.flares.add(flare);
        }

        super.despawned(b);
    }

    @Override
    public void draw(Bullet b){
        Draw.color(b.team.palette[1]);
        float rot = Time.time * spinSpeed + Mathf.randomSeed(b.id, 360f);
        PMDrawf.cross(b.x, b.y, size / 2f, size, rot);
        PMDrawf.cross(b.x, b.y, size / 1.25f / 2f, size / 1.25f, rot + 45f);
        
        Draw.color(b.team.color);
        PMDrawf.cross(b.x, b.y, size / 2f / 2f, size / 2f, rot);
        PMDrawf.cross(b.x, b.y, size / 2.5f / 2f, size / 2.5f, rot + 45f);

        Draw.reset();
    }
}