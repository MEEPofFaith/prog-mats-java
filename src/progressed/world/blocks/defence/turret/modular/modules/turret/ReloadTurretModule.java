package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
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
    public void init(){
        if(acceptCoolant && !consumes.has(ConsumeType.liquid)){
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }

        super.init();
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        if(acceptCoolant){
            stats.add(Stat.booster, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }
    }

    public void updateCooling(ModularTurretBuild parent, BaseTurretMount mount){
        if(mount.reload < reloadTime){
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

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);

        if(mount instanceof BaseTurretMount m){
            write.f(m.reload);
        }
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);

        if(mount instanceof BaseTurretMount m){
            m.reload = read.f();
        }
    }
}
