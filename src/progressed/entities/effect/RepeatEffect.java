package progressed.entities.effect;

import arc.graphics.g2d.*;
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
    public void render(EffectContainer e){
        var cont = e.inner();
        float life = e.time;
        for(int i = 0; i < times; i++){
            float del = i * interval;
            if(life > del && life <= del + effect.lifetime){
                cont.set(e.id + i, e.color, life - del, effect.lifetime, e.rotation, e.x, e.y, e.data);
                Draw.z(effect.layer);
                effect.render(cont);
            }
        }
    }
}
