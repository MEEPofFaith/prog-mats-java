package progressed.world.blocks.defence.turret.modular.modules.turret;

import mindustry.*;
import mindustry.gen.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class BaseTurretModule extends RangedModule{
    public BaseTurretModule(String name, ModuleSize size){
        super(name, size);
        mountType = TurretMount::new;
    }

    public BaseTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.remove(Stat.range);
        stats.add(Stat.shootRange, range / Vars.tilesize, StatUnit.blocks);
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        if(mount instanceof TurretMount m) updateTurret(parent, m);
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount mount){
        if(mount instanceof TurretMount m) drawTurret(parent, m);
    }

    public void updateTurret(ModularTurretBuild parent, TurretMount mount){}

    public void drawTurret(ModularTurretBuild parent, TurretMount mount){}

    public boolean hasAmmo(TurretMount mount){
        return false;
    }

    public void findTarget(ModularTurretBuild parent, TurretMount mount){}

    public void targetPosition(TurretMount mount, Posc pos){}
}