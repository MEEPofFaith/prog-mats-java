package progressed.entities.effect;

import arc.graphics.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;

public class WrapDataEffect extends WrapEffect{
    public Object data;
    public boolean allowColor = true;
    public boolean allowRotation = true;

    public WrapDataEffect(Effect effect, Object data){
        super();
        this.effect = effect;
        this.data = data;
    }

    public WrapDataEffect(Effect effect, Color color, Object data){
        super(effect, color);
        this.data = data;
    }

    public WrapDataEffect(Effect effect, Color color, float rotation, Object data){
        super(effect, color, rotation);
        this.data = data;
    }

    @Override
    public void create(float x, float y, float rotation, Color color, Object data){
        effect.create(x, y, allowRotation ? rotation : this.rotation, allowColor ? color : this.color, this.data);
    }
}
