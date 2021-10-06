package progressed.world.blocks.distribution.drones.stations;

import mindustry.gen.*;
import mindustry.world.blocks.payloads.*;

public class PayloadDroneStation extends DroneStation{
    public PayloadDroneStation(String name){
        super(name);
    }

    public class PayloadDroneStationBuild extends DroneStationBuild{
        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return payload == null && accepting();
        }
    }
}