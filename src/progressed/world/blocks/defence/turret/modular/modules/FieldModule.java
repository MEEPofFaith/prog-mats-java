package progressed.world.blocks.defence.turret.modular.modules;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

import static mindustry.Vars.*;

public class FieldModule extends ChargeModule{
    public boolean drawRotate = true, teamColor = true;
    public Color fieldColor = Pal.accent;
    public float radius = 32f * tilesize, arc = 30f, rotateSpeed = 2f;

    public FieldModule(String name, ModuleSize size){
        super(name, size);
        reload = 1f;
    }

    public FieldModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void init(){
        clipSize = Math.max(clipSize, radius * 2f);

        super.init();
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.range, radius / tilesize, StatUnit.blocks);
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        ChargeMount m = (ChargeMount)mount;

        if(m.target != null){
            m.rotation = Angles.moveToward(m.rotation, m.angleTo(m.target), rotateSpeed * efficiency(parent) * parent.delta());
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount mount){
        float rot = drawRotate ? mount.rotation - 90f : 0f;

        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.blockBuildCenter(mount.x, mount.y, region, drawRotate ? mount.rotation - 90f : 0f, mount.progress / deployTime));
            return;
        }

        ChargeMount m = (ChargeMount)mount;

        Drawf.shadow(region, m.x - elevation, m.y - elevation, rot);
        applyColor(parent, m);
        Draw.rect(region, m.x, m.y, rot);

        if(topRegion.found()){

            Draw.color(topColor);
            Draw.alpha(m.heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.rect(topRegion, m.x, m.y, rot);
            Draw.color();
            Draw.mixcol();
        }

        if(m.smoothEfficiency > 0.001f){
            rot = mount.rotation;
            Draw.z(Layer.shields - 0.99f);
            Draw.color(teamColor ? parent.team.color : fieldColor, 0.5f * m.smoothEfficiency);

            PMDrawf.arcFill(mount.x, mount.y, radius - Lines.getStroke() / 2f, arc / 360f, rot - arc / 2f);
            /* Later version of arc
            Fill.arc(mount.x, mount.y, radius - Lines.getStroke() / 2f, arc / 360f, m.rotation - arc / 2f);
             */

            Draw.alpha(m.smoothEfficiency);
            Lines.stroke(2f * m.smoothEfficiency);
            PMDrawf.arcLine(mount.x, mount.y, radius - Lines.getStroke() / 2f, arc, rot);
            /* Does not exist in v135, but does in later versions (Lines.swirl renamed,however Lines.swirl does not work properly.)
            Lines.arc(mount.x, mount.y, radius - Lines.getStroke() / 2f, arc / 360f, m.rotation - arc / 2f);
             */
            for(int sign : Mathf.signs){
                tr.trns(rot + arc / 2f * sign, radius);
                Lines.line(mount.x, mount.y, mount.x + tr.x, mount.y + tr.y, false);
            }
        }
    }

    @Override
    public void drawHighlight(ModularTurretBuild parent, BaseMount mount){
        Drawf.dashCircle(mount.x, mount.y, radius, parent.team.color);
    }
}
