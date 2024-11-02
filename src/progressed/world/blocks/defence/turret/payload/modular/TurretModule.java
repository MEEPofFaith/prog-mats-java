package progressed.world.blocks.defence.turret.payload.modular;

import arc.*;

public interface TurretModule{
    default String initDescription(String desc, boolean single){
        if(desc == null) return null;

        desc += "\n" + Core.bundle.get("pm-module-use");
        if(single) desc += "\n" + Core.bundle.get("pm-module-single");

        return desc;
    }

    default String initDescription(String desc){
        return initDescription(desc, false);
    }
}
