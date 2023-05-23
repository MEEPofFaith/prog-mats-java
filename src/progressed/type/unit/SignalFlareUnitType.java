package progressed.type.unit;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.ai.*;
import progressed.content.effects.*;
import progressed.entities.units.*;
import progressed.util.*;
import progressed.world.meta.*;

public class SignalFlareUnitType extends UnitType{
    public Effect flareEffect = OtherFx.flare;
    public float flareX, flareY;
    public float flareEffectChance = 0.5f, flareEffectSize = 1f;
    public float growSpeed = 0.05f;
    public float shadowSize = -1f;
    public float attraction;

    public SignalFlareUnitType(String name, float lifetime){
        super(name);
        this.lifetime = lifetime;
        constructor = SignalFlareUnit::new;
        aiController = EmptyAI::new;
        hidden = true;

        drag = 1f;
        speed = accel = 0f;
        isEnemy = false;
        canDrown = false;
        flying = false;
        fallSpeed = 1f / 30f;
        hitSize = 1f;
        deathExplosionEffect = null;

        EntityMapping.nameMap.put(this.name, constructor);
    }

    public SignalFlareUnitType(String name){
        this(name, 300f);
    }

    @Override
    public void init(){
        super.init();

        if(shadowSize < 0f) shadowSize = hitSize * 2f;
        if(deathExplosionEffect == null) deathExplosionEffect = OtherFx.flareFallEffect(this);
    }

    @Override
    public void drawShadow(Unit unit){
        // don't draw
    }

    @Override
    public void drawSoftShadow(float x, float y, float rotation, float alpha){
        Draw.color(0, 0, 0, 0.4f * alpha);
        float rad = 1.6f;
        float size = shadowSize * region.scl();
        Draw.rect(softShadowRegion, x, y, size * rad * Draw.xscl, size * rad * Draw.yscl, rotation - 90);
        Draw.color();
    }

    @Override
    public void drawOutline(Unit unit){
        Draw.reset();

        if(Core.atlas.isFound(outlineRegion)){
            SignalFlareUnit flare = (SignalFlareUnit)unit;

            Draw.rect(outlineRegion, unit.x, unit.y, outlineRegion.width / 4f, outlineRegion.height / 4f * flare.height, unit.rotation - 90f);
            Draw.reset();
        }
    }

    @Override
    public void drawBody(Unit unit){
        applyColor(unit);
        SignalFlareUnit flare = (SignalFlareUnit)unit;

        Draw.rect(region, unit.x, unit.y, region.width / 4f, region.height / 4f * flare.height, unit.rotation - 90f);
        Draw.reset();
    }

    @Override
    public void drawCell(Unit unit){
        applyColor(unit);
        SignalFlareUnit flare = (SignalFlareUnit)unit;

        Draw.color(cellColor(unit));
        Draw.rect(cellRegion, unit.x, unit.y, cellRegion.width / 4f, cellRegion.height / 4f * flare.height, unit.rotation - 90);
        Draw.reset();
    }

    @Override
    public Color cellColor(Unit unit){
        float f = ((SignalFlareUnit)unit).fout(), min = 0.5f;
        return super.cellColor(unit).mul(min + (1 - min) * f);
    }

    @Override
    public Unit create(Team team){
        Unit unit = super.create(team);
        unit.health = health;
        unit.maxHealth = attraction;
        unit.rotation = 90f;
        ((SignalFlareUnit)unit).lifetime = lifetime;
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
        stats.add(Stat.health, PMStatValues.signalFlareHealth(health, attraction, lifetime));
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

            SignalFlareUnit flare = ((SignalFlareUnit)unit);
            bars.add(new Bar(
                () -> Core.bundle.format("bar.pm-lifetime", PMUtls.stringsFixed(flare.fout() * 100f)),
                () -> Pal.accent,
                flare::fout
            ));
            bars.row();
        }).growX();
    }
}
