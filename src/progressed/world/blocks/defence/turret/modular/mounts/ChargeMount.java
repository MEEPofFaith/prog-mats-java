package progressed.world.blocks.defence.turret.modular.mounts;

import mindustry.gen.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;

public class ChargeMount extends BaseMount{
    public float charge, smoothEfficiency;
    public Teamc target;

    public ChargeMount(ModularTurretBuild parent, BaseModule module, int moduleNumber){
        super(parent, module, moduleNumber);
    }
}