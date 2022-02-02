package progressed.world.blocks.payloads;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.modules.*;

public class ModulePayload extends Block{
    public BaseModule module;

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
        size = module.size();

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

    public class ModulePayloadBuild extends Building{
        @Override
        public void draw(){
            module.drawPayload(this);
        }

        @Override
        public void drawCracks(){
            //don't
        }
    }
}