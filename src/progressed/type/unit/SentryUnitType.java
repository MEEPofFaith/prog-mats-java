package progressed.type.unit;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.unit.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.ai.*;
import progressed.entities.units.*;
import progressed.util.*;
import progressed.world.meta.*;

public class SentryUnitType extends ErekirUnitType{
    public float duration = 600f, riseSpeed = 0.125f;

    public SentryUnitType(String name){
        super(name);
        constructor = SentryUnit::new;
        aiController = SentryAI::new;
        
        speed = accel = 0f;
        drag = 0.12f;
        flying = true;
        isEnemy = false;
        useUnitCap = false;
        targetable = hittable = false;
        itemCapacity = 10;
        health = 200;
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public Color cellColor(Unit unit){
        float f = Mathf.clamp(((SentryUnit)unit).durationf());
        return Tmp.c1.set(Color.black).lerp(unit.team.color, f + Mathf.absin(Time.time, 2.5f, 1f - f));
    }

    @Override
    public void update(Unit unit){
        if(unit.elevation < 1 && !unit.dead && unit.health > 0) unit.elevation = Mathf.clamp(unit.elevation + riseSpeed * Time.delta);

        if(unit.health < Float.POSITIVE_INFINITY){ //I want Testing Utilities invincibility to work.
            SentryUnit sentry = ((SentryUnit)unit);
            sentry.duration -= Time.delta;
            sentry.clampDuration();
            if(sentry.duration <= 0f){
                sentry.kill();
            }
        }

        super.update(unit);
    }

    @Override
    public Unit create(Team team){
        Unit unit = super.create(team);
        unit.elevation = 0;
        unit.health = unit.maxHealth;
        ((SentryUnit)unit).duration = duration;
        return unit;
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.add(PMStat.sentryLifetime, (int)(duration / 60f), StatUnit.seconds);

        stats.remove(Stat.speed);
        stats.remove(Stat.itemCapacity);
    }

    @Override
    public void display(Unit unit, Table table){
        table.table(t -> {
            t.left();
            t.add(new Image(fullIcon)).size(8 * 4).scaling(Scaling.fit);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        }).growX().left();
        table.row();

        table.table(bars -> {
            bars.defaults().growX().height(20f).pad(4);

            bars.add(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
            bars.row();
            
            SentryUnit sentry = ((SentryUnit)unit);
            bars.add(new Bar(
                () -> Core.bundle.format("bar.pm-lifetime", PMUtls.stringsFixed(sentry.durationf() * 100f)),
                () -> Pal.accent,
                sentry::durationf
            ));
            bars.row();

            for(Ability ability : unit.abilities){
                ability.displayBars(unit, bars);
            }
        }).growX();

        if(unit.controller() instanceof LogicAI){
            table.row();
            table.add(Blocks.microProcessor.emoji() + " " + Core.bundle.get("units.processorcontrol")).growX().wrap().left();
            table.row();
            table.label(() -> Iconc.settings + " " + (long)unit.flag + "").color(Color.lightGray).growX().wrap().left();
        }
        
        table.row();
    }
}
