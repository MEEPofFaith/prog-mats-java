package progressed.entities.effect;

import arc.graphics.*;
import arc.util.*;
import mindustry.entities.*;

public class RepeatEffect extends Effect{
    public Effect effect;
    public float interval;
    public int times;

    public RepeatEffect(Effect effect, float interval, int times){
        this.effect = effect;
        this.interval = interval;
        this.times = times;
    }

    @Override
    public void init(){
        lifetime = interval * times + effect.lifetime;
        clip = effect.clip;
    }

    @Override
    public void create(float x, float y, float rotation, Color color, Object data){
        for(int i = 0; i < times; i++){
            Time.run(i * interval, () -> effect.create(x, y, rotation, color, data));
        }
    }
}
