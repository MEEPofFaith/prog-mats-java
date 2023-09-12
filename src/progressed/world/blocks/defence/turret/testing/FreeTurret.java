package progressed.world.blocks.defence.turret.testing;

import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.world.meta.*;

import static mindustry.type.ItemStack.*;

public class FreeTurret extends Turret{
    public BulletType shootType = Bullets.placeholder;

    public FreeTurret(String name){
        super(name);

        requirements(Category.turret, OS.username.equals("MEEP") ? BuildVisibility.sandboxOnly : BuildVisibility.hidden, with());
        size = 2;
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
            super.updateTile();
            unit.ammo(1);
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
