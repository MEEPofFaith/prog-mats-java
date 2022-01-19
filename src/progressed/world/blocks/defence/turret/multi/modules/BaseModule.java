package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;
import progressed.world.blocks.payloads.*;
import progressed.world.blocks.payloads.ModulePayload.*;
import progressed.world.meta.*;

public class BaseModule implements Cloneable{
    public String name, localizedName;
    /** Automatically set */
    public short mountID;

    public float deployTime = -1f;
    public boolean hasLiquids, usePower;
    public float powerUse;
    public float liquidCapacity = 10f;
    public float layerOffset;

    public Sound loopSound = Sounds.none;
    public float loopSoundVolume = 1f;

    public ModuleSize size;

    public Func3<ModularTurretBuild, BaseModule, Short, BaseMount> mountType = BaseMount::new;

    public final MountBars bars = new MountBars();
    public final Consumers consumes = new Consumers();

    public TextureRegion region;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public static final Vec2 tr = new Vec2(), tr2 = new Vec2();

    public BaseModule(String name, ModuleSize size){
        this.name = "prog-mats-" + name;
        this.size = size;
        localizedName = Core.bundle.get("pmturretmodule." + name + ".name", name);
    }

    public BaseModule(String name){
        this(name, ModuleSize.small);
    }

    public void init(){
        if(deployTime < 0) deployTime = size() * 2f * 60f;

        consumes.init();

        setBars();
    }

    public void load(){
        region = Core.atlas.find(name);
    }

    public void setStats(Stats stats){
        if(powerUse > 0) stats.add(Stat.powerUse, powerUse * 60f, StatUnit.powerSecond);

        consumes.display(stats);
    }

    public void setBars(){
        if(hasLiquids){
            Func<BaseMount, Liquid> current = mount -> mount.liquids == null ? Liquids.water : mount.liquids.current();
            bars.add("liquid", (entity, mount) -> new Bar(
                () -> mount.liquids.get(current.get(mount)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(mount).localizedName,
                () -> current.get(mount).barColor(),
                () -> mount == null || mount.liquids == null ? 0f : mount.liquids.get(current.get(mount)) / liquidCapacity
            ));
        }

        if(usePower){
            bars.add("power", (entity, mount) -> new Bar(
                () -> Core.bundle.get("bar.power"),
                () -> Pal.powerBar,
                () -> entity.power.status
            ));
        }
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public int size(){
        return size.ordinal() + 1;
    }

    public void onProximityAdded(ModularTurretBuild parent, BaseMount mount){}

    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        return isDeployed(mount);
    }

    public void update(ModularTurretBuild parent, BaseMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);

        if(!Vars.headless && mount.sound != null) mount.sound.update(mount.x, mount.y, shouldLoopSound(parent, mount));
    }

    public void remove(ModularTurretBuild parent, BaseMount mount){
        if(mount.sound != null){
            mount.sound.stop();
        }
    }

    public boolean isDeployed(BaseMount mount){
        return mount.progress >= deployTime;
    }

    public float speedScl(ModularTurretBuild parent, BaseMount mount){
        return 1f;
    }

    public float efficiency(ModularTurretBuild parent){
        return parent.power.status;
    }

    public void draw(ModularTurretBuild parent, BaseMount mount){
        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.blockBuildCenter(mount.x, mount.y, region, mount.rotation - 90, mount.progress / deployTime));
            return;
        }

        Draw.rect(region, mount.x, mount.y);
    }

    public void drawPayload(ModulePayloadBuild payload){
        Draw.rect(region, payload.x, payload.y);
    }

    public void display(ModularTurretBuild parent, Table table, BaseMount mount){
        table.table(t -> {
            t.left();
            t.add(new Image(region)).size(8 * 4);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        });

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4f);

            displayBars(parent, table, mount);
        }).growX();
    }

    public void displayBars(ModularTurretBuild parent, Table table, BaseMount mount){
        for(Func2<ModularTurretBuild, BaseMount, Bar> bar : bars.list()){
            try{
                table.add(bar.get(parent, mount)).growX();
                table.row();
            }catch(ClassCastException e){
                break;
            }
        }
    }

    public boolean acceptModule(BaseModule module){
        return true;
    }

    public int acceptStack(Item item, int amount, BaseMount mount){
        return 0;
    }

    public boolean acceptItem(Item item, BaseMount mount){
        return false;
    }

    public boolean acceptLiquid(Liquid liquid, BaseMount mount){
        return isDeployed(mount) && hasLiquids && consumes.liquidfilters.get(liquid.id) && mount.liquids.get(liquid) < liquidCapacity;
    }

    public void handleItem(Item item, BaseMount mount){}

    /** @return amount of liquid accepted */
    public float handleLiquid(Liquid liquid, float amount, BaseMount mount){
        float added = Math.min(amount, liquidCapacity - mount.liquids.get(liquid));
        mount.liquids.add(liquid, added);
        return added;
    }

    public float powerUse(ModularTurretBuild parent, BaseMount mount){
        return Mathf.num(isActive(parent, mount)) * powerUse;
    }

    public boolean shouldLoopSound(ModularTurretBuild parent, BaseMount mount){
        return false;
    }

    public void writeAll(Writes write, BaseMount mount){
        write.s(mountID);
        write.s(mount.mountNumber);
        write.b(version());
        if(mount.liquids != null) mount.liquids.write(write);
        write(write, mount);
    }

    public void readAll(Reads read, BaseMount mount){
        byte revision = read.b();
        if(mount.liquids != null) mount.liquids.read(read);
        read(read, revision, mount);
    }

    public void write(Writes write, BaseMount mount){
        write.f(mount.progress);
        write.f(mount.rotation);
    }

    public void read(Reads read, byte revision, BaseMount mount){
        mount.progress = read.f();
        mount.rotation = read.f();
    }

    public byte version(){
        return 0;
    }

    public BaseModule copy(){
        try{
            return (BaseModule)clone();
        }catch(CloneNotSupportedException suck){
            throw new RuntimeException("very good language design", suck);
        }
    }

    /** Modular Turrets have mounts of 3 sizes. */
    public enum ModuleSize{
        small, medium, large
    }
}