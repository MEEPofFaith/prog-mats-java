package progressed.world.blocks.defence.turret.payload.modular;

import arc.func.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

/** An interface for any block that's used as a modular turret module. */
public interface TurretModule{
    ModuleModule module();

    ModuleSize size();

    Building build();

    Iterable<Func<Building, Bar>> listModuleBars();

    default float x(){
        return build().x;
    }

    default float y(){
        return build().y;
    }

    default Block block(){
        return build().block;
    }

    default String name(){
        return build().block.localizedName;
    }

    default TextureRegion icon(){
        return build().block.fullIcon;
    }

    default ModularTurretBuild parent(){
        return module().parent;
    }

    default float deployTime(){
        return 60f; //1 second is a nice default
    }

    default void moduleUpdate(){
        module().moduleUpdate();
        if(isDeployed()) build().update();
    }

    default void moduleDraw(){
        Draw.z(Layer.turret);
        if(!isDeployed()){
            drawDeploy();
            return;
        }

        build().draw();
        if(module().highlight){
            Draw.z(Layer.overlayUI);
            module().highlight();
            build().drawSelect();
            Draw.z(Layer.turret);
        }
    }

    default void drawDeploy(){
        float elevation = block().size / 2f;
        Draw.draw(Draw.z(), () -> {
            PMDrawf.materialize(x() - elevation, y() - elevation, icon(), build().team.color, 0f, 0.1f, module().progress, true);
            PMDrawf.materialize(x(), y(), icon(), build().team.color, 0f, 0.1f, module().progress);
        });
    }

    default void swap(short number){
    }

    default boolean isSmall(){
        return size() == ModuleSize.small;
    }

    default boolean isMedium(){
        return size() == ModuleSize.medium;
    }

    default boolean isLarge(){
        return size() == ModuleSize.large;
    }

    default boolean checkSize(ModuleSize size){
        return size == size();
    }

    default boolean checkNumber(short number){
        return module().mountNumber == number;
    }

    default boolean checkLastNumber(short number){
        return module().lastNumber == number;
    }

    default void moduleRemoved(){}

    default boolean isModule(){
        return module().parent != null;
    }

    default boolean isDeployed(){
        return module().progress >= 0.999f;
    }

    default boolean isActive(){
        return isDeployed();
    }

    default boolean acceptModule(TurretModule module){
        return true;
    }
}
