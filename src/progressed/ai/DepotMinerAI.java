package progressed.ai;

import arc.math.geom.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.world.blocks.production.UnitMinerDepot.*;

import static mindustry.Vars.*;

public class DepotMinerAI extends AIController{
    protected final Vec2 targetPos = new Vec2(), vecOut = new Vec2();
    protected int pathId = -1;

    public boolean mining = true;
    public Tile targetTile;

    @Override
    public void updateMovement(){
        if(!unit.canMine()) return;

        if(unit.mineTile != null && !unit.mineTile.within(unit, unit.type.mineRange)){
            unit.mineTile(null);
        }

        if(mining){
            if(targetItem() == null || //No targetted item
                home().oreTiles.getNull(targetItem()) == null || //No ore target tile
                unit.stack.amount >= unit.type.itemCapacity ||
                (targetItem() != null && !unit.acceptsItem(targetItem())) || //Inventory full
                home().acceptStack(targetItem(), 1, unit) == 0 //Depot is full
            ){
                mining = false;
            }else{
                if(timer.get(timerTarget, 40)){ //Closer ore was passed on the way to the original target, save it as new closer target.
                    Tile ore = indexer.findClosestOre(unit, targetItem());
                    if(ore != targetTile && unit.within(ore.worldx(), ore.worldy(), unit.type.mineRange)){
                        home().oreTiles.put(targetItem(), ore);
                    }
                }

                if(targetTile != home().oreTiles.get(targetItem())){
                    targetTile = home().oreTiles.get(targetItem());
                    targetPos.set(targetTile.worldx(), targetTile.worldy());
                    pathId = Vars.controlPath.nextTargetId();
                }

                if(unit.within(targetTile, unit.type.mineRange)){
                    unit.mineTile = targetTile;
                }
            }
        }else{
            unit.mineTile = null;

            if(unit.stack.amount == 0){
                mining = true;
                return;
            }

            if(targetTile != home().tile){
                targetTile = home().tile;
                targetPos.set(home());
                pathId = Vars.controlPath.nextTargetId();
            }

            if(unit.within(home(), unit.type.range)){
                if(home().acceptStack(unit.stack.item, unit.stack.amount, unit) > 0){
                    Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, home());
                }

                unit.clearItem();
                mining = true;
            }
        }
        if(pathId != -1) move();
        faceMovement();
    }

    public void move(){
        vecOut.set(targetPos);

        boolean move = Vars.controlPath.getPathPosition(unit, pathId, targetPos, vecOut);
        if(move){
            moveTo(vecOut, mining && unit.within(targetPos, unit.type.mineRange / 2) ? unit.type.mineRange : 0f);
        }
    }

    public UnitMinerDepotBuild home(){
        return (UnitMinerDepotBuild)((BuildingTetherc)unit).building();
    }

    public Item targetItem(){
        return home().targetItem;
    }
}
