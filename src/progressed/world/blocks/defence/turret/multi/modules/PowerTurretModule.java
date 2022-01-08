package progressed.world.blocks.defence.turret.multi.modules;

import mindustry.entities.bullet.*;
import progressed.world.blocks.defence.turret.multi.*;

public class PowerTurretModule extends TurretModule{
    public BulletType shootType;

    public PowerTurretModule(String name){
        super(name);
    }

    @Override
    public BulletType useAmmo(TurretMount mount){
        //nothing used directly
        return shootType;
    }

    @Override
    public boolean hasAmmo(TurretMount mount){
        //you can always rotate, but never shoot if there's no power
        return true;
    }

    @Override
    public BulletType peekAmmo(TurretMount mount){
        return shootType;
    }

    @Override
    public float reloadSpeedScl(TurretMount mount){
        return mount.parent.efficiency();
    }
}