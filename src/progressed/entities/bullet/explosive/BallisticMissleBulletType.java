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
    public float height = 0.15f;
    public float targetRadius = 1f;
    public float shadowOffset = 18f;
    public Color targetColor = Color.red;
    public String sprite;
    public Effect blockEffect = MissileFx.missileBlocked;
    public float fartVolume = 50f;

    public TextureRegion region;

    public BallisticMissleBulletType(float speed, String sprite){
        super(speed, 0f);
        this.sprite = sprite;

        despawnEffect = MissileFx.missileExplosion;
        layer = Layer.effect + 1;
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

        if(b.data == null) b.data = new float[]{b.x, b.y, 0f};
        b.lifetime(b.dst(b.aimX, b.aimY) / speed);
        b.set(px, py);
        b.vel.setZero();
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(fragBullet instanceof BallisticMissleBulletType && b.fin() >= 0.5f){
            b.remove();
            b.data = true;
        }
    }

    @Override
    public void draw(Bullet b){
        //Target
        Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
        Fill.circle(b.x, b.y, splashDamageRadius);
        PMDrawf.target(b.x, b.y, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);

        //Missile
        if(b.data instanceof float[] startPos){
            float rot = b.angleTo(startPos[0], startPos[1]) + 180f,
                x = Mathf.lerp(startPos[0], b.x, b.fin()),
                y = Mathf.lerp(startPos[1], b.y, b.fin()),
                hScl = Interp.sineOut.apply(startPos[2] == 1f ? b.fin() : b.fslope());

            Drawf.shadow(region, x - shadowOffset * hScl, y - shadowOffset - hScl, rot);
            Draw.rect(region, DrawPsudo3D.xHeight(x, hScl * height), DrawPsudo3D.yHeight(y, hScl * height), rot);
        }
    }

    @Override
    public void despawned(Bullet b){
        if(!(b.data instanceof Boolean)){
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

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);

        if(b.data instanceof Boolean) return;

        if(despawnHit){
            hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet instanceof BallisticMissleBulletType){
            for(int i = 0; i < fragBullets; i++){
                Tmp.v1.setToRandomDirection().setLength(fragSpread * Mathf.sqrt(Mathf.random())).add(b.aimX, b.aimY);
                fragBullet.create(b, b.team, x, y, b.rotation(), -1f, 1f, 1f, new float[]{b.x, b.y, 1f}, null, Tmp.v1.x, Tmp.v1.y);
            }
        }else{
            super.createFrags(b, x, y);
        }
    }
}
