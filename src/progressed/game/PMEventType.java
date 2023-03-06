package progressed.game;

import mindustry.entities.*;
import mindustry.gen.*;

public class PMEventType{
    public static class BallisticMissileLand{
        public final Bullet bullet;
        public final Effect blockEffect;

        public BallisticMissileLand(Bullet bullet, Effect blockEffect){
            this.bullet = bullet;
            this.blockEffect = blockEffect;
        }
    }
}
