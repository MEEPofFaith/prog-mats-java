package progressed.entities.bullet.energy;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.util.*;

public class HealFieldBulletType extends BulletType{
    public float radius = 5f * Vars.tilesize, healing = 0.6f;
    public float growTime = 20f, fadeTime = 60f;
    public float areaEffectChance = 0.25f;
    public Effect areaEffect = Fx.none;

    public HealFieldBulletType(){
        super(0f, 0f);
        collides = false;
        hitEffect = despawnEffect = Fx.none;
    }

    @Override
    public void init(){
        super.init();

        lightRadius = Math.max(lightRadius, radius * 2.5f);
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        float r = radius * Math.min(b.time / growTime, 1f);

        if(b.time <= b.lifetime - areaEffect.lifetime && Mathf.chanceDelta(areaEffectChance)){
            PMMathf.randomCirclePoint(Tmp.v1, r).add(b);
            areaEffect.at(Tmp.v1);
        }

        float fade = 1f - Mathf.curve(b.time, b.lifetime - fadeTime, b.lifetime);
        Units.nearby(b.team, b.x, b.y, r, u -> {
            u.heal(healing * Time.delta * fade);
        });
    }

    @Override
    public void draw(Bullet b){
        float r = radius * Math.min(b.time / growTime, 1f),
            fade = 1f - Mathf.curve(b.time, b.lifetime - fadeTime, b.lifetime);
        Draw.z(Layer.bullet - 1f);
        Fill.light(b.x, b.y, Lines.circleVertices(r), r, Tmp.c1.set(Pal.heal).a(0f), Tmp.c2.set(Pal.heal).a(0.25f * fade));
        Draw.z(layer);
        Draw.color(Pal.heal, fade);
        Lines.circle(b.x, b.y, r);
    }

    @Override
    public void drawLight(Bullet b){
        float r = lightRadius * Math.min(b.time / growTime, 1f),
            fade = 1f - Mathf.curve(b.time, b.lifetime - fadeTime, b.lifetime);
        if(lightOpacity <= 0f || r <= 0f) return;
        Drawf.light(b.team, b, r, lightColor, lightOpacity * fade);
    }
}
