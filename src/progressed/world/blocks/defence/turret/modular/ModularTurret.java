package progressed.world.blocks.defence.turret.modular;

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
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
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
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.*;
import progressed.world.meta.*;

public class ModularTurret extends PayloadBlock{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTargetFast = timers++, timerTarget = timers++;
    public int targetIntervalFast = 5, targetInterval = 20;

    public ModuleGroup[] moduleGroups;
    public Vec2[] smallMountPos, mediumMountPos, largeMountPos;
    public Color mountColor = Pal.accent;

    public TextureRegion[] mountBases = new TextureRegion[3];

    private static ModuleSize selSize;
    private static int selNum, rowCount;

    public ModularTurret(String name){
        super(name);

        acceptsPayload = true;
        outputsPayload = false;
        hasLiquids = true;
        outputsLiquid = false;
        rotate = false;
        configurable = true;
        solid = true;
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);

        config(Integer.class, (ModularTurretBuild build, Integer swap) -> {
            build.allMounts.get(Point2.x(swap)).mountNumber = Point2.y(swap);
        });

        config(Boolean.class, (ModularTurretBuild build, Boolean accordingToAllKnownLawsOfAviationThereIsNoWayThatABeeShouldBeAbleToFlyItsWingsAreTooSmallToGetItsFatLittleBodyOffTheGroundTheBeeOfCourseFliesAnywaysBecauseBeesDontCareWhatHumansThinkIsImpossible) -> {
            build.sort();
            build.updatePos();
        });
    }

    @Override
    public void init(){
        consumePowerDynamic(b -> ((ModularTurretBuild)b).mountPower());

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

        stats.add(Stat.itemCapacity, PMStatValues.moduleCounts(moduleGroups));
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
        removeBar("power");
    }

    public class ModularTurretBuild extends PayloadBlockBuild<BuildPayload> implements ControlBlock, Ranged{
        public Seq<BaseMount> allMounts = new Seq<>();
        public Seq<BaseTurretMount> turretMounts = new Seq<>();
        public float logicControlTime;
        public boolean logicShooting = false;
        public BlockUnitc unit = (BlockUnitc)UnitTypes.block.create(team);

        @Override
        public void onProximityAdded(){
            super.onProximityAdded();

            allMounts.each(m -> m.onProximityAdded(this));
        }

        @Override
        public Unit unit(){
            //make sure stats are correct
            unit.tile(this);
            unit.team(team);
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                retarget(World.unconv((float)p1), World.unconv((float)p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && (unit == null || !unit.isPlayer())){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc pos){
                    retarget(pos);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void updateTile(){
            if(moveInPayload()){
                if(payload.block() instanceof ModulePayload module && acceptModule(module.module.size)){
                    addModule(module.module);
                }
                payload = null;
            }

            unit.tile(this);
            unit.team(team);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(timer(timerTargetFast, targetIntervalFast)){
                turretMounts.each(m -> m.module.fastRetarget, m -> m.findTarget(this));
            }

            if(timer(timerTarget, targetInterval)){
                turretMounts.each(m -> !m.module.fastRetarget, m -> m.findTarget(this));
            }

            allMounts.each(m -> m.update(this));
        }

        @Override
        public void onConfigureClosed(){
            unHighlight();
        }

        public void retarget(float x, float y){
            turretMounts.each(m -> m.module.logicControl, m -> m.targetPos.set(x, y));
        }

        public void retarget(Posc p){
            turretMounts.each(m -> m.module.logicControl, m -> m.module.targetPosition(m, p));
        }

        @Override
        public void remove(){
            super.remove();

            allMounts.each(m -> m.module.remove(this, m));
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

            allMounts.each(m -> m.draw(this));
        }

        public void unHighlight(){
            allMounts.each(m -> m.highlight = false);
        }

        public void highlightModule(){
            unHighlight();
            if(allMounts.any() && selNum >= 0) allMounts.get(selNum).highlight = true;
        }

        public void updatePos(){
            allMounts.each(m -> m.updatePos(this));
        }

        public void resetSwap(){
            allMounts.each(BaseMount::unSwap);
        }

        /** @return the module it adds. */
        public BaseMount addModule(BaseModule module){
            return addModule(module, nextMount(module.size));
        }

        /** @return the module it adds. */
        public BaseMount addModule(BaseModule module, int pos){
            BaseMount mount = module.mountType.get(
                this,
                module,
                pos
            );
            if(mount instanceof BaseTurretMount t) turretMounts.add(t);
            allMounts.add(mount);
            mount.updatePos(this);
            sort();

            return mount;
        }

        public void removeMount(BaseMount mount){
            mount.module.remove(this, mount);
            allMounts.remove(mount);
            if(mount instanceof BaseTurretMount t) turretMounts.remove(t);
        }

        public short nextMount(ModuleSize size){
            short mount = 0;
            for(BaseMount m : allMounts){
                if(m.checkSize(size) && m.mountNumber == mount){
                    mount = (short)(m.mountNumber + 1);
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
            allMounts.sort(m -> m.module.size.ordinal() * 100 + m.mountNumber);
        }

        @Override
        public void buildConfiguration(Table table){
            resetSelection();

            table.table(t -> rebuild(t, false, false)).top().expandY();
        }

        public void rebuild(Table table, boolean dropMenu, boolean slideDisplay){
            highlightModule();
            table.clearChildren();
            table.top();
            table.table(t -> {
                t.top();
                for(ModuleSize mSize : ModuleSize.values()){
                    t.button(mSize.title(), Styles.flatTogglet, () -> {
                        if(selSize != mSize){
                            selSize = mSize;
                            selNum = allMounts.indexOf(m -> m.checkSize(mSize));
                            rebuild(table, true, false);
                        }
                    }).update(b -> {
                        b.setChecked(selSize == mSize);
                    }).size(80f, 40f);
                }
                t.button(Icon.settings, Styles.cleari, () -> {
                    ProgMats.swapDialog.show(this);
                }).size(80f, 40f);
            }).top();
            if(!allMounts.contains(m -> m.checkSize(selSize))) return;
            table.row();
            table.table(Styles.black6, t -> {
                t.top().left();
                if(dropMenu){
                    t.setTransform(true);
                    t.actions(Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, 0.15f, Interp.pow3Out));
                    t.update(() -> {
                        t.setOrigin(Align.top);
                    });
                }
                t.table(m -> {
                    m.left().top();
                    rowCount = 0;
                    allMounts.each(mount -> mount.checkSize(selSize), mount -> {
                        ImageButton button = m.button(Tex.whiteui, Styles.clearTogglei, 32f, () -> {
                            int index = allMounts.indexOf(mount);
                            if(selNum != index){
                                selNum = index;
                                rebuild(table, false, true);
                            }
                        }).update(b -> {
                            b.setChecked(selNum == allMounts.indexOf(mount));
                        }).size(40f).get();
                        button.getStyle().imageUp = new TextureRegionDrawable(mount.module.region);
                        if(rowCount++ % 8 == 7){
                            m.row();
                        }
                    });
                    if(rowCount % 8 != 0){
                        int remaining = 8 - (rowCount % 8);
                        for(int j = 0; j < remaining; j++){
                            m.image(Styles.none);
                        }
                    }
                }).left().top().growY();

                t.row();

                if(selNum >= 0){
                    BaseMount mount = allMounts.get(selNum);
                    t.table(d -> {
                        if(slideDisplay){
                            d.setTransform(true);
                            d.actions(Actions.scaleTo(1f, 0f), Actions.scaleTo(1f, 1f, 0.15f, Interp.pow3Out));
                            d.update(() -> {
                                d.setOrigin(Align.top);
                            });
                        }
                        d.top().left();
                        mount.module.displayAll(this, d, table, mount);
                    }).top().left().grow();
                }
            }).top().grow();
        }

        public void resetSelection(){
            selNum = 0;
            selSize = allMounts.any() ? allMounts.first().module.size : ModuleSize.small;
        }

        public void setSelection(ModuleSize size){
            selSize = size;
            selNum = allMounts.indexOf(m -> m.checkSize(size));
            if(selNum == -1){
                resetSelection();
            }
        }

        /** @return if a module can be added. */
        public boolean acceptModule(ModuleSize size){
            return switch(size){
                case small -> smallMountPos != null && allMounts.count(BaseMount::isSmall) + 1 <= smallMountPos.length;
                case medium -> mediumMountPos != null && allMounts.count(BaseMount::isMedium) + 1 <= mediumMountPos.length;
                case large -> largeMountPos != null && allMounts.count(BaseMount::isLarge) + 1 <= largeMountPos.length;
            };
        }

        //If you couldn't tell already I really like switch cases.

        @Override
        public boolean acceptItem(Building source, Item item){
            return allMounts.contains(m -> m.module.acceptItem(item, m));
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            BaseMount mount = allMounts.find(m -> m.module.acceptItem(item, m));

            if(mount == null) return 0;
            return mount.module.acceptStack(item, amount, mount);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return allMounts.contains(m -> m.module.acceptLiquid(liquid, m));
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return super.acceptPayload(source, payload) &&
                payload instanceof BuildPayload p &&
                p.block() instanceof ModulePayload module &&
                acceptModule(module.module.size) &&
                !allMounts.contains(m -> !m.module.acceptModule(module.module));
        }

        @Override
        public void handleItem(Building source, Item item){
            BaseMount mount = allMounts.find(m -> m.module.acceptItem(item, m));
            mount.module.handleItem(item, mount);
        }

        @Override
        public int removeStack(Item item, int amount){
            //Cannot remove items
            return 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            BaseMount mount = allMounts.find(m -> m.module.acceptItem(item, m));

            if(mount != null) mount.module.handleItem(item, mount);
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            float a = amount;
            while(a > 0 && allMounts.contains(m -> m.module.acceptLiquid(liquid, m))){ //Distribute overflow from one mount to the next
                BaseMount mount = allMounts.find(m -> m.module.acceptLiquid(liquid, m));
                if(mount == null) continue;
                a -= mount.module.handleLiquid(liquid, a, mount);
            }
        }

        @Override
        public void dropped(){
            allMounts.each(m -> m.updatePos(this));
        }

        public float mountPower(){
            float use = 0f;
            for(BaseMount mount : allMounts){
                use += mount.module.powerUse(this, mount);
            }
            return use;
        }

        @Override
        public float range(){
            if(turretMounts.isEmpty()) return 0;

            float[] range = {Float.MIN_VALUE};
            turretMounts.each(m -> {
                if(m.module.range > range[0]) range[0] = m.module.range;
            });
            return range[0];
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(allMounts.size);
            allMounts.each(m -> m.module.writeAll(write, m));
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            int len = read.i();
            for(int i = 0; i < len; i++){
                short id = read.s();
                short moduleNumber = read.s();
                Block module = Vars.content.block(id);
                //Note: Installing or uninstalling other mods can change id and break saves.
                if(module instanceof ModulePayload p){
                    BaseMount mount = addModule(p.module, moduleNumber);
                    mount.module.readAll(read, mount);
                }
            }

            allMounts.each(m -> m.updatePos(this));
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
