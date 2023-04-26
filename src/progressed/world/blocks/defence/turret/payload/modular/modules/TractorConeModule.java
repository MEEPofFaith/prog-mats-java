package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.content.blocks.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class TractorConeModule extends BaseTurret{
    public final int timerTarget = timers++;
    public float retargetTime = 5f;

    public float tractorCone = 10f;
    public float shootLength = Float.NEGATIVE_INFINITY;
    public float force = 0.3f;
    public float scaledForce = 0f;
    public boolean ignoreMass = false;
    public float damage = 0f;
    public boolean targetAir = true, targetGround = false;

    public float tractorScl = 1f;
    public float tractorSpacing = 40f;
    public float tractorStroke = 2f;

    public Color tractorColor = Pal.lancerLaser.cpy();
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 300;

    public Sound shootSound = Sounds.tractorbeam;
    public float shootSoundVolume = 0.9f;

    public ModuleSize moduleSize = ModuleSize.small;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    public TractorConeModule(String name){
        super(name);
        coolantMultiplier = 1f;
        update = false;
        destructible = true;
        breakable = rebuildable = false;
        group = BlockGroup.turrets;
        connectedPower = false;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        if(damage > 0) stats.add(Stat.damage, damage * 60f, StatUnit.perSecond);
    }

    @Override
    public void init(){
        if(shootLength == Float.NEGATIVE_INFINITY) shootLength = size * tilesize / 2f;
        updateClipRadius(range + shootLength + tilesize);
        PMModules.setClip(clipSize);
        fogRadius = -1;

        super.init();
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
        removeBar("power");
        removeBar("heat");
    }

    public <T extends Building> void addModuleBar(String name, Func<T, Bar> sup){
        moduleBarMap.put(name, (Func<Building, Bar>)sup);
    }

    public class TractorConeModuleBuild extends BaseTurretBuild implements TurretModule{
        public ModuleModule module;
        public Unit target;
        public float strength, totalProgress;
        public boolean any;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;

            float eff = efficiency * coolantMultiplier, edelta = eff * delta();

            //retarget
            if(timer(timerTarget, retargetTime)){
                target = Units.closestEnemy(team, x, y, range, u -> u.checkTarget(targetAir, targetGround) && !(u.controller() instanceof MissileAI));
            }

            //consume coolant
            if(target != null && coolant != null){
                float maxUsed = coolant.amount;

                Liquid liquid = liquids.current();

                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, (1f / coolantMultiplier) / liquid.heatCapacity));

                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }

                coolantMultiplier = 1f + (used * liquid.heatCapacity * coolantMultiplier);
            }

            any = false;

            //look at target
            if(target != null && target.within(this, range + target.hitSize/2f) && target.team() != team && target.checkTarget(targetAir, targetGround) && efficiency > 0.02f){
                if(!headless){
                    control.sound.loop(shootSound, this, shootSoundVolume);
                }

                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta);
                strength = Mathf.lerpDelta(strength, 1f, 0.1f);

                //affect units in the cone
                Units.nearbyEnemies(team, x, y, range, u -> {
                    //Please do not pull in missiles
                    if(!(u.controller() instanceof MissileAI) && u.checkTarget(targetAir, targetGround) && Angles.within(rotation, angleTo(u), tractorCone)){
                        if(damage > 0){
                            u.damageContinuous(damage * eff);
                        }

                        if(status != StatusEffects.none){
                            u.apply(status, statusDuration);
                        }

                        any = true;

                        if(ignoreMass){
                            //similar to impulseNet but does not factor in mass
                            Tmp.v1.set(this).sub(u).limit((force + (1f - u.dst(this) / range) * scaledForce) * edelta);
                            u.vel.add(Tmp.v1);

                            //manually move units to simulate velocity for remote players
                            if(u.isRemote()) u.move(Tmp.v1);
                        }else{
                            u.impulseNet(Tmp.v1.set(this).sub(u).limit((force + (1f - u.dst(this) / range) * scaledForce) * edelta));
                        }
                    }
                });
            }else{
                strength = Mathf.lerpDelta(strength, 0, 0.1f);
            }

            totalProgress += strength * edelta;
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume() && target != null;
        }

        @Override
        public float estimateDps(){
            if(!any || damage <= 0) return 0f;
            return damage * 60f * efficiency * coolantMultiplier;
        }

        @Override
        public void draw(){
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), rotation - 90);
            Draw.rect(region, x, y, rotation - 90);

            //draw cone if applicable
            if(strength > 0.001f){
                Draw.z(Layer.bullet - 1f);
                float cx = x + Angles.trnsx(rotation, shootLength),
                    cy = y + Angles.trnsy(rotation, shootLength);

                PMDrawf.tractorCone(cx, cy, totalProgress * tractorScl, tractorSpacing, tractorStroke, () -> {
                    Draw.color(Tmp.c1.set(tractorColor).mulA(strength));
                    PMDrawf.arcFill(cx, cy, range, tractorCone / 360f, rotation - tractorCone / 2f);
                });
            }
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;
            super.drawSelect();
        }

        @Override
        public ModuleModule module(){
            return module;
        }

        @Override
        public ModuleSize size(){
            return moduleSize;
        }

        @Override
        public Building build(){
            return self();
        }

        @Override
        public Iterable<Func<Building, Bar>> listModuleBars(){
            return moduleBarMap.values();
        }

        @Override
        public void pickedUp(){
            module.progress = 0f;
            rotation = 90f;
            any = false;
            strength = 0f;
        }

        @Override
        public boolean isValid(){
            return super.isValid() || (parent() != null && parent().isValid());
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
