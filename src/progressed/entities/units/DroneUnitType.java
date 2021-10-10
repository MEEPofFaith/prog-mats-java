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
import progressed.ai.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

public class DroneUnitType extends UnitType{
    public float powerUse = 2f, chargeCapacity = 600f;
    public float riseSpeed = 0.025f;

    public TextureRegion container, liquid, tankBase, tankTop;

    public DroneUnitType(String name){
        super(name);
        constructor = DroneUnitEntity::new;
        defaultController = DroneAI::new;

        flying = lowAltitude = true;
        speed = 3f;
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

    public void display(Unit unit, Table table){
        if(unit instanceof DroneUnitEntity s){
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
                bars.add(new Bar("pm-drone-charge", Pal.powerBar, s::chargef));
            }).growX();

            table.row();
        }
    }

    @Override
    public <T extends Unit & Payloadc> void drawPayload(T unit){
        if(unit instanceof DroneUnitEntity s){
            if(s.cargo.hasItems()){
                Draw.rect(container, unit.x, unit.y);
            }
            if(s.cargo.hasLiquid()){
                Draw.rect(tankBase, unit.x, unit.y);
                Drawf.liquid(liquid, unit.x, unit.y, s.cargo.liquidCargo.amount / s.cargo.liquidCapacity, s.cargo.liquidCargo.liquid.color);
                Draw.rect(tankTop, unit.x, unit.y);
            }
        }

        super.drawPayload(unit);
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
