package progressed.entities.bullet.explosive;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;

import static mindustry.Vars.*;

/** @author MEEP */
public class RocketBulletType extends BasicBulletType{
    public float backSpeed = 1f;
    public float fallDrag = 0.05f, thrustDelay = 20f;
    public float thrusterSize = 4f, thrusterOffset = 8f, thrusterGrowth = 5f;
    public float trailDelay = -1f, trailOffset = 0f;
    public float acceleration = 0.03f;
    public float rotOffset = 0f;

    public float riseStart, riseEnd, targetLayer = -1;

    public BulletType bombBullet;
    public float bombInterval;

    public Sortf unitSort = Unit::dst2;

    public RocketBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite); //Speed means nothing
        layer = Layer.bullet - 1; //Don't bloom
        keepVelocity = false;
        reflectable = false;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = hitEffect = Fx.blastExplosion;
        hitSound = Sounds.largeExplosion;
        status = StatusEffects.blasted;
        scaledSplashDamage = true;
    }

    @Override
    public void init(){
        super.init();
        if(homingDelay < 0) homingDelay = thrustDelay;
        if(targetLayer < 0) targetLayer = layer;
        if(trailDelay < 0) trailDelay = thrusterGrowth;
    }

    @Override
    public void load(){
        super.load();
        backRegion = Core.atlas.find(sprite + "-outline");
    }

    @Override
    public void update(Bullet b){
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


                if(homingPower > 0.0001f && b.time >= homingDelay){
                    Teamc target;
                    //home in on allies if possible
                    if(healPercent > 0){
                        target = Units.bestTarget(null, b.x, b.y, homingRange,
                            e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                            t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id),
                            unitSort
                        );
                    }else{
                        target = Units.bestTarget(b.team, b.x, b.y, homingRange,
                            e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                            t -> collidesGround && !b.hasCollided(t.id),
                            unitSort
                        );
                    }

                    if(target != null){
                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                    }
                }

                if(weaveMag > 0){
                    b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale/2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
                }

                float angle = r.thrust ? b.rotation() : r.angle,
                    x = b.x + Angles.trnsx(angle + 180, trailOffset),
                    y = b.y + Angles.trnsy(angle + 180, trailOffset),
                    scale = Mathf.curve(b.time, thrustDelay, thrustDelay + thrusterGrowth);

                if(trailChance > 0){
                    if(Mathf.chanceDelta(trailChance)){
                        trailEffect.at(x, y, trailRotation ? b.rotation() : trailParam * scale, b.team.color, new float[]{b.rotation(), scale, getLayer(b)});
                    }
                }

                if(trailInterval > 0f){
                    if(b.timer(0, trailInterval)){
                        trailEffect.at(x, y, trailRotation ? b.rotation() : trailParam * scale, b.team.color, new float[]{b.rotation(), scale, getLayer(b)});
                    }
                }

                if(bombBullet != null){
                    if(b.time > thrustDelay + thrusterGrowth){
                        if(b.timer(1, bombInterval * (speed / b.vel.len()))){
                            bombBullet.create(b, b.x, b.y, b.rotation());
                        }
                    }
                }

                //updateTrail, but with the (x, y) above
                if(!headless && trailLength > 0 && b.time >= trailDelay){
                    if(b.trail == null){
                        b.trail = new Trail(trailLength);
                    }
                    b.trail.length = trailLength;
                    b.trail.update(x, y, trailInterp.apply(b.fin()) * scale);
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof RocketData r){
            float angle = r.thrust ? b.rotation() : r.angle;
            Draw.z(getLayer(b));
            float z = Draw.z();

            if(b.time >= thrustDelay || thrustDelay <= 0){ //Engine draw code stolen from units
                float scale = Mathf.curve(b.time, thrustDelay, thrustDelay + thrusterGrowth);
                float offset = thrusterOffset / 2f + thrusterOffset / 2f * scale;

                //drawTrail but with the above variables
                if(trailLength > 0 && b.trail != null){
                    Draw.z(z - 0.0001f);
                    b.trail.draw(b.team.color, trailWidth * scale);
                    Draw.z(z);
                }

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

            if(backRegion.found()){
                Draw.z(z - 0.01f);
                Draw.rect(backRegion, b.x, b.y, angle - 90f + rotOffset);
            }
            Draw.z(z);
            Draw.rect(frontRegion, b.x, b.y, angle - 90f + rotOffset);
            Draw.reset();
        }
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            TrailFadeFx.rocketTrailFade.at(b.x, b.y, trailWidth, b.team.color, new RocketTrailData(b.trail.copy(), getLayer(b)));
        }
    }

    public float getLayer(Bullet b){
        float progress = Mathf.curve(b.time, riseStart, riseEnd);
        return Mathf.lerp(layer, targetLayer, progress);
    }

    @Override
    public Bullet create(Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY){
        Bullet bullet = super.create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY);
        if(backSpeed != 0f){
            bullet.initVel(angle, -backSpeed * velocityScl);
            if(backMove){
                bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            }else{
                bullet.set(x, y);
            }
            if(keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        }
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

    public static class RocketTrailData{
        public Trail trail;
        public float layer;

        public RocketTrailData(Trail trail, float layer){
            this.trail = trail;
            this.layer = layer;
        }
    }
}
