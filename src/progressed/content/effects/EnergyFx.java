package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.util.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;

public class EnergyFx{
    public static Effect

    eruptorBurn = new Effect(30f, e -> {
        color(PMPal.magma);
        randLenVectors(e.id, 6, 64 * e.fin(), e.rotation, 20f, (x, y) -> {
            Lines.lineAngle(e.x + x, e.y + y, angle(x, y), 8f * e.fout());
        });
    }),

    bitTrail = new Effect(75f, e -> {
        float offset = Mathf.randomSeed(e.id);
        Color c = Tmp.c1.set(PMPal.pixelFront).lerp(PMPal.pixelBack, Mathf.absin(Time.time * 0.05f + offset, 1f, 1f));
        color(c);
        Fill.square(e.x, e.y, e.rotation * e.fout());
    }),

    bitBurst = new Effect(30f, e -> {
        float[] set = {Mathf.curve(e.time, 0, e.lifetime * 2/3), Mathf.curve(e.time, e.lifetime * 1/3, e.lifetime)};
        float offset = Mathf.randomSeed(e.id);
        Color c = Tmp.c1.set(PMPal.pixelFront).lerp(PMPal.pixelBack, Mathf.absin(Time.time * 0.05f + offset, 1f, 1f));
        color(c);
        stroke(2.5f);

        for(int i = 0; i < 2; i++){
            if(set[i] > 0 && set[i] < 1){
                for(int j = 0; j < 8; j++){
                    float s = 41 * set[i];
                    float front = Mathf.clamp(s, 0f, 21f - 2f * 3f);
                    float back = Mathf.clamp(s - 3f, 0f, 21f - 2f * 3f);

                    v1.trns(j * 45f, 0f, front);
                    v1.add(e.x, e.y);
                    v2.trns(j * 45f, 0f, back);
                    v2.add(e.x, e.y);

                    line(v1.x, v1.y, v2.x, v2.y);
                }
            }
        }
    }),

    kugelblitzGrow = new Effect(80f, e -> {
        int sides = 20 * 4;
        float space = 360f / sides;
        float fin = e.fin(Interp.circleOut);
        float rX = 20 * fin, rY = 10 * fin;
        float in = Color.whiteFloatBits, out = e.color.toFloatBits();

        for(int i = 0; i < sides; i++){
            float t1 = i * space, t2 = (i + 1) * space;
            Tmp.v1.trns(t1, PMMathf.circleStarPoint(t1)).scl(rX, rY).add(e.x, e.y);
            Tmp.v2.trns(t2, PMMathf.circleStarPoint(t2)).scl(rX, rY).add(e.x, e.y);
            Fill.quad(
                e.x, e.y, in,
                e.x, e.y, in,
                Tmp.v1.x, Tmp.v1.y, out,
                Tmp.v2.x, Tmp.v2.y, out
            );
        }

        Drawf.light(e.x, e.y, rX * 2f, e.color, 0.8f);
    }),

    kugelblitzCharge = makeSwirlEffect(30f, 8, 2f, 15f, 90f, false).layer(Layer.bullet - 0.03f),

    blackHoleSwirl = makeSwirlEffect(90f, 8, 3f, 180f, 720f, true, true).layer(Layer.effect + 0.005f),

    blackHoleDespawn = new Effect(80f, e -> {
        float rad = 24f;
        e.scaled(60f, s -> {
            Lines.stroke(6f * s.fout(), e.color);
            Lines.circle(e.x, e.y, 1.5f * rad * s.fin(Interp.pow3Out));
        });

        Lines.stroke(2f * e.fout(), Color.black);
        Lines.circle(e.x, e.y, rad * e.fin(Interp.pow3Out));
    }).layer(Layer.effect + 0.03f),

    sentinelBlast = new Effect(80f, 150f, e -> {
        color(Pal.missileYellow);

        e.scaled(50f, s -> {
            stroke(5f * s.fout());
            Lines.circle(e.x, e.y, 4f + s.fin() * 40f);
        });

        color(e.color);

        randLenVectors(e.id, 20, 3f + 60f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 1f + e.fout() * 6f);
        });

        color(Pal.missileYellowBack);
        stroke(e.fout());

        randLenVectors(e.id + 1, 11, 2f + 73f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, angle(x, y), 2f + e.fout() * 5f);
        });
    }),

    redBomb = new Effect(40f, 100f, e -> {
        color(Color.red);
        stroke(e.fout() * 2f);
        float circleRad = 4f + e.finpow() * 65f;
        Lines.circle(e.x, e.y, circleRad);

        color(Color.red);
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 6f, 100f * e.fout(), i*90);
        }

        color();
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, 3f, 35f * e.fout(), i*90);
        }

        Drawf.light(e.x, e.y, circleRad * 1.6f, Pal.heal, e.fout());
    });

    public static Effect makeSwirlEffect(Color color, float eLifetime, int length, float maxWidth, float minRot, float maxRot, float minDst, float maxDst, boolean light, boolean lerp){
        return new Effect(eLifetime, 400f, e -> {
            if(e.time < 1f) return;

            float lifetime = e.lifetime - length;
            float dst;
            if(minDst < 0 || maxDst < 0){
                dst = Math.abs(e.rotation);
            }else{
                dst = Mathf.randomSeed(e.id, minDst, maxDst);
            }
            float l = Mathf.clamp(e.time / lifetime);
            if(lerp){
                color(color, e.color, l);
            }else{
                color(color);
            }

            int points = (int)Math.min(e.time, length);
            float width = Mathf.clamp(e.time / (e.lifetime - length)) * maxWidth;
            float size = width / points;
            float baseRot = Mathf.randomSeed(e.id + 1, 360f), addRot = Mathf.randomSeed(e.id + 2, minRot, maxRot) * Mathf.sign(e.rotation);

            float fout, lastAng = 0f;
            for(int i = 0; i < points; i++){
                fout = 1f - Mathf.clamp((e.time - points + i) / lifetime);
                v1.trns(baseRot + addRot * PMMathf.cbrt(fout), Mathf.maxZero(dst * fout));
                fout = 1f - Mathf.clamp((e.time - points + i + 1) / lifetime);
                v2.trns(baseRot + addRot * PMMathf.cbrt(fout), Mathf.maxZero(dst * fout));

                float a2 = -v1.angleTo(v2) * Mathf.degRad;
                float a1 = i == 0 ? a2 : lastAng;

                float
                    cx = Mathf.sin(a1) * i * size,
                    cy = Mathf.cos(a1) * i * size,
                    nx = Mathf.sin(a2) * (i + 1) * size,
                    ny = Mathf.cos(a2) * (i + 1) * size;

                Fill.quad(
                    e.x + v1.x - cx, e.y + v1.y - cy,
                    e.x + v1.x + cx, e.y + v1.y + cy,
                    e.x + v2.x + nx, e.y + v2.y + ny,
                    e.x + v2.x - nx, e.y + v2.y - ny
                );
                if(light){
                    Drawf.light(
                        e.x + v1.x, e.y + v1.y,
                        e.x + v2.x, e.y + v2.y,
                        i * size * 6f, Draw.getColor().cpy(), l
                    );
                }

                lastAng = a2;
            }
            Draw.rect("hcircle", e.x + v2.x, e.y + v2.y, width * 2f, width * 2f, -Mathf.radDeg * lastAng);
        });
    }

    public static Effect makeSwirlEffect(float eLifetime, int length, float maxWidth, float minRot, float maxRot, boolean light, boolean lerp){
        return makeSwirlEffect(Color.black, eLifetime, length, maxWidth, minRot, maxRot, -1, -1, light, lerp);
    }

    public static Effect makeSwirlEffect(float eLifetime, int length, float maxWidth, float minRot, float maxRot, boolean lerp){
        return makeSwirlEffect(Color.black, eLifetime, length, maxWidth, minRot, maxRot, -1, -1, false, lerp);
    }
}
