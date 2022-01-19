package progressed.world.blocks.defence.turret.multi.modules.turret;

import arc.struct.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;
import progressed.world.meta.*;

public class PowerTurretModule extends TurretModule{
    public BulletType shootType;

    public PowerTurretModule(String name, ModuleSize size){
        super(name, size);
    }

    public PowerTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(Vars.content.block(mountID), shootType)));
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
        return super.speedScl(parent, mount) * efficiency(parent);
    }
}