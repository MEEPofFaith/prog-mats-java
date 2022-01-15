package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class ForceModule extends BaseModule{
    public float range = 80f;
    public boolean targetGround, targetAir = true;
    public float radius = -1f;
    public float force = 5f;
    public float scaledForce = 3f;
    public int maxTargets = 3;
    public float laserWidth = 0.6f;
    public Color laserColor = Color.white;

    public float damage;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 60f * 10f;

    public TextureRegion laserEnd, laser;

    public ForceModule(String name, ModuleSize size){
        super(name, size);
        mountType = ForceMount::new;
        loopSound = Sounds.tractorbeam;
    }

    public ForceModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void init(){
        super.init();

        if(radius < 0) radius = size() * 2;
    }

    @Override
    public void load(){
        super.load();

        laserEnd = Core.atlas.find(name + "-laser-end", "parallax-laser-end");
        laser = Core.atlas.find(name + "-laser", "parallax-laser");
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.shootRange, range / Vars.tilesize, StatUnit.blocks);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.damage, damage * 60f, StatUnit.perSecond);
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        return super.isActive(parent, mount) && ((ForceMount)mount).targets.any();
    }

    @Override
    public boolean shouldLoopSound(ModularTurretBuild parent, BaseMount mount){
        return isActive(parent, mount);
    }

    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        ForceMount m = (ForceMount)mount;

        if(m.timer.get(5f)){
            m.targets.clear();

            Units.nearbyEnemies(parent.team, mount.x, mount.y, range, u -> {
                if(
                    (targetGround && u.isGrounded() || (targetAir && u.isFlying()))
                    && !u.dead
                    && u.isAdded()
                ) m.targets.add(u);
            });

            m.targets.sort(u -> Mathf.dst2(m.x, m.y, u.x, u.y));
            if(m.targets.size > maxTargets) m.targets.removeRange(maxTargets, m.targets.size - 1);
        }

        if(m.targets.any()){
            for(Unit u : m.targets){
                if(u.within(m.x, m.y, range + u.hitSize / 2f)){
                    if(damage > 0){
                        u.damageContinuous(damage * efficiency(parent));
                    }

                    if(status != StatusEffects.none){
                        u.apply(status, statusDuration);
                    }

                    float ang = Mathf.mod(u.angleTo(m.x, m.y) + 180f, 360f);
                    u.impulseNet(Tmp.v1.trns(ang, radius).add(m.x, m.y).sub(u).limit((force + (1f - u.dst(m.x, m.y) / range) * scaledForce) * efficiency(parent) * parent.delta()));
                }
            }
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount mount){
        super.draw(parent, mount);

        ForceMount m = (ForceMount)mount;

        if(m.targets.any()){
            for(Unit u : m.targets){
                if(!u.isAdded()) continue;

                Draw.z(Layer.bullet);
                float ang = Mathf.mod(u.angleTo(m.x, m.y) + 180f, 360f);
                Draw.mixcol(laserColor, Mathf.absin(44f, 0.6f));

                tr.trns(ang, radius).add(m.x, m.y);

                Drawf.laser(parent.team, laser, laserEnd, laserEnd,
                    tr.x, tr.y, u.x, u.y,
                    efficiency(parent) * laserWidth
                );

                Draw.mixcol();
            }
        }
    }

    public class ForceMount extends BaseMount{
        public Interval timer = new Interval(6);
        public Seq<Unit> targets = new Seq<>();

        public ForceMount(ModularTurretBuild parent, BaseModule module, short moduleNumber, float offsetX, float offsetY){
            super(parent, module, moduleNumber, offsetX, offsetY);
        }
    }
}