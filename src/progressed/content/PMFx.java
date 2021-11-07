package progressed.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.explosive.RocketBulletType.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.energy.*;
import progressed.world.blocks.defence.turret.energy.AimLaserTurret.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

public class PMFx{
    private static final Rand rand = new Rand();
    private static final Vec2 v1 = new Vec2(), v2 = new Vec2();
    //From FireComp
    static final int frames = 40, ticksPerFrame = 90 / frames;
    static final TextureRegion[] fireFrames = new TextureRegion[frames];

    public static final Effect

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

    rocketTrail = new Effect(60, e -> {
        if(e.data instanceof float[] f){
            z(f[2]);
            color(e.color, Pal.lightishGray, Pal.darkerGray, e.fin());
            randLenVectors(e.id, 4, 24f * e.finpow() * f[1], f[0] + 180f, 15f * f[1], (x, y) -> {
                Fill.circle(e.x + x, e.y + y, (e.rotation - e.fin() * e.rotation) / 2f);
            });
        }
    }).layer(Layer.turret + 0.014f),

    incendBurning = new Effect(35f, e -> {
        color(Pal.lightPyraFlame, Pal.darkPyraFlame, e.fin());

        randLenVectors(e.id, 5, 2f + e.fin() * 12f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.1f + e.fout() * 1.9f);
        });
    }),

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

    pillarPlace = new Effect(15f, e -> {
        Draw.color(Pal.darkerGray);
        randLenVectors(e.id, 6, 4f + 20f * e.fin(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y,  4f * e.fout());
        });
    }).layer(Layer.blockUnder),

    earthquke = new Effect(10f, 100f, e -> {
        float rad = e.rotation;
        Draw.color(Pal.darkerGray);

        int points = 3;
        for(int i = 0; i < points; i++){
            float angle = Mathf.randomSeedRange(e.id + i, 360f);
            float length = Mathf.randomSeed(e.id * 2L + i, rad / 6f, rad / 1.5f);
            Drawf.tri(e.x + Angles.trnsx(angle, rad), e.y + Angles.trnsy(angle, rad), 6f, length * e.fout(), angle + 180);
        }
    }).layer(Layer.debris),

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
            lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 0.5f + e.fout() * 2f);
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
                    Drawf.light(s.x + x, s.y + y, rad * 2.6f, Pal.gray, 0.7f);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            Draw.color();
            s.scaled(2 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(s.x, s.y, (3f + i.fin() * 14f) * intensity);
                Drawf.light(s.x, s.y, i.fin() * 28f * 2f * intensity, Color.white, 0.9f * s.fout());
            });

            color(Pal.lighterOrange, Pal.redSpark, s.fin());
            stroke((2f * s.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(s.x + x, s.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                Drawf.light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, Draw.getColor(), 0.8f);
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
                    Drawf.light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            Draw.color();
            s.scaled(2 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(s.x, s.y, (3f + i.fin() * 14f) * intensity);
                Drawf.light(s.x, s.y, i.fin() * 28f * 2f * intensity, getColor(), 0.9f * s.fout());
            });

            color(Pal.lighterOrange, Pal.redSpark, s.fin());
            stroke((2f * s.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(s.x + x, s.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                Drawf.light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
            });
        });
    }),

    missileBlockedSmall = new Effect(38f, e -> {
        color(Pal.missileYellow);

        e.scaled(13f, s -> {
            stroke(2f * s.fout());

            randLenVectors(e.id, 8, 2f + 34f * s.fin(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + s.fout() * 8f);
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
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + s.fout() * 13f);
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
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 3f + s.fout() * 18f);
            });
        });

        float in = Interp.pow2Out.apply(Mathf.curve(e.fin(), 0f, 0.6f));
        float out = 1f - Interp.pow2In.apply(Mathf.curve(e.fin(), 0.6f));

        stroke(2f * out + 3f * e.fout());

        Lines.circle(e.x, e.y, 6f * out + 57f * in * out);
    }),

    critPierce = new Effect(20f, e -> {
        float rot = e.rotation - 90f;
        float fin = e.fin(Interp.pow5Out);
        float end = e.lifetime - 6f;
        float fout = 1f - Interp.pow2Out.apply(Mathf.curve(e.time, end, e.lifetime));
        float width = fin * fout;

        e.scaled(7f, s -> {
            stroke(0.5f + s.fout());
            color(Color.white, e.color, s.fin());
            Lines.circle(e.x + trnsx(rot, 0f, 5f * fin), e.y + trnsy(rot, 0f, 5f * fin), s.fin() * 6f);
        });

        color(Color.white, e.color, Mathf.curve(e.time, 0f, end));

        Fill.quad(
            e.x + trnsx(rot, 0f, 2f * fin), e.y + trnsy(rot, 0f, 2f * fin),
            e.x + trnsx(rot, 4f * width, -4f * fin), e.y + trnsy(rot, 4f * width, -4f * fin),
            e.x + trnsx(rot, 0f, 8f * fin), e.y + trnsy(rot, 0f, 8f * fin),
            e.x + trnsx(rot, -4f * width, -4f * fin), e.y + trnsy(rot, -4f * width, -4f * fin)
        );
    }),
    
    sniperCritMini = new Effect(90f, e -> {
        v1.trns(e.rotation + 90f, 0f, 32f * e.fin(Interp.pow2Out));
        
        randLenVectors(e.id, 2, 18f, (x, y) -> {
            float rot = Mathf.randomSeed((long)(e.id + x + y), 360);
            float tx = x * e.fin(Interp.pow2Out);
            float ty = y * e.fin(Interp.pow2Out);
            PMDrawf.plus(e.x + tx + v1.x, e.y + ty + v1.y, 3f, rot, e.color, e.fout());
        });
    }),
    
    sniperCrit = new Effect(120f, e -> {
        v1.trns(e.rotation + 90f, 0f, 48f * e.fin(Interp.pow2Out));
        
        randLenVectors(e.id, 6, 24f, (x, y) -> {
            float rot = Mathf.randomSeed((long)(e.id + x + y), 360);
            float tx = x * e.fin(Interp.pow2Out);
            float ty = y * e.fin(Interp.pow2Out);
            PMDrawf.plus(e.x + tx + v1.x, e.y + ty + v1.y, 4f, rot, e.color, e.fout());
        });
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

            Drawf.light(bullet.x + trnsx(e.rotation + startAngle * e.fout(),
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
    
    particle = new Effect(38f, e -> {
        color(e.color);

        randLenVectors(e.id, 2, 1f + 20f * e.fin(Interp.pow2Out), e.rotation, 120f, (x, y) -> {
            Drawf.tri(e.x + x, e.y + y, e.fslope() * 3f + 1, e.fslope() * 3f + 1, Mathf.angle(x, y));
        });
    }),

    fakeLightning = new Effect(10f, 500f, e -> {
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
    fakeLightningFast = new Effect(5f, 500f, e -> {
        Object[] data = (Object[])e.data;

        float length = (float)data[0];
        int tileLength = Mathf.round(length / tilesize);
        
        Lines.stroke((float)data[1] * e.fout());
        Draw.color(e.color, Color.white, e.fin());
        
        for(int i = 0; i < tileLength; i++){
            float offsetXA = i == 0 ? 0f : Mathf.randomSeed(e.id + (i * 6413), -4.5f, 4.5f);
            float offsetYA = (length / tileLength) * i;
            
            int f = i + 1;
            
            float offsetXB = f == tileLength ? 0f : Mathf.randomSeed(e.id + (f * 6413), -4.5f, 4.5f);
            float offsetYB = (length / tileLength) * f;
            
            v1.trns(e.rotation, offsetYA, offsetXA);
            v1.add(e.x, e.y);
            
            v2.trns(e.rotation, offsetYB, offsetXB);
            v2.add(e.x, e.y);
            
            Lines.line(v1.x, v1.y, v2.x, v2.y, false);
            Fill.circle(v1.x, v1.y, Lines.getStroke() / 2f);
            Drawf.light((Team)data[2], v1.x, v1.y, v2.x, v2.y, (float)data[1] * 3f, e.color, 0.4f);
        }

        Fill.circle(v2.x, v2.y, Lines.getStroke() / 2);
    }).layer(Layer.bullet + 0.01f),
    
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
            Drawf.light((Team)e.data, e.x + v1.x, e.y + v1.y, e.x + v2.x, e.y + v2.y, 14f / 2f + 60f * e.finpow(), Draw.getColor(), lightOpacity + (0.2f * e.finpow()));
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
                    
                    Drawf.light((Team)e.data, e.x, e.y, e.x + v1.x, e.y + v1.y, ((16f * 0.75f + Mathf.absin(Time.time, 0.5f, 1f)) * grow * strokes[i] * tscales[j]) / 2f + 60f * e.finpow(), colors[2], lightOpacity);
                }
            }
            Draw.reset();
        }
    }),
    
    everythingGunSwirl = new Effect(120f, 1600f, e -> {
        float[] data = (float[])e.data;
        color(e.color, Color.black, 0.25f + e.fin() * 0.75f);
        Fill.circle(e.x + trnsx(Mathf.randomSeed(e.id, 360f) + e.rotation * e.fin(), (16f + data[1]) * e.fin()),
            e.y + trnsy(Mathf.randomSeed(e.id, 360f) + e.rotation * e.fin(), (16f + data[1]) * e.fin()),
            data[0] * e.fout()
        );
    }).layer(Layer.bullet - 0.00999f),
    
    colliderFusion = new Effect(30f, e -> {
        color(Color.gray);
        e.scaled(15f, s -> {
            stroke(s.fout());
            Lines.circle(e.x, e.y, 3f * s.fout());
        });
        
        stroke(1f);

        randLenVectors(e.id, 16, 5f * e.fout(), e.rotation, 180f, (x, y) -> {
            float ang = angle(x, y, 0f, 0f);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fslope() * 5f);
        });
    }).layer(32.5f),

    superSmeltsmoke = new Effect(15, e -> {
        randLenVectors(e.id, 14, 6f + e.fin() * 18f, (x, y) -> {
            color(Color.white, e.color, e.fin());
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 3f, 45);
        });
    }),
    
    swordStab = new Effect(24f, e -> {
        color(e.color, Color.violet, e.fin());
        stroke(1f);

        e.scaled(15f, s -> {
            Lines.circle(e.x, e.y, 8f * s.fin());
        });

        randLenVectors(e.id, 16, 8f * e.fin(), e.rotation, 180f, (x, y) -> {
            float ang = angle(x, y);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fslope() * 4f);
        });
    }),
    
    flare = new Effect(50f, e -> {
        color(e.color, Color.gray, e.fin());

        randLenVectors(e.id, 2, e.fin() * 4f * e.rotation, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.1f + e.fslope() * 0.6f * e.rotation);
        });

        color();

        Drawf.light(Team.derelict, e.x, e.y, 20f * e.fslope(), Pal.lightFlame, 0.5f);
    }),
    
    aimChargeBegin = new Effect(300f, e -> {
        if(e.data instanceof AimLaserTurretBuild d){
            color(e.color);

            v1.trns(d.rotation, ((AimLaserTurret)(d.block)).shootLength);
            Fill.circle(d.x + v1.x, d.y + v1.y, 3f * e.fin());

            color();
        }
    }),
    
    aimCharge = new Effect(30f, e -> {
        if(e.data instanceof AimLaserTurretBuild d){
            color(e.color);

            v1.trns(d.rotation, ((AimLaserTurret)(d.block)).shootLength);
            randLenVectors(e.id, 3, 24f * e.fout(), (x, y) -> {
                Fill.circle(d.x + v1.x + x, d.y + v1.y + y, 2f * e.fin());
            });

            color();
        }
    }),
    
    sentinelBlast = new Effect(80f, e -> {
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
            Drawf.light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });
        randLenVectors(e.id + 1, 17,116f * e.fout(Interp.pow3Out), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fin() * 8f);
            Drawf.light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });
        randLenVectors(e.id + 2, 20,146f * e.fout(Interp.pow5Out), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fin() * 8f);
            Drawf.light(e.x + x, e.y + y, e.fin() * 24f, PMPal.apotheosisLaserDark, 0.7f);
        });

        Drawf.light(e.x, e.y, e.fin() * 36f, PMPal.apotheosisLaserDark, 0.7f);
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
                    Drawf.light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }
    }),

    apotheosisDamage = new Effect(30f, 100f * tilesize, e -> {
        if(!(e.data instanceof float[] f)) return;
        z(f[0]);

        float intensity = 2f * f[1];
        float baseLifetime = 20f + intensity * 4f;
        e.lifetime = 25f + intensity * 6f;

        color(PMPal.apotheosisLaserDark);
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
                    Drawf.light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            color(PMPal.apotheosisLaser, PMPal.apotheosisLaserDark, s.fin());
            stroke((2f * s.fout()));

            z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(s.x + x, s.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                Drawf.light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
            });
        });
    }),

    apotheosisPuddle = new Effect(60f * 10f, 30f * tilesize, e -> {
        if(!(e.data instanceof float[] f)) return;
        z(f[0] - (Layer.effect - Layer.scorch));

        float fin = Mathf.curve(e.time, 0, 10f);
        float rad = 1f - Mathf.curve(e.time, 60f * 9f, 60f * 10f);

        randLenVectors(e.id, 6, 32f * f[1], (x, y) -> {
            float px = e.x + x * fin,
                py = e.y +  y * fin,
                baseRad = 16f * f[1],
                radius = Mathf.randomSeed((long)(e.id + x + y), baseRad / 2f, baseRad) * rad;

            color(PMPal.apotheosisLaser);
            Fill.circle(px, py, radius);
            Drawf.light(px, py, radius * 1.5f, getColor(), 0.8f);
        });
    }),

    apotheosisPulse = new Effect(120f, 100f * tilesize, e -> {
        if(!(e.data instanceof float[] f)) return;
        z(f[0]);

        color(PMPal.apotheosisLaser, 0.7f);
        stroke(32f * f[1] * e.fout(Interp.pow5In));
        Lines.circle(e.x, e.y, 16f * tilesize * f[1] * e.fin(Interp.pow5Out));
    }),

    apotheosisTouchdown = new Effect(720f, 300f * tilesize, e -> {
        if(!(e.data instanceof float[] f)) return;
        z(f[0]);

        color(PMPal.apotheosisLaser, 0.7f);
        stroke(64f * f[1] * e.fout(Interp.pow5In));
        Lines.circle(e.x, e.y, 148f * tilesize * e.fin(Interp.pow3Out) * f[1]);
        Lines.circle(e.x, e.y, (10f + 162f * tilesize * e.fin(Interp.pow5Out)) * f[1]);
        Lines.circle(e.x, e.y, (16f + 178f * tilesize * e.fin(Interp.pow10Out)) * f[1]);
    }),
    
    staticSpark = new Effect(10f, e -> {
        color(e.color);
        stroke(e.fout() * 1.5f);

        randLenVectors(e.id, 7, e.finpow() * 27f, e.rotation, 45f, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 4f + 1f);
        });
    }),

    squareShieldRecharge = new Effect(20f, e -> {
        color(e.color, e.fout());
        stroke(1.5f + 1.5f * e.fout());
        Lines.square(e.x, e.y, e.rotation * e.finpow());
    }).layer(Layer.shields),

    squareShieldBreak = new Effect(40f, e -> {
        stroke(3f * e.fout(), e.color);
        Lines.square(e.x, e.y, e.rotation + e.fin());
    }),

    squareForceShrink = new Effect(20f, e -> {
        color(e.color, e.fout());
        if(renderer.animateShields){
            Fill.square(e.x, e.y, e.rotation * e.fout());
        }else{
            stroke(1.5f);
            Draw.alpha(0.09f);
            Fill.square(e.x, e.y, e.rotation * e.fout());
            Draw.alpha(1f);
            Lines.square(e.x, e.y, e.rotation * e.fout());
        }
    }).layer(Layer.shields);

    public static class LightningData{
        Position pos;
        float stroke;

        public LightningData(Position pos, float stroke){
            this.pos = pos;
            this.stroke = stroke;
        }
    }
}