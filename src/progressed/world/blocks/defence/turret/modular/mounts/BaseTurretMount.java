package progressed.world.blocks.defence.turret.modular.mounts;

import arc.math.geom.*;
import mindustry.gen.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;

public class BaseTurretMount extends BaseMount{
    public final BaseTurretModule module;
    /** Reload in ticks */
    public float reloadCounter;
    /** Weapon recoil */
    public float curRecoil;
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
