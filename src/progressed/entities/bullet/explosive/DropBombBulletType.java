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

import static progressed.graphics.DrawPseudo3D.*;

public class DropBombBulletType extends BulletType{
    public float height = 1f;
    public boolean drawZone = true;
    public float zoneLayer = Layer.bullet - 1f, shadowLayer = Layer.flyingUnit + 1;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = -1f;
    public float shadowOffset = -1f;
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
        lifetime = 30f;
        collides = hittable = absorbable = reflectable = keepVelocity = backMove = false;
        scaledSplashDamage = true;
        status = StatusEffects.blasted;
    }

    @Override
    public void load(){
        region = Core.atlas.find(sprite);

        //Set draw size to the max distance the shadow can travel
        drawSize = Math.max(drawSize, (shadowOffset + PMMathf.cornerDst(region.width / 4f, region.height / 4f)));
    }

    @Override
    public void init(){
        if(shadowOffset < 0) shadowOffset = height * 2f;
        if(shrinkRad < 0) shrinkRad = zoneRadius / 6f;

        super.init();
    }

    @Override
    public void draw(Bullet b){
        //TODO trail?

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

        float hScl = b.fout() * b.fdata,
            shX = b.x - shadowOffset * hScl,
            shY = b.y - shadowOffset * hScl;
        Draw.z(shadowLayer);
        Draw.scl(1f + hScl);
        Drawf.shadow(region, shX, shY, -45f);
        Draw.scl(1f + hMul(hScl));
        Draw.rect(region, xHeight(b.x, hScl * height), yHeight(b.y, hScl * height), 180);
    }
}
