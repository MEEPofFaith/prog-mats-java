package progressed.world.blocks.defence.turret.multi;

import arc.struct.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class ModularTurret extends Block{
    public int smallMountLimit, mediumMountLimit, largeMountLimit;

    public ModularTurret(String name){
        super(name);
    }

    public class ModularTurretBuild extends Building implements Ranged{
        public Seq<TurretMount> smallMounts = new Seq<>(), mediumMounts = new Seq<>(), largeMounts = new Seq<>(), allMounts = new Seq<>();

        /** @return if it was able to add the mount. */
        public boolean addModule(TurretModule module){
            TurretMount mount = module.mountType.get(module);
            if(mount == null) return false;
            switch(module.size){
                case small -> {
                    if(smallMounts.size + 1 > smallMountLimit) return false;
                    smallMounts.add(mount);
                }
                case medium -> {
                    if(mediumMounts.size + 1 > mediumMountLimit) return false;
                    mediumMounts.add(mount);
                }
                case large -> {
                    if(largeMounts.size + 1 > largeMountLimit) return false;
                    largeMounts.add(mount);
                }
            }
            allMounts.add(mount);

            return true;
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
    }
}