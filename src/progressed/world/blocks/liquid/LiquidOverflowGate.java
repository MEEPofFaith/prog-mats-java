package progressed.world.blocks.liquid;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;

public class LiquidOverflowGate extends LiquidBlock{
    public boolean invert = false;

    public LiquidOverflowGate(String name){
        super(name);
        canOverdrive = false;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.liquidCapacity);
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("liquid");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class LiquidOverfloatGateBuild extends Building{
        @Override
        public void draw(){
            Draw.rect(region, x, y);
        }

        @Override
        public Building getLiquidDestination(Building source, Liquid liquid){
            if(!enabled) return this;

            int from = relativeToEdge(source.tile);
            if(from == -1) return this;

            Building to = nearby((from + 2) % 4);
            boolean canForward = to != null && to.team == team && to.acceptLiquid(this, liquid) && to.liquids.get(liquid) < to.block.liquidCapacity;

            if(!canForward || invert){
                Building a = nearby(Mathf.mod(from - 1, 4));
                Building b = nearby(Mathf.mod(from + 1, 4));
                boolean ac = a != null && a.team == team && a.acceptLiquid(this, liquid) && a.liquids.get(liquid) < a.block.liquidCapacity;
                boolean bc = b != null && b.team == team && b.acceptLiquid(this, liquid) && b.liquids.get(liquid) < b.block.liquidCapacity;

                if(!ac && !bc){
                    return invert && canForward ? to : this;
                }

                if(ac && !bc){
                    to = a;
                }else if(bc && !ac){
                    to = b;
                }else{
                    to = (rotation & (1 << from)) == 0 ? a : b;
                }
            }

            return to;
        }
    }
}
