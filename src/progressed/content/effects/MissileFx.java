package progressed.content.effects;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;
import static mindustry.graphics.Drawf.*;
import static progressed.graphics.Draw3D.*;
import static progressed.util.PMUtls.*;

public class MissileFx{
    //From FireComp
    static final int frames = 40, ticksPerFrame = 90 / frames;
    static final TextureRegion[] fireFrames = new TextureRegion[frames];

    public static Effect

    rocketTrail = new Effect(60, e -> {
        if(e.data instanceof float[] f){
            z(f[2]);
            color(e.color, Pal.lightishGray, Pal.darkerGray, e.fin());
            randLenVectors(e.id, 4, 24f * e.finpow() * f[1], f[0] + 180f, 15f * f[1], (x, y) -> {
                Fill.circle(e.x + x, e.y + y, (e.rotation - e.fin() * e.rotation) / 2f);
            });
        }
    }).layer(Layer.turret + 0.014f),

    flameRing = new Effect(45f, e -> {
        if(fireFrames[0] == null){
            for(int i = 0; i < frames; i++){
                fireFrames[i] = Core.atlas.find("fire" + i);
            }
        }

        float amount = 40;
        float fin = e.fin(Interp.pow5Out);
        Draw.alpha(e.fout(Interp.pow3Out));
        for(int i = 0; i < amount; i++){
            int frame = (int)Mathf.mod(e.time * ticksPerFrame + Mathf.randomSeed(e.id + i * 2L, frames), frames);
            v1.trns(i * (360f / amount) + Mathf.randomSeedRange(e.id, 180f), 88f * fin);
            Draw.rect(fireFrames[frame], e.x + v1.x, e.y + v1.y);
        }
        Draw.color();
    }),

    incendBurning = new Effect(35f, e -> {
        color(Pal.lightPyraFlame, Pal.darkPyraFlame, e.fin());

        randLenVectors(e.id, 5, 2f + e.fin() * 12f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.1f + e.fout() * 1.9f);
        });
    }),

    missileSmoke = new Effect(120, e -> {
        if(e.data instanceof float[] data){
            float x = e.x + cameraXOffset(e.x, data[0]),
                y = e.y + cameraYOffset(e.y, data[0]);
            color(Color.gray, Mathf.clamp(e.fout() * 1.2f) * data[1]);
            Fill.circle(x, y, (1f + 10f * e.rotation) - e.fin() * 2f);
        }
    }).layer(Layer.effect + 0.21f),

    smallBoom = new Effect(30f, e -> {
        color(Pal.missileYellow);

        e.scaled(7, s -> {
            stroke(1.5f * s.fout());
            Lines.circle(e.x, e.y, 2f + s.fin() * 15f);
        });

        color(Color.gray);

        randLenVectors(e.id, 8, 1f + 15f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.25f + e.fout() * 2f);
        });

        color(Pal.missileYellowBack);
        stroke(e.fout() * 0.5f);

        randLenVectors(e.id + 1, 6, 0.5f + 14.5f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, angle(x, y), 0.5f + e.fout() * 2f);
        });
    }),

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
    }),

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
    }),

    missileBlockedSmall = new Effect(38f, e -> {
        color(Pal.missileYellow);

        e.scaled(13f, s -> {
            stroke(2f * s.fout());

            randLenVectors(e.id, 8, 2f + 34f * s.fin(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, angle(x, y), 2f + s.fout() * 8f);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke(0.5f * out + e.fout());

        Lines.circle(e.x, e.y, 2f * out + 13f * in * out);
    }),

    missileBlocked = new Effect(52f, e -> {
        color(Pal.missileYellow);

        e.scaled(24f, s -> {
            stroke(3f * s.fout());

            randLenVectors(e.id, 14, 2f + 53f * s.fin(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, angle(x, y), 2f + s.fout() * 13f);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke(out + 2f * e.fout());

        Lines.circle(e.x, e.y, 6f * out + 31f * in * out);
    }),

    missileBlockedLarge = new Effect(74f, e -> {
        color(Pal.missileYellow);

        e.scaled(32f, s -> {
            stroke(5f * s.fout());

            randLenVectors(e.id, 20, 4f + 114f * s.fin(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, angle(x, y), 3f + s.fout() * 18f);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke(2f * out + 3f * e.fout());

        Lines.circle(e.x, e.y, 6f * out + 57f * in * out);
    });
}
