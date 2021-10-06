package progressed.world.blocks.distribution.drones.stations;

public class LiquidDroneStation extends DroneStation{
    public LiquidDroneStation(String name){
        super(name);

        hasLiquids = true;
    }

    public class LiquidDroneStationBuild extends DroneStationBuild{

    }
}