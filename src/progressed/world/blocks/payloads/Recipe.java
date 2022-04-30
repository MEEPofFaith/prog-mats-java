package progressed.world.blocks.payloads;

import mindustry.type.*;
import mindustry.world.*;

public class Recipe{
    public float craftTime;
    public boolean requiresUnlock = true;
    public boolean blockBuild = true, centerBuild;

    public ItemStack[] buildCost;
    public LiquidStack liquidCost;
    public float powerUse;
    public Block inputBlock;

    public Block outputBlock;

    public Recipe(Block block){
        outputBlock = block;
        buildCost = block.requirements;

        if(block instanceof Missile m){
            craftTime = m.constructTime;
            powerUse = m.powerUse;
            inputBlock = m.prev;
        }
    }

    public Recipe(Block block, float powerUse, float craftTime){
        this(block);
        this.craftTime = craftTime;
        this.powerUse = powerUse;
    }

    public Recipe(Block outputBlock, Block inputBlock, float powerUse, float craftTime){
        this(outputBlock, powerUse, craftTime);
        this.inputBlock = inputBlock;
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
