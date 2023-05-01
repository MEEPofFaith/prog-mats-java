package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
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
import mindustry.world.meta.*;
import progressed.content.blocks.*;
import progressed.content.effects.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;

public class BoostModule extends Block{
    public ModuleSize moduleSize = ModuleSize.small;
    //per tick
    public float healPercent = 1f / 60f;
    public float speedBoost = 1.25f;
    public float regenEffectChance = 0.003f, overdriveEffectChance = 0.001f;

    public Effect regenEffect = Fx.regenParticle,
        overdriveEffect = ModuleFx.overdriveParticle;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    protected TextureRegion mendTop, overdriveTop;

    public BoostModule(String name){
        super(name);
        solid = true;
        destructible = true;
        breakable = rebuildable = false;
        group = BlockGroup.turrets;

        outlineIcon = true;
        suppressable = true;
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        PMModules.setClip(clipSize);
    }

    @Override
    public void load(){
        super.load();

        mendTop = Core.atlas.find(name + "-mend");
        overdriveTop = Core.atlas.find(name + "-overdrive");
    }

    @Override
    public void setStats(){
        super.setStats();

        if(healPercent > 0) stats.add(Stat.repairTime, (int)(1f / (healPercent / 100f) / 60f), StatUnit.seconds);
        if(speedBoost > 0) stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
        removeBar("power");
    }

    @Override
    public boolean canBreak(Tile tile){
        return state.isEditor() || state.rules.infiniteResources;
    }

    public class BoostModuleBuild extends Building implements TurretModule{
        public ModuleModule module;
        public float mendHeat, overdriveHeat;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;

            if(efficiency > 0){
                if(healPercent > 0){
                    boolean canHeal = !checkSuppression() && parent().damaged();

                    mendHeat = Mathf.lerpDelta(mendHeat, efficiency > 0 && canHeal ? 1f : 0f, 0.08f);

                    if(canHeal){
                        parent().healFract(healPercent * edelta());
                        parent().recentlyHealed();
                        if(Mathf.chanceDelta(regenEffectChance * parent().block.size * parent().block.size)) parentEffect(regenEffect);
                    }
                }
                if(speedBoost > 0){
                    boolean canOverdrive = parent().modules.contains(m -> m.block().canOverdrive && !(m instanceof BoostModuleBuild) && m.isActive());

                    overdriveHeat = Mathf.lerpDelta(overdriveHeat, efficiency > 0 && canOverdrive ? 1f : 0f, 0.08f);

                    if(canOverdrive){
                        parent().applyBoost(1 + speedBoost * efficiency, 2 * Time.delta);
                        if(Mathf.chanceDelta(overdriveEffectChance * parent().block.size * parent().block.size)) parentEffect(overdriveEffect);
                    }
                }
            }
        }

        @Override
        public void moduleDraw(){
            TurretModule.super.moduleDraw();

            if(isDeployed()){
                Draw.alpha(mendHeat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
                Draw.rect(mendTop, x, y);

                Draw.alpha(overdriveHeat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
                Draw.rect(overdriveTop, x, y);
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
            return !(module instanceof BoostModuleBuild);
        }

        @Override
        public void pickedUp(){
            module.progress = 0f;
            mendHeat = overdriveHeat = 0;
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
        public boolean isActive(){
            return TurretModule.super.isActive() && (mendHeat > 0.01f || overdriveHeat > 0.01f);
        }

        @Override
        public Iterable<Func<Building, Bar>> listModuleBars(){
            return moduleBarMap.values();
        }

        @Override
        public boolean isValid(){
            return super.isValid() || (parent() != null && parent().isValid());
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(mendHeat);
            write.f(overdriveHeat);
            module.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            mendHeat = read.f();
            overdriveHeat = read.f();
            (module == null ? new ModuleModule(self(), hasPower) : module).read(read);
        }
    }
}
