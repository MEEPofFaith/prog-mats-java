package progressed.world.blocks.defence.turret;

import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.util.*;

public class SwingContinuousTurret extends ContinuousTurret{
    public float rotateSpeedAccel = 0.5f, rotateSpeedDrag = 0.3f;
    public float aimChangeSpeedAccel = 0.5f, aimChangeSpeedDrag = 0.3f;

    public SwingContinuousTurret(String name){
        super(name);
    }

    public class SwingContinuousTurretBuild extends ContinuousTurretBuild{
        public float realRotateSpeed;

        @Override
        public void updateTile(){
            super.updateTile();

            realRotateSpeed *= Math.max(1f - rotateSpeedDrag * Time.delta, 0);
            rotation = Mathf.mod(rotation + realRotateSpeed * Time.delta, 360f);
        }

        @Override
        protected void turnToTarget(float targetRot){
            float targetSpeed = rotateSpeed * PMMathf.angleMoveDirection(rotation, targetRot);
            realRotateSpeed = Mathf.approachDelta(realRotateSpeed, targetSpeed, rotateSpeedAccel * efficiency);
        }

        protected void updateBullet(BulletEntry entry){
            if(!(entry instanceof SwingBulletEntry s)) return;

            float
                bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
                angle = rotation + entry.rotation;

            entry.bullet.rotation(angle);
            entry.bullet.set(bulletX, bulletY);

            //target length of laser
            float shootLength = Math.min(dst(targetPos), range);
            //current length of laser
            float curLength = dst(entry.bullet.aimX, entry.bullet.aimY);
            float resultLength;
            if(aimChangeSpeed == Float.POSITIVE_INFINITY){
                resultLength = shootLength;
            }else{
                //update aim change speed
                float targetSpeed = aimChangeSpeed * Mathf.sign(shootLength - curLength);
                s.aimChangeSpeed = Mathf.approachDelta(s.aimChangeSpeed, targetSpeed, aimChangeSpeedAccel * efficiency);
                s.aimChangeSpeed *= Math.max(1f - aimChangeSpeedDrag * Time.delta, 0);
                //resulting length of the bullet (smoothed)
                resultLength = curLength + s.aimChangeSpeed;
            }
            //actual aim end point based on length
            Tmp.v1.trns(rotation, lastLength = resultLength).add(x, y);

            entry.bullet.aimX = Tmp.v1.x;
            entry.bullet.aimY = Tmp.v1.y;

            if(isShooting() && hasAmmo()){
                entry.bullet.time = entry.bullet.lifetime * entry.bullet.type.optimalLifeFract * shootWarmup;
                entry.bullet.keepAlive = true;
            }
        }

        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                bullets.add(new SwingBulletEntry(bullet, offsetX, offsetY, angleOffset, 0f));

                //make sure the length updates to the last set value
                Tmp.v1.trns(rotation, shootY + lastLength).add(x, y);
                bullet.aimX = Tmp.v1.x;
                bullet.aimY = Tmp.v1.y;
            }
        }
    }

    public static class SwingBulletEntry extends BulletEntry{
        public float aimChangeSpeed;

        public SwingBulletEntry(Bullet bullet, float x, float y, float rotation, float life){
            super(bullet, x, y, rotation, life);
        }
    }
}
