package progressed.world.blocks.defence.turret.multi.mounts;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.turret.*;

public class TurretMount extends BaseMount{
    public final BaseTurretModule module;
    /** Ammo in the mount */
    public Seq<AmmoEntry> ammo = new Seq<>();
    public int totalAmmo;
    /** Reload in frames; 0 means ready to fire */
    public float reload;
    /** Weapon recoil */
    public float recoil;
    public int shotCounter;
    public boolean wasShooting, isShooting, charging;
    /** Target */
    public @Nullable Posc target;
    /** Aim position */
    public Vec2 targetPos = new Vec2();

    public TurretMount(ModularTurretBuild parent, BaseModule module, short moduleNumber, float offsetX, float offsetY){
        super(parent, module, moduleNumber, offsetX, offsetY);
        this.module = (BaseTurretModule)module;
    }

    public void findTarget(ModularTurretBuild parent){
        if(!module.hasAmmo(this)) return;
        module.findTarget(parent, this);
    }
}