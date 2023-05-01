package progressed.world.blocks.defence.turret.payload.modular;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.world.meta.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;

public class ModularTurret extends PayloadBlock{
    public ModuleGroup[] moduleGroups;
    public Vec2[] smallMountPos, mediumMountPos, largeMountPos;
    public Color mountColor = Pal.accent;

    public TextureRegion[] mountBases = new TextureRegion[3];

    protected static ModuleSize selSize;
    protected static final Table moduleDisplayTable = new Table();

    public ModularTurret(String name){
        super(name);

        acceptsPayload = true;
        outputsPayload = false;
        hasLiquids = true;
        hasPower = true;
        outputsLiquid = false;
        rotate = false;
        configurable = true;
        solid = true;
        suppressable = true; //For modules that can heal.
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);

        config(Integer.class, (ModularTurretBuild build, Integer swap) -> {
            build.modules.get(Point2.x(swap)).module().mountNumber = Point2.y(swap);
        });

        config(Boolean.class, (ModularTurretBuild build, Boolean accordingToAllKnownLawsOfAviationThereIsNoWayThatABeeShouldBeAbleToFlyItsWingsAreTooSmallToGetItsFatLittleBodyOffTheGroundTheBeeOfCourseFliesAnywaysBecauseBeesDontCareWhatHumansThinkIsImpossible) -> {
            build.sort();
            build.updatePos();
        });
    }

    @Override
    public void init(){
        super.init();

        if(moduleGroups == null) return;

        int sLen = 0, mLen = 0, lLen = 0;

        for(ModuleGroup group : moduleGroups){
            switch(group.size){
                case small -> sLen += group.amount();
                case medium -> mLen += group.amount();
                case large -> lLen += group.amount();
            }
        }

        int sCount = 0, mCount = 0, lCount = 0;

        for(ModuleGroup group : moduleGroups){
            switch(group.size){
                case small -> {
                    if(smallMountPos == null) smallMountPos = new Vec2[sLen];
                    for(int i = 0; i < group.amount(); i++){
                        smallMountPos[sCount] = group.pos(i);
                        sCount++;
                    }
                }
                case medium -> {
                    if(mediumMountPos == null) mediumMountPos = new Vec2[mLen];
                    for(int i = 0; i < group.amount(); i++){
                        mediumMountPos[mCount] = group.pos(i);
                        mCount++;
                    }
                }
                case large -> {
                    if(largeMountPos == null) largeMountPos = new Vec2[lLen];
                    for(int i = 0; i < group.amount(); i++){
                        largeMountPos[lCount] = group.pos(i);
                        lCount++;
                    }
                }
            }
        }
    }

    @Override
    public void load(){
        super.load();

        if(!inRegion.found() && minfo.mod != null) inRegion = Core.atlas.find(minfo.mod.name + "-factory-in-" + size + regionSuffix);

        for(int i = 0; i < 3; i++){
            mountBases[i] = Core.atlas.find(name + "-mount" + (i + 1), "prog-mats-mount" + (i + 1));
        }
    }

    public void setClip(float clip){
        float dst = 0;
        if(smallMountPos != null){
            for(Vec2 pos: smallMountPos){
                dst = Math.max(dst, Math.max(pos.x, pos.y));
            }
        }
        if(mediumMountPos != null){
            for(Vec2 pos: mediumMountPos){
                dst = Math.max(dst, Math.max(pos.x, pos.y));
            }
        }
        if(largeMountPos != null){
            for(Vec2 pos : largeMountPos){
                dst = Math.max(dst, Math.max(pos.x, pos.y));
            }
        }

        clipSize = Math.max(clipSize, clip + dst * 2f);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, inRegion, topRegion};
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.powerUse);
        stats.remove(Stat.liquidCapacity);

        stats.add(PMStat.mounts, PMStatValues.moduleCounts(moduleGroups));
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
        removeBar("power");
    }

    @Override
    public boolean canBreak(Tile tile){
        ModularTurretBuild b = (ModularTurretBuild)tile.build;
        return b.modules.isEmpty() || state.rules.infiniteResources || state.isEditor();
    }

    public class ModularTurretBuild extends PayloadBlockBuild<BuildPayload> implements ControlBlock{
        public Seq<TurretModule> modules = new Seq<>();
        public BlockUnitc unit = (BlockUnitc)UnitTypes.block.create(team);
        protected int selNum;

        @Override
        public Unit unit(){
            //make sure stats are correct
            unit.tile(this);
            unit.team(team);
            return (Unit)unit;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            modules.each(m -> m.build().control(type, p1, p2, p3, p4));

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            modules.each(m -> m.build().control(type, p1, p2, p3, p4));

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void updateTile(){
            if(moveInPayload()){
                if(payload.build instanceof TurretModule module){
                    addModule(module);
                }
                payload = null;
            }

            modules.each(TurretModule::moduleUpdate);

            unit.tile(this); //Set the unit's building back to the base. Turret#updateTile sets the unit's building to itself. (Vanilla is very much not prepared for me making multiple buildings share the same unit.)
            unit.team(team);
            unit.ammo(1f); //No way to display the separate ammos of different turrets, so just don't bother.
        }

        @Override
        public void onConfigureClosed(){
            unHighlight();
        }

        @Override
        public void remove(){
            super.remove();

            modules.each(m -> m.module().moduleRemoved());
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y); //region is the base

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                }
            }

            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            for(ModuleSize s : ModuleSize.values()){
                if(acceptModule(s)){
                    float mX = x + nextMountX(s),
                        mY = y + nextMountY(s);
                    Draw.color(mountColor, Mathf.absin(60f / Mathf.PI2, 1f));
                    Draw.rect(mountBases[s.ordinal()], mX, mY);
                    Draw.color();
                }
            }

            if(isPayload()) updatePos();

            modules.each(TurretModule::moduleDraw);
        }

        @Override
        public void applyBoost(float intensity, float duration){
            super.applyBoost(intensity, duration);
            modules.each(m -> m.build().applyBoost(intensity, duration));
        }

        @Override
        public void applyHealSuppression(float amount){
            super.applyHealSuppression(amount);
            modules.each(m -> m.build().applyHealSuppression(amount));
        }

        @Override
        public void applySlowdown(float intensity, float duration){
            super.applySlowdown(intensity, duration);
            modules.each(m -> m.build().applySlowdown(intensity, duration));
        }

        public void unHighlight(){
            modules.each(m -> m.module().highlight = false);
        }

        public void highlightModule(){
            unHighlight();
            if(modules.any() && selNum >= 0) modules.get(selNum).module().highlight = true;
        }

        public void updatePos(){
            modules.each(m -> m.module().updatePos(this));
        }

        /** @return the module it adds. */
        public TurretModule addModule(TurretModule module){
            return addModule(module, nextMount(module.size()));
        }

        /** @return the module it adds. */
        public TurretModule addModule(TurretModule module, short pos){
            module.module().moduleAdded(this, pos);
            module.module().updatePos(this);
            modules.add(module);
            sort();

            boolean has = modules.contains(m -> m.checkSize(module.size()));
            rebuild(false, !has, has);

            if(ProgMats.swapDialog.isShown()) ProgMats.swapDialog.rebuild();

            return module;
        }

        public void removeMount(TurretModule module){
            module.module().moduleRemoved();
            modules.remove(module);
        }

        public short nextMount(ModuleSize size){
            short mount = 0;
            for(TurretModule m : modules){
                if(m.checkSize(size) && m.module().mountNumber == mount){
                    mount = (short)(m.module().mountNumber + 1);
                }
            }
            return mount;
        }

        public Vec2[] getMountPos(ModuleSize size){
            return switch(size){
                case small -> smallMountPos;
                case medium -> mediumMountPos;
                case large -> largeMountPos;
            };
        }

        public int getMountCapacity(ModuleSize size){
            return getMountPos(size).length;
        }

        public int getMaxMounts(ModuleSize size){
            return getMountPos(size).length;
        }

        public float nextMountX(ModuleSize size){
            return nextMountX(size, nextMount(size));
        }

        public float nextMountX(ModuleSize size, int pos){
            return getMountPos(size)[pos].x;
        }

        public float nextMountY(ModuleSize size){
            return nextMountY(size, nextMount(size));
        }

        public float nextMountY(ModuleSize size, int pos){
            return getMountPos(size)[pos].y;
        }

        public void sort(){
            modules.sort(m -> m.size().ordinal() * 100 + m.module().mountNumber);
        }

        @Override
        public void buildConfiguration(Table table){
            resetSelection();

            rebuild(true, false, false);
            table.table(t -> t.add(moduleDisplayTable)).top().expandY();
        }

        public void rebuild(boolean highlight, boolean dropMenu, boolean slideDisplay){
            if(highlight) highlightModule();
            moduleDisplayTable.clearChildren();
            moduleDisplayTable.top();
            moduleDisplayTable.table(t -> {
                t.top();
                for(ModuleSize mSize : ModuleSize.values()){
                    t.button(mSize.title(), Styles.flatTogglet, () -> {
                        if(selSize != mSize){
                            selSize = mSize;
                            selNum = modules.indexOf(m -> m.checkSize(mSize));
                            rebuild(true, true, false);
                        }
                    }).update(b -> b.setChecked(selSize == mSize)).size(80f, 40f);
                }
                t.button(Icon.settings, Styles.cleari, () -> ProgMats.swapDialog.show(this)).size(80f, 40f);
            }).top();
            if(!modules.contains(m -> m.checkSize(selSize))) return;
            moduleDisplayTable.row();
            moduleDisplayTable.table(Styles.black6, t -> {
                t.top().left();
                if(dropMenu){
                    t.setTransform(true);
                    t.actions(Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, 0.15f, Interp.pow3Out));
                    t.update(() -> t.setOrigin(Align.top));
                }
                t.table(m -> {
                    m.left().top();
                    int[] rowCount = {0};
                    modules.each(module -> module.checkSize(selSize), module -> {
                        ImageButton button = m.button(Tex.whiteui, Styles.clearTogglei, 32f, () -> {
                            int index = modules.indexOf(module);
                            if(selNum != index){
                                selNum = index;
                                rebuild(true, false, true);
                            }
                        }).update(b -> b.setChecked(selNum == modules.indexOf(module))).size(40f).get();
                        button.getStyle().imageUp = new TextureRegionDrawable(module.icon());
                        if(rowCount[0]++ % 8 == 7){
                            m.row();
                        }
                    });
                    if(rowCount[0] % 8 != 0){
                        int remaining = 8 - (rowCount[0] % 8);
                        for(int j = 0; j < remaining; j++){
                            m.image(Styles.none);
                        }
                    }
                }).left().top().growY();

                t.row();

                if(selNum >= 0){
                    TurretModule module = modules.get(selNum);
                    t.table(d -> {
                        if(slideDisplay){
                            d.setTransform(true);
                            d.actions(Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, 0.15f, Interp.pow3Out));
                            d.update(() -> d.setOrigin(Align.top));
                        }
                        d.top().left();
                        module.module().moduleDisplay(d);
                    }).top().left().grow();
                }
            }).top().grow();
        }

        public void resetSelection(){
            selNum = 0;
            selSize = modules.any() ? modules.first().size() : ModuleSize.small;
        }

        public void setSelection(ModuleSize size){
            selSize = size;
            selNum = modules.indexOf(m -> m.checkSize(size));
            if(selNum == -1){
                resetSelection();
            }
        }

        /** @return if a module can be added. */
        public boolean acceptModule(ModuleSize size){
            return switch(size){
                case small -> smallMountPos != null && modules.count(TurretModule::isSmall) + 1 <= smallMountPos.length;
                case medium -> mediumMountPos != null && modules.count(TurretModule::isMedium) + 1 <= mediumMountPos.length;
                case large -> largeMountPos != null && modules.count(TurretModule::isLarge) + 1 <= largeMountPos.length;
            };
        }

        //If you couldn't tell already I really like switch cases.

        @Override
        public boolean acceptItem(Building source, Item item){
            return modules.contains(m -> m.build().acceptItem(this, item));
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            TurretModule mount = modules.find(m -> m.build().acceptStack(item, amount, this) > 0);

            if(mount == null) return 0;
            return mount.build().acceptStack(item, amount, this);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return modules.contains(m -> m.build().acceptLiquid(this, liquid));
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return super.acceptPayload(source, payload) &&
                payload instanceof BuildPayload p &&
                p.build instanceof TurretModule module &&
                acceptModule(module.size()) &&
                !modules.contains(m -> !m.acceptModule(module));
        }

        @Override
        public void handleItem(Building source, Item item){
            TurretModule mount = modules.find(m -> m.build().acceptItem(this, item));
            mount.build().handleItem(this, item);
        }

        @Override
        public int removeStack(Item item, int amount){
            //Cannot remove items
            return 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            TurretModule mount = modules.find(m -> m.build().acceptStack(item, amount, this) > 0);

            if(mount != null) mount.build().handleStack(item, amount, this);
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            TurretModule mount = modules.find(m -> m.build().acceptLiquid(this, liquid));
            mount.build().handleLiquid(this, liquid, amount);
        }

        @Override
        public void dropped(){
            modules.each(m -> m.module().updatePos(this));
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(modules.size);
            modules.each(m -> {
                write.s(m.block().id);
                write.b(m.build().version());
                m.build().writeAll(write);
            });
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                int amount = read.i();
                for(int i = 0; i < amount; i++){
                    Block module = content.block(read.s());
                    TurretModule moduleBuild = (TurretModule)module.newBuilding().create(module, Team.derelict);
                    byte version = read.b();
                    moduleBuild.build().readAll(read, version);
                    moduleBuild.build().tile = emptyTile;
                    moduleBuild.module().parent = this;

                    addModule(moduleBuild, moduleBuild.module().mountNumber);
                }
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }

    public static class ModuleGroup{
        public ModuleSize size;
        public ModuleGroupType groupType;
        public float offsetX, offsetY;

        public ModuleGroup(ModuleSize size, ModuleGroupType groupType, float offsetX, float offsetY){
            this.size = size;
            this.groupType = groupType;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public ModuleGroup(ModuleSize size){
            this(size, ModuleGroupType.single, 0f, 0f);
        }

        public Vec2 pos(int pos){
            float x = switch(groupType){
                case single -> offsetX;
                case quad -> -offsetX;
                case circle -> pos % 2 == 0 ? -offsetX : 0f;
                case oct -> pos % 2 == 0 ? -offsetX : -offsetY;
            };
            float y = switch(groupType){
                case single, quad -> offsetY;
                case circle -> pos % 2 == 0 ? offsetX : offsetY;
                case oct -> pos % 2 == 0 ? offsetY : offsetX;
            };

            Vec2 output = new Vec2(x, y);
            switch(groupType){
                case quad -> output.rotate(pos * -90f);
                case circle, oct -> output.rotate(Mathf.floor(pos/ 2f) * -90f);
            }

            return output;
        }

        public int amount(){
            return switch(groupType){
                case single -> 1;
                case quad -> 4;
                case circle, oct -> 8;
            };
        }

        public enum ModuleGroupType{
            single,
            quad,
            circle,
            oct
        }
    }
}
