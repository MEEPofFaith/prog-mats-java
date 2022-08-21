package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;

import java.util.*;

public class BaseModule extends Block{
    public ModuleSize moduleSize = ModuleSize.small;
    public int limit = -1;

    public BaseModule(String name){
        super(name);
        update = true;
        destructible = false;
    }

    public class BaseModuleBuild extends Building{
        public ModularTurretBuild parent;
        public float progress = 0;
        public int mountNumber, swapNumber;
        public boolean highlight;

        public void moduleDraw(){
            draw();
        }

        public void moduleUpdate(){
            //overriden by subclasses!
        }

        public void moduleRemoved(){
            //overriden by subclasses!
        }

        public float moduleHandleLiquid(Building source, Liquid liquid, float amount){
            liquids.add(liquid, amount);
            return amount;
        }

        public float powerUse(){
            return 0f;
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

        public void swap(int number){
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
