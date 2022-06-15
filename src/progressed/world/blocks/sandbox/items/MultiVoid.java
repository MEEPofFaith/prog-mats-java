package progressed.world.blocks.sandbox.items;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class MultiVoid extends Block{
    public MultiVoid(String name){
        super(name);
        requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        health = 1000000;
        update = solid = acceptsItems = hasLiquids = true;
        group = BlockGroup.transportation;
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }
    
    @Override
    public boolean canReplace(Block other){
        if(other.alwaysReplace) return true;
        return other.replaceable && (other != this || rotate) && this.group != BlockGroup.none && (other.group == BlockGroup.transportation || other.group == BlockGroup.liquids) &&
            (size == other.size || (size >= other.size && ((subclass != null && subclass == other.subclass) || group.anyReplace)));
    }

    public class MultiVoidBuild extends Building{
        @Override
        public boolean acceptItem(Building source, Item item){
            return enabled;
        }

        @Override
        public void handleItem(Building source, Item item){}

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return enabled;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){}
    }
}
