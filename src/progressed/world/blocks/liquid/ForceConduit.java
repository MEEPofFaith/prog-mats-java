package progressed.world.blocks.liquid;

import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;

public class ForceConduit extends Conduit{
    public float flowRate = -1f;

    public ForceConduit(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        if(flowRate < 0) flowRate = liquidCapacity / 2f;
    }

    public class ForceConduitBuild extends ConduitBuild{
        @Override
        public float moveLiquid(Building next, Liquid liquid){
            if(next == null) return 0;

            next = next.getLiquidDestination(self(), liquid);

            if(next.team == team && next.block.hasLiquids && liquids.get(liquid) > 0f){
                float flow = Math.min(liquids.get(liquid), flowRate);
                flow = Math.min(flow, next.block.liquidCapacity - next.liquids.get(liquid));

                if(flow > 0f && next.acceptLiquid(self(), liquid)){
                    next.handleLiquid(self(), liquid, flow);
                    liquids.remove(liquid, flow);
                    return flow;
                }else if(next.liquids.currentAmount() / next.block.liquidCapacity > 0.1f){
                    //TODO these are incorrect effect positions
                    float fx = (x + next.x) / 2f, fy = (y + next.y) / 2f;

                    Liquid other = next.liquids.current();
                    if((other.flammability > 0.3f && liquid.temperature > 0.7f) || (liquid.flammability > 0.3f && other.temperature > 0.7f)){
                        damage(1 * Time.delta);
                        next.damage(1 * Time.delta);
                        if(Mathf.chance(0.1 * Time.delta)){
                            Fx.fire.at(fx, fy);
                        }
                    }else if((liquid.temperature > 0.7f && other.temperature < 0.55f) || (other.temperature > 0.7f && liquid.temperature < 0.55f)){
                        liquids.remove(liquid, Math.min(liquids.get(liquid), 0.7f * Time.delta));
                        if(Mathf.chance(0.2f * Time.delta)){
                            Fx.steam.at(fx, fy);
                        }
                    }
                }
            }
            return 0;
        }
    }
}