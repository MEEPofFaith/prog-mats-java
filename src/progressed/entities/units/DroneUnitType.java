package progressed.entities.units;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.ai.*;
import progressed.entities.units.entity.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class DroneUnitType extends UnitType{
    public float powerUse = 1f, powerCapacity = 1000f;
    public float checkMultiplier = 0.875f;
    public float riseSpeed = 0.025f;
    public float engineSpread;

    public TextureRegion container, liquid, tankBase, tankTop;

    public DroneUnitType(String name){
        super(name);
        constructor = DroneUnitEntity::new;
        defaultController = DroneAI::new;

        flying = true;
        isCounted = false;  
        speed = 3f;
    }

    @Override
    public void init(){
        super.init();
        EntityMapping.nameMap.put(name, constructor);
    }

    @Override
    public void load(){
        super.load();

        container = Core.atlas.find("prog-mats-item-cargo-full");
        tankBase = Core.atlas.find("prog-mats-liquid-cargo-bottom");
        tankTop = Core.atlas.find("prog-mats-liquid-cargo-top");
        liquid = Core.atlas.find("prog-mats-liquid-cargo-liquid");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.commandLimit);
        stats.remove(Stat.payloadCapacity);
        stats.remove(Stat.itemCapacity);
        stats.remove(Stat.range);

        stats.add(Stat.powerUse, powerUse * 60f, StatUnit.powerSecond);
        stats.add(Stat.powerCapacity, powerCapacity, StatUnit.none);
    }

    @Override
    public void update(Unit unit){
        super.update(unit);

        if(unit.elevation < 1 && !unit.dead && unit.health > 0) unit.elevation = Mathf.clamp(unit.elevation + riseSpeed * Time.delta);
    }

    @Override
    public Unit create(Team team){
        Unit unit = super.create(team);
        unit.elevation = 0;
        return unit;
    }

    @Override
    public void drawEngine(Unit unit){
        if(!unit.isFlying()) return;

        float scale = unit.elevation;
        float offset = engineOffset / 2f + engineOffset / 2f * scale;

        for(int i : Mathf.zeroOne){
            if(unit instanceof DroneUnitEntity d){
                PMTrail t = i == 0 ? d.tleft : d.tright;
                t.draw(unit.team.color, (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * scale) * trailScl);
            }

            int side = Mathf.signs[i];
            float sideOffset =  engineSpread * side;

            Draw.color(unit.team.color);
            Fill.circle(
                unit.x + Angles.trnsx(unit.rotation + 90, sideOffset, offset),
                unit.y + Angles.trnsy(unit.rotation + 90, sideOffset, offset),
                (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f)) * scale
            );
            Draw.color(Color.white);
            Fill.circle(
                unit.x + Angles.trnsx(unit.rotation + 90, sideOffset, offset - 1f),
                unit.y + Angles.trnsy(unit.rotation + 90, sideOffset, offset - 1f),
                (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f)) / 2f * scale
            );
            Draw.color();
        }
    }

    public void display(Unit unit, Table table){
        if(unit instanceof DroneUnitEntity d){
            table.table(t -> {
                t.left();
                t.add(new Image(uiIcon)).size(iconMed).scaling(Scaling.fit);
                t.labelWrap(localizedName).left().width(190f).padLeft(5);
            }).growX().left();
            table.row();

            table.table(bars -> {
                bars.defaults().growX().height(20f).pad(4);
                bars.add(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
                bars.row();
                bars.add(new Bar("pm-drone-charge", Pal.powerBar, d::chargef));
            }).growX();

            table.row();
        }
    }

    @Override
    public <T extends Unit & Payloadc> void drawPayload(T unit){
        if(unit instanceof DroneUnitEntity d && d.cargo.drawCargo){
            if(d.cargo.hasItems()){
                Drawf.shadow(d.x, d.y, 2f * tilesize * 2f, 1f);
                Draw.rect(container, d.x, d.y);
            }
            if(d.cargo.hasLiquid()){
                LiquidStack liquidCargo = d.cargo.liquidCargo;
                Drawf.shadow(d.x, d.y, 2f * tilesize * 2f, 1f);
                Draw.rect(tankBase, d.x, d.y);
                Drawf.liquid(liquid, d.x, d.y, liquidCargo.amount / d.cargo.liquidCapacity, liquidCargo.liquid.color);
                Draw.rect(tankTop, d.x, d.y);
            }
        }

        super.drawPayload(unit);
    }

    @Override
    public void drawLight(Unit unit){
        super.drawLight(unit);

        if(unit instanceof DroneUnitEntity d && d.cargo.hasLiquid()){ //Can't figure out why this doesn't draw
            LiquidStack cargo = d.cargo.liquidCargo;
            if(cargo.amount > 0.01f){
                Color color = cargo.liquid.lightColor;
                float fract = 1f;
                float opacity = color.a * fract;
                if(opacity > 0.001f){
                    Drawf.light(d.team, d.x, d.y, 2f * 30f * fract, color, opacity); //I've Log.info'd, the code is reaching this point but just not drawing the light
                }
            }
        }
    }

    @Override
    public boolean logicVisible(){
        return false;
    }

    @Override
    public boolean isHidden(){
        return true;
    }
}
