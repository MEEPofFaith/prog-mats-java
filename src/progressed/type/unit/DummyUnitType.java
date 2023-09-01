package progressed.type.unit;

import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.ai.*;

public class DummyUnitType extends UnitType{
    public DummyUnitType(String name){
        super(name);

        controller = u -> new EmptyAI();
        envEnabled = Env.any;
        envDisabled = 0;
        isEnemy = false;
        allowedInPayloads = false;
        logicControllable = false;
        playerControllable = false;
        hidden = true;
        hoverable = false;
        canBoost = true;
        useUnitCap = false;
        killable = false;
    }
    
    @Override
    public void setStats(){
        super.setStats();
        
        stats.remove(Stat.health);
        stats.remove(Stat.armor);
        stats.remove(Stat.itemCapacity);
        stats.remove(Stat.speed);
        stats.remove(Stat.range);
    }

    @Override
    public void drawBody(Unit unit){
        applyColor(unit);

        Drawf.spinSprite(region, unit.x, unit.y, unit.rotation - 90);

        Draw.reset();
    }
}
