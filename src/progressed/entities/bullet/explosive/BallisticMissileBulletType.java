package progressed.entities.bullet.explosive;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.BallisticProjector.*;

/** @author MEEP */
public class BallisticMissileBulletType extends BasicBulletType{
    public float autoDropRadius, stopRadius, dropDelay, stopDelay;
    public boolean resumeSeek = true, randRot;
    public Effect rocketEffect = MissileFx.missileSmoke;
    public float trailChance = 0.5f, smokeTrailChance = 0.75f;
    public float targetRadius = 1f;
    public float thrusterRadius = 8f, thrusterLightRadius = -1f, thrusterLightOpacity = 0.5f;
    public float trailRnd, trailSize = 0.375f;
    public float riseTime = 60f, fallTime = 40f, elevation = 1f, shadowOffset, growScl = 4f;
    public Color thrusterColor = Pal.engine;
    public Color targetColor;
    public Effect blockEffect = Fx.none;
    public float fartVolume = 50f;
    public int splitBullets;
    public float splitVelocityMin = 0.2f, splitVelocityMax = 1f, splitLifeMin = 1f, splitLifeMax = 1f;
    public BulletType splitBullet;

    public Sortf unitSort = UnitSorts.closest;

    public BallisticMissileBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
        ammoMultiplier = 1;
        collides = hittable = absorbable = reflectable = keepVelocity = false;
        hitEffect = Fx.blockExplosionSmoke;
        shootEffect = smokeEffect = Fx.none;
        lightRadius = 32f;
        lightOpacity = 0.6f;
        lightColor = Pal.engine;
        trailEffect = Fx.none;
        status = StatusEffects.blasted;
    }

    @Override
    public void init(){
        super.init();

        drawSize = elevation + 64f;
        if(blockEffect == Fx.none) blockEffect = despawnEffect;
        if(thrusterLightRadius < 0) thrusterLightRadius = thrusterRadius * 6.5f;

        if(ProgMats.funiMode() && hitSound != Sounds.none){
            hitSound = PMSounds.gigaFard;
            hitSoundVolume = fartVolume;
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data == null){
            if(b.owner instanceof Unit unit){
                b.data = new ArcMissileData(unit.x, unit.y);
            }
            if(b.owner instanceof Building build){
                b.data = new ArcMissileData(build.x, build.y);
            }
        }
    }

    @Override
    public void update(Bullet b){
        if(b.data instanceof ArcMissileData data){
            float rise = Interp.pow5In.apply(Mathf.curve(b.time, 0f, riseTime));
            if(rise < 1f && Mathf.chanceDelta(smokeTrailChance)){
                float x = data.x,
                    y = data.y,
                    scl = 1f + (growScl - 1f) * rise;
                rocketEffect.at(x + Mathf.range(trailRnd * scl), y + Mathf.range(trailRnd * scl), trailSize * scl, new float[]{elevation * rise, 1f - rise});
            }

            //Find nearby target. Used for early dropping, starting and stopping, and homing.
            float range = Math.max(autoDropRadius, Math.max(stopRadius, homingRange));
            Teamc target = Units.bestTarget(b.team, b.x, b.y, range,
                e -> !e.dead() && e.checkTarget(collidesAir, collidesGround),
                t -> !t.dead() && collidesGround,
                unitSort
            );

            //Instant drop
            data.canDrop = riseTime < b.time && b.time < (b.lifetime - fallTime);
            if(autoDropRadius > 0f && data.canDrop && b.time >= dropDelay){
                if(target != null && b.within(target, autoDropRadius)){
                    b.time = b.lifetime - fallTime;
                }
            }

            //Start and stop
            if(stopRadius > 0f && b.time >= stopDelay){
                if(target != null && b.within(target, stopRadius)){
                    if(!data.stopped){
                        data.setVel(b.vel);
                        data.stopped = true;
                        b.vel.trns(b.vel.angle(), 0.001f);
                    }else if(resumeSeek && (((Healthc)target).dead() || ((Healthc)target).health() < 0f) && data.stopped){
                        b.vel.set(data.vel);
                        data.stopped = false;
                    }
                }else if(resumeSeek && data.stopped){
                    b.vel.set(data.vel);
                    data.stopped = false;
                }
            }

            if(!data.stopped){
                if(homingPower > 0.0001f && b.time >= homingDelay){
                    if(target != null && b.within(target, homingRange)){
                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                    }
                }

                if(weaveMag > 0){
                    b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale/2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
                }

                if(trailChance > 0){
                    if(Mathf.chanceDelta(trailChance)){
                        trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
                    }
                }

                if(trailInterval > 0f){
                    if(b.timer(0, trailInterval)){
                        trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
                    }
                }
            }

            if(splitBullet != null && !data.split && b.time >= (b.lifetime - fallTime)){
                data.split = true;
                for(int i = 0; i < splitBullets; i++){
                    splitBullet.create(b.owner, b.team, b.x, b.y, Mathf.random(360f), -1f, Mathf.random(splitVelocityMin, splitVelocityMax), Mathf.random(splitLifeMin, splitLifeMax), new ArcMissileData(b.x, b.y));
                }
            }
        }
    }

    @Override
    public void despawned(Bullet b){
        ArcMissileData data = (ArcMissileData)b.data;
        ShieldBuild s = data.shield;

        if(s != null && !s.broken && PMMathf.isInSquare(s.x, s.y, s.realRadius(), b.x, b.y)){
            data.blocked = true;
            blockEffect.at(b.x, b.y, b.rotation(), hitColor);
        }else{
            despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
        }
        hitSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);

        if(!b.hit && (fragBullet != null || splashDamageRadius > 0 || lightning > 0)){
            hit(b);
        }
    }

    @Override
    public void hit(Bullet b, float x, float y){
        b.hit = true;
        hitEffect.at(x, y, b.rotation(), hitColor);
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

        Effect.shake(hitShake, hitShake, b);

        createFrags(b, x, y);

        ArcMissileData data = ((ArcMissileData)b.data);

        if(!data.blocked){
            createPuddles(b, x, y);
            createIncend(b, x, y);

            if(suppressionRange > 0){
                //bullets are pooled, require separate Vec2 instance
                Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
            }

            createSplashDamage(b, x, y);

            for(int i = 0; i < lightning; i++){
                Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
            }
        }else{
            ShieldBuild s = data.shield;
            s.buildup += (b.damage() + splashDamage * s.realStrikeBlastResistance() * b.damageMultiplier()) * s.warmup;
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets / 2) * fragSpread);
                Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                if(f.type instanceof BallisticMissileBulletType) f.data = new ArcMissileData(x, y);
            }
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof ArcMissileData data){
            float x = data.x;
            float y = data.y;

            float rise = Interp.pow5In.apply(Mathf.curve(b.time, 0f, riseTime)),
                fadeOut = 1f - rise,
                fadeIn = Mathf.curve(b.time, b.lifetime - fallTime, b.lifetime),
                fall = 1f - fadeIn,
                rot = randRot ? Mathf.randomSeed(b.id, 360f) : 0f;
            Tmp.v1.trns(225f, rise * fall * shadowOffset * 2f);

            //Target
            Draw.z(Layer.bullet - 0.03f);
            if(autoDropRadius > 0f){
                float dropAlpha = Mathf.curve(b.time, riseTime * 2f/3f, riseTime) - Mathf.curve(b.time, b.lifetime - 8f, b.lifetime);
                Draw.color(Color.red, (0.25f + 0.5f * Mathf.absin(16f, 1f)) * dropAlpha);
                Fill.circle(b.x, b.y, autoDropRadius);
            }
            if(targetRadius > 0){
                float target = Mathf.curve(b.time, 0f, riseTime / 2f) - Mathf.curve(b.time, b.lifetime - fallTime / 2f, b.lifetime);
                float radius = targetRadius * target;
                PMDrawf.target(b.x, b.y, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), radius, targetColor != null ? targetColor : b.team.color, b.team.color, target);
            }

            //Missile
            if(fadeOut > 0 && fadeIn == 0){
                float rX = x + Draw3D.cameraXOffset(x, rise * elevation),
                    rY = y + Draw3D.cameraYOffset(y, rise * elevation),
                    scl = 1f + (growScl - 1f) * rise;
                Draw.scl(scl);
                //Engine stolen from launchpad
                if(thrusterRadius > 0f){
                    Draw.z(Layer.effect + 0.001f);
                    drawEngine(b.team, rX, rY, thrusterRadius, thrusterLightRadius, scl, fadeOut, b.id);
                }
                //Missile itself
                Draw.z(Layer.weather - 1);
                drawMissile(frontRegion, rX, rY, rot, fadeOut);
                //Missile shadow
                Draw.z(Layer.flyingUnit + 1f);
                drawShadow(frontRegion, x + Tmp.v1.x, y + Tmp.v1.y, rot, fadeOut);
            }else if(fadeOut == 0f && fadeIn > 0f){
                float fX = b.x + Draw3D.cameraXOffset(b.x, fall * elevation),
                    fY = b.y + Draw3D.cameraYOffset(b.y, fall * elevation),
                    rot2 = rot + (randRot ? Mathf.randomSeed(b.id + 1, 360f) : 0f),
                    scl = growScl - (growScl - 1f) * fadeIn;
                Draw.scl(scl);
                //Missile itself
                Draw.z(Layer.weather - 2f);
                drawMissile(backRegion, fX, fY, rot2, fadeIn);
                //Engine stolen from launchpad
                if(thrusterRadius > 0f){
                    Draw.z(Layer.weather - 1f);
                    drawEngine(b.team, fX, fY, thrusterRadius, thrusterLightRadius, scl, fadeIn, b.id + 2);
                }
                //Missile shadow
                Draw.z(Layer.flyingUnit + 1f);
                drawShadow(backRegion, b.x + Tmp.v1.x, b.y + Tmp.v1.y, rot, fadeIn);
            }

            Draw.reset();
        }
    }

    public void drawMissile(TextureRegion region, float x, float y, float rot, float a){
        Draw.color();
        Draw.alpha(a);
        Draw.rect(region, x, y, rot);
    }

    public void drawEngine(Team team, float x, float y, float size, float lightRadius, float scl, float alpha, long seed){
        Draw.color(thrusterColor, alpha);
        Fill.light(x, y, 10,
            size * 1.5625f * scl,
            Tmp.c1.set(Pal.engine).mul(1f, 1f, 1f, alpha),
            Tmp.c2.set(Pal.engine).mul(1, 1f, 1f, 0f)
        );
        PMDrawf.cross(x, y, size * 0.375f, size * 2.5f * scl, Time.time * 1.5f + Mathf.randomSeed(seed, 360f));
        Drawf.light(x, y, lightRadius * scl, thrusterColor, thrusterLightOpacity * alpha);
    }

    public void drawShadow(TextureRegion region, float x, float y, float rot, float a){
        Draw.color(0f, 0f, 0f, 0.22f * a);
        Draw.rect(region, x, y, rot);
    }

    @Override
    public void drawLight(Bullet b){
        //Do nothing
    }

    public static class ArcMissileData{
        public float x, y;
        public Vec2 vel;
        public boolean stopped, blocked, split, canDrop;
        public ShieldBuild shield;

        public ArcMissileData(float x, float y){
            this.x = x;
            this.y = y;
        }

        public void setVel(Vec2 vel){
            this.vel = vel.cpy();
        }
    }
}
