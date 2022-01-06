package progressed.world.blocks.defence.turret.multi;

import arc.util.*;
import mindustry.gen.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /** Turret module associated with this mount. */
    public final TurretModule module;
    /** Position of the mount */
    public final float x, y;
    /** Liquid module of the mount. Primarily used for cooling. */
    public LiquidModule liquids;
    /** Deploy progress */
    public float progress;
    /** Reload in frames; 0 means ready to fire */
    public float reload;
    /** Rotation relative to the unit this mount is on */
    public float rotation;
    /** Weapon recoil */
    public float recoil;
    /** Target */
    public @Nullable Posc target;
    /** Current heat, 0 to 1*/
    public float heat;

    public TurretMount(TurretModule module, float x, float y){
        this.module = module;
        this.x = x;
        this.y = y;
        if(module.hasLiquid) liquids = new LiquidModule();
    }
}