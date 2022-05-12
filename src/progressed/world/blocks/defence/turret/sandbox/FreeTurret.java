package progressed.world.blocks.defence.turret.sandbox;

import arc.struct.*;
import mindustry.entities.bullet.*;
import mindustry.logic.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;

/** Similar to {@link PowerTurret} but doesn't need power. */
public class FreeTurret extends Turret{
    public BulletType shootType;

    public FreeTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
    }

    public class FreeTurretBuild extends TurretBuild{
        @Override
        public void updateTile(){
            unit.ammo(unit.type().ammoCapacity);

            super.updateTile();
        }

        @Override
        public double sense(LAccess sensor){
            return switch(sensor){
                case ammo, ammoCapacity -> 1;
                default -> super.sense(sensor);
            };
        }

        @Override
        public BulletType useAmmo(){
            //nothing used directly
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
