package progressed.world.blocks.defence.turret.modular.modules;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.blocks.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.*;
import progressed.world.blocks.payloads.ModulePayload.*;
import progressed.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class BaseModule implements Cloneable{
    public String name, localizedName;
    /** Automatically set */
    public short mountID;

    public float deployTime = -1f;
    public boolean canOverdrive = true;
    public boolean hasLiquids;
    public float powerUse;
    public float liquidCapacity = 10f;
    public float layerOffset;
    public float elevation = -1f;
    public float clipSize;

    public Sound loopSound = Sounds.none;
    public float loopSoundVolume = 1f;

    public ModuleSize size;
    public boolean single;
    public ModuleStats mStats;

    public Func3<ModularTurretBuild, BaseModule, Integer, BaseMount> mountType = BaseMount::new;

    public final OrderedMap<String, Func2<ModularTurretBuild, BaseMount, Bar>> barMap = new OrderedMap<>();
    public boolean[] liquidFilter = {};
    /** Array of consumers used by this block. Only populated after init(). */
    public Consume[] consumers = {}, optionalConsumers = {}, nonOptionalConsumers = {}, updateConsumers = {};
    /** Set to true if this block has any consumers in its array. */
    public boolean hasConsumers;
    /** List for building up consumption before init(). */
    protected Seq<Consume> consumeBuilder = new Seq<>();
    /** The single power consumer, if applicable. */
    public ConsumePower consPower;

    public TextureRegion region;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public static final Vec2 shootOffset = new Vec2(), recoilOffset = new Vec2();

    public BaseModule(String name, ModuleSize size){
        this.name = "prog-mats-" + name;
        this.size = size;

        localizedName = Core.bundle.get("module." + this.name + ".name", Core.bundle.get("block." + this.name + ".name", this.name));
    }

    public BaseModule(String name){
        this(name, ModuleSize.small);
    }

    public void init(){
        //small = 1, medium = 2, large = 3
        if(elevation < 0) elevation = size() / 2f;
        if(deployTime < 0) deployTime = size() * 2f * 60f;
        clipSize = Math.max(clipSize, size() * tilesize);

        consumers = consumeBuilder.toArray(Consume.class);
        optionalConsumers = consumeBuilder.select(c -> c.optional && !c.ignore()).toArray(Consume.class);
        nonOptionalConsumers = consumeBuilder.select(c -> !c.optional && !c.ignore()).toArray(Consume.class);
        updateConsumers = consumeBuilder.select(c -> c.update && !c.ignore()).toArray(Consume.class);
        hasConsumers = consumers.length > 0;

        for(Consume cons : consumers){
            cons.apply(PMModules.cons);
        }

        liquidFilter = PMModules.cons.liquidFilter.clone();
        hasLiquids = PMModules.cons.hasLiquids;

        setBars();

        PMModules.setClip(clipSize);
    }

    public void load(){
        region = Core.atlas.find(name);
    }

    /**
     * Creates a consumer which directly uses power without buffering it.
     * @param powerPerTick The amount of power which is required each tick for 100% efficiency.
     * @return the created consumer object.
     */
    public ConsumePower consumePower(float powerPerTick){
        return consume(new ConsumePower(powerPerTick, 0.0f, false));
    }

    public ConsumeCoolant consumeCoolant(float amount){
        return consume(new ConsumeCoolant(amount));
    }

    public <T extends Consume> T consume(T consume){
        if(consume instanceof ConsumePower){
            //there can only be one power consumer
            consumeBuilder.removeAll(b -> b instanceof ConsumePower);
            consPower = (ConsumePower)consume;
        }
        consumeBuilder.add(consume);
        return consume;
    }

    public boolean consumesLiquid(Liquid liq){
        return liquidFilter[liq.id];
    }

    /** Set stats for both the module payload and the module. */
    public void setStats(Stats stats){
        if(powerUse > 0) stats.add(Stat.powerUse, powerUse * 60f, StatUnit.powerSecond);
        if(hasLiquids) stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        stats.add(Stat.size, " | ");
        stats.add(Stat.size, size.fullTitle());

        for(var c : consumers){
            c.display(stats);
        }
    }

    /** Set stats for just the module. */
    public void setModuleStats(Stats stats){
        mStats = new ModuleStats(stats);
        mStats.useCategories = true;
        mStats.remove(Stat.health);
        mStats.remove(Stat.size);
        mStats.add(Stat.size, size.fullTitle());
    }

    public void setBars(){
        if(hasLiquids){
            Func<BaseMount, Liquid> current = mount -> mount.liquids == null ? Liquids.water : mount.liquids.current();
            addBar("liquid", (entity, mount) -> new Bar(
                () -> mount.liquids.get(current.get(mount)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(mount).localizedName,
                () -> current.get(mount).barColor(),
                () -> mount == null || mount.liquids == null ? 0f : mount.liquids.get(current.get(mount)) / liquidCapacity
            ));
        }

        if(powerUse > 0){
            addBar("power", (entity, mount) -> new Bar(
                "bar.power",
                Pal.powerBar,
                () -> entity.power.status
            ));
        }
    }

    public void addBar(String name, Func2<ModularTurretBuild, BaseMount, Bar> sup){
        barMap.put(name, sup);
    }


    public String displayDescription(){
        ModulePayload payload = getPayload();
        return payload.description + "\n" + Core.bundle.format("mod.display", payload.minfo.mod.meta.displayName());
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public int size(){
        return size.ordinal() + 1;
    }

    public void onProximityAdded(ModularTurretBuild parent, BaseMount mount){}

    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        return isDeployed(mount) && powerValid(parent) && parent.enabled;
    }

    public boolean usePower(ModularTurretBuild parent, BaseMount mount){
        return isActive(parent, mount);
    }

    public void update(ModularTurretBuild parent, BaseMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);

        if(!headless && mount.sound != null) mount.sound.update(mount.x, mount.y, shouldLoopSound(parent, mount));
    }

    public void remove(ModularTurretBuild parent, BaseMount mount){
        if(mount.sound != null){
            mount.sound.stop();
        }
    }

    public boolean isDeployed(BaseMount mount){
        return mount.progress >= deployTime;
    }

    public float delta(ModularTurretBuild parent){
        return Time.delta * (canOverdrive ? parent.timeScale() : 1f);
    }

    public float efficiency(ModularTurretBuild parent){
        return powerUse > 0 ? parent.power.status : 1f;
    }

    public float edelta(ModularTurretBuild parent){
        return efficiency(parent) * delta(parent);
    }

    public boolean powerValid(ModularTurretBuild parent){
        return powerUse == 0 || efficiency(parent) > 0;
    }

    public void draw(ModularTurretBuild parent, BaseMount mount){
        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.blockBuildCenter(mount.x, mount.y, region, mount.rotation - 90, mount.progress / deployTime));
            return;
        }

        Drawf.shadow(region, mount.x - elevation, mount.y - elevation);
        applyColor(parent, mount);
        Draw.rect(region, mount.x, mount.y);
        Draw.mixcol();
    }

    public void drawHighlight(ModularTurretBuild parent, BaseMount mount){}

    public void drawPayload(ModulePayloadBuild payload){
        Draw.rect(region, payload.x, payload.y);
    }

    public void applyColor(ModularTurretBuild parent, BaseMount mount){
        if(mount.highlightAlpha > 0.001f) Draw.mixcol(parent.team.color, Mathf.absin(7f, 1f) * mount.highlightAlpha);
    }

    public void displayAll(ModularTurretBuild parent, Table table, Table parentTable, BaseMount mount){
        table.table(t -> {
            t.left();
            t.image(region).scaling(Scaling.fit).size(5f * 8f);
            t.label(() -> localizedName + " (" + (mount.mountNumber + 1) + ")").left().growX().padLeft(5);
            PMStatValues.moduleInfoButton(t, this, 5f * 8f).left().padLeft(5f);
            t.button(new TextureRegionDrawable(Icon.upload),2.5f * 8f, () -> {
                tryPickUp(parent, parentTable, mount);
            }).size(5f * 8f).left().padLeft(5f).tooltip("@pm-pickup.label");
        }).grow().top().left();

        table.row();

        display(parent, table, mount);
    }

    public void display(ModularTurretBuild parent, Table table, BaseMount mount){
        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4f);

            displayBars(parent, bars, mount);
        }).grow().top().left();
    }

    public void tryPickUp(ModularTurretBuild parent, Table parentTable, BaseMount mount){
        if(canPickUp(mount)){ //jeez this is a mess
            Payloadc p = (Payloadc)player.unit();
            BuildPayload module = new BuildPayload(getPayload(), parent.team);
            p.addPayload(module);
            Fx.unitPickup.at(mount.x, mount.y);
            Events.fire(new PickupEvent(player.unit(), module.build));
            boolean has = parent.allMounts.contains(m -> m.checkSize(mount.module.size));
            parent.removeMount(mount);
            parent.setSelection(mount.module.size);
            parent.rebuild(parentTable, !has, has);
        }else{
            showPickupFail(mount);
        }
    }

    public boolean canPickUp(BaseMount mount){
        Unit u = player.unit();
        if(!(u instanceof Payloadc p)) return false;

        ModulePayload mPay = getPayload();
        return p.payloadUsed() + mPay.size * mPay.size * tilePayload <= u.type.payloadCapacity + 0.01f
            && u.within(mount.x, mount.y, tilesize * mPay.size * 1.2f);
    }

    public void showPickupFail(BaseMount mount){
        Unit u = player.unit();
        if(!(u instanceof Payloadc p)){
            ui.showInfoToast("@pm-pickup.invalid", 2f);
        }else{
            ModulePayload mPay = getPayload();
            if(!player.unit().within(mount.x, mount.y, mPay.size * tilesize * 1.2f)){
                ui.showInfoToast("@pm-pickup.toofar", 2f);
            }else if(p.payloadUsed() + mPay.size * mPay.size * tilePayload > u.type.payloadCapacity + 0.01f){
                ui.showInfoToast("@pm-pickup.full", 2f);
            }
        }
    }

    public void displayBars(ModularTurretBuild parent, Table table, BaseMount mount){
        for(Func2<ModularTurretBuild, BaseMount, Bar> bar : barMap.values()){
            try{
                table.add(bar.get(parent, mount));
                table.row();
            }catch(ClassCastException e){
                break;
            }
        }
    }

    public boolean acceptModule(BaseModule module){
        return !single || module != this;
    }

    public int acceptStack(Item item, int amount, BaseMount mount){
        return 0;
    }

    public boolean acceptItem(Item item, BaseMount mount){
        return false;
    }

    public boolean acceptLiquid(Liquid liquid, BaseMount mount){
        return isDeployed(mount) && hasLiquids && consumesLiquid(liquid) && mount.liquids.get(liquid) < liquidCapacity;
    }

    public void handleItem(Item item, BaseMount mount){}

    /** @return amount of liquid accepted */
    public float handleLiquid(Liquid liquid, float amount, BaseMount mount){
        float added = Math.min(amount, liquidCapacity - mount.liquids.get(liquid));
        mount.liquids.add(liquid, added);
        return added;
    }

    public float powerUse(ModularTurretBuild parent, BaseMount mount){
        return Mathf.num(usePower(parent, mount)) * powerUse;
    }

    public boolean shouldLoopSound(ModularTurretBuild parent, BaseMount mount){
        return false;
    }

    public ModulePayload getPayload(){
        return (ModulePayload)content.block(mountID);
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
        write.f(mount.heat);
    }

    public void read(Reads read, byte revision, BaseMount mount){
        mount.progress = read.f();
        mount.rotation = read.f();
        if(revision >= 1){
            mount.heat = read.f();
        }
    }

    public byte version(){
        return 1;
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
        small, medium, large;

        public String title(){
            return Core.bundle.get("pm-size." + name().toLowerCase(Locale.ROOT) + ".short");
        }

        public String fullTitle(){
            return Core.bundle.get("pm-size." + name().toLowerCase(Locale.ROOT) + ".full");
        }

        public String amount(int amount){
            return Core.bundle.format("pm-size." + name().toLowerCase(Locale.ROOT) + ".amount", amount);
        }
    }
}
