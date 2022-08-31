package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.content.blocks.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;

import java.util.*;

public class BaseModule extends Block{
    public ModuleSize moduleSize = ModuleSize.small;
    public int limit = -1;

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

        public void moduleDisplay(Table table, Table parentTable){

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
