package progressed.entities.effect;

import arc.graphics.*;
import mindustry.entities.*;
import progressed.graphics.renders.*;

public class FlashEffect extends Effect{
    public Effect effect;
    public float flashDuration;

    public FlashEffect(Effect effect, float flashDuration){
        this.effect = effect;
        this.flashDuration = flashDuration;
    }

    @Override
    public void init(){
        effect.init();
        clip = effect.clip;
        lifetime = effect.lifetime;
    }

    @Override
    public void render(EffectContainer e){
    }

    @Override
    public void create(float x, float y, float rotation, Color color, Object data){
        PMRenders.flash(flashDuration);
        effect.create(x, y, rotation, color, data);
    }
}
