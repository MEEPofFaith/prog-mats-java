package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.entities.bullet.explosive.RocketBulletType.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.util.Tmp.*;
import static mindustry.Vars.*;
import static progressed.util.PMUtls.*;

public class UtilFx{
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

    groundCrack = new Effect(20f, 500f, e -> {
        if(!(e.data instanceof LightningData d)) return;
        e.lifetime = e.rotation;
        float tx = d.pos.getX(), ty = d.pos.getY(), dst = d.pos.dst(e.x, e.y);
        v1.set(d.pos).sub(e.x, e.y).nor();

        float normx = v1.x, normy = v1.y;
        float range = d.range;
        int links = Mathf.ceil(dst / range);
        float spacing = dst / links;

        Lines.stroke(d.stroke * Mathf.curve(e.fout(), 0f, 0.5f));
        Draw.color(e.color);

        Lines.beginLine();
        Lines.linePoint(e.x, e.y);

        rand.setSeed(e.id);

        for(int i = 0; i < links * Mathf.curve(e.fin(), 0f, 0.5f); i++){
            float nx, ny;
            if(i == links - 1){
                nx = tx;
                ny = ty;
            }else{
                float len = (i + 1) * spacing,
                    r = range / 2f;
                if(d.shrink) r *= 1f - (float)i / links;
                v1.setToRandomDirection(rand).scl(r);
                nx = e.x + normx * len + v1.x;
                ny = e.y + normy * len + v1.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
    }).followParent(false).layer(Layer.debris - 0.01f),

    lightning = new Effect(10f, 500f, e -> {
        if(!(e.data instanceof LightningData d)) return;
        e.lifetime = e.rotation;
        float tx = d.pos.getX(), ty = d.pos.getY(), dst = d.pos.dst(e.x, e.y);
        v1.set(d.pos).sub(e.x, e.y).nor();

        float normx = v1.x, normy = v1.y;
        float range = d.range;
        int links = Mathf.ceil(dst / range);
        float spacing = dst / links;

        Lines.stroke(d.stroke * e.fout());
        Draw.color(Color.white, e.color, e.fin());

        Lines.beginLine();
        Lines.linePoint(e.x, e.y);

        rand.setSeed(e.id);

        for(int i = 0; i < links; i++){
            float nx, ny;
            if(i == links - 1){
                nx = tx;
                ny = ty;
            }else{
                float len = (i + 1) * spacing,
                    r = range / 2f;
                if(d.shrink) r *= 1f - (i + 1f) / links;
                v1.setToRandomDirection(rand).scl(r);
                nx = e.x + normx * len + v1.x;
                ny = e.y + normy * len + v1.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
    }).followParent(false).layer(Layer.bullet + 0.01f);

    public static class LightningData{
        public Position pos;
        public float stroke, range = 6f;
        public boolean shrink;

        public LightningData(Position pos, float stroke){
            this.pos = pos;
            this.stroke = stroke;
        }

        public LightningData(Position pos, float stroke, boolean shrink, float range){
            this(pos, stroke);
            this.shrink = shrink;
            this.range = range;
        }
    }
}
