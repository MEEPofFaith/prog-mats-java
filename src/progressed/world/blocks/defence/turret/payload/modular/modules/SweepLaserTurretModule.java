package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.util.*;
import progressed.world.draw.*;

import static mindustry.Vars.*;

public class SweepLaserTurretModule extends PowerTurretModule{
    public float angleRnd = 30f;
    public float endDst = 10f * 8f, endRad = 2f * 8f;
    public float handleAngleRnd = 60f, handleLengthMin = 2f * 8f, handleLengthMax = 8f * 8f;
    public float sweepDuration = 60f;

    public SweepLaserTurretModule(String name){
        super(name);
        canOverdrive = false;
        shootSound = Sounds.none;
        loopSoundVolume = 0.5f;
        loopSound = Sounds.laserbeam;

        drawer = new DrawTurretModule();
    }

    public class SweepLaserTurretModuleBuild extends PowerTurretModuleBuild{
        public float dst, life, time;
        public Bullet bullet;

        @Override
        public boolean shouldConsume(){
            //still consumes power when bullet is around
            return bullet != null || isActive() || isShooting();
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;
            super.updateTile();

            if(bullet != null){
                updateBullet();
            }else if(reloadCounter < reload){
                wasShooting = true;

                if(coolant != null){
                    Liquid liquid = liquids.current();
                    float maxUsed = coolant.amount;
                    float used = (cheating() ? maxUsed : Math.min(liquids.get(liquid), maxUsed)) * delta();
                    reloadCounter += used * liquid.heatCapacity * coolantMultiplier;
                    liquids.remove(liquid, used);

                    if(Mathf.chance(0.06 * used)){
                        coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                    }
                }else{
                    reloadCounter += edelta();
                }
            }
        }

        protected void updateBullet(){
            if(life <= 0 || !bullet.isAdded() || bullet.owner != this){
                bullet = null;
                return;
            }

            float bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
                bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);
            bullet.set(bulletX, bulletY);

            Tmp.v1.trns(rotation, dst);
            float bx = x + Tmp.v1.x, by = y + Tmp.v1.y;
            path(Interp.smooth.apply(time), bx, by, rotation);
            bullet.aimX = Tmp.v1.x;
            bullet.aimY = Tmp.v1.y;
            bullet.time = Math.min(bullet.time + Time.delta, bullet.lifetime * bullet.type.optimalLifeFract);
            bullet.keepAlive = true;

            time += Time.delta / sweepDuration;
            life -= Time.delta / Math.max(efficiency, 0.00001f);

            wasShooting = true;
            heat = 1f;
            curRecoil = 1f;
        }

        @Override
        protected void updateCooling(){
            //do nothing, cooling is irrelevant here
        }

        @Override
        protected void updateReload(){
            //updated in updateTile() depending on coolant
        }

        @Override
        protected void updateShooting(){
            if(bullet != null) return;

            if(reloadCounter >= reload && efficiency > 0 && !charging() && shootWarmup >= minWarmup){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter = 0f;
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            if(bullet != null) return;
            rotation = targetRot;
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

            PMMathf.randomCirclePoint(Tmp.v2, endRad);
            Tmp.v1.trns(pathRot, endDst / 2).add(Tmp.v2);
            float x1 = Tmp.v1.x + bx, y1 = Tmp.v1.y + by; //Left point
            Tmp.v1.trns(pathRot, -endDst / 2).sub(Tmp.v2);
            float x2 = Tmp.v1.x + bx, y2 = Tmp.v1.y + by; //Right point

            int flip = Mathf.randomSign();
            float len = Mathf.rand.random(handleLengthMin, handleLengthMax);
            Tmp.v3.trns(pathRot + 180 + Mathf.rand.random(handleAngleRnd) * flip, len); //Left
            Tmp.v4.trns(pathRot + Mathf.rand.random(handleAngleRnd) * flip, len); //Right

            Tmp.v1.set(PMMathf.bezier(t, x1, y1, x2, y2, Tmp.v3, Tmp.v4));
        }

        @Override
        public void pickedUp(){
            super.pickedUp();
            bullet = null;
        }

        @Override
        public float activeSoundVolume(){
            return 1f;
        }

        @Override
        public boolean shouldActiveSound(){
            return bullet != null;
        }
    }
}
