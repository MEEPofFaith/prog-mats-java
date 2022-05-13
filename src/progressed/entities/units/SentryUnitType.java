package progressed.entities.units;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
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
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.ai.*;
import progressed.entities.units.entity.*;
import progressed.util.*;

public class SentryUnitType extends UnitType{
    public float duration = 600f, riseSpeed = 0.125f;

    public SentryUnitType(String name){
        super(name);
        constructor = SentryUnitEntity::new;
        aiController = SentryAI::new;
        
        speed = accel = 0f;
        drag = 0.12f;
        flying = lowAltitude = true;
        isEnemy = false;
        useUnitCap = false;
        itemCapacity = 10;
        health = 200;
        engineSize = -1f;
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public void update(Unit unit){
        if(unit.elevation < 1 && !unit.dead && unit.health > 0) unit.elevation = Mathf.clamp(unit.elevation + riseSpeed * Time.delta);

        if(unit.health < Float.POSITIVE_INFINITY){ //I want my invincibility to work.
            SentryUnitEntity sentry = ((SentryUnitEntity)unit);
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
        ((SentryUnitEntity)unit).duration = duration;
        return unit;
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.add(Stat.health, Core.bundle.format("stat.pm-sentry-lifetime", (int)(duration / 60f)));

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
            
            SentryUnitEntity sentry = ((SentryUnitEntity)unit);
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
