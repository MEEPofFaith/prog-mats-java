package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.payloads.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.multi.*;

public class TurretModule implements Cloneable{
    public String name;
    /** Automatically set */
    public short mountID;

    public float deployTime;
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

    public void update(Team t, float x, float y, TurretMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);
    }

    public void draw(Team t, float x, float y, TurretMount mount){
        Vec2 tr = Tmp.v1;

        float rot = mount.rotation;

        Draw.z(Layer.turret + layerOffset);

        if(mount.progress < deployTime){
            Drawf.construct(x, y, region, 0f, mount.progress / deployTime, 1f, mount.progress);
            return;
        }

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

    public boolean acceptItem(Item item, TurretMount mount){
        return false;
    }

    public boolean acceptLiquid(Liquid liquid, TurretMount mount){
        return false;
    }

    public boolean acceptPayload(BuildPayload payload, TurretMount mount){
        return false;
    }

    public void writeAll(Writes write, TurretMount mount){
        write.s(mountID);
        write.b(version());
        if(mount.liquids != null) mount.liquids.write(write);
        write(write, mount);
    }

    public void readAll(Reads read, TurretMount mount){
        byte revision = read.b();
        if(mount.liquids != null) mount.liquids.read(read);
        read(read, revision, mount);
    }

    public void write(Writes write, TurretMount mount){
        write.f(mount.progress);
        write.f(mount.reload);
        write.f(mount.rotation);
    }

    public void read(Reads read, byte revision, TurretMount mount){
        mount.progress = read.f();
        mount.reload = read.f();
        mount.rotation = read.f();
    }

    public byte version(){
        return 0;
    }

    /** Modular Turrets have mounts of 3 sizes. */
    public static enum ModuleSize{
        small, medium, large
    }
}