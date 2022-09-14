package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.*;
import arc.func.*;
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
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import progressed.content.blocks.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;
import progressed.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class BaseModule extends Block{
    public ModuleSize moduleSize;
    public int limit = -1;
    /** Map of bars by name. Only displayed in Modular Turret config ui. */
    protected OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    public BaseModule(String name, ModuleSize size){
        super(name);
        moduleSize = size;
        update = true;
        destructible = false;
    }

    public BaseModule(String name){
        this(name, ModuleSize.small);
    }

    public void init(){
        PMModules.setClip(clipSize);
        super.init();
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
    }

    public Iterable<Func<Building, Bar>> listModuleBars(){
        return moduleBarMap.values();
    }

    public class BaseModuleBuild extends Building{
        public ModularTurretBuild parent;
        public float progress = 0, hAlpha;
        public short mountNumber, swapNumber;
        public boolean highlight;

        public void moduleAdded(ModularTurretBuild parent, short pos){
            this.parent = parent;
            mountNumber = swapNumber = pos;
            if(hasPower) parent.power.graph.add(this);
        }

        public void moduleDraw(){
            highlight();
            draw();
            Draw.reset();
        }

        public void highlight(){
            if(hAlpha > 0.001f) Draw.mixcol(parent.team.color, Mathf.absin(7f, 1f) * hAlpha);
        }

        public void moduleUpdate(){
            if(hasPower && power.graph != parent.power.graph){
                power.graph.remove(this);
                parent.power.graph.add(this);
            }
            updateConsumption();
            hAlpha = Mathf.approachDelta(hAlpha, Mathf.num(highlight), 0.15f);
        }

        public void moduleRemoved(){
            parent = null;
            highlight = false;
            hAlpha = 0f;
            if(hasPower) power.graph.remove(this);
        }

        public float moduleHandleLiquid(Building source, Liquid liquid, float amount){
            liquids.add(liquid, amount);
            return amount;
        }

        public boolean isModule(){
            return parent != null;
        }

        @Override
        public float efficiencyScale(){
            return Mathf.num(isModule());
        }

        public boolean acceptModule(BaseModule module){
            if(limit > 0){
                int count = parent.allMounts.count(m -> m.block == module);
                return count < limit;
            }
            return true;
        }

        public void moduleDisplay(Table table){
            table.table(t -> {
                t.left();
                t.image(region).scaling(Scaling.fit).size(5f * 8f);
                t.label(() -> localizedName + " (" + (mountNumber + 1) + ")").left().growX().padLeft(5);
                PMStatValues.infoButton(t, block, 5f * 8f).left().padLeft(5f);
                t.button(new TextureRegionDrawable(Icon.upload),2.5f * 8f, () -> {
                    tryPickUp(parent);
                }).size(5f * 8f).left().padLeft(5f).tooltip("@pm-pickup.label");
            }).growX().top();

            table.row();

            table.table(bars -> {
                bars.defaults().growX().height(18f).pad(4f);

                displayModuleBars(bars);
            }).growX().top();
        }

        public void displayModuleBars(Table table){
            for(Func<Building, Bar> bar : listModuleBars()){
                var result = bar.get(self());
                if(result == null) continue;
                table.add(result).growX();
                table.row();
            }
        }

        public void tryPickUp(ModularTurretBuild parent){
            if(canPickUp()){ //jeez this is a mess
                Payloadc p = (Payloadc)player.unit();
                BuildPayload module = new BuildPayload(self());
                p.addPayload(module);
                Fx.unitPickup.at(x, y);
                Events.fire(new PickupEvent(player.unit(), module.build));
                boolean has = parent.allMounts.contains(m -> m.checkSize(moduleSize));
                parent.removeMount(self());
                parent.setSelection(moduleSize);
                parent.rebuild(true, !has, has);
            }else{
                showPickupFail();
            }
        }

        public boolean canPickUp(){
            Unit u = player.unit();
            if(!(u instanceof Payloadc p)) return false;

            return p.payloadUsed() + size * size * tilePayload <= u.type.payloadCapacity + 0.01f
                && u.within(x, y, tilesize * size * 1.2f);
        }

        public void showPickupFail(){
            Unit u = player.unit();
            if(!(u instanceof Payloadc p)){
                ui.showInfoToast("@pm-pickup.invalid", 2f);
            }else{
                if(!player.unit().within(x, y, size * tilesize * 1.2f)){
                    ui.showInfoToast("@pm-pickup.toofar", 2f);
                }else if(p.payloadUsed() + size * size * tilePayload > u.type.payloadCapacity + 0.01f){
                    ui.showInfoToast("@pm-pickup.full", 2f);
                }
            }
        }

        public void updatePos(ModularTurretBuild parent){
            this.parent = parent;
            if(mountNumber != swapNumber){
                mountNumber = swapNumber;
                progress = 0;
            }

            Vec2 offset = parent.getMountPos(moduleSize)[mountNumber];
            x = parent.x + offset.x;
            y = parent.y + offset.y;
        }

        public void unSwap(){
            swapNumber = mountNumber;
        }

        public void swap(short number){
            swapNumber = number;
        }

        //Method reference shorter and cleaner.
        public ModuleSize moduleSize(){
            return moduleSize;
        }

        public boolean isSmall(){
            return moduleSize == ModuleSize.small;
        }

        public boolean isMedium(){
            return moduleSize == ModuleSize.medium;
        }

        public boolean isLarge(){
            return moduleSize == ModuleSize.large;
        }

        public boolean checkSize(ModuleSize size){
            return size == moduleSize;
        }

        public boolean checkNumber(int number){
            return mountNumber == number;
        }

        public boolean checkSwap(int number){
            return swapNumber == number;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.s(mountNumber);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            mountNumber = swapNumber = read.s();
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
