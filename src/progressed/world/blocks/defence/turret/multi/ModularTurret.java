package progressed.world.blocks.defence.turret.multi;

import arc.struct.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.payloads.*;

public class ModularTurret extends Block{
    public int smallMountLimit, mediumMountLimit, largeMountLimit;

    public ModularTurret(String name){
        super(name);
    }

    public class ModularTurretBuild extends Building implements Ranged{
        public Seq<TurretMount> smallMounts = new Seq<>(), mediumMounts = new Seq<>(), largeMounts = new Seq<>(), allMounts = new Seq<>();

        /** @return if it was able to add the mount. */
        public TurretMount addModule(TurretModule module){
            TurretMount mount = module.mountType.get(module);
            if(mount == null) return null;
            switch(module.size){
                case small -> {
                    if(smallMounts.size + 1 > smallMountLimit) return null;
                    smallMounts.add(mount);
                }
                case medium -> {
                    if(mediumMounts.size + 1 > mediumMountLimit) return null;
                    mediumMounts.add(mount);
                }
                case large -> {
                    if(largeMounts.size + 1 > largeMountLimit) return null;
                    largeMounts.add(mount);
                }
            }
            allMounts.add(mount);

            return mount;
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