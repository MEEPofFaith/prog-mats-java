package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.meta.*;

public class LiquidTurretModule extends TurretModule{
    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();

    public boolean extinguish = true;

    public LiquidTurretModule(String name, ModuleSize size){
        super(name, size);

        hasLiquids = true;
        loopSound = Sounds.spray;
        shootSound = Sounds.none;
        smokeEffect = Fx.none;
        shootEffect = Fx.none;
    }

    public LiquidTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
    }

    /** Initializes accepted ammo map. Format: [liquid1, bullet1, liquid2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = ObjectMap.of(objects);
    }

    @Override
    public boolean shouldLoopSound(ModularTurretBuild parent, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return false;
        return m.wasShooting && parent.enabled;
    }

    @Override
    public void findTarget(ModularTurretBuild parent, BaseTurretMount mount){
        if(extinguish && mount.liquids.current().canExtinguish()){
            int tx = World.toTile(mount.x), ty = World.toTile(mount.y);
            Fire result = null;
            float mindst = 0f;
            int tr = (int)(range / Vars.tilesize);
            for(int x = -tr; x <= tr; x++){
                for(int y = -tr; y <= tr; y++){
                    Tile other = Vars.world.tile(x + tx, y + ty);
                    var fire = Fires.get(x + tx, y + ty);
                    float dst = fire == null ? 0 : Mathf.dst2(mount.x, mount.y, fire.x ,fire.y);
                    //do not extinguish fires on other team blocks
                    if(other != null && fire != null && Fires.has(other.x, other.y) && dst <= range * range && (result == null || dst < mindst) && (other.build == null || other.team() == parent.team)){
                        result = fire;
                        mindst = dst;
                    }
                }
            }

            if(result != null){
                mount.target = result;
                //don't run standard targeting
                return;
            }
        }

        super.findTarget(parent, mount);
    }

    @Override
    public BulletType useAmmo(ModularTurretBuild parent, TurretMount mount){
        if(parent.cheating()) return ammoTypes.get(mount.liquids.current());
        BulletType type = ammoTypes.get(mount.liquids.current());
        mount.liquids.remove(mount.liquids.current(), 1f / type.ammoMultiplier);
        return type;
    }

    @Override
    public BulletType peekAmmo(TurretMount mount){
        return ammoTypes.get(mount.liquids.current());
    }

    @Override
    public boolean hasAmmo(TurretMount mount){
        return ammoTypes.get(mount.liquids.current()) != null && mount.liquids.currentAmount() >= 1f / ammoTypes.get(mount.liquids.current()).ammoMultiplier;
    }

    @Override
    public boolean acceptLiquid(Liquid liquid, BaseMount mount){
        return isDeployed(mount) && mount.liquids.get(liquid) < liquidCapacity && ammoTypes.get(liquid) != null
            && (mount.liquids.current() == liquid || (ammoTypes.containsKey(liquid) &&
            (!ammoTypes.containsKey(mount.liquids.current()) || mount.liquids.get(mount.liquids.current()) <= 1f / ammoTypes.get(mount.liquids.current()).ammoMultiplier + 0.001f)));
    }
}
