package progressed.content.effects;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.world.blocks.defence.*;
import progressed.world.blocks.defence.ShieldProjector.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;
import static progressed.graphics.DrawPseudo3D.*;

public class Pseudo3DFx{
    public static Effect

    absorbedSmall = new Effect(38f, e -> {
        color(Pal.missileYellow);
        float z = e.rotation;
        float zScl = 1f + hMul(z);

        e.scaled(13f, s -> {
            stroke(2f * s.fout() * zScl);

            randLenVectors(e.id, 8, (2f + 34f * s.fin()) * zScl, (x, y) -> {
                lineAngle(xHeight(e.x + x, z), yHeight(e.y + y, z), angle(x, y), (2f + s.fout() * 8f) * zScl);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke((0.5f * out + e.fout()) * zScl);

        Lines.circle(xHeight(e.x, z), yHeight(e.y, z), (2f * out + 13f * in * out) * zScl);
    }),

    absorbed = new Effect(52f, e -> {
        color(Pal.missileYellow);
        float z = e.rotation;
        float zScl = 1f + hMul(z);

        e.scaled(24f, s -> {
            stroke(3f * s.fout() * zScl);

            randLenVectors(e.id, 14, (2f + 53f * s.fin()) * zScl, (x, y) -> {
                lineAngle(xHeight(e.x + x, z), yHeight(e.y + y, z), angle(x, y), (2f + s.fout() * 13f) * zScl);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke((out + 2f * e.fout()) * zScl);

        Lines.circle(xHeight(e.x, z), yHeight(e.y, z), (6f * out + 31f * in * out) * zScl);
    }),

    absorbedLarge = new Effect(74f, e -> {
        color(Pal.missileYellow);
        float z = e.rotation;
        float zScl = 1f + hMul(z);

        e.scaled(32f, s -> {
            stroke(5f * s.fout() * zScl);

            randLenVectors(e.id, 20, (4f + 114f * s.fin() * zScl), (x, y) -> {
                lineAngle(xHeight(e.x + x, z), yHeight(e.y + y, z), angle(x, y), (3f + s.fout() * 18f) * zScl);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke((2f * out + 3f * e.fout()) * zScl);

        Lines.circle(xHeight(e.x, z), yHeight(e.y, z), (6f * out + 57f * in * out) * zScl);
    }),

    shieldRecharge = new Effect(20f, e -> {
        if(!(e.data instanceof ShieldBuild build) || !build.isAdded()) return;
        color(e.color, e.fout());
        stroke(1f + e.fout());
        float finpow = e.finpow();
        float[] corners = build.getShieldCorners(build.realRadius() * finpow);
        build.drawCorners(corners, build.realHeight() * finpow, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
            Lines.line(x1, y1, x2, y2);
            Lines.line(x1, y1, x4, y4);
            Lines.line(x2, y2, x3, y3);
            Lines.line(x4, y4, x3, y3);
        });
    }).layer(Layer.shields),

    shieldBreak = new Effect(40f, e -> {
        if(!(e.data instanceof ShieldSizeData data)) return;
        stroke(3f * e.fout(), e.color);
        float[] corners = ShieldProjector.getShieldCorners(e.x, e.y, e.rotation, data.sides, data.rotation);
        ShieldProjector.drawCorners(corners, data.height, data.sides, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
            Lines.line(x1, y1, x2, y2);
            Lines.line(x1, y1, x4, y4);
            Lines.line(x2, y2, x3, y3);
            Lines.line(x4, y4, x3, y3);
        });
    }),

    shieldShrink = new Effect(20f, e -> {
        if(!(e.data instanceof ShieldSizeData data)) return;
        float radius = e.rotation * e.fout();
        if(radius > 0.001f){
            Draw.color(e.color);
            float[] corners = ShieldProjector.getShieldCorners(e.x, e.y, e.rotation, data.sides, data.rotation);

            if(renderer.animateShields){
                Draw.z(Layer.shields + 0.001f);
            }else{
                Draw.z(Layer.shields);
                Lines.stroke(1.5f);
                Draw.alpha(0.09f + Mathf.clamp(0.08f));
            }
            Fill.polyBegin();
            ShieldProjector.drawCorners(corners, data.height, data.sides, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
                Fill.quad(x1, y1, x2, y2, x3, y3, x4, y4);
                Fill.polyPoint(x4, y4);
            });
            Fill.polyEnd();

            if(renderer.animateShields){
                Draw.z(Layer.shields + 1.01f);
            }else{
                Draw.z(Layer.shields + 0.001f);
            }
            Draw.alpha(1f);
            ShieldProjector.drawCorners(corners, data.height, data.sides, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
                //Lines.quad(x1, y1, x2, y2, x3, y3, x4, y4); //Corners expand out wildly for some reason.
                Lines.line(x1, y1, x2, y2);
                Lines.line(x1, y1, x4, y4);
                Lines.line(x2, y2, x3, y3);
                Lines.line(x4, y4, x3, y3);
            });
            Draw.color();
        }
    });

    public static class ShieldSizeData{
        public int sides;
        public float height;
        public float rotation;

        public ShieldSizeData(int sides, float height, float rotation){
            this.sides = sides;
            this.height = height;
            this.rotation = rotation;
        }
    }
}
