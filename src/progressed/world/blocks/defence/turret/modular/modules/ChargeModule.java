package progressed.world.blocks.defence.turret.modular.modules;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class ChargeModule extends BaseModule{
    public float reload = 60f;
    public Color topColor = PMPal.overdrive;
    public Cons2<ModularTurretBuild, ChargeMount> activate = (p, m) -> {};

    public TextureRegion topRegion;

    public ChargeModule(String name, ModuleSize size){
        super(name, size);
        mountType = ChargeMount::new;
    }

    public ChargeModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        if(isDeployed(mount) && mount instanceof ChargeMount m){
            m.heat = Mathf.lerpDelta(m.heat, Mathf.num(isActive(parent, mount)), 0.08f);
            activate.get(parent, m);
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount mount){
        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.blockBuildCenter(mount.x, mount.y, region, mount.rotation - 90, mount.progress / deployTime));
            return;
        }

        Drawf.shadow(region, mount.x - elevation, mount.y - elevation);
        applyColor(parent, mount);
        Draw.rect(region, mount.x, mount.y);

        ChargeMount m = (ChargeMount)mount;

        Draw.color(topColor);
        Draw.alpha(m.heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
        Draw.rect(topRegion, m.x, m.y);
        Draw.color();
        Draw.mixcol();
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        return super.isActive(parent, mount) && parent.consValid();
    }
}