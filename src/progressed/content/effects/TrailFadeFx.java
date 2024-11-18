package progressed.content.effects;

import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.entities.bullet.explosive.RocketBulletType.*;
import progressed.graphics.perspective.*;
import progressed.graphics.trails.*;

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
        trail.draw(e.color, e.rotation);
        trail.drawCap(e.color, e.rotation);
    }),

    rocketTrailFade = new Effect(440f, e -> {
        if(!(e.data instanceof RocketTrailData data)) return;
        z(data.layer);
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = data.trail.length * 1.4f;

        if(!state.isPaused()){
            data.trail.shorten();
        }
        data.trail.draw(e.color, e.rotation);
        data.trail.drawCap(e.color, e.rotation);
    }),

    driftTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof DriftTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
            trail.drift();
        }
        trail.draw(e.color, e.rotation);
        trail.drawCap(e.color, e.rotation);
    }),

    zTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof ZTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
        }
        int col = e.color.rgba8888();
        float size = e.rotation;
        Draw3D.highBloom(() -> {
            Tmp.c1.rgba8888(col);
            trail.draw(Tmp.c1, size);
            trail.drawCap(Tmp.c1, size);
        });
    }).layer(Layer.flyingUnit + 1.9f);
}
