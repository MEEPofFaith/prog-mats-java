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
import progressed.world.blocks.defence.BallisticProjector.*;

//TODO Set to proper name later
public class BallisticMissleBulletType extends BulletType{
    public boolean drawZone = true;
    public float height = 0.15f;
    public float zoneLayer = Layer.bullet - 1f;
    public float targetRadius = 1f, zoneRadius = -1f;
    public float shadowOffset = 18f;
    public float splitTime = 0.5f;
    public Color targetColor = Color.red;
    public String sprite;
    public Effect blockEffect = MissileFx.missileBlocked;
    public float fartVolume = 50f;

    public TextureRegion region;

    public BallisticMissleBulletType(float speed, String sprite){
        super(speed, 0f);
        this.sprite = sprite;

        despawnEffect = MissileFx.missileExplosion;
        layer = Layer.flyingUnit + 1;
        ammoMultiplier = 1;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        scaleLife = true;
        scaledSplashDamage = true;
        status = StatusEffects.blasted;
        drawSize = Float.MAX_VALUE;
    }

    @Override
    public void init(){
        if(blockEffect == Fx.none) blockEffect = despawnEffect;
        if(ProgMats.farting() && hitSound != Sounds.none){
            hitSound = PMSounds.gigaFard;
            hitSoundVolume = fartVolume;
        }
        if(zoneRadius < 0) zoneRadius = splashDamageRadius;

        super.init();
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        float px = b.x + b.lifetime * b.vel.x,
            py = b.y + b.lifetime * b.vel.y;

        b.data = new float[]{b.x, b.y, 0f};
        b.lifetime(b.dst(px, py) / speed);
        b.set(px, py);
        b.vel.setZero();
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(fragBullet instanceof BallisticMissleBulletType && b.fin() >= splitTime){
            b.remove();
            ((float[])b.data)[2] = 1f;
        }
    }

    @Override
    public void draw(Bullet b){
        //Target
        Draw.z(zoneLayer - 0.01f);
        if(drawZone && zoneRadius > 0f){
            Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
            Fill.circle(b.x, b.y, zoneRadius);
        }
        Draw.z(zoneLayer);
        PMDrawf.target(b.x, b.y, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);

        //Missile
        Draw.z(layer);
        float[] startPos = (float[])b.data;;
        float rot = b.angleTo(startPos[0], startPos[1]) + 180f,
            x = Mathf.lerp(startPos[0], b.x, b.fin()),
            y = Mathf.lerp(startPos[1], b.y, b.fin()),
            hScl = Interp.sineOut.apply(Mathf.slope(Mathf.lerp(b.fdata, 1f, b.fin())));

        Drawf.shadow(region, x - shadowOffset * hScl, y - shadowOffset - hScl, rot);
        Draw.rect(region, DrawPsudo3D.xHeight(x, hScl * height), DrawPsudo3D.yHeight(y, hScl * height), rot);
    }

    @Override
    public void despawned(Bullet b){
        if(((float[])b.data)[2] != 1f){
            ShieldBuild shield = (ShieldBuild)Units.findEnemyTile(b.team, b.x, b.y, BallisticProjector.maxShieldRange, build -> build instanceof ShieldBuild s && !s.broken && PMMathf.isInSquare(s.x, s.y, s.realRadius(), b.x, b.y));
            if(shield != null){ //Ballistic Shield blocks the missile
                blockEffect.at(b.x, b.y, b.rotation(), hitColor);
                despawnSound.at(b);

                Effect.shake(despawnShake, despawnShake, b);

                shield.hit = 1f;
                shield.buildup += (b.damage() + splashDamage * shield.realStrikeBlastResistance() * b.damageMultiplier()) * shield.warmup;

                return;
            }
        }

        if(!fragOnHit || fragBullet instanceof BallisticMissleBulletType){
            createFrags(b, b.x, b.y);
            return;
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
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet instanceof BallisticMissleBulletType){
            float[] startPos = (float[])b.data;
            float sx = Mathf.lerp(startPos[0], b.x, b.fin()), sy = Mathf.lerp(startPos[1], b.y, b.fin());
            float offset = (1f + b.fdata) * splitTime;
            for(int i = 0; i < fragBullets; i++){
                Tmp.v1.setToRandomDirection().setLength(fragSpread * Mathf.sqrt(Mathf.random())).add(b.x, b.y);
                float lifeScl = Mathf.dst(sx, sy, Tmp.v1.x, Tmp.v1.y) / fragBullet.range;
                Bullet frag = fragBullet.create(b, b.team, sx, sy, Tmp.v1.angleTo(sx, sy) + 180f, 1f, lifeScl);
                frag.fdata = offset;
            }
        }else{
            super.createFrags(b, x, y);
        }
    }
}
