package progressed.content.blocks;

import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.*;

import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static BaseModule

    augment;

    public static void load(){
        augment = new BoostModule("augment"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            limit = 1;
            hasPower = true;

            consumePower(2f);
        }};
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
