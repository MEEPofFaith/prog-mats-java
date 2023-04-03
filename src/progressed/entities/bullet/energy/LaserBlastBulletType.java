package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class LaserBlastBulletType extends BulletType{
    public float length, width;
    
    public LaserBlastBulletType(float speed, float damage){
        super(speed, damage);
        trailWidth = -1f;
        shootEffect = smokeEffect = Fx.none;
        displayAmmoMultiplier = false;
    }

    @Override
    public void init(){
        super.init();

        if(trailWidth < 0) trailWidth = width / 2f;
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            Tmp.v1.trns(b.rotation() - 180f, length / 2f - width / 2f);
            b.trail.update(b.x + Tmp.v1.x, b.y + Tmp.v1.y);
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);

        Draw.color(hitColor);
        PMDrawf.pill(b.x, b.y, b.rotation(), length, width);

        Draw.color(Color.white);
        PMDrawf.pill(b.x, b.y, b.rotation(), length / 2f, width / 2f);

        Draw.color();
    }
}
