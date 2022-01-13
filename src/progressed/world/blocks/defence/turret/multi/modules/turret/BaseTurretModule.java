package progressed.world.blocks.defence.turret.multi.modules.turret;

import mindustry.gen.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class BaseTurretModule extends BaseModule{
    public float range = 80f;

    public BaseTurretModule(String name, ModuleSize size){
        super(name, size);
        mountType = TurretMount::new;
    }

    public BaseTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        if(mount instanceof TurretMount m) updateTurret(parent, m);
    }

    @Override
    public void draw(BaseMount mount){
        if(mount instanceof TurretMount m) drawTurret(m);
    }

    public void updateTurret(ModularTurretBuild parent, TurretMount mount){}

    public void drawTurret(TurretMount mount){}

    public boolean hasAmmo(TurretMount mount){
        return false;
    }

    public void findTarget(ModularTurretBuild parent, TurretMount mount){}

    public void targetPosition(TurretMount mount, Posc pos){}
}