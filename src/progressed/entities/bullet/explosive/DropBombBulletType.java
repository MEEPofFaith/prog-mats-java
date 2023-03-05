package progressed.entities.bullet.explosive;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.headless;
import static progressed.graphics.DrawPseudo3D.*;

public class DropBombBulletType extends BulletType{
    public float height = 1f;
    public boolean drawZone = true;
    public float zoneLayer = Layer.bullet - 1f, shadowLayer = Layer.flyingUnit + 1;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = -1f;
    public float shadowOffset = -1f;
    public boolean trailOver = true;
    public Color targetColor = Color.red;
    public String sprite;
    public TextureRegion region;

    public DropBombBulletType(float damage, float radius, String sprite){
        super(0f, 0f);
        this.sprite = sprite;
        splashDamageRadius = radius;
        splashDamage = damage;

        despawnEffect = MissileFx.smallBoom;
        hitSound = Sounds.explosion;
        layer = Layer.flyingUnit + 1.8f;
        lifetime = 30f;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        scaledSplashDamage = true;
        status = StatusEffects.blasted;
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        //Set draw size to the max distance the shadow can travel
        drawSize = Math.max(drawSize, (shadowOffset + PMMathf.cornerDst(region.width / 4f, region.height / 4f)) * 2);
    }

    @Override
    public void init(){
        if(shadowOffset < 0) shadowOffset = height * 2f;
        if(shrinkRad < 0) shrinkRad = zoneRadius / 6f;

        super.init();
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new HeightTrail(trailLength);
            }
            HeightTrail trail = (HeightTrail)b.trail;
            trail.length = trailLength;
            trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)), b.fout() * b.fdata * height);
        }
    }

    @Override
    public void draw(Bullet b){
        //Target
        Draw.z(zoneLayer - 0.01f);
        if(drawZone && zoneRadius > 0f){
            Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
            Fill.circle(b.x, b.y, zoneRadius);
            Draw.color(Color.red, 0.5f);
            float subRad = b.fin() * (zoneRadius + shrinkRad),
                inRad = Math.max(0, zoneRadius - subRad),
                outRad = Math.min(zoneRadius, zoneRadius + shrinkRad - subRad);

            PMDrawf.ring(b.x, b.y, inRad, outRad);
        }
        Draw.z(zoneLayer);
        PMDrawf.target(b.x, b.y, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);

        //Bomb
        float hScl = b.fout() * b.fdata,
            shX = b.x - shadowOffset * hScl,
            shY = b.y - shadowOffset * hScl;
        Draw.z(shadowLayer);
        Draw.scl(1f + hScl);
        Drawf.shadow(region, shX, shY, -45f);
        Draw.z(layer + DrawPseudo3D.layerOffset(b.x, b.y) + hScl / 100f);
        drawTrail(b);
        Draw.scl(1f + hMul(hScl));
        Draw.rect(region, xHeight(b.x, hScl * height), yHeight(b.y, hScl * height), 180);
    }

    public void drawTrail(Bullet b){
        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(z + 0.0001f * (trailOver ? 1 : -1));
            b.trail.draw(trailColor, trailWidth);
            b.trail.drawCap(trailColor, trailWidth); //Also draw cap since the trail is on top
            Draw.z(z);
        }
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail instanceof HeightTrail trail && trail.size() > 0){
            TrailFadeFx.heightTrailFade.at(b.x, b.y, trailWidth, trailColor, trail.copy());
        }
    }
}
