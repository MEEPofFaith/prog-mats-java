package progressed.entities.bullet.pseudo3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import blackhole.utils.*;
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
import static progressed.graphics.Draw3D.*;

public class ArcBulletType extends BulletType{
    private static float cdist = 0f;
    private static Unit result;

    public boolean zAbsorbable = true, isInheritive;
    public Color absorbEffectColor = Pal.missileYellowBack;
    public Effect absorbEffect = Pseudo3DFx.absorbedSmall;
    public float gravity = 1f;
    /** Scalar for bullet lifetime. Used to make the bullet despawn early for mid-air fragging. */
    public float lifetimeScl = 1f;
    public float arcFragCone = 1f, intervalDropCone = 0f;
    public float angleDriftDrag = 0.02f;

    public boolean bloomTrail = true;

    public boolean drawZone = false;
    public float zoneLayer = Layer.bullet;
    public float targetRadius = 12f, zoneRadius = 3f * 8f, progressRadius = -1f;
    public float spikesWidth1 = -1f, spikesLength1 = -1f;
    public float spikesWidth2 = -1f, spikesLength2 = -1f;
    public float spokeWidth = 2f, spokeLength = 8f;
    public float spikeSpin = 0.5f;
    public float zoneLifeOffset = 0f;
    public float growTime = 10f, shrinkTime = 0f;
    public Color zoneColor = Color.red, targetColor = Color.red;

    static{
        BlackHoleUtils.immuneBulletTypes.add(ArcBulletType.class);
    }

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
        if(spikesLength2 < 0) spikesLength2 = zoneRadius / 2f;
        if(spikesLength1 < 0) spikesLength1 = spikesLength2 / 2f;
        if(spikesWidth1 < 0) spikesWidth1 = spikesLength1 / 2f;
        if(spikesWidth2 < 0) spikesWidth2 = spikesWidth1;
        if(progressRadius < 0) progressRadius = Math.max((drawZone ? zoneRadius + Math.max(spikesLength1, spikesLength2) / 2f : 0f), targetRadius + spokeLength / 2f) + 4f;

        if(fragBullet instanceof ArcBulletType a){
            a.isInheritive = true;
            a.zoneLifeOffset = a.zoneLifeOffset * a.lifetimeScl + lifetimeScl;
            shrinkTime = 0;
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
        }else{ //Invalid data, remove
            b.data = new ArcBulletData(); //Prevent crash
            b.remove();
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
                ((ArcBulletData)aType.create3DInherit(b, a, arcFragCone).data).splitFrom = (ArcBulletType)b.type;
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
                    aType.create3DInherit(b, a, aType.intervalDropCone);
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

            if(target != null){ //idk what I'm doing, but https://stackoverflow.com/questions/22099490/calculate-vector-after-rotating-it-towards-another-by-angle-θ-in-3d-space
                ArcBulletData data = (ArcBulletData)b.data;
                Tmp.v31.set(b.vel, data.zVel); //Current velocity

                float v2 = Tmp.v31.len2();
                Vec2 tZvel = Math3D.homingZVel(b.x, b.y, data.z, target.x(), target.y(), v2, data.gravity); //Find target z vels.

                //Two potential target velocities
                Tmp.v1.set(b.vel).setAngle(b.angleTo(target));
                Tmp.v2.set(Tmp.v1).setLength(Mathf.sqrt(v2 - tZvel.x * tZvel.x));
                Tmp.v32.set(Tmp.v2.x, Tmp.v2.y, tZvel.x);
                Tmp.v2.set(Tmp.v1).setLength(Mathf.sqrt(v2 - tZvel.y * tZvel.y));
                Tmp.v33.set(Tmp.v2.x, Tmp.v2.y, tZvel.y);

                float v = Mathf.sqrt(v2);
                float angle = (float)Math.acos(Tmp.v31.dot(Tmp.v32) / (v * v)) * Mathf.radDeg;
                float angle2 = (float)Math.acos(Tmp.v31.dot(Tmp.v33) / (v * v)) * Mathf.radDeg;

                if(angle2 < angle){ //Pick the closer one
                    Tmp.v32.set(Tmp.v33);
                    angle = angle2;
                }

                float h = homingPower * Time.delta;
                if(angle <= h){
                    Tmp.v31.set(Tmp.v32);
                }else{
                    Tmp.v33.set(Tmp.v31).crs(Tmp.v32).crs(Tmp.v31).nor(); //Thingy

                    float c = Mathf.cosDeg(h);
                    float s = Mathf.sinDeg(h);
                    Tmp.v31.scl(c).add(Tmp.v33.scl(s));
                }

                b.vel.set(Tmp.v31);
                data.zVel = Tmp.v31.z;
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
        drawTargetZone(b);
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
        Draw.z(zoneLayer);
        Draw.color(zoneColor);

        float realLife = b.lifetime * lifetimeScl;
        float grow = Mathf.curve(b.time, 0, growTime);
        float shrink = Mathf.curve(b.time, realLife - shrinkTime, realLife);
        float scl = grow - shrink;

        ArcBulletType splitFrom = ((ArcBulletData)b.data).splitFrom;
        boolean split = splitFrom != null;
        if(split) grow = Interp.smooth.apply(grow);
        float fout = 1 - shrink;

        float x = b.aimX, y = b.aimY;
        float ang = Mathf.randomSeed(b.id, 360) + b.time * spikeSpin;
        if(drawZone){
            float zR = split ? Mathf.lerp(splitFrom.zoneRadius, zoneRadius, grow) * fout : zoneRadius * scl;
            PMDrawf.ring(x, y, zR, zR + 2f);

            float sW1 = split ? Mathf.lerp(splitFrom.spikesWidth1, spikesWidth1, grow) * fout : spikesWidth1 * scl,
                sL1 = split ? Mathf.lerp(splitFrom.spikesLength1, spikesLength1, grow) * fout : spikesLength1 * scl;
            for(int i = 0; i < 4; i++){
                float a = ang + 90 * i;
                Drawf.tri(x + Angles.trnsx(a, zR), y + Angles.trnsy(a, zR), sW1, sL1, a + 180);
                Drawf.tri(x + Angles.trnsx(a, zR), y + Angles.trnsy(a, zR), sW1, sL1 / 2f, a);
            }
            float sW2 = split ? Mathf.lerp(splitFrom.spikesWidth2, spikesWidth2, grow) * fout : spikesWidth2 * scl,
                sL2 = split ? Mathf.lerp(splitFrom.spikesLength2, spikesLength2, grow) * fout : spikesLength2 * scl;
            for(int i = 0; i < 4; i++){
                float a = ang + 45 + 90 * i;
                Drawf.tri(x + Angles.trnsx(a, zR), y + Angles.trnsy(a, zR), sW2, sL2, a + 180);
                Drawf.tri(x + Angles.trnsx(a, zR), y + Angles.trnsy(a, zR), sW2, sL2 / 2f, a);
            }
        }

        float fin = b.fin() / lifetimeScl;
        float pR = split ? Mathf.lerp(splitFrom.progressRadius, progressRadius, grow) * fout : progressRadius * scl;
        PMDrawf.progressRing(x, y, pR, pR + 4f, fin);

        float tR = split ? Mathf.lerp(splitFrom.targetRadius, targetRadius, grow) * fout : targetRadius * scl,
            sW = split ? Mathf.lerp(splitFrom.spokeWidth, spokeWidth, grow) * fout : spokeWidth * scl,
            sL = split ? Mathf.lerp(splitFrom.spokeLength, spokeLength, grow) * fout : spokeLength * scl;
        PMDrawf.ring(x, y, tR, tR + 2);
        Lines.stroke(sW);
        for(int i = 0; i < 4; i++){
            float a = -ang + 90 * i;
            Lines.lineAngleCenter(x + Angles.trnsx(a, tR), y + Angles.trnsy(a, tR), a, sL, false);
        }
    }

    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        ArcBulletData data = (ArcBulletData)b.data;
        Drawf.light(Draw3D.x(b.x, data.z), Draw3D.y(b.y, data.z), lightRadius * (1f + hMul(data.z)), lightColor, lightOpacity * Draw3D.scaleAlpha(data.z));
    }

    /** Just draws a line from the aim pos to homing target */
    public void drawHomingDebug(Bullet b){
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
                Lines.stroke(4, Color.red);
                Lines.line(b.aimX, b.aimY, target.x(), target.y());
            }
        }
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
        return create3DVel(owner, team, x, y, z, angle, zVel, gravity, accel, 0, aimX, aimY);
    }

    public Bullet create3DVel(Entityc owner, Team team, float x, float y, float z, float angle, float zVel, float accel, float vel, float aimX, float aimY){
        return create3DVel(owner, team, x, y, z, angle, zVel, gravity, accel, vel, aimX, aimY);
    }

    public Bullet create3DVel(Entityc owner, Team team, float x, float y, float z, float angle, float zVel, float gravity, float accel, float vel, float aimX, float aimY){
        ArcBulletData data = new ArcBulletData(z, zVel, gravity).setAccel(accel);

        Bullet bullet = beginBulletCreate(owner, team, x, y, aimX, aimY);
        bullet.initVel(angle, vel); //Non-zero so that rotation is correct
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
        data.accel = Tmp.v32.len();

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

    public Bullet create3DInherit(Bullet b, float angle, float inaccCone){
        return create3DInherit(b, angle, inaccCone, gravity);
    }

    public Bullet create3DInherit(Bullet b, float angle, float inaccCone, float gravity){
        Tmp.v1.trns(angle, inaccCone * Mathf.sqrt(Mathf.random()));
        ArcBulletData oldData = (ArcBulletData)b.data;
        ArcBulletData data = oldData.copy();
        data.gravity = gravity;

        Bullet bullet;
        if(!Mathf.zero(inaccCone)){
            PMMathf.randomCirclePoint(Tmp.v1, inaccCone);
            data.driftYaw = Tmp.v1.x + oldData.driftYaw;
            data.driftPitch = Tmp.v1.y + oldData.driftPitch;
        }

        bullet = beginBulletCreate(b.owner, b.team, b.x, b.y, b.aimX, b.aimY);
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
        public float accel;
        public float lastZ, z, zVel, gravity;
        public float driftYaw, driftPitch;
        public ArcBulletType splitFrom;

        public ArcBulletData(float z, float zVel, float gravity){
            this.z = z;
            this.zVel = zVel;
            this.gravity = gravity;
        }

        public ArcBulletData(float z, float zVel){
            this(z, zVel, 1f);
        }

        public ArcBulletData(){
            this(0, 0);
        }

        public void backMove(Bullet b){
            b.vel.sub(Tmp.v1.trns(b.rotation(), accel * Time.delta));
            z -= zVel * Time.delta;
            zVel += gravity * Time.delta;
        }

        /** Calculates time to impact based on z, zVel, and gravity, and sets lifetime accordingly. */
        public void updateLifetime(Bullet b){
            b.lifetime(PMMathf.solve(-0.5f * gravity, zVel, z) + b.time);
        }

        /** Sets constant acceleration in based on distance to target, current velocity, and lifetime. */
        public void updateAccel(Bullet b){
            float life = b.lifetime() - b.time();
            float d = Mathf.dst(b.x, b.y, b.aimX, b.aimY);
            accel = (2 * (d - b.vel.len() * life)) / (life * life);
        }

        /** Sets the bullet's aim pos based on accel, initial velocity, and lifetime */
        public void updateAimPos(Bullet b){
            float life = b.lifetime() - b.time();
            b.aimX = 0.5f * xAccel(b) * life * life + b.vel.x * life + b.x;
            b.aimY = 0.5f * yAccel(b) * life * life + b.vel.y * life + b.y;
        }

        public void update(Bullet b){
            b.vel.add(Tmp.v1.trns(b.rotation(), accel * Time.delta));
            lastZ = z;
            z += zVel * Time.delta;
            zVel -= gravity * Time.delta;

            boolean needUpdate = false;
            if(!Mathf.zero(driftYaw)){
                b.vel.rotate(driftYaw);
                driftYaw *= 1f - ((ArcBulletType)b.type).angleDriftDrag;
                needUpdate = true;
            }
            if(!Mathf.zero(driftPitch)){
                Tmp.v1.set(b.vel.len(), zVel).rotate(driftPitch);
                zVel = Tmp.v1.y;
                driftPitch *= 1f - ((ArcBulletType)b.type).angleDriftDrag;

                updateLifetime(b);
                needUpdate = true;
            }
            if(needUpdate) updateAimPos(b);
        }

        public ArcBulletData setAccel(float a){
            accel = a;
            return this;
        }

        public float xAccel(Bullet b){
            return accel * Mathf.cosDeg(b.rotation());
        }

        public float yAccel(Bullet b){
            return accel * Mathf.sinDeg(b.rotation());
        }

        public ArcBulletData copy(){
            try{
                return (ArcBulletData)clone();
            }catch(CloneNotSupportedException whywhywhywhywhywhywhywhy){
                throw new RuntimeException("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", whywhywhywhywhywhywhywhy);
            }
        }
    }
}
