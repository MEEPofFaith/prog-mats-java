package progressed.world.blocks.payloads;

import mindustry.type.*;
import mindustry.world.*;

public class Recipe{
    public float craftTime;
    public boolean requiresUnlock;

    public ItemStack[] buildCost;
    public LiquidStack[] liquidCost;
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

    public boolean hasInputBlock(){
        return inputBlock != null;
    }
}