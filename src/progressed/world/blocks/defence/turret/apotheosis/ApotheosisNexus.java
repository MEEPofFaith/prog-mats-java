package progressed.world.blocks.defence.turret.apotheosis;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.entities.*;

import static mindustry.Vars.*;

public class ApotheosisNexus extends ReloadTurret{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    public final int damageTimer = timers++;
    public int targetInterval = 20;
    public int damageInterval = 5;

    public float powerUse = 1f;
    public float speed, duration = 60f;
    public float damage, splashDamageRadius = tilesize;
    public StatusEffect status;
    public float statusDuration = 6f * 10f;
    public float cooldown = 0.02f;

    public Sortf unitSort = Unit::dst2;

    public ApotheosisNexus(String name){
        super(name);

        hasPower = true;
        canOverdrive = false;
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisNexusBuild::isActive);
        super.init();
    }

    public class ApotheosisNexusBuild extends ReloadTurretBuild implements ControlBlock{
        public IntSeq chargers = new IntSeq();
        public float heat, logicControlTime = -1;
        public float activeTime;
        public float boost; //Just used in calculations
        public int shotCounter;
        public boolean logicShooting = false;
        public Posc target;
        public Vec2 targetPos = new Vec2(), curPos = new Vec2();
        public boolean wasShooting, isShooting;
        public BlockUnitc unit;

        @Override
        public void created(){
            unit = (BlockUnitc)UnitTypes.block.create(team);
            unit.tile(this);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && (unit == null || !unit.isPlayer())){
                targetPos.set(World.unconv((float)p1), World.unconv((float)p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && (unit == null || !unit.isPlayer())){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc){
                    targetPosition((Posc)p1);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor){
            return switch(sensor){
                case rotation -> rotation;
                case shootX -> World.conv(targetPos.x);
                case shootY -> World.conv(targetPos.y);
                case shooting -> isShooting() ? 1 : 0;
                case progress -> Mathf.clamp(reload / reloadTime);
                default -> super.sense(sensor);
            };
        }

        public boolean isShooting(){
            return (isControlled() ? (unit != null && unit.isShooting()) : logicControlled() ? logicShooting : target != null);
        }

        @Override
        public void draw(){
            super.draw();

            if(isShooting){
                Draw.color(team.color);
                Fill.circle(curPos.x, curPos.y, calcRadius());
            }
        }

        @Override
        public Unit unit(){
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        public boolean isActive(){
            return (target != null || wasShooting || isShooting) && enabled;
        }

        public void targetPosition(Position pos){
            if(!consValid() || pos == null) return;

            if(!isShooting){
                curPos.set(pos);
            }else{
                Tmp.v1.trns(curPos.angleTo(pos), Math.min(calcSpeed() * edelta(), curPos.dst(pos)));
                curPos.add(Tmp.v1);
            }
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;
            checkConnections();

            wasShooting = false;

            heat = Mathf.lerpDelta(heat, 0f, cooldown);

            if(unit != null){
                unit.health(health);
                unit.rotation(rotation);
                unit.team(team);
                unit.set(x, y);
            }

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(consValid()){

                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot = true;

                    if(isControlled()){ //player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                    }else if(logicControlled()){ //logic behavior
                        canShoot = logicShooting;
                    }else{ //default AI behavior
                        targetPos.set(target);

                        if(Float.isNaN(rotation)){
                            rotation = 0;
                        }
                    }

                    targetPosition(targetPos);

                    if(canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }

            float dur = calcDuration();
            if(isShooting && activeTime < dur){
                activeTime += Time.delta / Math.max(efficiency(), 0.00001f);
                if(timer.get(damageTimer, damageInterval)){
                    PMDamage.allNearbyEnemies(team, curPos.x, curPos.y, calcRadius(), h -> {
                        h.damage(calcDamage());
                        if(h instanceof Unit u){
                            u.apply(status, statusDuration);
                        }
                    });
                }

                if(activeTime >= dur){
                    isShooting = false;
                    reload %= reloadTime;
                    shotCounter++;
                }
            }

            if(acceptCoolant){
                updateCooling();
            }
        }

        protected void updateShooting(){
            reload += delta() * baseReloadSpeed();

            if(reload >= reloadTime && !isShooting){
                isShooting = true;
                activeTime = 0f;
            }
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        protected void findTarget(){
            target = Units.bestTarget(team, x, y, range, e -> !e.dead(), b -> true, unitSort);
        }

        protected void checkConnections(){
            int index = 0;
            while(index < chargers.size){
                if(world.build(chargers.get(index)) == null){
                    chargers.removeIndex(index);
                }else{
                    index++;
                }
            }
        }

        protected float calcDamage(){
            boost = 0f;
            chargers.each(i -> {
                Building other = world.build(i);
                if(other.consValid()){
                    boost += ((ApotheosisChargeTower)(other.block())).damageBoost * other.efficiency();
                }
            });
            return damage + boost;
        }

        protected float calcRadius(){
            boost = 0f;
            chargers.each(i -> {
                Building other = world.build(i);
                if(other.consValid()){
                    boost += ((ApotheosisChargeTower)(other.block())).radiusBoost * other.efficiency();
                }
            });
            return splashDamageRadius + boost;
        }

        protected float calcSpeed(){
            boost = 0f;
            chargers.each(i -> {
                Building other = world.build(i);
                if(other.consValid()){
                    boost += ((ApotheosisChargeTower)(other.block())).speedBoost * other.efficiency();
                }
            });
            return speed + boost;
        }


        protected float calcDuration(){
            boost = 0f;
            chargers.each(i -> {
                Building other = world.build(i);
                if(other.consValid()){
                    boost += ((ApotheosisChargeTower)(other.block())).durationBoost * other.efficiency();
                }
            });
            return duration + boost;
        }
    }
}