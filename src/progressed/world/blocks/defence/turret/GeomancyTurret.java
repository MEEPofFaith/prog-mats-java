package progressed.world.blocks.defence.turret;

import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.world.blocks.defence.*;

public class GeomancyTurret extends PowerTurret{
    public MagmaPylon pylon;

    public GeomancyTurret(String name){
        super(name);
    }

    @Override
    public void init(){
        if(pylon == null){
            throw new RuntimeException(name + " does not have a magma pylon!");
        }

        super.init();
    }

    public class GeomancyTurretBuild extends PowerTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            targetPos.set(pos); //No projectile, no leading needed
        }

        @Override
        public BulletType useAmmo(){
            //literally nothing
            return null;
        }
    }
}