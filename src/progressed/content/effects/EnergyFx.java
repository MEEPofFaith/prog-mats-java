package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.graphics.renders.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.line;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;
import static mindustry.graphics.Drawf.*;

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

    kugelblitzCharge = new Effect(80f, e -> {
        float in = 6f,
            out = 160f;
        PMRenderer.blackHole(e.x, e.y, in * e.fin(), (in + (out - in) * 0.33f) * e.fin(), e.color);
    }),

    blackHoleSwirl = makeSwirlEffect(90f, 8, 3f, 90f, 720f, true).layer(Layer.effect + 0.005f),

    blackHoleDespawn = new Effect(24f, e -> {
        color(Color.darkGray, Color.black, e.fin());

        e.scaled(12f, s -> {
            stroke(2f * e.fout());
            Lines.circle(e.x, e.y, s.fin() * 10f);
        });

        stroke(2f * e.fout());
        randLenVectors(e.id, 4, e.fin() * 15f, (x, y) -> {
            float ang = angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });

        color(e.color);
        randLenVectors(e.id * 2L, 4, e.fin() * 15f, (x, y) -> {
            float ang = angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });
    }).layer(Layer.effect + 0.005f),

    blackHoleAbsorb = new Effect(20f, e -> {
        color(Color.black);
        stroke(2f * e.fout(Interp.pow3In));
        Lines.circle(e.x, e.y, 8f * e.fout(Interp.pow3In));
    }),

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
    }),

    harbingerCharge = new Effect(150f, 1600f, e -> {
        Color[] colors = {Color.valueOf("D99F6B55"), Color.valueOf("E8D174aa"), Color.valueOf("F3E979"), Color.valueOf("ffffff")};
        float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
        float[] strokes = {2f, 1.5f, 1, 0.3f};
        float[] lenscales = {1, 1.12f, 1.15f, 1.17f};

        float lightOpacity = 0.4f + (e.finpow() * 0.4f);

        Draw.color(colors[0], colors[2], 0.5f + e.finpow() * 0.5f);
        Lines.stroke(Mathf.lerp(0f, 28f, e.finpow()));
        Lines.circle(e.x, e.y, 384f * (1f - e.finpow()));

        for(int i = 0; i < 36; i++){
            v1.trns(i * 10f, 384f * (1 - e.finpow()));
            v2.trns(i * 10f + 10f, 384f * (1f - e.finpow()));
            light(e.x + v1.x, e.y + v1.y, e.x + v2.x, e.y + v2.y, 14f / 2f + 60f * e.finpow(), Draw.getColor(), lightOpacity + (0.2f * e.finpow()));
        }

        float fade = 1f - Mathf.curve(e.time, e.lifetime - 30f, e.lifetime);
        float grow = Mathf.curve(e.time, 0f, e.lifetime - 30f);

        for(int i = 0; i < 4; i++){
            float baseLen = (900f + (Mathf.absin(Time.time / ((i + 1f) * 2f) + Mathf.randomSeed(e.id), 0.8f, 1.5f) * (900f / 1.5f))) * 0.75f * fade;
            Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time / 3f + Mathf.randomSeed(e.id), 1.0f, 0.3f) / 3f));
            for(int j = 0; j < 2; j++){
                int dir = Mathf.signs[j];
                for(int k = 0; k < 10; k++){
                    float side = k * (360f / 10f);
                    for(int l = 0; l < 4; l++){
                        Lines.stroke((16f * 0.75f + Mathf.absin(Time.time, 0.5f, 1f)) * grow * strokes[i] * tscales[l]);
                        Lines.lineAngle(e.x, e.y, (e.rotation + 360f * e.finpow() + side) * dir, baseLen * lenscales[l], false);
                    }

                    v1.trns((e.rotation + 360f * e.finpow() + side) * dir, baseLen * 1.1f);

                    light(e.x, e.y, e.x + v1.x, e.y + v1.y, ((16f * 0.75f + Mathf.absin(Time.time, 0.5f, 1f)) * grow * strokes[i] * tscales[j]) / 2f + 60f * e.finpow(), colors[2], lightOpacity);
                }
            }
            Draw.reset();
        }
    }),

    //[circle radius, distance]
    everythingGunSwirl = new Effect(120f, 1600f, e -> {
        float[] data = (float[])e.data;
        v1.trns(Mathf.randomSeed(e.id, 360f) + e.rotation * e.fin(), (16f + data[1]) * e.fin());
        color(e.color, Color.black, 0.25f + e.fin() * 0.75f);
        Fill.circle(e.x + v1.x, e.y + v1.y, data[0] * e.fout());
    }).layer(Layer.bullet - 0.00999f);

    public static Effect makeSwirlEffect(Color color, float eLifetime, int length, float maxWidth, float minRot, float maxRot, float minDst, float maxDst, boolean lerp){
        return new Effect(eLifetime, 400f, e -> {
            if(e.time < 1f) return;

            float lifetime = e.lifetime - length;
            float dst;
            if(minDst < 0 || maxDst < 0){
                dst = Math.abs(e.rotation);;
            }else{
                dst = Mathf.randomSeed(e.id, minDst, maxDst);
            }
            if(lerp){
                color(color, e.color, Mathf.clamp(e.time / lifetime));
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
                v1.trns(baseRot + addRot * Mathf.sqrt(fout), Mathf.maxZero(dst * fout));
                fout = 1f - Mathf.clamp((e.time - points + i + 1) / lifetime);
                v2.trns(baseRot + addRot * Mathf.sqrt(fout), Mathf.maxZero(dst * fout));

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

                lastAng = a2;
            }
            Draw.rect("hcircle", e.x + v2.x, e.y + v2.y, width * 2f, width * 2f, -Mathf.radDeg * lastAng);
        });
    }

    public static Effect makeSwirlEffect(float eLifetime, int length, float maxWidth, float minRot, float maxRot, boolean lerp){
        return makeSwirlEffect(Color.black, eLifetime, length, maxWidth, minRot, maxRot, -1, -1, lerp);
    }
}
