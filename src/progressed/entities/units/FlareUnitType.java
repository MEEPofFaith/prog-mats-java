package progressed.entities.units;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.ai.*;
import progressed.content.effects.*;
import progressed.entities.units.entity.*;
import progressed.util.*;
import progressed.world.meta.*;

public class FlareUnitType extends UnitType{
    public Effect flareEffect = OtherFx.flare;
    public float flareX, flareY;
    public float flareEffectChance = 0.5f, flareEffectSize = 1f;
    public float duration, growSpeed = 0.05f;
    public float shadowSize = -1f;
    public float attraction;

    public FlareUnitType(String name, float duration){
        super(name);
        this.duration = duration;
        constructor = FlareUnitEntity::new;
        aiController = EmptyAI::new;

        drag = 1f;
        speed = accel = 0f;
        isEnemy = false;
        canDrown = false;
        flying = false;
        fallSpeed = 1f / 30f;
        hitSize = 1f;
    }

    public FlareUnitType(String name){
        this(name, 300f);
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);

        if(shadowSize < 0f) shadowSize = hitSize * 2f;
    }

    @Override
    public void update(Unit unit){
        FlareUnitEntity flare = (FlareUnitEntity)unit;
        flare.duration -= Time.delta;
        flare.clampDuration();

        flare.height = Mathf.lerpDelta(flare.height, 1f, growSpeed);

        if(Mathf.chanceDelta(flareEffectChance) && flareEffect != Fx.none && !(unit.dead || unit.health < 0f)){
            flareEffect.at(
                unit.x + flareX,
                unit.y + flareY * flare.height,
                flareEffectSize,
                unit.team.color
            );
        }
    }

    @Override
    public void drawShadow(Unit unit){
        // don't draw
    }

    @Override
    public void drawSoftShadow(Unit unit){
        FlareUnitEntity flare = (FlareUnitEntity)unit;

        Draw.color(0, 0, 0, 0.4f * flare.animation);
        float rad = 1.6f;
        float size = shadowSize * Draw.scl * 16f;
        Draw.rect(softShadowRegion, unit, size * rad * flare.height, size * rad * flare.height);
        Draw.color();
    }

    @Override
    public void drawOutline(Unit unit){
        Draw.reset();

        if(Core.atlas.isFound(outlineRegion)){
            FlareUnitEntity flare = (FlareUnitEntity)unit;

            Draw.alpha(flare.animation);
            Draw.rect(outlineRegion, unit.x, unit.y, outlineRegion.width / 4f, outlineRegion.height / 4f * flare.height, unit.rotation - 90f);
            Draw.reset();
        }
    }

    @Override
    public void drawBody(Unit unit){
        applyColor(unit);
        FlareUnitEntity flare = (FlareUnitEntity)unit;

        Draw.alpha(flare.animation);
        Draw.rect(region, unit.x, unit.y, region.width / 4f, region.height / 4f * flare.height, unit.rotation - 90f);
        Draw.reset();
    }

    @Override
    public void drawCell(Unit unit){
        applyColor(unit);
        FlareUnitEntity flare = (FlareUnitEntity)unit;

        Draw.color(cellColor(unit));
        Draw.rect(cellRegion, unit.x, unit.y, cellRegion.width / 4f, cellRegion.height / 4f * flare.height, unit.rotation - 90);
        Draw.reset();
    }

    @Override
    public Color cellColor(Unit unit){
        return super.cellColor(unit).mul(1f, 1f, 1f, ((FlareUnitEntity)unit).animation);
    }

    @Override
    public Unit create(Team team){
        Unit unit = super.create(team);
        unit.health = health;
        unit.maxHealth = attraction;
        unit.rotation = 90f;
        ((FlareUnitEntity)unit).duration = duration;
        return unit;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.flying);
        stats.remove(Stat.speed);
        stats.remove(Stat.canBoost);
        stats.remove(Stat.itemCapacity);
        stats.remove(Stat.range);

        stats.remove(Stat.health);
        stats.add(Stat.health, PMStatValues.signalFlareHealth(health, attraction, duration));
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

            FlareUnitEntity flare = ((FlareUnitEntity)unit);
            bars.add(new Bar(
                () -> Core.bundle.format("bar.pm-lifetime", PMUtls.stringsFixed(flare.durationf() * 100f)),
                () -> Pal.accent,
                flare::durationf
            ));
            bars.row();
        }).growX();
    }
    
    @Override
    public boolean isHidden(){
        return true;
    }
}
