package progressed.entities.comp;

import arc.graphics.*;
import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import progressed.gen.entities.*;

@EntityComponent(base = true)
@EntityDef(value = {TurretParentEffectStatec.class, BuildChildc.class}, pooled = true, serialize = false)
abstract class TurretParentEffectStateComp implements Posc, Drawc, Timedc, Rotc, BuildChildc{
    @Import float time, lifetime, rotation, x, y;
    @Import int id;

    Color color = new Color(Color.white);
    Effect effect;
    Object data;

    @Override
    public void draw(){
        lifetime = effect.render(id, color, time, lifetime, rotation, x, y, data);
    }

    @Replace
    public float clipSize(){
        return effect.clip;
    }
}
