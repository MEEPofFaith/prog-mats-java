package progressed.world.blocks.defence.turret.multi.mounts;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.audio.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.BaseModule.*;

public class BaseMount{
    /** Turret module associated with this mount. */
    public final BaseModule module;
    /** The mount's offset array term. */
    public int mountNumber;
    public int tempNumber;
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
    public SoundLoop sound;

    public BaseMount(ModularTurretBuild parent, BaseModule module, int mountNumber){
        this.module = module;
        this.mountNumber = mountNumber;
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
        Vec2 offset = parent.getMountPos(module.size)[mountNumber];
        x = parent.x + offset.x;
        y = parent.y + offset.y;
    }

    public void draw(ModularTurretBuild parent){
        Draw.z(Layer.turret + module.layerOffset);
        module.draw(parent, this);
    }

    public void move(int number){
        mountNumber = number;
        progress = 0;
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
}