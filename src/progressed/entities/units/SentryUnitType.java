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
    public int engines = 4;
    public float engineRotOffset = 45f, duration = 600f, riseSpeed = 0.125f;

    public SentryUnitType(String name){
        super(name);
        constructor = SentryUnitEntity::new;
        defaultController = SentryAI::new;
        
        speed = accel = 0f;
        drag = 0.12f;
        flying = lowAltitude = true;
        onTitleScreen = false;
        engineOffset = 6f;
        engineSize = 2f;
        isCounted = false;
        itemCapacity = 10;
        health = 200;
        commandLimit = 2;
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public void drawEngine(Unit unit){
        if(!unit.isFlying()) return;

        float scl = unit.elevation;
        float offset = engineOffset * scl;

        Draw.color(unit.team.color);
        for(int i = 0; i < engines; i++){
            float a = unit.rotation + engineRotOffset + (i * 360f / engines);
            Fill.circle(
                unit.x + Angles.trnsx(a, offset),
                unit.y + Angles.trnsy(a, offset),
                getEngineSize(unit, a) * scl
            );
        }

        Draw.color(Color.white);
        for(int i = 0; i < engines; i++){
            float a = unit.rotation + engineRotOffset + (i * 360f / engines);
            Fill.circle(
                unit.x + Angles.trnsx(a, offset - 1f),
                unit.y + Angles.trnsy(a, offset - 1f),
                getEngineSize(unit, a) / 2f * scl
            );
        }

        Draw.reset();
    }

    public float getEngineSize(Unit unit, float angle){
        float min = 0f, max = 3f;
        float amount = Mathf.curve(unit.vel.len(), 0.01f, 16f);
        float multiplier = (max - ((max - 1f) - amount * (max - 1f))) - (Mathf.curve(Angles.angleDist(angle, unit.vel.angle()), 0f, 180f) * (max - min) * amount);
        return (engineSize + Mathf.absin(2, engineSize / 4f)) * multiplier;
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

            addBar(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
            bars.row();
            
            SentryUnitEntity sentry = ((SentryUnitEntity)unit);
            addBar(new Bar(
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
