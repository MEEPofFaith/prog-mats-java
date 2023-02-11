package progressed.world.blocks.defence.turret.payload;

import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.explosive.*;
import progressed.world.draw.*;

import static mindustry.Vars.*;

public class BallisticMissileTurret extends SinglePayloadAmmoTurret{
    public BallisticMissileTurret(String name){
        super(name);

        outlineIcon = false;
        outlinedIcon = 1;
        rotateSpeed = 3600f;

        drawer = new DrawPayloadTurret(false);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.shootRange);
        stats.add(Stat.shootRange, "@-@ @", StatValues.fixValue(minRange / tilesize), StatValues.fixValue(range / tilesize), StatUnit.blocks.localized());
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Pal.accentBack);
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
        protected void findTarget(){
            float range = range();

            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded() && unitFilter.get(e) && dst(e) >= minRange, unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && unitFilter.get(e) && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround) && dst(e) >= minRange, b -> targetGround && buildingFilter.get(b) && dst(b) >= minRange, unitSort);

                if(target == null && canHeal()){
                    target = Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this);
                }
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            Drawf.dashCircle(x, y, minRange, Pal.accentBack);
        }
    }
}
