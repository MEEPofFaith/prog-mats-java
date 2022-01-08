package progressed.world.blocks.defence.turret.multi;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /** Turret module associated with this mount. */
    public final TurretModule module;
    /** Position of the mount */
    public final float x, y;
    /** Ammo in the mount */
    public Seq<AmmoEntry> ammo = new Seq<>();
    /** Liquid module of the mount. Primarily used for cooling. */
    public LiquidModule liquids;
    /** Deploy progress */
    public float progress;
    /** Reload in frames; 0 means ready to fire */
    public float reload;
    /** Rotation relative to the unit this mount is on */
    public float rotation;
    /** Weapon recoil */
    public float recoil;
    public boolean wasShooting, charging;
    /** Target */
    public @Nullable Posc target;
    /** Aim position */
    public Vec2 targetPos = new Vec2();
    /** Current heat, 0 to 1*/
    public float heat;

    public TurretMount(TurretModule module, float x, float y){
        this.module = module;
        this.x = x;
        this.y = y;
        if(module.hasLiquid) liquids = new LiquidModule();
    }

    public void update(ModularTurretBuild parent){
        module.update(parent, this);
    }

    public void draw(ModularTurretBuild parent){
        module.draw(parent, this);
    }

    /** @return the ammo type that will be returned if useAmmo is called. */
    public BulletType peekAmmo(){
        return ammo.peek().type();
    }

    /** @return whether the turret has ammo. */
    public boolean hasAmmo(ModularTurretBuild parent){
        return ammo.size > 0;
    }

    public boolean canHeal(ModularTurretBuild parent){
        return module.targetHealing && hasAmmo(parent) && peekAmmo().collidesTeam && peekAmmo().healPercent > 0;
    }

    public void findTarget(ModularTurretBuild parent){
        module.findTarget(parent, this);
    }
}