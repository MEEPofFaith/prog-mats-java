package progressed.world.blocks.defence.turret.modular.mounts;

import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;

public class BaseTurretMount extends BaseMount{
    public final BaseTurretModule module;
    /** Reload in frames; 0 means ready to fire */
    public float reload;
    /** Weapon recoil */
    public float recoil;
    /** Target */
    public Posc target;
    /** Aim position */
    public Vec2 targetPos = new Vec2();

    public BaseTurretMount(ModularTurretBuild parent, BaseModule module, int mountNumber){
        super(parent, module, mountNumber);
        this.module = (BaseTurretModule)module;
    }

    public void findTarget(ModularTurretBuild parent){
        module.findTarget(parent, this);
    }
}
