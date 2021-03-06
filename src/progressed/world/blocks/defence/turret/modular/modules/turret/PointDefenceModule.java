package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class PointDefenceModule extends ReloadTurretModule{
    public Color color = Color.white;
    public Effect beamEffect = ModuleFx.piercePointBeam;
    public Effect hitEffect = Fx.pointHit;
    public Effect shootEffect = Fx.sparkShoot;

    public Sound shootSound = Sounds.lasershoot;

    public float bulletDamage = 10f;
    public float shootY = 3f;
    public int pierceCap;

    public PointDefenceModule(String name, ModuleSize size){
        super(name, size);
        fastRetarget = true;
        logicControl = playerControl = false;
    }

    public PointDefenceModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
        if(pierceCap > 0) stats.add(Stat.damage, Core.bundle.format("stat.pm-point-defence-pierce", pierceCap));
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);
        if(!isDeployed(mount)) return;

        BaseTurretMount m = (BaseTurretMount)mount;

        //pooled bullets
        if(m.target != null && !m.target.isAdded()){
            m.target = null;
            findTarget(parent, m);
        }else{
            //Look at target
            if(m.target instanceof Bullet b && b.within(m, range) && b.team != parent.team && b.type() != null && b.type().hittable){
                float dest = m.angleTo(b);
                m.rotation = Angles.moveToward(m.rotation, dest, rotateSpeed * edelta(parent));
                m.reloadCounter += edelta(parent);

                updateCooling(parent, m);

                //Shoot when possible
                if(Angles.within(m.rotation, dest, b.hitSize) && m.reloadCounter >= reload){
                    Tmp.v1.trns(m.rotation, shootY);
                    float len = PMDamage.bulletCollideLine(
                        m.x + Tmp.v1.x, m.y + Tmp.v1.y,
                        m.rotation, range - shootY, parent.team,
                        bulletDamage, pierceCap, hitEffect, color);

                    beamEffect.at(m.x + Tmp.v1.x, m.y + Tmp.v1.y, m.rotation, color, len);
                    shootEffect.at(m.x + Tmp.v1.x, m.y + Tmp.v1.y, m.rotation, color);
                    shootSound.at(m.x + Tmp.v1.x, m.y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));
                    m.reloadCounter = 0;
                    m.curRecoil = 1f;
                }
            }
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount m){
        BaseTurretMount mount = (BaseTurretMount)m;
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation - 90f;

        if(mount.progress < deployTime){
            drawDeploy(parent, m);
            return;
        }

        shootOffset.trns(rot + 90f, -mount.curRecoil);

        Drawf.shadow(region, x + shootOffset.x - elevation, y + shootOffset.y - elevation, rot);
        applyColor(parent, mount);
        Draw.rect(region, x + shootOffset.x, y + shootOffset.y, rot);

        if(heatRegion.found() && mount.heat > 0.001f){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + shootOffset.x, y + shootOffset.y, rot);
            Draw.blend();
            Draw.color();
        }

        if(liquidRegion.found()){
            Drawf.liquid(liquidRegion, x + shootOffset.x, y + shootOffset.y, mount.liquids.currentAmount() / liquidCapacity, mount.liquids.current().color, rot);
        }

        if(topRegion.found()){
            Draw.z(Layer.turret + topLayerOffset);
            Draw.rect(topRegion, x + shootOffset.x, y + shootOffset.y, rot);
        }
        Draw.mixcol();
    }

    @Override
    public void findTarget(ModularTurretBuild parent, BaseTurretMount m){
        m.target = Groups.bullet.intersect(m.x - range, m.y - range, range * 2, range * 2)
            .min(b -> b.team != parent.team && b.type().hittable && !b.within(m, Mathf.maxZero(shootY - 3f)), b -> b.dst2(m));
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        return super.isActive(parent, mount) && ((BaseTurretMount)mount).target != null;
    }
}
