package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.type.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class ReloadTurretModule extends BaseTurretModule{
    public float reloadTime = 30f;

    public boolean acceptCoolant = true;
    public float coolantUsage = 0.2f;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;

    public ReloadTurretModule(String name, ModuleSize size){
        super(name, size);
    }

    public ReloadTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        if(acceptCoolant){
            stats.add(Stat.booster, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }
    }

    public void updateCooling(ModularTurretBuild parent, TurretMount mount){
        if(mount.reload < reloadTime && mount.charge <= 0){
            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
            Liquid liquid = mount.liquids.current();

            float used = Math.min(mount.liquids.get(liquid), maxUsed * Time.delta) * efficiency(parent);
            mount.reload += used * liquid.heatCapacity * coolantMultiplier;
            mount.liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                coolEffect.at(mount.x + Mathf.range(size() * Vars.tilesize / 2f), mount.y + Mathf.range(size() * Vars.tilesize / 2f));
            }
        }
    }

    public boolean shouldReload(ModularTurretBuild parent, TurretMount mount){
        return !mount.charging && !mount.isShooting && powerValid(parent);
    }
}
