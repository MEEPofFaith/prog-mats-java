package progressed.ai;

import mindustry.entities.units.*;
import mindustry.gen.*;
import progressed.entities.units.entity.*;
import progressed.entities.units.entity.DroneUnitEntity.*;
import progressed.world.blocks.distribution.drones.stations.DroneStation.*;

public class DroneAI extends AIController{
    @Override
    public void updateMovement(){
        if(unit instanceof DroneUnitEntity d){
            if(d.target == null){
                findDestination(d);
            }else{
                if(d.within(d.target, 8f)){
                    d.arrived = true;
                }
                if(d.arrived){
                    switch(d.state){
                        case charging, idle -> {
                            if(d.getPad() != null){
                                d.getPad().charging = true;
                                if(d.charged()){
                                    d.getPad().charging = false;
                                    reset(d);
                                }
                            }else{
                                fail(d);
                            }
                        }
                        case pickup -> {
                            if(d.getStation() != null){
                                loading(d);
                                if(d.getStation().load == 1){
                                    d.cargo.drawCargo = true;
                                    d.getStation().loadCargo(d);
                                    reset(d);
                                }
                            }else{
                                fail(d);
                            }
                        }
                        case dropoff -> {
                            if(d.getStation() != null){
                                d.cargo.drawCargo = false;
                                loading(d);
                                if(d.getStation().load == 0){
                                    d.getStation().takeCargo(d);
                                    d.deactivate();
                                    reset(d);
                                    updateRoutes(d);
                                    d.cargo.drawCargo = true;
                                }
                            }else{
                                fail(d);
                            }
                        }
                    }
                }
            }
            moveTo(d.target, 1f, 50f);
        }
    }

    public void findDestination(DroneUnitEntity d){
        if(d.hasRoutes()){
            if(d.checkCompleteRoute(d.curRoute)){
                switch(d.state){
                    case charging, idle -> setTarget(d, d.getStation(d.curRoute, 0), 1);
                    case pickup -> setTarget(d, d.getStation(d.curRoute, 1), 2);
                    case dropoff -> {
                        if(d.estimateUse(d.curRoute) > d.charge * d.getType().checkMultiplier){
                            setTarget(d, d.getPad(), 0);
                        }else{
                            setTarget(d, d.getStation(d.curRoute, 0), 1);
                        }
                    }
                }
            }else{
                d.nextRoute();
            }
        }else{
            d.updateRoutes();
            setTarget(d, d.getPad(), 3);
        }
    }

    public void updateRoutes(DroneUnitEntity d){
        d.updateRoutes();
        d.nextRoute();
    }

    public void fail(DroneUnitEntity d){
        if(!d.dead){
            reset(d);
            d.updateRoutes();
            d.cargo.empty();
            d.state = DroneState.dropoff;
        }
    }

    public void reset(DroneUnitEntity d){
        if(d.getStation() != null) d.getStation().arrived = false;
        d.target = null;
        d.arrived = false;
    }

    public void setTarget(DroneUnitEntity d, Teamc t, int state){
        d.target = t;
        d.state = DroneState.all[state];
    }

    public void loading(DroneUnitEntity d){
        DroneStationBuild station = d.getStation();
        if(station != null){
            station.setLoading(d);
            station.updateCargo(d);
        }
    }

    @Override
    public void command(UnitCommand command){}
}