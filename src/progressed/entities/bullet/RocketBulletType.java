package progressed.entities.bullet;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/** @author MEEP */
public class RocketBulletType extends BasicBulletType{
    public float backSpeed = 1f;
    public float fallDrag = 0.05f, thrustDelay = 20f;
    public float thrusterSize = 4f, thrusterOffset = 8f, thrusterGrowth = 5f;
    public float acceleration = 0.03f;

    public RocketBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite); //Speed means nothing
        layer = Layer.bullet - 1; //Don't bloom
        keepVelocity = false;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = hitEffect = Fx.explosion;
    }

    @Override
    public void init(){
        super.init();
        if(homingDelay < 0) homingDelay = thrustDelay;
    }

    @Override
    public void load(){
        super.load();
        backRegion = Core.atlas.find(sprite + "-outline");
    }

    @Override
    public float range(){ //TODO proper range calculation
        return super.range();
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(b.data instanceof RocketData r){
            if(b.time < thrustDelay && thrustDelay > 0){
                b.vel.scl(Math.max(1f - fallDrag * Time.delta, 0));
            }else{
                if(!r.thrust){
                    b.vel.setAngle(r.angle);
                    b.vel.setLength(speed);
                    r.thrust = true;
                }
                b.vel.scl(Math.max(1f + acceleration * Time.delta, 0));
            }
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof RocketData r){
            float angle = r.thrust ? b.rotation() : r.angle;

            if(b.time >= thrustDelay || thrustDelay <= 0){ //Engine draw code stolen from units
                float scale = Mathf.curve(b.time, thrustDelay, thrustDelay + thrusterGrowth);
                float offset = thrusterOffset / 2f + thrusterOffset / 2f * scale;

                Draw.color(b.team.color);
                Fill.circle(
                    b.x + Angles.trnsx(angle + 180, offset),
                    b.y + Angles.trnsy(angle + 180, offset),
                    (thrusterSize + Mathf.absin(Time.time, 2f, thrusterSize / 4f)) * scale
                );
                Draw.color(Color.white);
                Fill.circle(
                    b.x + Angles.trnsx(angle + 180, offset - thrusterSize / 2f),
                    b.y + Angles.trnsy(angle + 180, offset - thrusterSize / 2f),
                    (thrusterSize + Mathf.absin(Time.time, 2f, thrusterSize / 4f)) / 2f  * scale
                );
                Draw.color();
            }

            Draw.z(layer - 0.01f);
            Draw.rect(backRegion, b.x, b.y, angle - 90f);
            Draw.z(layer);
            Draw.rect(frontRegion, b.x, b.y, angle - 90f);
            Draw.reset();
        }
    }

    @Override
    public Bullet create(Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data){
        Bullet bullet = super.create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data);
        bullet.initVel(angle, -backSpeed * velocityScl);
        if(backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
        }else{
            bullet.set(x, y);
        }
        if(keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        bullet.data = new RocketData(angle);
        return bullet;
    }

    public static class RocketData{ //I could use data for lock and fdata for angle, but this looks nicer
        float angle;
        boolean thrust;

        public RocketData(float angle){
            this.angle = angle;
        }
    }
}