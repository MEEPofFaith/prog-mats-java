package progressed.world.blocks.defence.turret.energy;

import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;

public class NexusTurret extends ItemTurret{
    public NexusTurret(String name){
        super(name);

        shootEffect = smokeEffect = Fx.none;
        recoil = 0f;
        minRange = 0f;
        shootY = 0f;
    }

    public class NexusTurretBuild extends ItemTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(shoot.firstShotDelay / Time.delta);
            }

            targetPos.set(pos).add(offset);
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        public float drawrot(){
            return -90f;
        }
    }
}
