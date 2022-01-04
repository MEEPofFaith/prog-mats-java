package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class TurretModule implements Cloneable{
    public String name;

    public ModuleSize size = ModuleSize.small;

    public TextureRegion region;
    public TextureRegion heatRegion;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public Func<TurretModule, TurretMount> mountType = TurretMount::new;

    public TurretModule(String name){
        this.name = name;
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public TurretModule copy(){
        try{
            return (TurretModule)clone();
        }catch(CloneNotSupportedException suck){
            throw new RuntimeException("very good language design", suck);
        }
    }

    public void load(){
        region = Core.atlas.find(name);
        heatRegion = Core.atlas.find(name + "-heat");
    }

    /** Modular Turrets have mounts of 3 sizes. */
    public static enum ModuleSize{
        small, medium, large
    }
}