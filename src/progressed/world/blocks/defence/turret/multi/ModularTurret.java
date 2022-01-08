package progressed.world.blocks.defence.turret.multi;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.TurretModule.*;
import progressed.world.blocks.payloads.*;

public class ModularTurret extends PayloadBlock{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    public int targetInterval = 20;

    public float[] smallMountPos, mediumMountPos, largeMountPos;

    public ModularTurret(String name){
        super(name);

        acceptsPayload = true;
        outputsPayload = false;
        rotate = false;
    }

    @Override
    public void init(){
        consumes.add(new ModularTurretConsumePower());

        super.init();
    }

    public class ModularTurretBuild extends PayloadBlockBuild<BuildPayload> implements ControlBlock, Ranged{
        public Seq<TurretMount> smallMounts = new Seq<>(), mediumMounts = new Seq<>(), largeMounts = new Seq<>(), allMounts = new Seq<>();
        public float logicControlTime;
        public boolean logicShooting = false;
        public BlockUnitc unit = (BlockUnitc)UnitTypes.block.create(team);

        @Override
        public Unit unit(){
            //make sure stats are correct
            unit.tile(this);
            unit.team(team);
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                retarget(World.unconv((float)p1), World.unconv((float)p2));
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

                if(p1 instanceof Posc pos){
                    retarget(pos);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void updateTile(){
            if(moveInPayload()){
                if(payload.block() instanceof TurretModulePayload module && acceptModule(module.module)){
                    addModule(module.module);
                }
                payload = null;
            }

            unit.tile(this);
            unit.team(team);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            //Have all mounts retarget at the same time for less laggyness.
            if(timer(timerTarget, targetInterval)){
                allMounts.each(TurretMount::findTarget);
            }

            allMounts.each(TurretMount::update);
        }

        public void retarget(float x, float y){
            allMounts.each(m -> m.targetPos.set(x, y));
        }

        public void retarget(Posc p){
            allMounts.each(m -> m.module.targetPosition(m, p));
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y); //region is the base

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                }
            }

            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            //Draw in order of small/medium/large
            smallMounts.each(TurretMount::draw);
            mediumMounts.each(TurretMount::draw);
            largeMounts.each(TurretMount::draw);
        }

        /** @return the module it adds. */
        public TurretMount addModule(TurretModule module){
            TurretMount mount = module.mountType.get(
                this,
                module,
                x + newModuleX(module.size),
                y + newModuleY(module.size)
            );
            switch(module.size){
                case small -> smallMounts.add(mount);
                case medium -> mediumMounts.add(mount);
                case large -> largeMounts.add(mount);
            }
            allMounts.add(mount);

            return mount;
        }

        public float newModuleX(ModuleSize size){
            return switch(size){
                case small -> smallMountPos[smallMounts.size * 2];
                case medium -> mediumMountPos[mediumMounts.size * 2];
                case large -> largeMountPos[largeMounts.size * 2];
            };
        }

        public float newModuleY(ModuleSize size){
            return switch(size){
                case small -> smallMountPos[smallMounts.size * 2 + 1];
                case medium -> mediumMountPos[mediumMounts.size * 2 + 1];
                case large -> largeMountPos[largeMounts.size * 2 + 1];
            };
        }

        /** @return if a module can be added. */
        public boolean acceptModule(TurretModule module){
            return switch(module.size){
                case small -> smallMountPos != null && smallMounts.size + 1 <= smallMountPos.length / 2;
                case medium -> mediumMountPos != null && mediumMounts.size + 1 <= mediumMountPos.length / 2;
                case large -> largeMountPos != null && largeMounts.size + 1 <= largeMountPos.length / 2;
            };
        }

        //If you couldn't tell already I really like switch cases.

        @Override
        public boolean acceptItem(Building source, Item item){
            return allMounts.contains(m -> m.module.acceptItem(item, m));
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return allMounts.contains(m -> m.module.acceptLiquid(liquid, m));
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return super.acceptPayload(source, payload) &&
                payload instanceof BuildPayload p &&
                p.block() instanceof TurretModulePayload module &&
                acceptModule(module.module);
        }

        @Override
        public void handleItem(Building source, Item item){
            TurretMount mount = allMounts.find(m -> m.module.acceptItem(item, m));
            mount.module.handleItem(item, mount);
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            float a = amount;
            while(a > 0){ //Distribute overflow from one mount to the next
                TurretMount mount = allMounts.find(m -> m.module.acceptLiquid(liquid, m));
                if(mount == null) break;
                a -= mount.module.handleLiquid(liquid, a, mount);
            }
        }

        public float powerUse(){
            float use = 0f;
            for(TurretMount mount : allMounts){
                use += mount.module.powerUse(mount);
            }
            return use;
        }

        @Override
        public float range(){
            if(allMounts.isEmpty()) return 0;

            float[] range = {Float.MIN_VALUE};
            allMounts.each(m -> {
                if(m.module.range > range[0]) range[0] = m.module.range;
            });
            return range[0];
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(allMounts.size);
            allMounts.each(m -> m.module.writeAll(write, m));
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            int len = read.i();
            for(int i = 0; i < len; i++){
                short id = read.s();
                Block module = Vars.content.block(id);
                //Note: Installing or uninstalling other mods can change id and break saves.
                if(module instanceof TurretModulePayload p){
                    TurretMount mount = addModule(p.module);
                    mount.module.readAll(read, mount);
                }
            }
        }
    }

    public class ModularTurretConsumePower extends ConsumePower{
        @Override
        public float requestedPower(Building entity){
            if(entity instanceof ModularTurretBuild m) return m.powerUse();
            return 0f;
        }
    }
}