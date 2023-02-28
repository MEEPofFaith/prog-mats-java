package progressed.content.effects;

import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.entities.bullet.explosive.RocketBulletType.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static mindustry.Vars.*;

public class TrailFadeFx{
    public static Effect

    PMTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof PMTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
        }
        trail.drawCap(e.color, e.rotation);
        trail.draw(e.color, e.rotation);
    }),

    rocketTrailFade = new Effect(440f, e -> {
        if(!(e.data instanceof RocketTrailData data)) return;
        z(data.layer);
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = data.trail.length * 1.4f;

        if(!state.isPaused()){
            data.trail.shorten();
        }
        data.trail.drawCap(e.color, e.rotation);
        data.trail.draw(e.color, e.rotation);
    }),

    driftTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof DriftTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
            trail.drift();
        }
        trail.drawCap(e.color, e.rotation);
        trail.draw(e.color, e.rotation);
    }),

    heightTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof HeightTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
        }
        trail.drawCap(e.color, e.rotation);
        trail.draw(e.color, e.rotation);
    }).layer(Layer.flyingUnit + 1.9f);
}
