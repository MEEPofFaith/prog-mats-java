package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.geom.*;
import mindustry.gen.*;

public class BaseTurretModule extends BaseModule{
    public boolean fastRetarget = false, logicControl = true;
    public float range = 80f;

    public BaseTurretModule(String name){
        super(name);
    }

    public class BaseTurretModuleBuild extends BaseModuleBuild{
        public Vec2 targetPos = new Vec2();

        public void targetPosition(Posc p){
            targetPos.set(p);
        }

        public void findTarget(){
            //overriden by subclasses!
        }

        public float range(){
            return range;
        }

        public boolean fastRetarget(){
            return fastRetarget;
        }

        public boolean logicControl(){
            return logicControl;
        }
    }
}
