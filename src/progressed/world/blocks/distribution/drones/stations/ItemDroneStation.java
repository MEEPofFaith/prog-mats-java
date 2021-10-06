package progressed.world.blocks.distribution.drones.stations;

public class ItemDroneStation extends DroneStation{
    public ItemDroneStation(String name){
        super(name);

        hasItems = true;
    }

    public class ItemDroneStationBuild extends DroneStationBuild{

    }
}