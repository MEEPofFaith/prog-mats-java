package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.renders.*;

public class UltraRailgunBulletType extends BulletType{
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
    public Effect endEffect;

    public UltraRailgunBulletType(float minSpeed, float speed, float damage){
        super(speed, damage);
        this.minSpeed = Math.max(0.001f, minSpeed);
        pierce = pierceBuilding = true;
        pierceDamageFactor = 1f;
        hittable = false;
        absorbable = false;
        drawSize = 3600 * 8;
        hitSize = 12f;
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
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        float shield = 0;
        if(entity instanceof Shieldc s) shield = s.shield();
        super.hitEntity(b, entity, health + shield);
        hitSpeedLoss(b, entity.hitSize());
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);
        hitSpeedLoss(b, build.hitSize());
    }

    protected void hitSpeedLoss(Bullet b, float hitSize){
        float loss = Math.min(maxSpeedLoss, hitSpeedLossPerHitsize * hitSize);
        b.vel.setLength(Math.max(minSpeed, b.vel.len() - loss));
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
        if(dim >= 0) PMRenderer.dimAlpha(dim);
        PMRenderer.dimGlowLine(x1, y1, x2, y2, glowWidth, scl);

        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i]);

            float colorFin = i / (float)(colors.length - 1);
            float baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin);
            Lines.stroke(baseStroke * width * scl);

            Lines.line(x1, y1, x2, y2, false);
        }
    }
}
