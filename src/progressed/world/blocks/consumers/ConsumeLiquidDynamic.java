package progressed.world.blocks.consumers;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class ConsumeLiquidDynamic extends Consume{
    public final Func<Building, LiquidStack[]> liquids;

    public <T extends Building>  ConsumeLiquidDynamic(Func<T, LiquidStack[]> liquids){
        this.liquids = (Func<Building, LiquidStack[]>)liquids;
    }

    @Override
    public void applyLiquidFilter(Bits filter){
        //this must be done dynamically
    }

    @Override
    public ConsumeType type(){
        return ConsumeType.liquid;
    }

    @Override
    public void build(Building tile, Table table){
        LiquidStack[][] current = {liquids.get(tile)};

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
        int i = 0;

        for(LiquidStack stack : liquids.get(tile)){
            table.add(new ReqImage(stack.liquid.uiIcon,
                () -> tile.liquids != null && tile.liquids.get(stack.liquid) >= stack.amount)).size(iconMed).top().left();
            if(++i % 4 == 0) table.row();
        }
    }

    @Override
    public String getIcon(){
        return "icon-liquid-consume";
    }

    @Override
    public void update(Building entity){

    }

    @Override
    public void trigger(Building entity){
        for(LiquidStack stack : liquids.get(entity)){
            entity.liquids.remove(stack.liquid, stack.amount);
        }
    }

    @Override
    public boolean valid(Building entity){
        if(entity.liquids == null) return false;

        for(LiquidStack stack : liquids.get(entity)){
            Log.info("@ < @", entity.liquids.get(stack.liquid), stack.amount);
            if(entity.liquids.get(stack.liquid) < stack.amount){
                return false;
            }
        }

        return true;
    }

    @Override
    public void display(Stats stats){
        //should be handled by the block
    }
}