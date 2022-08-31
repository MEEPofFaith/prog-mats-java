package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import progressed.content.effects.*;

public class BoostModule extends BaseModule{
    public float healPercentSec = 2f;
    public float boostAmount = 0.25f;
    public float effectChance = 0.003f;

    public Effect regenEffect = Fx.regenParticle,
        overdriveEffect = ModuleFx.overdriveParticle;

    public BoostModule(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        healPercentSec /= 60 * 100; //0-100/sec -> 0-1/tick
    }

    public class BoostModuleBuild extends BaseModuleBuild{
        @Override
        public void moduleUpdate(){
            if(efficiency > 0){
                if(healPercentSec > 0 && parent.damaged()){
                    parent.healFract(healPercentSec * edelta());
                    parent.recentlyHealed();
                    parentEffect(regenEffect);
                }
                if(boostAmount > 0){
                    parent.applyBoost(1 + boostAmount * efficiency, 2);
                    parentEffect(overdriveEffect);
                }
            }
        }

        public void parentEffect(Effect effect){
            if(Mathf.chanceDelta(effectChance * parent.block.size * parent.block.size)){
                effect.at(
                    parent.x + Mathf.range(parent.block.size * Vars.tilesize / 2f - 1f),
                    parent.y + Mathf.range(parent.block.size * Vars.tilesize / 2f - 1f)
                );
            }
        }
    }
}
