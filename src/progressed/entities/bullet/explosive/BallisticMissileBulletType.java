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
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.*;
import progressed.world.blocks.defence.ShieldProjector.*;

import static mindustry.Vars.*;
import static progressed.graphics.DrawPseudo3D.*;

public class BallisticMissileBulletType extends BulletType{
    public boolean drawZone = true;
    public float height = 0.5f;
    public float zoneLayer = Layer.bullet - 1f, shadowLayer = Layer.flyingUnit + 1;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = 4f;
    public float shadowOffset = -1f;
    public float splitTime = 0.5f;
    public float splitLifeMaxOffset = 10f;
    public Color targetColor = Color.red;
    public String sprite;
    public Effect blockEffect = MissileFx.missileBlocked;
    public float fartVolume = 50f;
    public boolean spinShade = true;
    public Interp hInterp = PMMathf.arc, posInterp = Interp.pow2In;

    public TextureRegion region, blRegion, trRegion;

    public BallisticMissileBulletType(String sprite){
        super(1f, 0f);
        this.sprite = sprite;

        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        layer = Layer.flyingUnit + 2;
        ammoMultiplier = 1;
        lifetime = 120f;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        scaleLife = true;
        scaledSplashDamage = true;
        status = StatusEffects.blasted;
        drawSize = Float.MAX_VALUE;
    }

    @Override
    public void init(){
        if(shadowOffset < 0) shadowOffset = height * 48f;
        if(ProgMats.farting() && hitSound != Sounds.none){
            hitSound = PMSounds.gigaFard;
            hitSoundVolume = fartVolume;
        }

        super.init();
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        if(spinShade){
            blRegion = Core.atlas.find(sprite + "-bl");
            trRegion = Core.atlas.find(sprite + "-tr");
        }
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
    public void updateTrail(Bullet b){
        float x = tX(b), y = tY(b), h = hScl(b) * height;
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new HeightTrail(trailLength);
            }
            HeightTrail trail = (HeightTrail)b.trail;
            trail.length = trailLength;
            trail.update(x, y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)), h);
        }

        float[] data = (float[])b.data;
        data[2] = x;
        data[3] = y;
        data[4] = h;
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
        float rot = b.angleTo(data[0], data[1]) + 180f,
            x = tX(b),
            y = tY(b),
            hScl = hScl(b),
            lasthX = xHeight(data[2], data[4]),
            lasthY = yHeight(data[3], data[4]),
            hX = xHeight(x, hScl * height),
            hY = yHeight(y, hScl * height),
            hRot = Angles.angle(lasthX, lasthY, hX, hY);

        Draw.z(shadowLayer);
        Drawf.shadow(region, x - shadowOffset * hScl, y - shadowOffset * hScl, hRot - 90f);
        Draw.z(layer); //Unsure that the trail is drawn underneath.
        drawTrail(b);
        Draw.z(layer + hScl / 100f);
        Draw.scl(hScale(hScl));
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
        float h = hScl(b) * height;
        Drawf.light(xHeight(tX(b), h), yHeight(tY(b), h), lightRadius, lightColor, lightOpacity);
    }

    public float tX(Bullet b){
        return Mathf.lerp(((float[])b.data)[0], b.x, posInterp(b));
    }

    public float tY(Bullet b){
        return Mathf.lerp(((float[])b.data)[1], b.y, posInterp(b));
    }

    public float hScl(Bullet b){
        if(b.fdata == 0) return hInterp.apply(b.fin());
        return hInterp.apply(Mathf.lerp(b.fdata, 1f, b.fin()));
    }

    public float posInterp(Bullet b){
        if(posInterp == Interp.linear) b.fin();
        if(b.fdata == 0) return b.fin(posInterp);
        return posInterp.apply(b.fdata + b.fin() * (1f - b.fdata)) * b.fin();
    }

    @Override
    public void despawned(Bullet b){
        if(fragBullet instanceof BallisticMissileBulletType){
            createFrags(b, b.x, b.y);
            return;
        }

        ShieldBuild shield = (ShieldBuild)Units.findEnemyTile(b.team, b.x, b.y, ShieldProjector.maxShieldRange, build -> build instanceof ShieldBuild s && !s.broken && PMMathf.isInSquare(s.x, s.y, s.realRadius(), b.x, b.y));
        if(shield != null){ //Ballistic Shield blocks the missile
            blockEffect.at(b.x, b.y, b.rotation(), hitColor);
            despawnSound.at(b);

            Effect.shake(despawnShake, despawnShake, b);

            shield.hit = 1f;
            shield.buildup += (b.damage() + splashDamage * shield.realStrikeBlastResistance() * b.damageMultiplier()) * shield.warmup;

            return;
        }

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
            UtilFx.heightTrailFade.at(tX(b), tY(b), trailWidth, trailColor, trail.copy());
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
