package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import progressed.content.effects.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

public class BoostModule extends Block{
    public ModuleSize moduleSize = ModuleSize.small;
    public float healPercentSec = 2f;
    public float boostAmount = 0.25f;
    public float regenEffectChance = 0.003f, overdriveEffectChance = 0.001f;

    public Effect regenEffect = Fx.regenParticle,
        overdriveEffect = ModuleFx.overdriveParticle;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    public BoostModule(String name){
        super(name);
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        healPercentSec /= 60 * 100; //0-100/sec -> 0-1/tick
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
    }

    public class BoostModuleBuild extends Building implements TurretModule{
        public ModuleModule module;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        public void moduleUpdate(){
            module.moduleUpdate();
            if(!isDeployed()) return;
            if(efficiency > 0){
                if(healPercentSec > 0 && parent().damaged()){
                    parent().healFract(healPercentSec * edelta());
                    parent().recentlyHealed();
                    if(Mathf.chanceDelta(regenEffectChance * parent().block.size * parent().block.size)) parentEffect(regenEffect);
                }
                if(boostAmount > 0){
                    parent().applyBoost(1 + boostAmount * efficiency, 2 * Time.delta);
                    if(Mathf.chanceDelta(overdriveEffectChance * parent().block.size * parent().block.size)) parentEffect(overdriveEffect);
                }
            }
        }

        public void parentEffect(Effect effect){
            effect.at(
                parent().x + Mathf.range(parent().block.size * Vars.tilesize / 2f - 1f),
                parent().y + Mathf.range(parent().block.size * Vars.tilesize / 2f - 1f)
            );
        }

        @Override
        public boolean acceptModule(TurretModule module){
            return !parent().modules.contains(m -> m.block() == module.block());
        }

        @Override
        public ModuleModule module(){
            return module;
        }

        @Override
        public Building build(){
            return self();
        }

        @Override
        public ModuleSize size(){
            return moduleSize;
        }

        @Override
        public Iterable<Func<Building, Bar>> listModuleBars(){
            return moduleBarMap.values();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            module.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            (module == null ? new ModuleModule(self(), hasPower) : module).read(read);
        }
    }
}
