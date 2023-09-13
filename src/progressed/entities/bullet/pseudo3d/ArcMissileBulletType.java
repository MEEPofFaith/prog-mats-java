package progressed.entities.bullet.pseudo3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.graphics.*;

public class ArcMissileBulletType extends ArcBasicBulletType{
    public boolean drawZone = true;
    public float zoneLayer = Layer.bullet - 1f, shadowLayer = Layer.flyingUnit + 1;
    public float targetRadius = 1f, zoneRadius = 3f * 8f, shrinkRad = -1f;
    public Color targetColor = Color.red;
    public float accel = 0.1f;
    public float lifetimeScl = 1f;

    public ArcMissileBulletType(String sprite){
        super(0f, sprite);

        despawnEffect = MissileFx.missileExplosion;
        hitSound = Sounds.largeExplosion;
        ammoMultiplier = 1;
        scaleLife = true;
        status = StatusEffects.blasted;
        layer = Layer.flyingUnit + 2;
        absorbEffect = Pseudo3DFx.absorbed;
    }

    public ArcMissileBulletType(){
        this("prog-mats-basic-missile");
    }

    @Override
    public void init(Bullet b){
        if(accel < 0) accel = 1f;
        super.init(b);
    }

    @Override
    public void arcBulletDataInit(Bullet b){
        ArcBulletData a = (ArcBulletData)b.data;
        a.updateLifetime(b);
        //a.setAccel(b.vel.angle(), acceleration);
        a.updateAimPos(b);
        b.lifetime *= lifetimeScl;
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet instanceof ArcBulletType){
            for(int i = 0; i < fragBullets; i++){
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets/2) * fragSpread);
                fragBullet.create(b, x, y, a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
            }
        }else{
            super.createFrags(b, x, y);
        }
    }

    @Override
    public void draw(Bullet b){
        //Target
        Draw.z(zoneLayer - 0.01f);
        if(drawZone && zoneRadius > 0f){
            Draw.color(Color.red, 0.25f + 0.25f * Mathf.absin(16f, 1f));
            Fill.circle(b.aimX, b.aimY, zoneRadius);
            Draw.color(Color.red, 0.5f);
            float subRad = b.fin() * (zoneRadius + shrinkRad),
                inRad = Math.max(0, zoneRadius - subRad),
                outRad = Math.min(zoneRadius, zoneRadius + shrinkRad - subRad);

            PMDrawf.ring(b.aimX, b.aimY, inRad, outRad);
        }
        Draw.z(zoneLayer);
        PMDrawf.target(b.aimX, b.aimY, Time.time * 1.5f + Mathf.randomSeed(b.id, 360f), targetRadius, targetColor != null ? targetColor : b.team.color, b.team.color, 1f);

        super.draw(b);
    }
}
