package progressed.world.blocks.defence.turret.payload.modular.modules.turret;

import arc.math.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ReloadTurretModule extends TargetingModule{
    public float reload = 10f;

    public ReloadTurretModule(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        if(coolant != null){
            stats.add(Stat.booster, StatValues.boosters(reload, coolant.amount, coolantMultiplier, true, l -> l.coolant && consumesLiquid(l)));
        }
    }

    public class ReloadTurretModuleBuild extends TargetingModuleBuild{
        public float reloadCounter;

        protected void updateCooling(){
            if(reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : 1f;
                coolant.update(this);
                reloadCounter += coolant.amount * edelta() * capacity * coolantMultiplier;

                if(Mathf.chance(0.06 * coolant.amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        protected float baseReloadSpeed(){
            return efficiency;
        }
    }
}
