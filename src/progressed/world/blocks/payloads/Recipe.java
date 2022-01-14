package progressed.world.blocks.payloads;

import mindustry.type.*;
import mindustry.world.*;

public class Recipe{
    public float craftTime;
    public boolean requiresUnlock;
    public boolean blockBuild = true, centerBuild;

    public ItemStack[] buildCost;
    public LiquidStack liquidCost;
    public float powerUse;
    public Block inputBlock;

    public Block outputBlock;

    public Recipe(Block block){
        outputBlock = block;

        if(block instanceof Missile m){
            craftTime = m.constructTime;
            requiresUnlock = m.requiresUnlock;
            buildCost = m.requirements;
            powerUse = m.powerUse;
            inputBlock = m.prev;
        }
    }

    public Liquid getLiquidInput(){
        return liquidCost != null ? liquidCost.liquid : null;
    }

    public boolean hasLiquidInput(Liquid liquid){
        return liquidCost != null && liquidCost.liquid == liquid;
    }

    public boolean hasInputBlock(){
        return inputBlock != null;
    }
}