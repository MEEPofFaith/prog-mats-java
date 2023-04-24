package progressed.entities.pattern;

import arc.math.*;
import arc.util.*;
import mindustry.entities.pattern.*;

public class EnhancedShootHelix extends ShootPattern{
    public float scl = 2f, mag = 1.5f, offset = Mathf.PI;

    public EnhancedShootHelix(){
        shots = 2;
    }

    public EnhancedShootHelix(float scl, float mag){
        this();
        this.scl = scl;
        this.mag = mag;
        offset = scl * Mathf.halfPi;
    }

    @Override
    public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
        for(int i = 0; i < shots; i++){
            float off = offset + i * Mathf.PI2 * scl / shots;
            handler.shoot(Mathf.cos(off, scl, scl * mag), 0, 0, firstShotDelay + shotDelay * i,
                b -> b.moveRelative(0f, Mathf.sin(b.time + off, scl, mag)));
        }
    }
}
