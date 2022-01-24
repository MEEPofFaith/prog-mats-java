package progressed.world.blocks.defence.turret.modular.mounts;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.audio.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;

public class BaseMount implements Position{
    /** Turret module associated with this mount. */
    public final BaseModule module;
    /** The mount's offset array term. */
    public int mountNumber;
    public int swapNumber;
    /** Position of the mount */
    public float x, y;
    /** Liquid module of the mount. Primarily used for cooling. */
    public LiquidModule liquids;
    /** Deploy progress */
    public float progress;
    /** Rotation relative to the unit this mount is on */
    public float rotation = 90;
    /** Current heat, 0 to 1*/
    public float heat;
    public boolean highlight;
    public SoundLoop sound;

    public BaseMount(ModularTurretBuild parent, BaseModule module, int mountNumber){
        this.module = module;
        this.mountNumber = swapNumber = mountNumber;
        updatePos(parent);

        if(module.hasLiquids) liquids = new LiquidModule();
        if(module.loopSound != Sounds.none) sound = new SoundLoop(module.loopSound, module.loopSoundVolume);
    }

    public void onProximityAdded(ModularTurretBuild parent){
        module.onProximityAdded(parent, this);
    }

    public void update(ModularTurretBuild parent){
        module.update(parent, this);
    }

    public void updatePos(ModularTurretBuild parent){
        if(mountNumber != swapNumber){
            mountNumber = swapNumber;
            progress = 0;
        }

        Vec2 offset = parent.getMountPos(module.size)[mountNumber];
        x = parent.x + offset.x;
        y = parent.y + offset.y;
    }

    public void draw(ModularTurretBuild parent){
        Draw.z(Layer.turret + module.layerOffset);
        module.draw(parent, this);
        if(highlight){
            Draw.z(Layer.overlayUI);
            module.drawHighlight(parent, this);
        }
    }

    public void unSwap(){
        swapNumber = mountNumber;
    }

    public void swap(int number){
        swapNumber = number;
    }

    public boolean valid(ModularTurretBuild parent){
        return parent != null && !parent.dead && parent.allMounts.contains(this);
    }

    //Method reference shorter and cleaner.
    public boolean isSmall(){
        return module.size == ModuleSize.small;
    }

    public boolean isMedium(){
        return module.size == ModuleSize.medium;
    }

    public boolean isLarge(){
        return module.size == ModuleSize.large;
    }

    public boolean checkSize(ModuleSize size){
        return size == module.size;
    }

    public boolean checkNumber(int number){
        return mountNumber == number;
    }

    public boolean checkSwap(int number){
        return swapNumber == number;
    }

    @Override
    public float getX(){
        return x;
    }

    @Override
    public float getY(){
        return y;
    }
}