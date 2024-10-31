package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class EMPCloudBulletType extends BulletType{
    protected static Rand cloudRand = new Rand();

    public float radius = 20f * tilesize;
    public float growTime = 2f * 60f;
    public float empInterval = 20f;
    public float timeDuration = 60f * 10f;
    public float powerDamageScl = 2f, powerSclDecrease = 0.2f;
    public boolean hitUnits = true;
    public float unitDamageScl = 0.5f;
    public Color cloudColor = PMPal.nukeEmp.cpy().a(0.125f);
    public int cloudGroups = 15, cloudsPerGroup = 6;
    public float cloudLifeMin = 0.9f;
    public float cloudRad = 9f * tilesize, cloudRadRand = -1f;
    public Effect hitPowerEffect = MissileFx.hitEmpSpark;
    public float lightningEffectChance = 0.5f;
    public LightningEffect lightningEffect = LightningFx.empLightning;

    public EMPCloudBulletType(float damage){
        super(0f, damage);
        collides = hittable = absorbable = false;
        hitEffect = despawnEffect = Fx.none;
        hitColor = PMPal.nukeEmp;
        layer = Layer.effect + 0.021f;
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, (radius + cloudRad + cloudRadRand) * 2);
        if(cloudRadRand < 0f) cloudRadRand = cloudRad / 2f;
    }

    @Override
    public float continuousDamage(){
        return damage / empInterval * 60f;
    }

    @Override
    public void draw(Bullet b){
        Draw.color(cloudColor);
        float scl = scl(b);
        for(int i = 0; i < cloudGroups; i++){
            cloudRand.setSeed(b.id + i);
            float lifeScl = cloudRand.random(cloudLifeMin, 1f);

            float fin = b.fin() / lifeScl;
            if(fin >= 1) continue;

            Angles.randLenVectors(b.id + i + cloudGroups, Interp.pow5Out.apply(Mathf.curve(b.time / lifeScl, 0f, growTime)), cloudsPerGroup, scl * radius, (x, y, in, out) -> {
                float fout = Interp.pow5Out.apply(1f - fin);
                float rad = scl * fout * (cloudRad + cloudRand.range(cloudRadRand));
                Fill.circle(b.x + x, b.y + y, rad);
            });
        }
    }

    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        float scl = Interp.pow3Out.apply(Mathf.curve(b.time, 0f, growTime));
        Drawf.light(b, lightRadius * scl, lightColor, lightOpacity);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        updateLightningEffects(b);
        updateEmp(b);
    }

    public void updateLightningEffects(Bullet b){
        if(lightningEffectChance > 0.01f &&
            b.time < b.lifetime - lightningEffect.lifetime &&
            Mathf.chanceDelta(lightningEffectChance * b.fout(Interp.pow5Out))
        ){
            float rad = rad(b);
            PMMathf.randomCirclePoint(Tmp.v1, rad);
            Tmp.v1.add(b);
            float x1 = Tmp.v1.x, y1 = Tmp.v1.y;
            PMMathf.randomCirclePoint(Tmp.v1, rad);
            Tmp.v1.add(b);
            lightningEffect.at(x1, y1, Tmp.v1.x, Tmp.v1.y, hitColor);
        }
    }

    public void updateEmp(Bullet b){
        if(!b.timer(3, empInterval)) return;

        float rad = rad(b);
        Vars.indexer.allBuildings(b.x, b.y, rad, other -> {
            if(other.team != b.team && other.power != null){
                if(other.power.graph.getLastPowerProduced() > 0f){
                    other.applySlowdown(powerSclDecrease, timeDuration);
                    other.damage(b.damage * powerDamageScl);
                    hitPowerEffect.at(other.x, other.y, b.angleTo(other), hitColor);
                }
            }
        });

        if(hitUnits){
            Units.nearbyEnemies(b.team, b.x, b.y, radius, other -> {
                if(other.team != b.team && other.hittable()){
                    hitPowerEffect.at(other.x, other.y, b.angleTo(other), hitColor);
                    other.damage(damage * unitDamageScl);
                    other.apply(status, statusDuration);
                }
            });
        }
    }

    public float scl(Bullet b){
        return Interp.pow5Out.apply(Mathf.curve(b.time, 0f, growTime));
    }

    public float rad(Bullet b){
        return scl(b) * radius;
    }
}
