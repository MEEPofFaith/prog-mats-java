package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;

public class ModuleFx{
    public static Effect

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

    abyssSwirl = EnergyFx.makeSwirlEffect(Color.black, 30f, 5, 1.5f, 0f, 0f, 12f, 40f, false).layer(Layer.effect + 1.005f),

    abyssGrow = new Effect(45f, e -> {
        float rad = 2f + e.fin(Interp.pow2Out) * 3f;
        Fill.light(e.x, e.y, Lines.circleVertices(rad), rad, Color.black, Color.darkGray);
    }).layer(Layer.effect + 1.006f),

    abyssBurst = new Effect(30f, e -> {
        float rad = 5f + e.fin(Interp.pow3Out) * 16f;
        Fill.light(e.x, e.y, Lines.circleVertices(rad), rad, Color.clear, Tmp.c1.set(Color.black).lerp(Color.clear, e.fin(Interp.pow3In)));
    }).layer(Layer.effect + 1.007f);
}
