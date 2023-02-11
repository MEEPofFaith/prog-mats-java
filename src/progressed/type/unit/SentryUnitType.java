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
import progressed.util.*;
import progressed.world.meta.*;

public class SentryUnitType extends ErekirUnitType{
    public float riseSpeed = 0.125f;

    public SentryUnitType(String name){
        super(name);
        constructor = TimedKillUnit::create;
        aiController = SentryAI::new;
        
        speed = accel = 0f;
        drag = 0.12f;
        flying = true;
        isEnemy = false;
        useUnitCap = false;
        targetable = hittable = false;
        itemCapacity = 10;
        health = 200;

        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public Color cellColor(Unit unit){
        float f = ((TimedKillUnit)unit).fout(), min = 0.5f;
        return super.cellColor(unit).mul(min + (1 - min) * f);
    }

    @Override
    public void update(Unit unit){
        if(unit.elevation < 1 && !unit.dead && unit.health > 0) unit.elevation = Mathf.clamp(unit.elevation + riseSpeed * Time.delta);

        if(unit.health == Float.POSITIVE_INFINITY){ //I want Testing Utilities invincibility to work.
            ((TimedKillUnit)unit).time = 0;
        }

        super.update(unit);
    }

    @Override
    public Unit create(Team team){
        Unit unit = super.create(team);
        unit.elevation = 0;
        unit.health = unit.maxHealth;
        //((SentryUnit)unit).duration = duration;
        return unit;
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.add(PMStat.sentryLifetime, (int)(lifetime / 60f), StatUnit.seconds);

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

            TimedKillUnit sentry = ((TimedKillUnit)unit);
            bars.add(new Bar(
                () -> Core.bundle.format("bar.pm-lifetime", PMUtls.stringsFixed(sentry.fout() * 100f)),
                () -> Pal.accent,
                sentry::fout
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
