package progressed.world.blocks.defence.turret.multi.mounts;

import arc.graphics.g2d.*;
import mindustry.graphics.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.BaseModule.*;

public class BaseMount{
    /** Turret module associated with this mount. */
    public final BaseModule module;
    /** Offset of the mount compared to the base. */
    public final float offsetX, offsetY;
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

    public BaseMount(ModularTurretBuild parent, BaseModule module, float offsetX, float offsetY){
        this.module = module;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        updatePos(parent);
        if(module.hasLiquids) liquids = new LiquidModule();
    }

    public void onProximityAdded(ModularTurretBuild parent){
        module.onProximityAdded(parent, this);
    }

    public void update(ModularTurretBuild parent){
        module.update(parent, this);
    }

    public void updatePos(ModularTurretBuild parent){
        x = parent.x + offsetX;
        y = parent.y + offsetY;
    }

    public void draw(){
        Draw.z(Layer.turret + module.layerOffset);
        module.draw(this);
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
}