package progressed.world.blocks.defence.turret.multi;

import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.TurretModule.*;
import progressed.world.blocks.payloads.*;

public class ModularTurret extends PayloadBlock{
    public final int timerTarget = timers++;
    public int targetInterval = 20;

    public float[] smallMountPos, mediumMountPos, largeMountPos;

    public ModularTurret(String name){
        super(name);

        acceptsPayload = true;
        outputsPayload = false;
    }

    public class ModularTurretBuild extends PayloadBlockBuild<BuildPayload> implements Ranged{
        public Seq<TurretMount> smallMounts = new Seq<>(), mediumMounts = new Seq<>(), largeMounts = new Seq<>(), allMounts = new Seq<>();

        @Override
        public void updateTile(){
            //Have all mounts retarget at the same time for less laggyness.
            if(timer(timerTarget, targetInterval)){
                allMounts.each(m -> m.findTarget(this));
            }

            allMounts.each(m -> m.update(this));
        }

        public void retarget(float x, float y){
            allMounts.each(m -> m.targetPos.set(x, y));
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y); //region is the base

            drawTeamTop();

            //Draw in order of small/medium/large
            smallMounts.each(m -> m.draw(this));
            mediumMounts.each(m -> m.draw(this));
            largeMounts.each(m -> m.draw(this));
        }

        /** @return the module it adds. */
        public TurretMount addModule(TurretModule module){
            TurretMount mount = module.mountType.get(module,
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
                ((p.block() instanceof TurretModulePayload module && acceptModule(module.module)) ||
                allMounts.contains(m -> m.module.acceptPayload(p, m)));
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
}