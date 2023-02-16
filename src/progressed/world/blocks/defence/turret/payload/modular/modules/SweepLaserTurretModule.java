package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.util.*;

public class SweepLaserTurretModule extends PowerTurretModule{
    public float angleRnd = 30f;
    public float endDst = 10f * 8f, endRad = 2f * 8f;
    public float handleAngleRnd = 60f, handleLengthMin = 2f * 8f, handleLengthMax = 8f * 8f;
    public float sweepDuration = 60f;

    public SweepLaserTurretModule(String name){
        super(name);
    }

    public class SweepLaserTurretModuleBuild extends PowerTurretModuleBuild{
        public float dst, life, time;
        public Bullet bullet;

        @Override
        public void draw(){ //TODO remove debug
            super.draw();

            if(bullet != null){
                Tmp.v1.trns(rotation, dst);
                float bx = x + Tmp.v1.x, by = y + Tmp.v1.y;
                Fill.circle(bx, by, 2f);
                for(int i = 0; i < 20; i++){
                    path(i / 20f, bx, by, rotation);
                    float x1 = Tmp.v1.x, y1 = Tmp.v1.y;
                    path((i + 1) / 20f, bx, by, rotation);
                    float x2 = Tmp.v1.x, y2  = Tmp.v1.y;
                    Lines.line(x1, y1, x2, y2);
                }

                Lines.line(x, y, bullet.x, bullet.y);
            }
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;
            super.updateTile();

            if(bullet != null) updateBullet();
        }

        protected void updateBullet(){
            if(life <= 0 || !bullet.isAdded() || bullet.time > bullet.lifetime || bullet.owner != this){
                bullet = null;
                return;
            }

            Tmp.v1.trns(rotation, dst);
            float bx = x + Tmp.v1.x, by = y + Tmp.v1.y;
            path(Interp.smooth.apply(time), bx, by, rotation);
            bullet.rotation(Tmp.v2.angle());
            bullet.set(Tmp.v1);
            bullet.time = Math.min(bullet.time, bullet.lifetime * bullet.type.optimalLifeFract);
            bullet.keepAlive = true;

            time += Time.delta / sweepDuration;
            life -= Time.delta / Math.max(efficiency, 0.00001f);

            wasShooting = true;
            heat = 1f;
            curRecoil = 1f;
        }

        @Override
        protected void updateReload(){
            if(bullet != null) return;
            super.updateReload();
        }

        @Override
        protected void updateShooting(){
            if(bullet != null) return;
            super.updateShooting();
        }

        @Override
        protected void turnToTarget(float targetRot){
            if(bullet != null) return;
            super.turnToTarget(targetRot);
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            super.bullet(type, xOffset, yOffset, angleOffset, mover);

            dst = Mathf.clamp(dst(targetPos), minRange, range());
        }

        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            this.bullet = bullet;
            time = 0;
            life = sweepDuration;
        }

        public void path(float t, float bx, float by, float rot){
            Mathf.rand.setSeed(id + pos() + totalShots);
            float pathRot = rot + 90f + Mathf.rand.range(angleRnd);

            Tmp.v1.trns(pathRot, endDst / 2).add(PMMathf.randomCirclePoint(Tmp.v2, endRad));
            float x1 = Tmp.v1.x + bx, y1 = Tmp.v1.y + by; //Left point
            Tmp.v1.trns(pathRot, -endDst / 2).add(PMMathf.randomCirclePoint(Tmp.v2, endRad));
            float x2 = Tmp.v1.x + bx, y2 = Tmp.v1.y + by; //Right point

            int flip = Mathf.randomSign();
            Tmp.v3.trns(pathRot + 180 + Mathf.rand.random(handleAngleRnd) * flip, Mathf.rand.random(handleLengthMin, handleLengthMax)); //Left
            Tmp.v4.trns(pathRot + Mathf.rand.random(handleAngleRnd) * flip, Mathf.rand.random(handleLengthMin, handleLengthMax)); //Right

            Tmp.v1.set(PMMathf.bezier(t, x1, y1, x2, y2, Tmp.v3, Tmp.v4));
            Tmp.v2.set(PMMathf.bezierDeriv(t, x1, y1, x2, y2, Tmp.v3, Tmp.v4));
        }
    }
}
