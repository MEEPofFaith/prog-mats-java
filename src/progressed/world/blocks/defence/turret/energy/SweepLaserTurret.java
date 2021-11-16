package progressed.world.blocks.defence.turret.energy;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.entities.bullet.energy.*;

public class SweepLaserTurret extends PowerTurret{
    public SweepLaserTurret(String name){
        super(name);
        targetAir = false;
    }

    public class SweepLaserTurretBuild extends PowerTurretBuild{
        public Bullet bullet;

        @Override
        public void targetPosition(Posc pos){
            if(peekAmmo() instanceof SweepLaserBulletType anime){
                Vec2 offset = Tmp.v1.setZero();
                if(pos instanceof Hitboxc h){
                    float blastTime = anime.lifetime * (anime.blastTime + 1f) / 2f;
                    offset.set(h.deltaX(), h.deltaY()).scl(blastTime / Time.delta);
                }

                targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, 99999999f));

                if(targetPos.isZero()){
                    targetPos.set(pos);
                }
            }else{
                super.targetPosition(pos);
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(bullet != null){
                heat = 1f;
                recoil = recoilAmount;
                if(!bullet.isAdded()) bullet = null;
            }
        }

        @Override
        protected void updateShooting(){
            if(bullet == null) super.updateShooting();
        }

        @Override
        protected void updateCooling(){
            if(bullet == null) super.updateCooling();
        }

        @Override
        public boolean shouldTurn(){
            return bullet == null;
        }

        @Override
        protected void bullet(BulletType type, float angle){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            bullet = type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }
    }
}