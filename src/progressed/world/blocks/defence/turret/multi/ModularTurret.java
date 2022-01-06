package progressed.world.blocks.defence.turret.multi;

import arc.*;
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
import progressed.world.blocks.payloads.*;

public class ModularTurret extends PayloadBlock{
    public float[] smallMountPos, mediumMountPos, largeMountPos;

    public ModularTurret(String name){
        super(name);

        acceptsPayload = true;
        outputsPayload = false;
    }

    public class ModularTurretBuild extends PayloadBlockBuild<BuildPayload> implements Ranged{
        public Seq<TurretMount> smallMounts = new Seq<>(), mediumMounts = new Seq<>(), largeMounts = new Seq<>(), allMounts = new Seq<>();

        @Override
        public void updateTile(){ //There's probably a better way to do this.
            for(int i = 0; i < smallMounts.size; i++){
                TurretMount m = smallMounts.get(i);
                m.module.update(team, x + smallMountPos[i * 2], y + smallMountPos[i * 2 + 1], m);
            }

            for(int i = 0; i < mediumMounts.size; i++){
                TurretMount m = mediumMounts.get(i);
                m.module.update(team, x + mediumMountPos[i * 2], y + mediumMountPos[i * 2 + 1], m);
            }

            for(int i = 0; i < largeMounts.size; i++){
                TurretMount m = largeMounts.get(i);
                m.module.update(team, x + largeMountPos[i * 2], y + largeMountPos[i * 2 + 1], m);
            }
        }

        @Override
        public void draw(){ //There's probably a better way to do this.
            Draw.rect(region, x, y); //region is the base

            for(int i = 0; i < smallMounts.size; i++){
                TurretMount m = smallMounts.get(i);
                m.module.draw(team, x + smallMountPos[i * 2], y + smallMountPos[i * 2 + 1], m);
            }

            for(int i = 0; i < mediumMounts.size; i++){
                TurretMount m = mediumMounts.get(i);
                m.module.draw(team, x + mediumMountPos[i * 2], y + mediumMountPos[i * 2 + 1], m);
            }

            for(int i = 0; i < largeMounts.size; i++){
                TurretMount m = largeMounts.get(i);
                m.module.draw(team, x + largeMountPos[i * 2], y + largeMountPos[i * 2 + 1], m);
            }
        }

        /** @return the module it adds. */
        public TurretMount addModule(TurretModule module){
            TurretMount mount = module.mountType.get(module);
            switch(module.size){
                case small -> {
                    smallMounts.add(mount);
                }
                case medium -> {
                    mediumMounts.add(mount);
                }
                case large -> {
                    largeMounts.add(mount);
                }
            }
            allMounts.add(mount);

            return mount;
        }

        /** @return if a module can be added. */
        public boolean acceptModule(TurretModule module){
            return switch(module.size){
                case small -> smallMountPos != null && smallMounts.size + 1 <= smallMountPos.length / 2;
                case medium -> mediumMountPos != null && mediumMounts.size + 1 <= mediumMountPos.length / 2;
                case large -> largeMountPos != null && largeMounts.size + 1 <= largeMountPos.length / 2;
            };
        }

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