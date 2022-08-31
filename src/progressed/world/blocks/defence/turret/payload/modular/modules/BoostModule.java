package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import progressed.content.effects.*;

public class BoostModule extends BaseModule{
    public float healPercentSec = 2f;
    public float boostAmount = 0.25f;
    public float regenEffectChance = 0.003f, overdriveEffectChance = 0.001f;

    public Effect regenEffect = Fx.regenParticle,
        overdriveEffect = ModuleFx.overdriveParticle;

    public BoostModule(String name){
        super(name);
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        healPercentSec /= 60 * 100; //0-100/sec -> 0-1/tick
    }

    public class BoostModuleBuild extends BaseModuleBuild{
        @Override
        public void moduleUpdate(){
            super.moduleUpdate();
            if(efficiency > 0){
                if(healPercentSec > 0 && parent.damaged()){
                    parent.healFract(healPercentSec * edelta());
                    parent.recentlyHealed();
                    if(Mathf.chanceDelta(regenEffectChance * parent.block.size * parent.block.size)) parentEffect(regenEffect);
                }
                if(boostAmount > 0){
                    parent.applyBoost(1 + boostAmount * efficiency, 2 * Time.delta);
                    if(Mathf.chanceDelta(overdriveEffectChance * parent.block.size * parent.block.size)) parentEffect(overdriveEffect);
                }
            }
        }

        public void parentEffect(Effect effect){
            effect.at(
                parent.x + Mathf.range(parent.block.size * Vars.tilesize / 2f - 1f),
                parent.y + Mathf.range(parent.block.size * Vars.tilesize / 2f - 1f)
            );
        }
    }
}
