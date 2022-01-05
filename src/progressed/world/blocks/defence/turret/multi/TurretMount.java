package progressed.world.blocks.defence.turret.multi;

import arc.util.*;
import mindustry.gen.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /** Turret module associated with this mount. */
    public final TurretModule module;
    /** Liquid module of the mount. Primarily used for cooling. */
    public LiquidModule liquids;
    /** reload in frames; 0 means ready to fire */
    public float reload;
    /** rotation relative to the unit this mount is on */
    public float rotation;
    /** weapon recoil */
    public float recoil;
    /** Target */
    public @Nullable Posc target;
    /** current heat, 0 to 1*/
    public float heat;

    public TurretMount(TurretModule module){
        this.module = module;
        if(module.hasLiquid) liquids = new LiquidModule();
    }
}