package progressed.world.blocks.defence.turret.multi;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretMount{
    /** Turret module associated with this mount. */
    public final TurretModule module;
    /** Offset of the mount compared to the base. */
    public final float offsetX, offsetY;
    /** Position of the mount */
    public float x, y;
    /** Ammo in the mount */
    public Seq<AmmoEntry> ammo = new Seq<>();
    public int totalAmmo;
    /** Liquid module of the mount. Primarily used for cooling. */
    public LiquidModule liquids;
    /** Deploy progress */
    public float progress;
    /** Reload in frames; 0 means ready to fire */
    public float reload;
    /** Rotation relative to the unit this mount is on */
    public float rotation = 90;
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

    public TurretMount(TurretModule module, float offsetX, float offsetY){
        this.module = module;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        if(module.hasLiquids) liquids = new LiquidModule();
    }

    public void onProximityAdded(ModularTurretBuild parent){
        module.onProximityAdded(parent, this);
    }

    public void update(ModularTurretBuild parent){
        module.update(parent, this);
    }

    public void updatePos(ModularTurretBuild parent){
        x = parent.x + offsetX;
        y = parent.y + offsetY;
    }

    public void draw(){
        module.draw(this);
    }

    public boolean canHeal(){
        return module.targetHealing && module.hasAmmo(this) && module.peekAmmo(this).collidesTeam && module.peekAmmo(this).healPercent > 0;
    }

    public void findTarget(ModularTurretBuild parent){
        if(!module.hasAmmo(this)) return;
        module.findTarget(parent, this);
    }
}