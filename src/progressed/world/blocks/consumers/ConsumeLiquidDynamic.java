package progressed.world.blocks.consumers;

import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;

import static mindustry.Vars.*;

public class ConsumeLiquidDynamic extends Consume{
    public final Func<Building, LiquidStack> liquids;

    @SuppressWarnings("unchecked")
    public <T extends Building>  ConsumeLiquidDynamic(Func<T, LiquidStack> liquids){
        this.liquids = (Func<Building, LiquidStack>)liquids;
    }

    @Override
    public void apply(Block block){
        block.hasLiquids = true;
    }

    @Override
    public void build(Building tile, Table table){
        LiquidStack[] current = {liquids.get(tile)};

        table.table(cont -> {
            table.update(() -> {
                if(current[0] != liquids.get(tile)){
                    rebuild(tile, cont);
                    current[0] = liquids.get(tile);
                }
            });

            rebuild(tile, cont);
        });
    }

    private void rebuild(Building tile, Table table){
        table.clear();

        LiquidStack stack = liquids.get(tile);
        if(stack != null)
            table.add(new ReqImage(stack.liquid.uiIcon,
                () -> tile.liquids != null && tile.liquids.get(stack.liquid) >= stack.amount)).size(iconMed).top().left();
    }

    @Override
    public void trigger(Building entity){
        LiquidStack stack = liquids.get(entity);
        if(stack != null)
            entity.liquids.remove(stack.liquid, stack.amount);
    }

    @Override
    public float efficiency(Building build){
        LiquidStack l = liquids.get(build);
        return l != null ? Math.min(build.liquids.get(l.liquid) / (l.amount * build.edelta()), 1f) : 1f;
    }
}
