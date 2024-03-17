package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import blackhole.utils.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.effect.*;
import progressed.graphics.renders.*;

public class UltraRailgunBulletType extends BulletType{
    public static final Effect defaultShockwave = new Effect(360, 3600 * 8, e -> {
        float radius = (float)e.data;
        e.lifetime = 3f * Mathf.sqrt(radius);
        radius *= e.fin(Interp.pow3Out);
        float thinScl = 0.05f; //Small radius of ellipse compared to large radius
        float thickScl = 0.5f; //Thickness of ring from edge

        int sides = Lines.circleVertices(radius);
        float space = 360f / sides;
        Draw.color(Color.white, 0.8f * e.fout(Interp.pow3Out));
        for(int i = 0; i < sides; i++){
            float ang = space * i, ang2 = ang + space;
            float x1 = Mathf.cosDeg(ang) * radius * thinScl,
                y1 = Mathf.sinDeg(ang) * radius,
                x2 = Mathf.cosDeg(ang2) * radius * thinScl,
                y2 = Mathf.sinDeg(ang2) * radius,
                x3 = x2 * thickScl,
                y3 = y2 * thickScl,
                x4 = x1 * thickScl,
                y4 = y1 * thickScl;

            Fill.quad(
                e.x + Angles.trnsx(e.rotation, x1, y1),
                e.y + Angles.trnsy(e.rotation, x1, y1),
                e.x + Angles.trnsx(e.rotation, x2, y2),
                e.y + Angles.trnsy(e.rotation, x2, y2),
                e.x + Angles.trnsx(e.rotation, x3, y3),
                e.y + Angles.trnsy(e.rotation, x3, y3),
                e.x + Angles.trnsx(e.rotation, x4, y4),
                e.y + Angles.trnsy(e.rotation, x4, y4)
            );
        }
    }).layer(Layer.bullet - 0.03f); //Don't bloom

    public float accel = 1f;
    public float hitSpeedLossPerHitsize = 0.1f;
    public float maxSpeedLoss = 20f;
    public float minSpeed;
    public Color[] colors = {Color.valueOf("ffffff55"), Color.valueOf("ffffffaa"), Color.white};
    public float strokeFrom = 1.25f, strokeTo = 0.25f;
    public float width = 8f;
    public float glowWidth = 64f;
    public float fadeInTime = 15f;
    public float fadeOutTime = 120f;
    public Effect shockwaveEffect = defaultShockwave;
    public float minSizeThreshold = 15f, maxSizeThreshold = 50f;
    public float thresholdDecreaseMaxHealth = 100_000;
    public float shockwaveSizeScl = 8f;
    public Effect endShockwaveEffect = new FlashEffect(new WrapDataEffect(defaultShockwave, 75f * 8f), 30f);
    public Effect endEffect;

    static{
        BlackHoleUtils.immuneBulletTypes.add(UltraRailgunBulletType.class);
    }

    public UltraRailgunBulletType(float minSpeed, float speed, float damage){
        super(speed, damage);
        this.minSpeed = Math.max(0.001f, minSpeed);
        pierce = pierceBuilding = true;
        pierceDamageFactor = 1f;
        hittable = false;
        absorbable = false;
        drawSize = 3600 * 8;
        hitSize = 12f;
        hitEffect = despawnEffect = Fx.none;
    }

    @Override
    public void init(){
        super.init();

        if(endEffect == null){
            endEffect = new Effect(fadeOutTime, drawSize, e -> {
                Tmp.v1.trns(e.rotation, (float)e.data);
                drawBeam(e.x - Tmp.v1.x, e.y - Tmp.v1.y, e.x, e.y, e.fout()  * 0.9f, e.fout());
            });
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        float shield = 0;
        if(entity instanceof Shieldc s) shield = s.shield();
        health += shield;
        super.hitEntity(b, entity, health);
        handleShockwave(b, entity.hitSize(), health);
        hitSpeedLoss(b, entity.hitSize());
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);
        handleShockwave(b, build.hitSize(), initialHealth);
        hitSpeedLoss(b, build.hitSize());
    }

    protected void hitSpeedLoss(Bullet b, float hitSize){
        float loss = Math.min(maxSpeedLoss, hitSpeedLossPerHitsize * hitSize);
        b.vel.setLength(Math.max(minSpeed, b.vel.len() - loss));
    }

    protected void handleShockwave(Bullet b, float hitSize, float health){
        if(b.damage <= 0){
            endShockwaveEffect.at(b.x, b.y, b.rotation());
        }else{
            float thresh = Mathf.lerp(maxSizeThreshold, minSizeThreshold, Mathf.clamp(health / thresholdDecreaseMaxHealth));
            if(hitSize >= thresh){
                shockwaveEffect.at(b.x, b.y, b.rotation(), hitSize * shockwaveSizeScl);
            }
        }
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        b.vel.setLength(Math.min(speed, b.vel.len() + accel * Time.delta));
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
        endEffect.at(b.x, b.y, Angles.angle(b.originX, b.originY, b.x, b.y), Mathf.dst(b.originX, b.originY, b.x, b.y));
    }

    @Override
    public void draw(Bullet b){
        drawBeam(b.originX, b.originY, b.x, b.y, Mathf.curve(b.time, 0, fadeInTime)  * 0.9f, 1f);
    }

    protected void drawBeam(float x1, float y1, float x2, float y2, float dim, float scl){
        if(dim >= 0) PMRenders.dimAlpha(dim);
        PMRenders.dimGlowLine(x1, y1, x2, y2, glowWidth, scl);

        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i]);

            float colorFin = i / (float)(colors.length - 1);
            float baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin);
            Lines.stroke(baseStroke * width * scl);

            Lines.line(x1, y1, x2, y2, false);
        }
    }
}
