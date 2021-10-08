package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class LiquidDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;

    public TextureRegion liquidRegion, topRegion;

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
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    public class LiquidDroneStationBuild extends DroneStationBuild{
        @Override
        public void updateTile(){

            if(liquids.total() > 0.01f){
                dumpLiquid(liquids.current());
            }
        }

        @Override
        public boolean ready(){
            return isOrigin() ? liquids.total() >= liquidCapacity * transportThreshold : liquids.total() <= liquidCapacity;
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

                Draw.rect(topRegion, x, y);
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return isOrigin() && (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && drone == null;
        }
    }
}