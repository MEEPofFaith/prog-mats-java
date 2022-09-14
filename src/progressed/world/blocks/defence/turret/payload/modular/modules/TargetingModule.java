package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.blocks.*;

public class TargetingModule extends BaseModule{
    public boolean fastRetarget = false, logicControl = true, playerControl = true;
    public float range = 80f;

    public TargetingModule(String name){
        super(name);
    }

    public class TargetingModuleBuild extends BaseModuleBuild implements ControlBlock, Ranged{
        public Vec2 targetPos = new Vec2();
        public BlockUnitc unit;

        public void findTarget(){
            //overriden by subclasses!
        }

        public boolean fastRetarget(){
            return fastRetarget;
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public Unit unit(){
            return (Unit)(unit = parent != null ? (BlockUnitc)parent.unit() : null);
        }


    }
}
