package progressed.world.blocks.defence.turret.payload;

import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.entities.bullet.explosive.*;

public class BallisticMissileTurret extends SinglePayloadAmmoTurret{
    public BallisticMissileTurret(String name){
        super(name);
    }

    public class BallisticMissileTurretBuild extends SinglePayloadAmmoTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(shoot.firstShotDelay / Time.delta);
            }

            targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

            if(bullet instanceof BallisticMissleBulletType m && pos instanceof Hitboxc h){
                offset.add(Tmp.v2.set(h.deltaX(), h.deltaY()).scl(m.minLifetime));
                targetPos.sub(pos);
                if(offset.len2() > targetPos.len2()) targetPos.set(offset);
                targetPos.add(pos);
            }

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }
    }
}
