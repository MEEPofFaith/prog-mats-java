package progressed.content.effects;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.renders.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;
import static mindustry.graphics.Drawf.*;
import static progressed.util.PMUtls.*;

public class MissileFx{
    public static Effect

    shootSmokeDownpour = new Effect(70f, e -> {
        rand.setSeed(e.id);
        for(int i = 0; i < 25; i++){
            Tmp.v1.trns(e.rotation + 180f + rand.range(30f), rand.random(e.finpow() * 40f));
            e.scaled(e.lifetime * rand.random(0.3f, 1f), b -> {
                color(e.color, Pal.lightishGray, b.fin());
                Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, b.fout() * 3.4f + 0.3f);
            });
        }
    }),

    rocketTrailSmoke = new Effect(180f, 300f, b -> {
        float intensity = 2f;

        color(b.color, 0.7f);
        for(int i = 0; i < 4; i++){
            rand.setSeed(b.id * 2 + i);
            float lenScl = rand.random(0.5f, 1f);
            int fi = i;
            b.scaled(b.lifetime * lenScl, e -> {
                randLenVectors(e.id + fi - 1, e.fin(Interp.pow10Out), (int)(2.9f * intensity), 28f * intensity, e.rotation - 180, 15, (x, y, in, out) -> {
                    float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((1f + intensity) * 1.65f);

                    Fill.circle(e.x + x, e.y + y, rad);
                    Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                });
            });
        }
    }).layer(Layer.bullet - 1f),

    flameRing = new Effect(45f, e -> {
        if(Fire.regions[0] == null){
            for(int i = 0; i < Fire.frames; i++){
                Fire.regions[i] = Core.atlas.find("fire" + i);
            }
        }

        float amount = 40;
        float fin = e.fin(Interp.pow5Out);
        Draw.alpha(e.fout(Interp.pow3Out));
        for(int i = 0; i < amount; i++){
            int frame = (int)Mathf.mod(e.time * ((float)Fire.duration / Fire.frames) + Mathf.randomSeed(e.id + i * 2L, Fire.frames), Fire.frames);
            v1.trns(i * (360f / amount) + Mathf.randomSeedRange(e.id, 180f), 88f * fin);
            Draw.rect(Fire.regions[frame], e.x + v1.x, e.y + v1.y);
        }
        Draw.color();
    }),

    incendBurning = new Effect(35f, e -> {
        color(Pal.lightPyraFlame, Pal.darkPyraFlame, e.fin());

        randLenVectors(e.id, 5, 2f + e.fin() * 12f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.1f + e.fout() * 1.9f);
        });
    }),

    smallBoom = new Effect(30f, e -> {
        color(Pal.missileYellow);

        e.scaled(7, s -> {
            stroke(1.5f * s.fout());
            Lines.circle(e.x, e.y, 2f + s.fin() * 15f);
        });

        color(Color.darkGray);
        alpha(0.9f);
        randLenVectors(e.id, 13, 1f + 27f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.5f + e.fout() * 7f);
        });

        stroke(e.fout() * 0.75f, Pal.missileYellowBack);
        randLenVectors(e.id + 1, 6, 0.5f + 32f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, angle(x, y), 0.5f + e.fout() * 2f);
        });
    }).layer(Layer.bullet - 0.021f),

    missileExplosion = new Effect(30, 500f, e -> {
        float intensity = 2f;
        float baseLifetime = 25f + intensity * 15f;
        e.lifetime = 50f + intensity * 64f;

        color(Color.darkGray);
        alpha(0.9f);
        for(int i = 0; i < 5; i++){
            rand.setSeed(e.id * 2L + i);
            float lenScl = rand.random(0.25f, 1f);
            int fi = i;
            e.scaled(e.lifetime * lenScl, s -> {
                randLenVectors(s.id + fi - 1, s.fin(Interp.pow10Out), (int)(2.8f * intensity), 25f * intensity, (x, y, in, out) -> {
                    float fout = s.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((2f + intensity) * 2.35f);
                    Fill.circle(s.x + x, s.y + y, rad);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            color(Color.gray);
            s.scaled(3 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(s.x, s.y, (3f + i.fin() * 14f) * intensity);
                light(s.x, s.y, i.fin() * 28f * 2f * intensity, Color.white, 0.9f * i.fout());
            });

            color(Pal.lighterOrange, Pal.lightOrange, Pal.redSpark, s.fin());
            stroke((2f * s.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngleCenter(s.x + x, s.y + y, angle(x, y), 1f + out * 4 * (4f + intensity));
                light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, Draw.getColor(), 0.8f);
            });
        });
    }).layer(Layer.bullet - 0.021f),

    nuclearExplosion = new Effect(30, 500f, e -> {
        float intensity = 8f;
        float baseLifetime = 25f + intensity * 15f;
        e.lifetime = 50f + intensity * 64f;

        color(Color.darkGray);
        alpha(0.9f);
        for(int i = 0; i < 5; i++){
            rand.setSeed(e.id * 2L + i);
            float lenScl = rand.random(0.25f, 1f);
            int fi = i;
            e.scaled(e.lifetime * lenScl, s -> {
                randLenVectors(s.id + fi - 1, s.fin(Interp.pow10Out), (int)(2.8f * intensity), 25f * intensity, (x, y, in, out) -> {
                    float fout = s.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((2f + intensity) * 2.35f);
                    Fill.circle(s.x + x, s.y + y, rad);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            //TODO probably won't keep these
            PMRenderer.dimGlow(e.x, e.y, Interp.pow10Out.apply(Mathf.clamp(e.fin() * 2)) * 25f * intensity, 0.9f * Interp.sineOut.apply(Mathf.clamp(s.fout() * 3)) * 0.5f);
            PMRenderer.dimAlpha(Interp.sineOut.apply(Mathf.clamp(s.fslope() * 3/2)) * 0.5f);

            color(Color.gray);
            s.scaled(5 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(s.x, s.y, (3f + i.fin() * 14f) * intensity);
                light(s.x, s.y, i.fin() * 28f * 2f * intensity, getColor(), 0.9f * i.fout());
            });

            color(Pal.lighterOrange, Pal.lightOrange, Pal.redSpark, s.fin());
            stroke((2f * s.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngleCenter(s.x + x, s.y + y, angle(x, y), 1f + out * 4 * (4f + intensity));
                light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
            });
        });
    }).layer(Layer.bullet - 0.021f),

    hitEmpSpark = new Effect(40, e -> {
        color(e.color);
        stroke(e.fout() * 1.2f);

        randLenVectors(e.id, 11, e.finpow() * 18f, e.rotation, 360f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });
    });
}
