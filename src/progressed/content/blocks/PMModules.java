package progressed.content.blocks;

import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.*;

import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static BaseModule

    test;

    public static void load(){
        test = new BoostModule("aaaaaa"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
        }};
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
