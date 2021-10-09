package progressed.ai;

import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import progressed.entities.units.entity.*;
import progressed.entities.units.entity.DroneUnitEntity.*;

public class DroneAI extends AIController{
    @Override
    public void updateMovement(){
        if(unit instanceof DroneUnitEntity d){
            if(d.target == null){
                findDestination(d);
            }else{
                if(d.within(d.target, 4f)){
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
                                d.getStation().setTranfering();
                                d.load += d.loadSpeed() * Time.delta;
                                if(d.load >= 1){
                                    d.getStation().loadCargo(d);
                                    d.load = 1;
                                    reset(d);
                                }
                            }else{
                                fail(d);
                            }
                        }
                        case dropoff -> {
                            if(d.getStation() != null){
                                d.getStation().setTranfering();
                                d.load -= d.loadSpeed() * Time.delta;
                                if(d.load <= 0){
                                    d.getStation(d.curRoute, 0).active = false;
                                    d.getStation().active = false;
                                    d.getStation().takeCargo(d);
                                    if(!d.getStation().connected) d.getStation().configure(-1);
                                    d.load = 0;
                                    reset(d);
                                    updateRoutes(d);
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
                        if(d.estimateUse(d.curRoute) > d.charge){
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
            updateRoutes(d);
            d.load = 0;
            d.cargo.empty();
        }
    }

    public void reset(DroneUnitEntity d){
        d.target = null;
        d.arrived = false;
    }

    public void setTarget(DroneUnitEntity d, Teamc t, int state){
        d.target = t;
        d.state = DroneState.all[state];
    }

    @Override
    public void command(UnitCommand command){}
}
