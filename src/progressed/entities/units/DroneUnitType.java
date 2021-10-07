package progressed.entities.units;

import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.ai.*;
import progressed.entities.units.entity.*;

public class DroneUnitType extends UnitType{
    public float powerUse = 2f, chargeCapacity = 600f;

    public DroneUnitType(String name){
        super(name);
        constructor = DroneUnitEntity::new;
        defaultController = DroneAI::new;

        flying = true;
        speed = 3f;
    }

    @Override
    public void update(Unit unit){
        if(unit instanceof DroneUnitEntity d){
            d.charge -= Time.delta * powerUse * (d.vel.len() / speed);
        }
    }

    @Override
    public boolean isHidden(){
        return true;
    }
}
