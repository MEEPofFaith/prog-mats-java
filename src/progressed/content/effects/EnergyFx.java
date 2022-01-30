package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.bullet.energy.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.energy.*;
import progressed.world.blocks.defence.turret.energy.AimLaserTurret.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;
import static mindustry.Vars.*;
import static mindustry.graphics.Drawf.*;
import static progressed.util.PMUtls.*;

public class EnergyFx{
    public static Effect

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

    kugelblitzChargeBegin = new Effect(80f, e -> {
        Draw.z(Layer.max - 0.01f);
        Fill.light(e.x, e.y, 60, 6f * e.fin(), Tmp.c1.set(e.color).lerp(Color.black, 0.5f + Mathf.absin(10f, 0.4f)), Color.black);
    }),

    kugelblitzCharge = new Effect(38f, e -> {
        color(Tmp.c1.set(e.color).lerp(Color.black, 0.5f), Color.black, e.fin());
        randLenVectors(e.id, 2, 45f * e.fout(), e.rotation, 180f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fslope() * 5f);
        });
    }),

    blackHoleSwirl = new Effect(90f, e -> {
        Bullet bullet = (Bullet)e.data;

        if(bullet != null && bullet.type instanceof BlackHoleBulletType b){
            float a = Mathf.clamp(e.fin() * 8f);
            Tmp.c1.set(bullet.team.color).lerp(Color.black, 0.5f + Mathf.absin(Time.time + Mathf.randomSeed(e.id), 10f, 0.4f)).a(a);
            Tmp.c2.set(Color.black).a(a);
            float startAngle = Mathf.randomSeed(e.id, 360f, 720f);

            Fill.light(bullet.x + trnsx(e.rotation + startAngle * e.fout(),
                    b.suctionRadius * e.fout()),
                bullet.y + trnsy(e.rotation + startAngle * e.fout(), b.suctionRadius * e.fout()),
                60,
                b.swirlSize * e.fout(),
                Tmp.c1,
                Tmp.c2
            );

            light(bullet.x + trnsx(e.rotation + startAngle * e.fout(),
                    b.suctionRadius * e.fout()),
                bullet.y + trnsy(e.rotation + startAngle * e.fout(),
                    b.suctionRadius * e.fout()),
                b.swirlSize * e.fout(),
                Tmp.c1,
                0.7f * a
            );
        }
    }).layer(Layer.max - 0.02f),

    blackHoleDespawn = new Effect(24f, e -> {
        color(Color.darkGray, Color.black, e.fin());

        e.scaled(12f, s -> {
            stroke(2f * e.fout());
            Lines.circle(e.x, e.y, s.fin() * 10f);
        });

        stroke(2f * e.fout());
        randLenVectors(e.id, 4, e.fin() * 15f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });

        color(e.color);
        randLenVectors(e.id * 2, 4, e.fin() * 15f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f);
        });
    }).layer(Layer.max - 0.03f),

    blackHoleAbsorb = new Effect(20f, e -> {
        color(Color.black);
        stroke(2f * e.fout(Interp.pow3In));
        Lines.circle(e.x, e.y, 8f * e.fout(Interp.pow3In));
    }).layer(Layer.max - 0.04f),

    aimChargeBegin = new Effect(300f, e -> {
        if(e.data instanceof AimLaserTurretBuild d){
            color(e.color);

            v1.trns(d.rotation, ((AimLaserTurret)(d.block)).shootLength);
            Fill.circle(d.x + v1.x, d.y + v1.y, 3f * e.fin());

            color();
        }
    }).layer(Layer.bullet - 0.01f),

    aimCharge = new Effect(30f, e -> {
        if(e.data instanceof AimLaserTurretBuild d){
            color(e.color);

            v1.trns(d.rotation, ((AimLaserTurret)(d.block)).shootLength);
            randLenVectors(e.id, 3, 24f * e.fout(), (x, y) -> {
                Fill.circle(d.x + v1.x + x, d.y + v1.y + y, 2f * e.fin());
            });

            color();
        }
    }).layer(Layer.bullet - 0.01f),

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
            lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + e.fout() * 5f);
        });
    }),

    apotheosisCharge = new Effect(5f * 60f, 500f, e -> {
        color(PMPal.apotheosisLaserDark);
        Fill.circle(e.x, e.y, e.fin() * 26);
        color(PMPal.apotheosisLaser);
        Fill.circle(e.x, e.y, e.fin() * 14);

        stroke(e.fin() * 6f, PMPal.apotheosisLaserDark);
        Lines.circle(e.x, e.y, 2f + 100f * e.fout());
        Lines.circle(e.x, e.y, 5f + 120f * e.fout(Interp.pow3Out));
        Lines.circle(e.x, e.y, 8f + 150f * e.fout(Interp.pow5Out));

        color(PMPal.apotheosisLaser);
        randLenVectors(e.id, 14, 92f * e.fout(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fin() * 8f);
            light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });
        randLenVectors(e.id + 1, 17,116f * e.fout(Interp.pow3Out), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fin() * 8f);
            light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });
        randLenVectors(e.id + 2, 20,146f * e.fout(Interp.pow5Out), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fin() * 8f);
            light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });

        light(e.x, e.y, e.fin() * 36f, PMPal.apotheosisLaserDark, 0.7f);
    }),

    apotheosisChargerBlast = new Effect(30f, e -> {
        stroke(0.5f + 0.5f*e.fout(), PMPal.apotheosisLaser);
        float spread = 3f;

        rand.setSeed(e.id);
        for(int i = 0; i < 20; i++){
            float ang = e.rotation + rand.range(17f);
            v1.trns(ang, rand.random(e.fin() * 55f));
            lineAngle(e.x + v1.x + rand.range(spread), e.y + v1.y + rand.range(spread), ang, e.fout() * 5f * rand.random(1f) + 1f);
        }
    }),

    apotheosisBlast = new Effect(300f, 400f, e -> {
        float fin = Interp.pow3Out.apply(Mathf.curve(e.fin(), 0f, 0.5f));
        float fade = 1f - Interp.pow2Out.apply(Mathf.curve(e.fin(), 0.6f, 1f));

        z(Layer.effect - 0.0005f);
        color(PMPal.apotheosisLaser);
        stroke(fin * 6f * fade);
        for(int dir : Mathf.signs){
            for(int i = 0; i < 3; i++){
                PMDrawf.ellipse(e.x, e.y, fin * 64f, 1f - 0.75f * fin, 1f + fin, fin * (375f + (i * 240f)) * dir);
            }
        }
        stroke(fin * 8f * fade);
        Lines.circle(e.x, e.y, fin * 80f);
    }).layer(Layer.effect + 0.001f),

    apotheosisClouds = new Effect(360f, 400f, e -> {
        float intensity = 2f;
        e.lifetime = 300f + intensity * 25f;

        z(Layer.effect - 0.001f);
        color(PMPal.apotheosisLaserDark);
        alpha(0.9f);
        for(int i = 0; i < 12; i++){
            rand.setSeed(e.id * 2L + i);
            float lenScl = rand.random(0.25f, 1f);
            int fi = i;
            e.scaled(e.lifetime * lenScl, s -> {
                randLenVectors(s.id + fi - 1, s.fin(Interp.pow10Out), (int)(7.3f * intensity), 60f * intensity, (x, y, in, out) -> {
                    float fout = s.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((2f + intensity) * 3.52f);

                    Fill.circle(s.x + x, s.y + y, rad);
                    light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }
    }),

    apotheosisDamage = new Effect(30f, 100f * tilesize, e -> {
        if(!(e.data instanceof ApotheosisEffectData d)) return;
        z(Layer.effect + d.layerOffset);

        float intensity = 2f * d.scl;
        float baseLifetime = 20f + intensity * 4f;
        e.lifetime = 25f + intensity * 6f;

        color(d.laserColorDark);
        alpha(0.9f);
        for(int i = 0; i < 5; i++){
            rand.setSeed(e.id * 2L + i);
            float lenScl = rand.random(0.25f, 1f);
            int fi = i;
            e.scaled(e.lifetime * lenScl, s -> {
                randLenVectors(s.id + fi - 1, s.fin(Interp.pow10Out), (int)(3.1f * intensity), 30f * intensity, (x, y, in, out) -> {
                    float fout = s.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((2f + intensity) * 2.35f);

                    Fill.circle(s.x + x, s.y + y, rad);
                    light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            color(d.laserColor, d.laserColorDark, s.fin());
            stroke((2f * s.fout()));

            z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(s.x + x, s.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
            });
        });
    }),

    apotheosisPuddle = new Effect(60f * 10f, 30f * tilesize, e -> {
        if(!(e.data instanceof ApotheosisEffectData d)) return;
        z(Layer.scorch + 1f); //Used in a MultiEffect, .layer() doesn't quite work.

        float fin = Mathf.curve(e.time, 0, 10f);
        float rad = 1f - Mathf.curve(e.time, 60f * 9f, 60f * 10f);

        randLenVectors(e.id, 6, 32f * d.scl, (x, y) -> {
            float px = e.x + x * fin,
                py = e.y +  y * fin,
                baseRad = 16f * d.scl,
                radius = Mathf.randomSeed((long)(e.id + x + y), baseRad / 2f, baseRad) * rad;

            color(d.laserColor);
            Fill.circle(px, py, radius);
            light(px, py, radius * 1.5f, getColor(), 0.8f);
        });
    }),

    apotheosisPulse = new Effect(120f, 100f * tilesize, e -> {
        if(!(e.data instanceof ApotheosisEffectData d)) return;
        z(Layer.effect + d.layerOffset);

        color(d.laserColor, 0.7f);
        stroke(32f * d.scl * e.fout(Interp.pow5In));
        Lines.circle(e.x, e.y, 16f * tilesize * d.scl * e.fin(Interp.pow5Out));
    }),

    apotheosisTouchdown = new Effect(720f, 300f * tilesize, e -> {
        if(!(e.data instanceof ApotheosisEffectData d)) return;
        z(Layer.effect + d.layerOffset);

        color(d.laserColor, 0.7f);
        stroke(64f * d.scl * e.fout(Interp.pow5In));
        Lines.circle(e.x, e.y, 148f * tilesize * e.fin(Interp.pow3Out) * d.scl);
        Lines.circle(e.x, e.y, (10f + 162f * tilesize * e.fin(Interp.pow5Out)) * d.scl);
        Lines.circle(e.x, e.y, (16f + 178f * tilesize * e.fin(Interp.pow10Out)) * d.scl);
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
            light((Team)e.data, e.x + v1.x, e.y + v1.y, e.x + v2.x, e.y + v2.y, 14f / 2f + 60f * e.finpow(), Draw.getColor(), lightOpacity + (0.2f * e.finpow()));
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

                    light((Team)e.data, e.x, e.y, e.x + v1.x, e.y + v1.y, ((16f * 0.75f + Mathf.absin(Time.time, 0.5f, 1f)) * grow * strokes[i] * tscales[j]) / 2f + 60f * e.finpow(), colors[2], lightOpacity);
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

    public static class ApotheosisEffectData{
        public float layerOffset;
        public float scl;
        public Color laserColor, laserColorDark;

        public ApotheosisEffectData(float layerOffset, float scl, Color c1, Color c2){
            this.layerOffset = layerOffset;
            this.scl = scl;
            laserColor = c1;
            laserColorDark = c2;
        }
    }
}