package progressed.ai;

import mindustry.entities.units.*;
import mindustry.gen.*;
import progressed.entities.units.entity.*;
import progressed.entities.units.entity.DroneUnitEntity.*;

import static mindustry.Vars.*;

public class DroneAI extends AIController{
    @Override
    public void updateMovement(){
        if(unit instanceof DroneUnitEntity d){
            if(d.target == null){
                findDestination(d);
            }else{
                if(d.within(d.target, 1f)){
                    d.arrived = true;
                }
                moveTo(d.target, 0.002f, 50f);
                if(d.arrived){
                    switch(d.state){
                        case charging -> {
                            d.stopped = true;
                            d.getPad().charging = true;
                            if(d.charged()){
                                d.getPad().charging = false;
                                reset(d);
                            }
                        }
                        case pickup -> {
                            reset(d);
                        }
                        case dropoff -> {
                            reset(d);
                            updateRoutes(d);
                        }
                    }
                }
            }
        }
    }

    public void findDestination(DroneUnitEntity d){
        if(d.hasRoutes()){
            if(d.checkCompleteRoute(d.curRoute)){
                switch(d.state){
                    case charging -> setTarget(d, world.build(d.routes.get(d.curRoute * 2)), 1);
                    case pickup -> setTarget(d, world.build(d.routes.get(d.curRoute * 2 + 1)), 2);
                    case dropoff -> {
                        int next = d.routes.get(d.curRoute * 2);
                        if(d.estimateUse(d.curRoute) > d.charge){
                            setTarget(d, d.getPad(), 0);
                        }else{
                            setTarget(d, world.build(next), 1);
                        }
                    }
                }
            }else{
                d.nextRoute();
            }
        }else{
            d.updateRoutes();
        }
    }

    public void updateRoutes(DroneUnitEntity d){
        d.updateRoutes();
        d.nextRoute();
    }

    public void reset(DroneUnitEntity d){
        d.target = null;
        d.arrived = false;
        d.stopped = false;
    }

    public void setTarget(DroneUnitEntity d, Teamc t, int state){
        d.target = t;
        d.state = DroneState.all[state];
    }
}
