package progressed.entities.units;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import progressed.ai.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.iconMed;
import static mindustry.Vars.state;

public class DroneUnitType extends UnitType{
    public float powerUse = 2f, chargeCapacity = 600f;

    public DroneUnitType(String name){
        super(name);
        constructor = DroneUnitEntity::new;
        defaultController = DroneAI::new;

        flying = true;
        speed = 3f;
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
    public boolean logicVisible(){
        return false;
    }

    @Override
    public boolean isHidden(){
        return true;
    }
}
