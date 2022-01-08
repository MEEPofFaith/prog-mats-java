package progressed.world.blocks.defence.turret.multi;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /** The base this mount is on. */
    public final ModularTurretBuild parent;
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
    public int shotCounter;
    public boolean wasShooting, charging;
    /** Target */
    public @Nullable Posc target;
    /** Aim position */
    public Vec2 targetPos = new Vec2();
    /** Current heat, 0 to 1*/
    public float heat;

    public TurretMount(ModularTurretBuild parent, TurretModule module, float x, float y){
        this.parent = parent;
        this.module = module;
        this.x = x;
        this.y = y;
        if(module.hasLiquids) liquids = new LiquidModule();
    }

    public void update(){
        module.update(this);
    }

    public void draw(){
        module.draw(this);
    }

    public boolean canHeal(){
        return module.targetHealing && module.hasAmmo(this) && module.peekAmmo(this).collidesTeam && module.peekAmmo(this).healPercent > 0;
    }

    public void findTarget(){
        module.findTarget(this);
    }
}