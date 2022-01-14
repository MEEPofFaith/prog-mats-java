package progressed.world.blocks.payloads;

import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class ModulePayload extends Block{
    public BaseModule module;

    public float elevation = -1f;

    public ModulePayload(String name){
        super(name);
        requirements(Category.turret, BuildVisibility.sandboxOnly, ItemStack.empty);

        outlineIcon = true;
        health = 200;
        solid = true;
        update = true;
        hasShadow = false;
        rebuildable = false;
        drawDisabled = false;
        squareSprite = false;
    }

    @Override
    public void load(){
        super.load();
        module.load();
    }

    @Override
    public void init(){
        if(elevation < 0) elevation = size / 3f;

        super.init();
        module.init();
        module.mountID = id;
    }

    @Override
    public void setStats(){
        super.setStats();

        module.setStats(stats);
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        module.createIcons(packer);
    }

    @Override
    public boolean canBeBuilt(){
        return false;
    }

    public class TurretModulePayloadBuild extends Building{
        @Override
        public void draw(){
            Draw.z(Layer.blockUnder - 1f);
            Drawf.shadow(region, x - elevation, y - elevation);
            Draw.z(Layer.block);
            Draw.rect(region, x, y);
        }

        @Override
        public void drawCracks(){
            //don't
        }
    }
}