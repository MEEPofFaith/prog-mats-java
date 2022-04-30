package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class OrbBulletType extends BulletType{
    public Color color = Pal.lancerLaser;
    public float radius = 3f;

    public int driftTrails = 3, driftTrailLength = 10;
    public float driftTrailWidth = 1f, driftTrailVelocity = 2f, driftTrailSwingSpeed = 1f, driftTrailArc = 60f;

    public OrbBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(!hasDriftTrails()) return;
        DriftTrail[] trails = new DriftTrail[driftTrails];
        for(int i = 0; i < driftTrails; i++){
            trails[i] = new DriftTrail(driftTrailLength);
        }
        b.data = trails;
    }

    @Override
    public void updateTrail(Bullet b){
        super.updateTrail(b);

        if(!headless && hasDriftTrails()){
            DriftTrail[] data = (DriftTrail[])b.data;
            for(int i = 0; i < driftTrails; i++){
                float rot = b.rotation() - 180f + Mathf.sinDeg(
                driftTrailSwingSpeed * b.time +
                    i * (360f / driftTrails) +
                    Mathf.randomSeed(b.id, 360f)
                ) * driftTrailArc;
                data[i].update(b.x, b.y, Tmp.v1.trns(rot, driftTrailVelocity));
            }
        }
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        Fill.circle(b.x, b.y, radius);
    }

    @Override
    public void drawTrail(Bullet b){
        super.drawTrail(b);

        if(hasDriftTrails()){
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            for(DriftTrail t : (DriftTrail[])b.data){
                if(t != null) t.draw(Pal.lancerLaser, driftTrailWidth);
            }
            Draw.z(z);
        }
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);

        if(!hasDriftTrails()) return;
        for(DriftTrail t : (DriftTrail[])b.data){
            UtilFx.driftTrailFade.at(b.x, b.y, driftTrailWidth, color, t);
        }
    }

    //I'm lazy
    public boolean hasDriftTrails(){
        return driftTrails > 0 && driftTrailLength > 0;
    }
}
