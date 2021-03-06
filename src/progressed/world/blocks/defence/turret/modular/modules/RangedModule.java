package progressed.world.blocks.defence.turret.modular.modules;

import mindustry.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class RangedModule extends BaseModule{
    public boolean clipRange = true;
    public float range = 80f;

    public RangedModule(String name, ModuleSize size){
        super(name, size);
    }

    public RangedModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void init(){
        if(clipRange) clipSize = Math.max(clipSize, range * 2f);

        super.init();
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.range, range / Vars.tilesize, StatUnit.blocks);
    }

    @Override
    public void drawHighlight(ModularTurretBuild parent, BaseMount mount){
        Drawf.dashCircle(mount.x, mount.y, range(mount), parent.team.color);
    }

    public float range(BaseMount mount){
        return range;
    }
}
