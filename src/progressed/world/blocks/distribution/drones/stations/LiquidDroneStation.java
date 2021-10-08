package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;

public class LiquidDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;

    public TextureRegion liquidRegion, tankRegion;

    public LiquidDroneStation(String name){
        super(name);

        hasItems = false;
        hasLiquids = true;
        liquidCapacity = 300;
        outputsLiquid = true;
        selectColor = Liquids.cryofluid.color;
    }

    @Override
    public void load(){
        super.load();

        liquidRegion = Core.atlas.find(name + "-liquid");
        tankRegion = Core.atlas.find("pm-liquid-cargo");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, tankRegion};
    }

    public class LiquidDroneStationBuild extends DroneStationBuild{
        @Override
        public void updateTile(){

            if(liquids.total() > 0.01f){
                dumpLiquid(liquids.current());
            }
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            d.cargo.load(new LiquidStack(liquids.current(), liquids.currentAmount()));
            liquids.clear();
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            liquids.add(d.cargo.liquidCargo.liquid, d.cargo.liquidCargo.amount);
            drone.cargo.empty();
        }

        @Override
        public boolean ready(){
            return active || connected && (isOrigin() ? liquids.total() >= liquidCapacity * transportThreshold : liquids.total() <= liquidCapacity);
        }

        @Override
        public void draw(){
            super.draw();

            if(drone != null){
                drawLoad();
            }else{
                if(liquids.total() > 0.01f){
                    Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
                }

                Draw.rect(tankRegion, x, y);
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return isOrigin() && (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && drone == null;
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.button(Icon.trash, () -> liquids.clear()).tooltip("@pm-drone-dump-liquid").size(40);
        }
    }
}