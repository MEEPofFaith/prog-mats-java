package progressed.world.blocks.defence.turret.sandbox;

import arc.struct.*;
import mindustry.entities.bullet.*;
import mindustry.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.world.meta.*;

public class FreeTurret extends Turret{
    public BulletType shootType;

    public FreeTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
    }

    public void limitRange(float margin){
        limitRange(shootType, margin);
    }

    public class FreeTurretBuild extends TurretBuild{
        @Override
        public void updateTile(){
            unit.ammo(1);

            super.updateTile();
        }

        @Override
        public double sense(LAccess sensor){
            return switch(sensor){
                case ammo -> 1;
                case ammoCapacity -> 1;
                default -> super.sense(sensor);
            };
        }

        @Override
        public BulletType useAmmo(){
            //nothing used
            return shootType;
        }

        @Override
        public boolean hasAmmo(){
            return true;
        }

        @Override
        public BulletType peekAmmo(){
            return shootType;
        }
    }
}
