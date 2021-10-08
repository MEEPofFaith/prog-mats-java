package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.type.*;

public class ItemDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;

    public TextureRegion containerRegion;

    public ItemDroneStation(String name){
        super(name);

        hasLiquids = false;
        hasItems = true;
        itemCapacity = 100;
        selectColor = Color.yellow;
    }

    @Override
    public void load(){
        super.load();

        containerRegion = Core.atlas.find(name + "-container");
    }

    public class ItemDroneStationBuild extends DroneStationBuild{
        @Override
        public void updateTile(){

            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }
        }

        @Override
        public boolean ready(){
            return isOrigin() ? items.total() >= itemCapacity * transportThreshold : items.total() <= itemCapacity;
        }

        @Override
        public void draw(){
            super.draw();

            if(drone != null){
                drawLoad();
            }else{
                Draw.rect(containerRegion, x, y);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return isOrigin() && items.total() + 1 <= itemCapacity && drone == null;
        }
    }
}