package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.bullet.explosive.RocketBulletType.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.util.Tmp.*;
import static progressed.util.PMUtls.*;

public class UtilFx{
    public static Effect

    PMTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof PMTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        trail.shorten();
        trail.drawCap(e.color, e.rotation);
        trail.draw(e.color, e.rotation);
    }),

    rocketTrailFade = new Effect(440f, e -> {
        if(!(e.data instanceof RocketTrailData data)) return;
        z(data.layer);

        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = data.trail.length * 1.4f;

        data.trail.shorten();
        data.trail.drawCap(e.color, e.rotation);
        data.trail.draw(e.color, e.rotation);
    }),

    dronePowerKill = new Effect(80f, e -> {
        color(Color.scarlet);
        alpha(e.fout(Interp.pow4Out));

        float size = 10f + e.fout(Interp.pow10In) * 25f;
        Draw.rect(Icon.power.getRegion(), e.x, e.y, size, size);
    }),

    groundCrack = new Effect(20f, 500f, e -> {
        if(!(e.data instanceof LightningData d)) return;
        float tx = d.pos.getX(), ty = d.pos.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
        v1.set(d.pos).sub(e.x, e.y).nor();

        float normx = v1.x, normy = v1.y;
        float range = 6f;
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
                float len = (i + 1) * spacing;
                v1.setToRandomDirection(rand).scl(range/2f);
                nx = e.x + normx * len + v1.x;
                ny = e.y + normy * len + v1.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
    }).followParent(false).layer(Layer.debris - 0.01f),

    lightning = new Effect(10f, 500f, e -> {
        if(!(e.data instanceof LightningData d)) return;
        float tx = d.pos.getX(), ty = d.pos.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
        v1.set(d.pos).sub(e.x, e.y).nor();

        float normx = v1.x, normy = v1.y;
        float range = 6f;
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
                float len = (i + 1) * spacing;
                v1.setToRandomDirection(rand).scl(range/2f);
                nx = e.x + normx * len + v1.x;
                ny = e.y + normy * len + v1.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
    }).followParent(false).layer(Layer.bullet + 0.01f),

    //[length, width, team]
    lightningFast = new Effect(5f, 500f, e -> {
        if(!(e.data instanceof LightningData d)) return;
        float tx = d.pos.getX(), ty = d.pos.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
        v1.set(d.pos).sub(e.x, e.y).nor();

        float normx = v1.x, normy = v1.y;
        float range = 6f;
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
                float len = (i + 1) * spacing;
                v1.setToRandomDirection(rand).scl(range/2f);
                nx = e.x + normx * len + v1.x;
                ny = e.y + normy * len + v1.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
    }).followParent(false).layer(Layer.bullet + 0.01f);

    public static class LightningData{
        public Position pos;
        public float stroke;

        public LightningData(Position pos, float stroke){
            this.pos = pos;
            this.stroke = stroke;
        }
    }
}