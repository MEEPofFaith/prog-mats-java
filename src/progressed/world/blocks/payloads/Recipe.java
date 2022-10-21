package progressed.world.blocks.payloads;

import mindustry.type.*;
import mindustry.world.*;

public class Recipe{
    public float craftTime;
    public boolean requiresUnlock = true;
    public boolean blockBuild = true, centerBuild;

    public ItemStack[] itemRequirements;
    public LiquidStack liquidRequirements;
    public float powerUse;
    public Block inputBlock;

    public Block outputBlock;

    public Recipe(Block block){
        outputBlock = block;
        itemRequirements = block.requirements;

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
        return liquidRequirements != null ? liquidRequirements.liquid : null;
    }

    public boolean hasLiquidInput(Liquid liquid){
        return liquidRequirements != null && liquidRequirements.liquid == liquid;
    }

    public boolean hasInputBlock(){
        return inputBlock != null;
    }

    public boolean showReqList(){
        return itemRequirements.length > 0 || liquidRequirements != null;
    }

    public boolean unlocked(){
        return !requiresUnlock || outputBlock.unlockedNow();
    }
}
