package progressed.world.blocks.sandbox.units;

import arc.graphics.g2d.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;
import progressed.*;

public class CapBlock extends Wall{
    public CapBlock(String name){
        super(name);
        requirements(Category.units, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;
        health = ProgMats.sandboxBlockHealth;
    }

    public class CapBlockBuild extends WallBuild{
        @Override
        public void draw(){
            Draw.rect(region, x, y);
    
            drawTeamTop();
        }
    }
}
