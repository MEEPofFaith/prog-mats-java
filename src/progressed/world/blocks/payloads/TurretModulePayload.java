package progressed.world.blocks.payloads;

import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.multi.modules.*;

public class TurretModulePayload extends Block{
    public TurretModule module;

    public float elevation = -1f;

    public TurretModulePayload(String name){
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
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        module.createIcons(packer);
    }

    public void drawBase(Tile tile){
        Draw.z(Layer.blockUnder - 1f);
        Drawf.shadow(region, tile.drawx() - elevation, tile.drawy() - elevation);
        Draw.z(Layer.block);
        Draw.rect(region, tile.drawx(), tile.drawy());
    }

    public class TurretModulePayloadBuild extends Building{
        @Override
        public void drawCracks(){
            //don't
        }
    }
}