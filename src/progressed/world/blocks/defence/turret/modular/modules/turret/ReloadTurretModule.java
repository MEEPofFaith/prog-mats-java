package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.math.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class ReloadTurretModule extends BaseTurretModule{
    public float reload = 30f;

    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** If not null, this consumer will be used for coolant. */
    public ConsumeLiquidBase coolant;

    public ReloadTurretModule(String name, ModuleSize size){
        super(name, size);
    }

    public ReloadTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        if(coolant != null){
            stats.add(Stat.booster, StatValues.boosters(reload, coolant.amount, coolantMultiplier, true, l -> l.coolant && consumesLiquid(l)));
        }
    }

    public void updateCooling(ModularTurretBuild parent, BaseTurretMount mount){
        LiquidModule pLiquid = parent.liquids; //A hacky solution to a hacky system
        parent.liquids = mount.liquids;

        if(mount.reloadCounter < reload && coolant != null && coolant.efficiency(parent) > 0 && parent.efficiency > 0){
            float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(parent).heatCapacity : 1f;
            coolant.update(parent);
            mount.reloadCounter += coolant.amount * edelta(parent) * capacity * coolantMultiplier;

            if(Mathf.chance(0.06 * coolant.amount)){
                coolEffect.at(mount.x + Mathf.range(size() * Vars.tilesize / 2f), mount.y + Mathf.range(size() * Vars.tilesize / 2f));
            }
        }

        parent.liquids = pLiquid;
    }

    public boolean shouldReload(ModularTurretBuild parent, TurretMount mount){
        return !charging(mount) && mount.queuedBullets == 0 && powerValid(parent);
    }

    public boolean charging(TurretMount mount){
        return false;
    }

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);

        if(mount instanceof BaseTurretMount m){
            write.f(m.reloadCounter);
        }
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);

        if(mount instanceof BaseTurretMount m){
            m.reloadCounter = read.f();
        }
    }
}
