package progressed.world.blocks.defence.turret;

import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;

public class GeomancyTurret extends PowerTurret{
    public GeomancyTurret(String name){
        super(name);
    }

    public class GeomancyTurretBuild extends PowerTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            targetPos.set(pos); //Don't lead
        }
    }
}