package progressed.world.blocks.defence.turret.multi.mounts;

import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class ChargeMount extends BaseMount{
    public float charge, heat;

    public ChargeMount(ModularTurretBuild parent, BaseModule module, float offsetX, float offsetY){
        super(parent, module, offsetX, offsetY);
    }
}