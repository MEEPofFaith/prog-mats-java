package progressed.entities.pattern;

import arc.math.*;
import mindustry.entities.pattern.*;

public class ShootRand extends ShootPattern{
    public float xRand;

    /** Called on a single "trigger pull". This function should call the handler with any bullets that result. */
    public void shoot(int totalShots, BulletHandler handler){
        for(int i = 0; i < shots; i++){
            handler.shoot(Mathf.range(xRand / 2f), 0, 0, firstShotDelay + shotDelay * i);
        }
    }
}
