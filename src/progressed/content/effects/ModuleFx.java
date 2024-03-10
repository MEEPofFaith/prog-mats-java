package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.entities.effect.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;
import static progressed.util.PMUtls.*;

public class ModuleFx{
    public static Effect

    skeetShoot = new Effect(10, e -> {
        color(e.color);
        float w = 1.2f + 7 * e.fout();

        Drawf.tri(e.x, e.y, w, 30f * e.fout(), e.rotation);
        color(e.color);

        for(int i : Mathf.signs){
            Drawf.tri(e.x, e.y, w * 0.9f, 18f * e.fout(), e.rotation + i * 90f);
        }

        Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
    }),

    skeetLine = new Effect(20f, e -> {
        if(!(e.data instanceof Vec2 v)) return;

        color(e.color);
        stroke(e.fout() * 0.9f + 0.6f);

        Fx.rand.setSeed(e.id);
        for(int i = 0; i < 7; i++){
            Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
            Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
        }

        e.scaled(14f, b -> {
            stroke(b.fout() * 1.5f);
            color(e.color);
            Lines.line(e.x, e.y, v.x, v.y);
        });
    }),

    skeetEnd = new Effect(14f, e -> {
        color(e.color);
        Drawf.tri(e.x, e.y, e.fout() * 1.5f, 5f, e.rotation);
    }),

    steamBurst = new Effect(40f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 9; i++){
            Tmp.v1.trns(e.rotation + rand.range(30f), rand.random(e.finpow() * 13.5f * tilesize));
            e.scaled(e.lifetime * rand.random(0.3f, 1f), b -> {
                color(e.color, Pal.lightishGray, b.fin());
                Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, b.fout() * 3.4f + 0.3f);
            });
        }
    }),

    hitSteam = new Effect(14, e -> {
        color(e.color, Pal.lightishGray, e.fin());
        stroke(0.5f + e.fout());

        randLenVectors(e.id, 2, 1f + e.fin() * 15f, e.rotation, 50f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });
    }),

    overdriveParticle = new Effect(100f, e -> {
        color(PMPal.overdrive);

        Fill.square(e.x, e.y, e.fslope() * 1.5f + 0.14f, 45f);
    }),

    abyssBeam = new Effect(10f, 20f * 8f, e -> {
        if(!(e.data instanceof Vec2 v)) return;

        Lines.stroke(3f * e.fout(), Color.black);
        Lines.line(e.x, e.y, v.x, v.y);

        Lines.stroke(1.5f * e.fout(), Color.darkGray);
        Lines.line(e.x, e.y, v.x, v.y);

        int lines = (int)(Mathf.dst(e.x, e.y, v.x, v.y) / 8f);
        float len = 4f;
        float dst = Mathf.dst(e.x, e.y, v.x, v.y);
        float range = dst - len;
        float ang = Angles.angle(e.x, e.y, v.x, v.y);
        Fx.rand.setSeed(e.id);
        for(int i = 0; i < lines; i++){
            float d = Fx.rand.random(0f, range);
            float w = Fx.rand.random(-3f, 3f);
            Vec2 p = Tmp.v1.trns(ang, d, w);
            Lines.lineAngle(e.x + p.x, e.y + p.y, ang, len);
        };
    }).layer(Layer.effect + 1.004f),

    abyssSwirl = new SwirlEffect(30f, Color.black, 5, 1.5f, 0f, 0f, 12f, 40f, false, false).layer(Layer.effect + 1.005f),

    abyssGrow = new Effect(45f, e -> {
        float rad = 2f + e.fin(Interp.pow2Out) * 3f;
        Fill.light(e.x, e.y, Lines.circleVertices(rad), rad, Color.black, Color.darkGray);
    }).layer(Layer.effect + 1.006f),

    abyssBurst = new Effect(30f, e -> {
        float rad = 5f + e.fin(Interp.pow3Out) * 16f;
        Fill.light(e.x, e.y, Lines.circleVertices(rad), rad, Color.clear, Tmp.c1.set(Color.black).lerp(Color.clear, e.fin(Interp.pow3In)));
    }).layer(Layer.effect + 1.007f);
}
