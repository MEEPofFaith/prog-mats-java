package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class BaseTurretModule extends RangedModule{
    public boolean logicControl = true, playerControl = true;
    public boolean fastRetarget = false;
    public float rotateSpeed = 5f;
    public float shootCone = 8f;

    public Color heatColor = Pal.turretHeat;
    public float recoil;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;

    public boolean buildTop;
    public float topLayerOffset;

    public TextureRegion heatRegion, liquidRegion, topRegion;

    public BaseTurretModule(String name, ModuleSize size){
        super(name, size);
        mountType = BaseTurretMount::new;
    }

    public BaseTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void load(){
        super.load();
        heatRegion = Core.atlas.find(name + "-heat");
        liquidRegion = Core.atlas.find(name + "-liquid");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.remove(Stat.range);
        stats.add(Stat.shootRange, range / Vars.tilesize, StatUnit.blocks);
    }

    public boolean hasAmmo(TurretMount mount){
        return false;
    }

    public void findTarget(ModularTurretBuild parent, BaseTurretMount mount){}

    public void targetPosition(BaseTurretMount mount, Posc pos){}

    @Override
    public void drawDeploy(ModularTurretBuild parent, BaseMount mount){
        Draw.draw(Draw.z(), () -> {
            PMDrawf.materialize(mount.x - elevation, mount.y - elevation, region, parent.team.color, mount.rotation - 90f, 0.1f, mount.progress / deployTime, true);
            PMDrawf.materialize(mount.x, mount.y, region, parent.team.color, mount.rotation - 90f, 0.1f, mount.progress / deployTime);
            if(buildTop) PMDrawf.materialize(mount.x, mount.y, topRegion, parent.team.color, mount.rotation - 90f, 0.1f, mount.progress / deployTime);
        });
    }
}
