package progressed.world.blocks.defence.turret.energy;

import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;

public class NexusTurret extends PowerTurret{
    public NexusTurret(String name){
        super(name);

        shootEffect = smokeEffect = Fx.none;
        recoil = 0f;
    }

    public class NexusTurretBuild extends PowerTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            if(pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(bullet.lifetime / Time.delta);
            }

            targetPos.set(pos).add(offset);

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }
    }
}
