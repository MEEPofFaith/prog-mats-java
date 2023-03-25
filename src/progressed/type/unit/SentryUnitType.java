package progressed.type.unit;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
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
    public float startElevation = 0.5f, riseSpeed = -1f;
    public float anchorDrag = 0.15f;
    public float pullScale = 0.01f, anchorPullScale = 0.003f;
    public boolean anchorOver = true;

    public TextureRegion anchorRegion, anchorOutlineRegion, anchorCellRegion;
    public TextureRegion tetherRegion, tetherEndRegion;

    public SentryUnitType(String name){
        super(name);
        constructor = SentryUnit::new;
        aiController = SentryAI::new;
        
        speed = accel = 0f;
        drag = 0.1f;
        flying = true;
        isEnemy = false;
        useUnitCap = false;
        targetable = vulnerableWithPayloads = hittable = false;
        itemCapacity = 10;
        health = 200;
        engineSize = -1;

        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public void init(){
        super.init();

        if(riseSpeed < 0) riseSpeed = (1f - startElevation) * 0.125f;
    }

    @Override
    public void load(){
        super.load();

        anchorRegion = Core.atlas.find(name + "-anchor");
        anchorOutlineRegion = Core.atlas.find(name + "-anchor-outline");
        anchorCellRegion = Core.atlas.find(name + "-anchor-cell");

        tetherRegion = Core.atlas.find(name + "-tether", "prog-mats-sentry-tether");
        tetherEndRegion = Core.atlas.find(name + "-tether-end", "prog-mats-sentry-tether-end");
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        super.getRegionsToOutline(out);
        out.add(anchorRegion);
    }

    @Override
    public void drawOutline(Unit unit){
        super.drawOutline(unit);

        SentryUnit sentry = (SentryUnit)unit;
        float z = Draw.z();
        Draw.z(z + (anchorOver ? 0.5f : -0.5f));
        if(anchorOver){
            Draw.color(unit.team.color);
            Drawf.laser(tetherRegion, tetherEndRegion, sentry.anchorX, sentry.anchorY, unit.x, unit.y);
            Draw.color();
        }
        Drawf.spinSprite(anchorOutlineRegion, sentry.anchorX, sentry.anchorY, sentry.anchorRot);
        Draw.z(z);
    }

    @Override
    public void drawBody(Unit unit){
        super.drawBody(unit);

        SentryUnit sentry = (SentryUnit)unit;
        float z = Draw.z();
        Draw.z(z + (anchorOver ? 0.5f : -0.5f));
        Drawf.spinSprite(anchorRegion, sentry.anchorX, sentry.anchorY, sentry.anchorRot);
        if(!anchorOver){
            Draw.color(unit.team.color);
            Drawf.laser(tetherRegion, tetherEndRegion, sentry.anchorX, sentry.anchorY, unit.x, unit.y);
            Draw.color();
        }
        Draw.z(z);
    }

    @Override
    public void drawCell(Unit unit){
        super.drawCell(unit);

        SentryUnit sentry = (SentryUnit)unit;
        float z = Draw.z();
        Draw.z(z + (anchorOver ? 0.5f : -0.5f));
        applyColor(unit);
        Draw.color(cellColor(unit));
        Drawf.spinSprite(anchorCellRegion, sentry.anchorX, sentry.anchorY, sentry.anchorRot);
        Draw.reset();
        Draw.z(z);
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
        unit.elevation = startElevation;
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

    public static class AnchorEngine extends UnitEngine{
        public AnchorEngine(float x, float y, float radius, float rotation){
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.rotation = rotation;
        }

        public AnchorEngine(){
        }

        @Override
        public void draw(Unit unit){
            SentryUnit sentry = (SentryUnit)unit;
            SentryUnitType type = (SentryUnitType)unit.type;
            float scale = type.useEngineElevation ? unit.elevation : 1f;

            if(scale <= 0.0001f) return;

            float rot = sentry.anchorRot - 90;
            Color color = type.engineColor == null ? unit.team.color : type.engineColor;

            Tmp.v1.set(x, y).rotate(rot);
            float ex = Tmp.v1.x, ey = Tmp.v1.y;
            float rad = (radius + Mathf.absin(Time.time, 2f, radius / 4f)) * scale;

            float z = Draw.z();
            Draw.z(z + (type.anchorOver ? 0.5f : -0.5f));
            Draw.color(color);
            Fill.circle(
                sentry.anchorX + ex,
                sentry.anchorY + ey,
                rad
            );
            Draw.color(type.engineColorInner);
            Fill.circle(
                sentry.anchorX + ex - Angles.trnsx(rot + rotation, rad / 4f),
                sentry.anchorY + ey - Angles.trnsy(rot + rotation, rad / 4f),
                rad / 2f
            );
            Draw.z(z);
        }
    }
}
