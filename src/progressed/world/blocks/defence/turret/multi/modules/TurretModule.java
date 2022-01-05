package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.multi.*;

public class TurretModule implements Cloneable{
    public String name;
    /** Automatically set */
    public short mountID;

    public float range;
    public boolean hasLiquid;

    public ModuleSize size = ModuleSize.small;

    public float elevation = -1f;
    public float layerOffset;
    public Color heatColor = Pal.turretHeat;
    public TextureRegion region;
    public TextureRegion heatRegion;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public Func<TurretModule, TurretMount> mountType = TurretMount::new;

    public TurretModule(String name){
        this.name = name;
    }

    public void init(){
        //small = 1, medium = 2, large = 3
        if(elevation < 0) elevation = size.ordinal() + 1;
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public void draw(float x, float y, TurretMount mount){
        Vec2 tr = Tmp.v1;

        float rot = mount.rotation;

        Draw.z(Layer.turret + layerOffset);
        tr.trns(rot, -mount.recoil);

        Drawf.shadow(region, x + tr.x, y + tr.y - elevation, rot - 90);
        Draw.rect(region, x + tr.x, y + tr.y, rot - 90);

        if(heatRegion.found() && mount.heat > 0.001f){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + tr.x, y + tr.y, rot - 90);
            Draw.blend();
            Draw.color();
        }
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