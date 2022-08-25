package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.util.*;

public class BoostModule extends BaseModule{
    public float healPercent = 5f / 60f; //Per second

    public BoostModule(String name){
        super(name);
    }

    public class BoostModuleBuild extends BaseModuleBuild{
        @Override
        public void moduleUpdate(){
            if(parent.damaged()){
                parent.heal(healPercent * Time.delta * parent.maxHealth());
                parent.recentlyHealed();
            }
        }
    }
}
