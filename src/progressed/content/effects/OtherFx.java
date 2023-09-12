package progressed.content.effects;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import progressed.graphics.*;
import progressed.type.unit.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;
import static mindustry.Vars.*;
import static mindustry.graphics.Drawf.light;
import static mindustry.graphics.Drawf.*;
import static progressed.graphics.PMDrawf.*;
import static progressed.util.PMUtls.*;

public class OtherFx{
    public static final Effect

    groundRise = new Effect(30, e -> {
        Tile t = world.tileWorld(e.x, e.y);
        if(t == null) return;

        Floor f = t.floor();
        if(f instanceof SteamVent) return;
        TextureRegion region = f.variantRegions[Mathf.randomSeed(t.pos(), 0, Math.max(0, f.variantRegions.length - 1))];
        float x = t.drawx(), y = t.drawy() + e.rotation * e.fout();

        Draw.z(Draw.z() - ((float)t.y / world.height()) / 1000f);;
        for(int i = 0; i < region.width; i++){
            PixmapRegion image = Core.atlas.getPixmap(region);
            float c1 = Tmp.c1.set(image.get(i, 0)).toFloatBits();
            float c2 = Tmp.c2.set(Tmp.c1).lerp(Color.black, e.fout() / 4f).toFloatBits();

            float px = x - region.width / 4f / 2f + i / 4f, py = y - region.height / 4f / 2f, by = py - e.rotation * e.fout();
            float p = 1f / 8f;

            Fill.quad(px - p, by, c2, px - p, py, c1, px + p, py, c1, px + p, by, c2);
        }

        rect(region, x, y);
    }).layer(Layer.floor + 0.01f),

    concretionSlam = new Effect(23, e -> {
        color(Tmp.c1.set(e.color).mul(1.1f));
        randLenVectors(e.id, 6, 10f * e.finpow(), e.rotation, 15f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.4f);
        });
    }).layer(Layer.floor),

    earthquke = new Effect(10f, 100f, e -> {
        float rad = e.rotation;
        Draw.color(PMPal.darkBrown);

        int points = 3;
        for(int i = 0; i < points; i++){
            float angle = Mathf.randomSeedRange(e.id + i, 360f);
            float length = Mathf.randomSeed(e.id * 2L + i, rad / 6f, rad);
            tri(e.x + Angles.trnsx(angle, rad), e.y + Angles.trnsy(angle, rad), 6f, length * e.fout() / 4f, angle);
            tri(e.x + Angles.trnsx(angle, rad), e.y + Angles.trnsy(angle, rad), 6f, length * e.fout(), angle + 180);
        }
    }).layer(Layer.floor),

    pillarPlace = new Effect(15f, e -> {
        Draw.color(PMPal.darkBrown);
        randLenVectors(e.id, 6, 4f + 20f * e.fin(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y,  4f * e.fout());
        });
    }).layer(Layer.blockUnder),

    pillarBlast = new Effect(20, e -> {
        color(Pal.bulletYellow);
        e.scaled(6, s -> {
            stroke(3f * s.fout());
            Lines.circle(e.x, e.y, 3f + s.fin() * 6f);
        });

        color(Pal.lightPyraFlame);
        e.scaled(15, s -> {
            randLenVectors(e.id, 3, 2f + 13f * s.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, s.fout() * 3f + 0.5f);
            });
        });

        color(Color.gray);
        randLenVectors(e.id + 1, 3, 2f + 16f * e.finpow(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout() * 3f + 0.5f);
        });

        color(Pal.lightPyraFlame);
        stroke(e.fout());
        randLenVectors(e.id + 2, 4, 1f + 16f * e.finpow(), (x, y) -> {
            lineAngle(e.x + x, e.y + y, angle(x, y), 1f + e.fout() * 3f);
        });

        light(e.x, e.y, 50f, Pal.lightPyraFlame, 0.8f * e.fout());
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
    
    miniCrit = new Effect(90f, e -> {
        v1.trns(e.rotation + 90f, 0f, 32f * e.fin(Interp.pow2Out));

        color(e.color, e.fout());
        randLenVectors(e.id, 2, 18f, (x, y) -> {
            float rot = Mathf.randomSeed((long)(e.id + x + y), 360);
            float tx = x * e.fin(Interp.pow2Out);
            float ty = y * e.fin(Interp.pow2Out);
            plus(e.x + tx + v1.x, e.y + ty + v1.y, 3f, rot);
        });
    }),
    
    crit = new Effect(120f, e -> {
        v1.trns(e.rotation + 90f, 0f, 48f * e.fin(Interp.pow2Out));

        color(e.color, e.fout());
        randLenVectors(e.id, 6, 24f, (x, y) -> {
            float rot = Mathf.randomSeed((long)(e.id + x + y), 360);
            float tx = x * e.fin(Interp.pow2Out);
            float ty = y * e.fin(Interp.pow2Out);
            plus(e.x + tx + v1.x, e.y + ty + v1.y, 4f, rot);
        });
    }),
    
    swordStab = new Effect(24f, e -> {
        color(e.color, Color.crimson, e.fin());

        e.scaled(15f, s -> {
            stroke(s.fout() * 1.5f);
            Lines.circle(e.x, e.y, 8f * s.fin());
        });

        stroke(1f);
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

        light(e.x, e.y, 20f * e.fslope(), Pal.lightFlame, 0.5f);
    }),

    linkActivation = new Effect(30f, e -> {
        stroke(e.fout() * 1.5f, e.color);
        Lines.circle(e.x, e.y, e.fin() * 12f * tilesize);
    }),
    
    staticSpark = new Effect(10f, e -> {
        color(e.color);
        stroke(e.fout() * 1.5f);

        randLenVectors(e.id, 7, e.finpow() * 27f, e.rotation, 45f, (x, y) -> {
            float ang = angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 4f + 1f);
        });
    }),

    fard = new Effect(30, 500f, e -> {
        float intensity = 8f;
        float baseLifetime = 25f + intensity * 15f;
        e.lifetime = 50f + intensity * 64f;

        color(Color.brown);
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
                    light(s.x + x, s.y + y, rad * 2.6f, getColor(), 0.7f);
                });
            });
        }

        e.scaled(baseLifetime, s -> {
            Draw.color();
            s.scaled(2 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(s.x, s.y, (3f + i.fin() * 14f) * intensity);
                light(s.x, s.y, i.fin() * 28f * 2f * intensity, getColor(), 0.9f * s.fout());
            });

            color(Color.tan, Color.brick, s.fin());
            stroke((2f * s.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(s.id + 1, s.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(s.x + x, s.y + y, angle(x, y), 1f + out * 4 * (4f + intensity));
                light(s.x + x, s.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
            });
        });
    }).layer(Layer.groundUnit - 1f);

    public static Effect flareFallEffect(SignalFlareUnitType flare){
        return new Effect(1f / flare.fallSpeed, e -> {
            float rot = 90f * e.fout() - 90f;

            //Shadow
            flare.drawSoftShadow(e.x, e.y, rot, e.fout());
            //Body
            alpha(e.fout());
            rect(flare.region, e.x, e.y, flare.region.width / 4f, flare.region.height / 4f * e.rotation, rot);
            //Cell
            color(e.color, e.fout());
            rect(flare.cellRegion, e.x, e.y, flare.cellRegion.width / 4f, flare.cellRegion.height / 4f * e.rotation, rot);
        }).layer(flare.groundLayer);
    }
}
