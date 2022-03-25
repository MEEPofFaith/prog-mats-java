package progressed.world.blocks.defence.turret.modular.mounts;

import arc.struct.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;

public class TurretMount extends BaseTurretMount{
    public final TurretModule module;
    /** Ammo in the mount */
    public Seq<AmmoEntry> ammo = new Seq<>();
    public int totalAmmo;
    public float charge;
    public BulletType chargeShot;
    public Bullet bullet;
    public int shotCounter;
    public boolean wasShooting, isShooting, charging;

    public TurretMount(ModularTurretBuild parent, BaseModule module, int moduleNumber){
        super(parent, module, moduleNumber);
        this.module = (TurretModule)module;
    }

    @Override
    public void findTarget(ModularTurretBuild parent){
        if(!module.hasAmmo(this)) return;
        module.findTarget(parent, this);
    }
}
