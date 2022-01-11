package progressed.world.blocks.defence.turret.multi.modules;

import mindustry.entities.bullet.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class PowerTurretModule extends TurretModule{
    public BulletType shootType;

    public PowerTurretModule(String name){
        super(name);
    }

    @Override
    public BulletType useAmmo(ModularTurretBuild parent, TurretMount mount){
        //nothing used directly
        return shootType;
    }

    @Override
    public boolean hasAmmo(TurretMount mount){
        //you can always rotate, but never shoot if there's no power
        return isDeployed(mount);
    }

    @Override
    public BulletType peekAmmo(TurretMount mount){
        return shootType;
    }

    @Override
    public float speedScl(ModularTurretBuild parent, BaseMount mount){
        return parent.efficiency();
    }
}