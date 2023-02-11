package progressed.world.blocks.defence.turret;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.unit.*;
import progressed.entities.units.*;
import progressed.util.*;
import progressed.world.meta.*;

public class SignalFlareTurret extends ItemTurret{
    public int flareLimit = 1;

    public SignalFlareTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("pm-reload", (SignalFlareTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reloadCounter / reload)
        ));

        addBar("pm-flare-limit", (SignalFlareTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-flare-limit", entity.flares.size, flareLimit),
            () -> entity.team.color,
            entity::countf
        ));
    }

    public class SignalFlareTurretBuild extends ItemTurretBuild{
        protected IntSeq readUnits = new IntSeq();

        public float tX, tY;
        public int count, amount;
        public Seq<SignalFlareUnit> flares = new Seq<>();
        public boolean targetFound;
        public Bullet bullet;

        @Override
        public boolean canControl(){
            return false;
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4) {
            // cannot control
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            // cannot control
        }

        @Override
        public void updateTile(){
            if(!readUnits.isEmpty()){
                flares.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if(unit instanceof SignalFlareUnit f){
                        flares.add(f);
                    }
                });
                readUnits.clear();
            }
            //flares.remove(f -> !f.isAdded()); //Is this needed?

            wasShooting = false;

            curRecoil = Math.max(curRecoil - Time.delta / recoilTime , 0);
            heat = Math.max(heat - Time.delta / cooldownTime, 0);

            unit.health(health);
            unit.rotation(rotation);
            unit.team(team);
            unit.set(x, y);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(bullet != null && !bullet.isAdded()){
                bullet = null;
            }

            if(hasAmmo()){
                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(count > 0){
                    float targetRot = angleTo(targetPos);

                    if(shouldTurn()){
                        turnToTarget(targetRot);
                    }

                    if(Angles.angleDist(rotation, targetRot) < shootCone){
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }

            if(coolant != null){
                updateCooling();
            }
        }

        @Override
        protected void updateShooting(){
            if(flares.size < flareLimit && bullet == null){
                if(reloadCounter >= reload && !charging()){
                    BulletType type = peekAmmo();

                    shoot(type);

                    reloadCounter = 0f;

                    targetFound = false;
                }else{
                    reloadCounter += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
                }
            }
        }

        @Override
        protected void updateCooling(){
            if(flares.size < flareLimit && bullet == null){
                super.updateCooling();
            }
        }

        @Override
        protected void findTarget(){
            tX = 0f;
            tY = 0f;
            count = 0;

            Groups.bullet.intersect(x - range, y - range, range * 2f, range * 2f, b -> {
                if(b.team != team && within(b, range) && !checkType(b.type) && b.type().speed > 0.01f){
                    tX += b.x;
                    tY += b.y;
                    count++;
                }
            });

            if(count > 0 && !targetFound){
                targetPos.set(tX / count, tY / count);
                targetFound = true;
            }else{
                targetFound = false;
            }
        }

        protected boolean checkType(BulletType b){
            return (b instanceof SignalFlareBulletType) || (b == Bullets.fireball);
        }

        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                this.bullet = bullet;
            }
        }

        @Override
        public BlockStatus status(){
            return (flares.size >= flareLimit || bullet != null) ? BlockStatus.noOutput : super.status();
        }

        public float countf(){
            return (flares.size / (float)flareLimit);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            BulletType type = ammoTypes.get(item);

            int a = getAmount(item);

            if(type == null || a >= ammoPerShot) return 0;

            return Math.min((int)((ammoPerShot - a) / ammoTypes.get(item).ammoMultiplier), amount);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return (totalAmmo == 0 || getAmount(item) < ammoPerShot) && super.acceptItem(source, item);
        }

        public int getAmount(Item item){
            BulletType type = ammoTypes.get(item);

            amount = 0;
            ammo.each(a -> {
                if(a.type() == type){
                    amount = a.amount;
                }
            });
            return amount;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.b(flares.size);
            for(SignalFlareUnit f : flares){
                write.i(f.id);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision == 4) read.i();
            if(revision >= 5){
                int count = read.b();
                readUnits.clear();
                for(int i = 0; i < count; i++){
                    readUnits.add(read.i());
                }
            }
        }

        @Override
        public byte version(){
            return 5;
        }
    }
}
