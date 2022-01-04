package progressed.world.blocks.defence.turret.multi.mounts;

import arc.util.*;
import mindustry.gen.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /* Module associated wit this mount. */
    public final TurretModule module;
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
    }
}