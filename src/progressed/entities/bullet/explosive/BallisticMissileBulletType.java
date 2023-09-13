package progressed.entities.bullet.explosive;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.game.*;
import progressed.graphics.*;
import progressed.graphics.trails.*;
import progressed.util.*;

import static mindustry.Vars.*;
import static progressed.graphics.Draw3D.*;

@Deprecated
public class BallisticMissileBulletType extends BulletType{
    public boolean drawZone = true;
    public float height = 1f, heightRnd;
    public float zoneLayer = Layer.bullet - 1f, shadowLayer = Layer.flyingUnit + 1;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = -1f;
    public float shadowOffset = -1f, shadowGrowth = -1f;
    public float splitTime = 0.5f;
    public float splitLifeMaxOffset = 10f;
    public Color targetColor = Color.red;
    public String sprite;
    public Effect blockEffect = Pseudo3DFx.absorbed;
    public float fartVolume = 50f;
    public boolean spinShade = true;
    public Interp hInterp = PMInterp.flightArc, posInterp = Interp.pow2In, rotInterp = PMInterp.sineInverse;

    public TextureRegion region, blRegion, trRegion;

    /** Convert over to {@link ArcMissileBulletType} */
    public BallisticMissileBulletType(String sprite){
        super(1f, 0f);
        this.sprite = sprite;

        shootEffect = smokeEffect = Fx.none;
        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        layer = Layer.flyingUnit + 2;
        ammoMultiplier = 1;
        lifetime = 120f;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        scaleLife = true;
        scaledSplashDamage = true;
        status = StatusEffects.blasted;
    }

    @Override
    public void init(){
        if(shadowOffset < 0) shadowOffset = height * 2f;
        if(shadowGrowth < 0) shadowGrowth = height(height) / 2f;
        if(shrinkRad < 0) shrinkRad = zoneRadius / 6f;
        if(ProgMats.farting() && hitSound != Sounds.none){
            hitSound = PMSounds.gigaFard;
            hitSoundVolume = fartVolume;
        }

        super.init();

        //Since the offset tilts it away from the camera, you only need the draw size to be the range of the missile.
        drawSize = Math.max(drawSize, range * 2);

        //Ensure that split missiles also have enough draw size, as limitRange only applies to the main missile.
        if(fragBullet instanceof BallisticMissileBulletType m){
            m.drawSize = Math.max(fragBullet.drawSize, (drawSize + fragRandomSpread) * 2);

            //Ensure that the visual positions also match up.
            m.lifetime = lifetime;
            m.height = height;
            m.heightRnd = heightRnd = 0f;
            m.hInterp = hInterp;
            m.posInterp = posInterp;
            m.rotInterp = rotInterp;
        }

        if(intervalBullet instanceof DropBombBulletType b){
            b.height = height;
            b.shadowOffset = shadowOffset;
        }
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        if(spinShade){
            blRegion = Core.atlas.find(sprite + "-bl");
            trRegion = Core.atlas.find(sprite + "-tr");
        }

        //Set draw size to the max distance the shadow can travel
        drawSize = Math.max(drawSize, (shadowOffset + PMMathf.cornerDst(region.width / 4f, region.height / 4f)) * (1f + heightRnd) * 2);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        float px = b.x + b.lifetime * b.vel.x,
            py = b.y + b.lifetime * b.vel.y;

        b.data = new float[]{b.x, b.y, b.x, b.y, 0f};
        b.lifetime(lifetime);
        b.set(px, py);
        b.vel.setZero();
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(fragBullet instanceof BallisticMissileBulletType && b.fin() >= splitTime){
            b.remove();
        }
    }

    @Override
    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            float realAimX = b.aimX < 0 ? b.x : b.aimX;
            float realAimY = b.aimY < 0 ? b.y : b.aimY;

            Teamc target;
            //home in on allies if possible
            if(heals()){
                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                    t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
                }
            }

            if(target != null){ //Instead of rotating the bullet, shift towards the homing target.
                Tmp.v1.setZero();
                if(target instanceof Hitboxc h){
                    Tmp.v1.set(h.deltaX(), h.deltaY()).scl(b.lifetime - b.time);
                }
                Tmp.v1.add(target);

                Tmp.v2.trns(b.angleTo(Tmp.v1), b.dst(Tmp.v1)).limit(homingPower * Time.delta);
                b.move(Tmp.v2);
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        float x = tX(b), y = tY(b), h = hScl(b);
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new HeightTrail(trailLength);
            }
            HeightTrail trail = (HeightTrail)b.trail;
            trail.length = trailLength;
            trail.update(x, y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)), h * height);
        }

        float[] data = (float[])b.data;
        data[2] = x;
        data[3] = y;
        data[4] = h;
    }

    @Override
    public void updateBulletInterval(Bullet b){
        if(intervalBullet != null && b.time > intervalDelay && b.timer.get(2, bulletInterval)){
            float x = tX(b), y = tY(b);
            float ang = Angles.angle(((float[])b.data)[0], ((float[])b.data)[1], x, y);
            for(int i = 0; i < intervalBullets; i++){
                Bullet spawned = intervalBullet.create(b, x, y, ang + Mathf.range(intervalRandomSpread) + intervalAngle + ((i - (intervalBullets - 1f)/2f) * intervalSpread));
                if(intervalBullet instanceof DropBombBulletType){
                    spawned.fdata = hScl(b);
                    spawned.lifetime *= spawned.fdata;
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float[] data = (float[])b.data;

        float lerp = Mathf.lerp(b.fdata, 1f, b.fin());
        //Target
        Draw.z(zoneLayer - 0.01f);
        if(drawZone && zoneRadius > 0f){
            Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
            Fill.circle(b.x, b.y, zoneRadius);
            Draw.color(Color.red, 0.5f);
            float subRad = lerp * (zoneRadius + shrinkRad),
                inRad = Math.max(0, zoneRadius - subRad),
                outRad = Math.min(zoneRadius, zoneRadius + shrinkRad - subRad);

            PMDrawf.ring(b.x, b.y, inRad, outRad);
        }
        Draw.z(zoneLayer);
        PMDrawf.target(b.x, b.y, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);

        //Missile
        float x = tX(b),
            y = tY(b),
            hScl = hScl(b),
            shX = x - shadowOffset * hScl,
            shY = y - shadowOffset * hScl,
            lasthX = xHeight(data[2], data[4] * height),
            lasthY = yHeight(data[3], data[4] * height),
            hX = xHeight(x, hScl * height),
            hY = yHeight(y, hScl * height),
            hRot = Angles.angle(lasthX, lasthY, hX, hY);

        Draw.z(shadowLayer);
        Draw.scl(1f + hScl * shadowGrowth);
        Drawf.shadow(region, shX, shY, shadowRot(b, shX, shY));
        Draw.z(layer + Draw3D.layerOffset(x, y) + hScl / 100f);
        Draw.scl();
        drawTrail(b);
        Draw.scl(1f + hMul(hScl * height));
        if(spinShade){
            PMDrawf.spinSprite(region, trRegion, blRegion, hX, hY, hRot);
        }else{
            Draw.rect(region, hX, hY, hRot);
        }
        Draw.scl();
    }

    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        float hScl = hScl(b),
            h = hScl * height;
        Drawf.light(xHeight(tX(b), h), yHeight(tY(b), h), lightRadius * (1f + hMul(h)), lightColor, lightOpacity);
    }

    public float tX(Bullet b){
        return Mathf.lerp(((float[])b.data)[0], b.x, posInterp(b));
    }

    public float tY(Bullet b){
        return Mathf.lerp(((float[])b.data)[1], b.y, posInterp(b));
    }

    public float hScl(Bullet b){
        float hScl;
        if(b.fdata == 0){
            hScl = hInterp.apply(b.fin());
        }else{
            hScl = hInterp.apply(Mathf.lerp(b.fdata, 1f, b.fin()));
        }
        if(heightRnd != 0) hScl *= 1 + Mathf.randomSeedRange(b.id, heightRnd);
        return hScl;
    }

    public float posInterp(Bullet b){
        if(b.fdata == 0) return b.fin(posInterp);
        float a = posInterp.apply(b.fdata);
        return (posInterp.apply(b.fdata + b.fin() * (1f - b.fdata)) - a) / (1 - a);
    }

    public float shadowRot(Bullet b, float shX, float shY){
        float[] data = (float[])b.data;
        float flat = b.angleTo(data[0], data[1]) + 180f;
        float fin = b.fdata == 0 ? rotInterp.apply(b.fin()) : rotInterp.apply(Mathf.lerp(b.fdata, 1f, b.fin()));
        return fin < 0.5f ?
            PMMathf.lerpAngle(-135, flat, fin * 2) :
            PMMathf.lerpAngle(flat, b.angleTo(shX, shY) + 180, fin * 2 - 1);
    }

    @Override
    public void despawned(Bullet b){
        if(fragBullet instanceof BallisticMissileBulletType){
            createFrags(b, b.x, b.y);
            return;
        }

        Events.fire(new PMEventType.BallisticMissileLand(b, blockEffect));

        if(b.absorbed) return;

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        if(despawnHit){
            hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail instanceof HeightTrail trail && trail.size() > 0){
            TrailFadeFx.heightTrailFade.at(tX(b), tY(b), trailWidth, trailColor, trail.copy());
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet instanceof BallisticMissileBulletType){
            float sx = tX(b), sy = tY(b);
            float dst = b.dst(sx, sy);

            float offset = (1f + b.fdata) * splitTime;
            for(int i = 0; i < fragBullets; i++){
                Tmp.v1.setToRandomDirection().setLength(fragRandomSpread * Mathf.sqrt(Mathf.random())).add(b.x, b.y);
                float lifeScl = Mathf.dst(sx, sy, Tmp.v1.x, Tmp.v1.y) / fragBullet.range;
                Bullet frag = fragBullet.create(b, b.team, sx, sy, Tmp.v1.angleTo(sx, sy) + 180f, 1f, lifeScl);
                frag.fdata = offset;
                frag.lifetime *= 1 - offset;
                if(splitLifeMaxOffset != 0){ //Closer = shorter lifetime, farther = longer lifetime
                    float scl = (frag.dst(sx, sy) - dst) / fragRandomSpread;
                    frag.lifetime += scl * splitLifeMaxOffset;
                }
                if(fragBullet.trailLength > 0 && b.trail != null){
                    frag.trail = b.trail.copy();
                }
            }
        }else{
            super.createFrags(b, x, y);
        }
    }
}
