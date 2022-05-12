package progressed.world.blocks.defence.turret.energy;

import arc.func.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.entities.bullet.energy.*;

public class SweepLaserTurret extends PowerTurret{
    public float retractDelay;
    public Cons<SweepLaserTurretBuild> pointDrawer = tile -> {};

    public SweepLaserTurret(String name){
        super(name);
        targetAir = false;
    }

    public class SweepLaserTurretBuild extends PowerTurretBuild{
        public Bullet bullet;

        @Override
        public void draw(){
            super.draw();

            pointDrawer.get(this);
        }

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
                curRecoil = 1f;
                Tmp.v1.trns(rotation, shootY - curRecoil);
                bullet.set(x + Tmp.v1.x, y + Tmp.v1.y);
                if(!bullet.isAdded() || bullet.type instanceof SweepLaserBulletType s && bullet.fin() >= s.retractTime + retractDelay) bullet = null;
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
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                this.bullet = bullet;
            }
        }
    }
}
