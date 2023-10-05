package progressed.entities.bullet.pseudo3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.graphics.trails.*;
import progressed.util.*;

import static mindustry.Vars.*;
import static progressed.graphics.Draw3D.hMul;

public class ArcBulletType extends BulletType{
    private static float cdist = 0f;
    private static Unit result;

    public boolean zAbsorbable = true, isInheritive, inheritZVel = true, inheritVelDrift = false;
    public Color absorbEffectColor = Pal.missileYellowBack;
    public Effect absorbEffect = Pseudo3DFx.absorbedSmall;
    public float gravity = 1f;
    /** Scalar for bullet lifetime. Used to make the bullet despawn early for mid-air fragging. */
    public float lifetimeScl = 1f;
    public float arcFragDrift = 0f, intervalDropDrift = 0f;
    public float targetDriftDrag = 0.05f;

    public boolean bloomTrail = true;

    public boolean drawZone = false;
    public float zoneLayer = Layer.bullet - 1f;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = -1f;
    public float zoneLifeOffset = 0f;
    public Color targetColor = Color.red;

    public ArcBulletType(float speed, float damage){
        super(speed, damage);

        collides = hittable = absorbable = reflectable = false;
        despawnHit = true;
        scaleLife = true;
        backMove = true;
        trailLength = 8;
        layer = PMLayer.skyBloom;
        shootEffect = smokeEffect = Fx.none;
        lightOpacity = 0f;
    }

    public ArcBulletType(float speed){
        this(speed, 0f);
    }
    
    public void initDrawSize(float range){
        float size = (range + rangeChange + zoneRadius) * 2f; //Probably good enough
        drawSize = Math.max(size, drawSize);
        if(fragBullet instanceof ArcBulletType a){
            a.initDrawSize(range);
        }
        if(intervalBullet instanceof ArcBulletType a){
            a.initDrawSize(range);
        }
    }

    @Override
    public void init(){
        if(fragBullet instanceof ArcBulletType a){
            a.isInheritive = true;
            a.zoneLifeOffset = a.zoneLifeOffset * a.lifetimeScl + lifetimeScl;
        }
        if(intervalBullet instanceof ArcBulletType a) a.isInheritive = true;

        super.init();
    }

    @Override
    public void init(Bullet b){
        if(b.data instanceof ArcBulletData){
            ArcBulletData a = (ArcBulletData)b.data;
            a.updateLifetime(b);
            arcBulletDataInit(b);
        }else{
            b.remove(); //Invalid data
        }

        super.init(b);
    }

    public void arcBulletDataInit(Bullet b){
        if(isInheritive) return;
        ArcBulletData a = (ArcBulletData)b.data;
        a.updateAccel(b);
    }

    @Override
    public void update(Bullet b){
        if(!(b.data instanceof ArcBulletData data)) return;
        data.update(b);
        //Log.info(data.z);
        super.update(b);
        if(b.time > b.lifetime * lifetimeScl || data.z <= 0f) b.remove();
    }

    @Override
    public void despawned(Bullet b){
        if(b.absorbed) return;

        if(despawnHit){
            boolean hit = calcNearbyHit(b);
            if(!hit) hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            TrailFadeFx.heightTrailFade.at(b.x, b.y, trailWidth, trailColor, b.trail.copy());
        }
    }

    public boolean calcNearbyHit(Bullet b){
        cdist = 0f;
        result = null;
        float range = 1f;

        Units.nearbyEnemies(b.team, b.x - range, b.y - range, range*2f, range*2f, e -> {
            if(e.dead() || !e.checkTarget(collidesAir, collidesGround) || !e.hittable()) return;

            e.hitbox(Tmp.r1);
            if(!Tmp.r1.contains(b.x, b.y)) return;

            float dst = e.dst(b.x, b.y) - e.hitSize;
            if((result == null || dst < cdist)){
                result = e;
                cdist = dst;
            }
        });

        if(result != null){
            b.collision(result, b.x, b.y);
            return true;
        }else if(collidesTiles){
            Building build = world.buildWorld(b.x, b.y);
            if(build != null && build.team != b.team){
                build.collision(b);
                hit(b);
                return true;
            }
        }
        return false;
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet instanceof ArcBulletType aType){
            for(int i = 0; i < fragBullets; i++){
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets/2f) * fragSpread);
                aType.create3DInherit(b, a, aType.arcFragDrift, false);
            }
        }else{
            super.createFrags(b, x, y);
        }
    }

    @Override
    public void updateBulletInterval(Bullet b){
        if(intervalBullet instanceof ArcBulletType aType){
            if(b.time >= intervalDelay && b.timer.get(2, bulletInterval)){
                float ang = b.rotation();
                for(int i = 0; i < intervalBullets; i++){
                    float a = ang + Mathf.range(intervalRandomSpread) + intervalAngle + ((i - (intervalBullets - 1f)/2f) * intervalSpread);
                    aType.create3DInherit(b, a, aType.intervalDropDrift, true);
                }
            }
        }else{
            super.updateBulletInterval(b);
        }
    }

    @Override
    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            Teamc target;
            //home in on allies if possible
            if(heals()){
                target = Units.closestTarget(null, b.aimX, b.aimY, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                    t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, b.aimX, b.aimY, homingRange,
                        e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                        t -> t != null && collidesGround && !b.hasCollided(t.id));
                }
            }

            if(target != null){
                Tmp.v1.set(b.aimX, b.aimY).approachDelta(Tmp.v2.set(target.x(), target.y()), homingPower);
                b.aimX = Tmp.v1.x;
                b.aimY = Tmp.v1.y;
                ArcBulletData data = (ArcBulletData)b.data;
                data.updateAccel(b);
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new HeightTrail(trailLength);
            }
            HeightTrail trail = (HeightTrail)b.trail;
            trail.length = trailLength;
            trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)), ((ArcBulletData)b.data).z);
        }
    }

    @Override
    public void draw(Bullet b){
        if(drawZone) drawTargetZone(b);
        Draw.z(layer);
        drawTrail(b);
    }

    @Override
    public void drawTrail(Bullet b){
        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            Draw3D.highBloom(bloomTrail, () -> b.trail.draw(trailColor, trailWidth));
            Draw.z(z);
        }
    }

    public void drawTargetZone(Bullet b){
        Draw.z(zoneLayer - 0.01f);
        if(drawZone && zoneRadius > 0f){
            Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
            Fill.circle(b.aimX, b.aimY, zoneRadius);
            Draw.color(Color.red, 0.5f);
            float fin = zoneLifeOffset + b.fin() * (1f - zoneLifeOffset);
            float subRad = fin * (zoneRadius + shrinkRad),
                inRad = Math.max(0, zoneRadius - subRad),
                outRad = Math.min(zoneRadius, zoneRadius + shrinkRad - subRad);

            PMDrawf.ring(b.aimX, b.aimY, inRad, outRad);
        }
        Draw.z(zoneLayer);
        PMDrawf.target(b.aimX, b.aimY, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);
    }

    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        ArcBulletData data = (ArcBulletData)b.data;
        Drawf.light(Draw3D.xHeight(b.x, data.z), Draw3D.yHeight(b.y, data.z), lightRadius * (1f + hMul(data.z)), lightColor, lightOpacity * Draw3D.scaleAlpha(data.z));
    }

    public Bullet create3D(Entityc owner, Team team, float x, float y, float z, float angle, float tilt, float aimX, float aimY){
        return create3D(owner, team, x, y, z, angle, tilt, gravity, aimX, aimY);
    }

    public Bullet create3D(Entityc owner, Team team, float x, float y, float z, float angle, float tilt, float gravity, float aimX, float aimY){
        return create3D(owner, team, x, y, z, angle, tilt, gravity, 1f, aimX, aimY);
    }

    public Bullet create3D(Entityc owner, Team team, float x, float y, float z, float angle, float tilt, float gravity, float velocityScl, float aimX, float aimY){
        Vec3 vel = Math3D.rotate(Tmp.v31, speed, angle, 0f, tilt);
        ArcBulletData data = new ArcBulletData(z, vel.z * velocityScl, gravity);

        //Taken from normal bullet create
        Bullet bullet = beginBulletCreate(owner, team, x, y, aimX, aimY);
        bullet.rotation(angle);
        bullet.vel.set(vel.x, vel.y);
        if(backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            data.backMove(bullet);
        }else{
            bullet.set(x, y);
        }
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        bullet.add();
        return bullet;
    }

    public Bullet create3DVel(Entityc owner, Team team, float x, float y, float z, float angle, float zVel, float accel, float aimX, float aimY){
        return create3DVel(owner, team, x, y, z, angle, zVel, gravity, accel, aimX, aimY);
    }

    public Bullet create3DVel(Entityc owner, Team team, float x, float y, float z, float angle, float zVel, float gravity, float accel, float aimX, float aimY){
        ArcBulletData data = new ArcBulletData(z, zVel, gravity).setAccel(angle, accel);

        Bullet bullet = beginBulletCreate(owner, team, x, y, aimX, aimY);
        bullet.initVel(angle, 0);
        if(backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            data.backMove(bullet);
        }else{
            bullet.set(x, y);
        }
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        bullet.add();
        return bullet;
    }

    public Bullet create3DStraight(Entityc owner, Team team, float x, float y, float z, float angle, float tilt, float vel, float accel){
        Math3D.rotate(Tmp.v31, vel, angle, 0f, tilt);
        Math3D.rotate(Tmp.v32, accel, angle, 0f, tilt);
        Tmp.v1.set(Tmp.v31.x, Tmp.v31.y);

        ArcBulletData data = new ArcBulletData(z, Tmp.v31.z, -Tmp.v32.z);
        data.xAccel = Tmp.v32.x;
        data.yAccel = Tmp.v32.y;

        Bullet bullet = beginBulletCreate(owner, team, x, y);
        bullet.vel.set(Tmp.v1);
        bullet.rotation(Tmp.v1.angle());
        if(backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            data.backMove(bullet);
        }else{
            bullet.set(x, y);
        }
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        data.updateLifetime(bullet);
        data.updateAimPos(bullet);
        bullet.add();
        return bullet;
    }

    public Bullet create3DInherit(Bullet b, float angle, float maxTargetDrift, boolean drop){
        return create3DInherit(b, angle, maxTargetDrift, gravity, drop);
    }

    public Bullet create3DInherit(Bullet b, float angle, float maxTargetDrift, float gravity, boolean drop){
        Tmp.v1.trns(angle, maxTargetDrift * Mathf.sqrt(Mathf.random()));
        ArcBulletData data = ((ArcBulletData)b.data).copy();
        data.gravity = gravity;
        data.addTargetDrift(Tmp.v1);
        if(inheritVelDrift) data.addTargetDrift(b.vel);
        if(!inheritZVel) data.zVel = 0;
        float tx = drop ? b.x : b.aimX,
            ty = drop ? b.y : b.aimY;

        Bullet bullet = beginBulletCreate(b.owner, b.team, b.x, b.y, tx, ty);
        bullet.initVel(b.rotation(), b.vel.len());
        if(backMove){
            bullet.set(b.x - bullet.vel.x * Time.delta, b.y - bullet.vel.y * Time.delta);
            data.backMove(bullet);
        }else{
            bullet.set(b.x, b.y);
        }
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        if(trailLength > 0){
            bullet.trail = b.trail.copy();
        }
        bullet.add();
        return bullet;
    }

    public Bullet beginBulletCreate(Entityc owner, Team team, float x, float y, float damage, float aimX, float aimY){
        //Taken from normal bullet create
        Bullet bullet = Bullet.create();
        bullet.type = this;
        bullet.owner = owner;
        bullet.team = team;
        bullet.time = 0f;
        bullet.originX = x;
        bullet.originY = y;
        //bullet.aimTile = world.tileWorld(aimX, aimY);
        bullet.aimX = aimX;
        bullet.aimY = aimY;
        bullet.damage = (damage < 0 ? this.damage : damage) * bullet.damageMultiplier();
        return bullet;
    }

    public Bullet beginBulletCreate(Entityc owner, Team team, float x, float y, float aimX, float aimY){
        return beginBulletCreate(owner, team, x, y, -1f, aimX, aimY);
    }

    public Bullet beginBulletCreate(Entityc owner, Team team, float x, float y){
        return beginBulletCreate(owner, team, x, y, -1f, x, y);
    }

    public static class ArcBulletData implements Cloneable{
        public float xAccel, yAccel;
        public float lastZ, z, zVel, gravity;
        public float targetDriftX, targetDriftY;

        public ArcBulletData(float z, float zVel, float gravity){
            this.z = z;
            this.zVel = zVel;
            this.gravity = gravity;
        }

        public ArcBulletData(float z, float zVel){
            this(z, zVel, 1f);
        }

        public void backMove(Bullet b){
            b.vel.sub(xAccel * Time.delta, yAccel * Time.delta);
            z -= zVel * Time.delta;
            zVel += gravity * Time.delta;

            if(targetDriftX != 0 || targetDriftY != 0){
                b.aimX -= targetDriftX * Time.delta;
                b.aimY -= targetDriftY * Time.delta;
            }
        }

        /** Sets bullet lifetime based on initial z, initial z velocity, and the given gravity constant. */
        public void updateLifetime(Bullet b){
            //Calculate lifetime
            b.lifetime(PMMathf.solve(-0.5f * gravity, zVel, z));
        }

        /** Sets constant acceleration in the x and y directions based on distance to target, initial velocity, and lifetime. */
        public void updateAccel(Bullet b){
            float life = b.lifetime() - b.time();
            //Calculate accels
            float dx = b.aimX - b.x;
            xAccel = (2 * (dx - b.vel.x * life)) / (life * life);
            float dy = b.aimY - b.y;
            yAccel = (2 * (dy - b.vel.y * life)) / (life * life);
        }

        /** Sets the bullet's aim pos based on accel, initial velocity, and lifetime */
        public void updateAimPos(Bullet b){
            float life = b.lifetime() - b.time();
            b.aimX = 0.5f * xAccel * life * life + b.vel.x * life + b.x;
            b.aimY = 0.5f * yAccel * life * life + b.vel.y * life + b.y;
        }

        public void update(Bullet b){
            b.vel.add(xAccel * Time.delta, yAccel * Time.delta);
            lastZ = z;
            z += zVel * Time.delta;
            zVel -= gravity * Time.delta;

            if(targetDriftX != 0 || targetDriftY != 0){
                b.aimX += targetDriftX * Time.delta;
                b.aimY += targetDriftY * Time.delta;
                float drag = Math.max(1f - ((ArcBulletType)b.type).targetDriftDrag * Time.delta, 0);
                targetDriftX *= drag;
                targetDriftY *= drag;
                updateAccel(b);
            }
        }

        public ArcBulletData setAccel(float angle, float a){
            xAccel = Angles.trnsx(angle, a);
            yAccel = Angles.trnsy(angle, a);
            return this;
        }

        public ArcBulletData addTargetDrift(Vec2 drift){
            targetDriftX += drift.x;
            targetDriftY += drift.y;
            return this;
        }

        public ArcBulletData copy(){
            try{
                return (ArcBulletData)clone();
            }catch(CloneNotSupportedException whywhywhywhywhywhywhywhy){
                throw new RuntimeException("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", whywhywhywhywhywhywhywhy);
            }
        }

        /** Calculates proper initial velocity for the bullet. Normal, horizontal velocity is stored in the input vec2, vertical velocity is returned as a float. */
        public static float calcVel(Vec2 vec, float speed, float angle, float tilt, float inaccuracy){
            PMMathf.randomCirclePoint(vec, inaccuracy);
            float horiInacc = vec.x;
            float vertInacc = vec.y;
            Math3D.rotate(Tmp.v31, speed, angle, horiInacc, tilt + vertInacc);
            vec.set(Tmp.v31.x, Tmp.v31.y);
            return Tmp.v31.z;
        }
    }
}
