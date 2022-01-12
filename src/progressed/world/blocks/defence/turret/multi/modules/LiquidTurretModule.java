package progressed.world.blocks.defence.turret.multi.modules;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class LiquidTurretModule extends TurretModule{
    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();

    public boolean extinguish = true;

    public LiquidTurretModule(String name){
        super(name);

        acceptCoolant = false;
        hasLiquids = true;
    }

    /** Initializes accepted ammo map. Format: [liquid1, bullet1, liquid2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = ObjectMap.of(objects);
    }

    @Override
    public void findTarget(ModularTurretBuild parent, TurretMount mount){
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
    protected void effects(TurretMount mount){
        float x = mount.x, y = mount.y;

        Effect fshootEffect = shootEffect == Fx.none ? peekAmmo(mount).shootEffect : shootEffect;
        Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo(mount).smokeEffect : smokeEffect;

        fshootEffect.at(x + tr.x, y + tr.y, mount.rotation, mount.liquids.current().color);
        fsmokeEffect.at(x + tr.x, y + tr.y, mount.rotation, mount.liquids.current().color);
        shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

        if(shootShake > 0){
            Effect.shake(shootShake, shootShake, x, y);
        }

        mount.recoil = recoilAmount;
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
        return ammoTypes.get(mount.liquids.current()) != null && mount.liquids.total() >= 1f / ammoTypes.get(mount.liquids.current()).ammoMultiplier;
    }

    @Override
    public boolean acceptLiquid(Liquid liquid, BaseMount mount){
        return isDeployed(mount) && mount.liquids.get(liquid) < liquidCapacity && ammoTypes.get(liquid) != null
            && (mount.liquids.current() == liquid || (ammoTypes.containsKey(liquid) &&
            (!ammoTypes.containsKey(mount.liquids.current()) || mount.liquids.get(mount.liquids.current()) <= 1f / ammoTypes.get(mount.liquids.current()).ammoMultiplier + 0.001f)));
    }
}