package progressed.world.blocks.defence.turret.payload.modular.modules.turret;

import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.*;

import static mindustry.Vars.*;

public class TargetingModule extends BaseModule{
    public boolean fastRetarget = false, logicControl = true, playerControl = true;
    public float range = 80f;
    public float rotateSpeed = 5;

    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** If not null, this consumer will be used for coolant. */
    public ConsumeLiquidBase coolant;

    public TargetingModule(String name){
        super(name);
    }

    @Override
    public void init(){
        if(coolant == null){
            coolant = findConsumer(c -> c instanceof ConsumeCoolant);
        }

        //just makes things a little more convenient
        if(coolant != null){
            //TODO coolant fix
            coolant.update = false;
            coolant.booster = true;
            coolant.optional = true;
        }

        fogRadius = Math.max(Mathf.round(range / tilesize), fogRadius);
        super.init();
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
    }

    public class TargetingModuleBuild extends BaseModuleBuild implements Ranged{
        public float rotation = 90;

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

        public float estimateDps(){
            return 0f;
        }

        @Override
        public void drawModuleSelect(){
            Drawf.dashCircle(x, y, range(), team.color);
        }
    }
}
