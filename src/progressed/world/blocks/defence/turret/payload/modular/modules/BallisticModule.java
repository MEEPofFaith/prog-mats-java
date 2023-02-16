package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.entities.*;
import progressed.entities.bullet.explosive.*;
import progressed.world.draw.*;
import progressed.world.meta.*;

public class BallisticModule extends ItemTurretModule{
    public BallisticModule(String name){
        super(name);
        consumeAmmoOnce = false;
        recoil = 0f;
        shootY = 0f;
        shootEffect = smokeEffect = Fx.none;

        drawer = new DrawTurretModule();
    }

    @Override
    public void limitRange(float margin){
        for(var entry : ammoTypes.copy().entries()){
            limitRange(entry.value, margin);
        }
    }

    @Override
    public void limitRange(BulletType bullet, float margin){
        bullet.speed = range + bullet.rangeChange + margin;
    }

    public class BallisticModuleBuild extends ItemTurretModuleBuild{
        protected Seq<Posc> targets = new Seq<>();

        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(shoot.firstShotDelay / Time.delta);
            }

            if(bullet instanceof BallisticMissileBulletType){
                targetPos.set(pos).add(offset);
                if(pos instanceof Hitboxc h){
                    offset.set(h.deltaX(), h.deltaY()).scl(bullet.lifetime);
                    targetPos.add(offset);
                }
            }else{
                targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
            }

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        protected void updateReload(){
            if(queuedBullets > 0) return;
            super.updateReload();
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;
            super.drawSelect();
            Drawf.dashCircle(x, y, minRange, Pal.accentBack);
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            if(!isControlled() && !logicControlled()){
                targets.clear();
                PMDamage.allNearbyEnemies(team, x, y, range, h -> {
                    if(h instanceof Unit u){
                        if(!u.dead() && unitFilter.get(u) && (u.isGrounded() || targetAir) && (!u.isGrounded() || targetGround) && dst(u) >= minRange) targets.add(u);
                    }else if(h instanceof Building b){
                        if(targetGround && buildingFilter.get(b) && dst(b) >= minRange) targets.add(b);
                    }
                });

                if(!targets.isEmpty()){
                    target = targets.random();
                    targetPosition(target);
                }
            }
            turnToTarget(angleTo(targetPos));

            super.bullet(type, xOffset, yOffset, angleOffset, mover);
        }

        @Override
        public float drawrot(){
            return 0;
        }
    }
}
