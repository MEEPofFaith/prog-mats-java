package progressed.type.unit;

import mindustry.gen.*;
import mindustry.type.unit.*;
import progressed.entities.units.*;

public class DriftUnitType extends ErekirUnitType{
    public DriftUnitType(String name){
        super(name);
        constructor = DriftTrailUnit::new;

        trailLength = 1; //Trick the game into running drawTrail()
        engineSize = -1; //Don't make center engine by default, Chances are, it's gonna be a drift trail engine.

        circleTarget = true;
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public void drawTrail(Unit unit){
        DriftTrailUnit d = (DriftTrailUnit)unit;
        if(d.driftTrails != null){
            d.driftTrails.each(t -> t.draw(engineColor == null ? d.team.color : engineColor, 1f));
        }
    }
}
