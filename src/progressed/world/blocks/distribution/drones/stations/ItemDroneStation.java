package progressed.world.blocks.distribution.drones.stations;

import arc.graphics.*;
import mindustry.gen.*;
import mindustry.type.*;

public class ItemDroneStation extends DroneStation{
    public ItemDroneStation(String name){
        super(name);

        hasLiquids = false;
        hasItems = true;
        itemCapacity = 100;
        selectColor = Color.yellow;
    }

    public class ItemDroneStationBuild extends DroneStationBuild{
        @Override
        public void updateTile(){
            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return accepting() && items.total() + 1 <= itemCapacity;
        }
    }
}