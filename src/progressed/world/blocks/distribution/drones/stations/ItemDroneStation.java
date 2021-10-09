package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

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

        containerRegion = Core.atlas.find("pm-item-cargo");
    }

    public class ItemDroneStationBuild extends DroneStationBuild{
        @Override
        public void updateTile(){

            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            int[] it = new int[content.items().size];
            for(int i = 0; i < content.items().size; i++){
                it[i] = items.get(content.items().get(i));
            }
            d.cargo.load(it);
            items.clear();
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            for(int i = 0; i < content.items().size; i++){
                items.set(content.items().get(i), d.cargo.itemCargo[i]);
            }
            drone.cargo.empty();
        }

        @Override
        public boolean ready(){
            return active || connected && (isOrigin() ? items.total() >= itemCapacity * transportThreshold : items.total() <= itemCapacity);
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