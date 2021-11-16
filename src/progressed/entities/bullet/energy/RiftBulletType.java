package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.*;
import progressed.graphics.*;

public class RiftBulletType extends SweepLaserBulletType{
    public Color edgeColor;

    public RiftBulletType(float damage){
        super();
        this.damage = damage;
        color = Color.black;
        collidesGround = collidesTiles = true;
        collidesAir = false;
        layer = Layer.groundUnit + 0.1f;
    }

    @Override
    public void init(){
        super.init();

        if(edgeColor == null) edgeColor = color.cpy().a(0f);
    }

    @Override
    public void sweepBlast(Bullet b, SweepLaserData data){
        float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
        Tmp.v1.trns(data.rotation - 90f + a, -length / 2f).add(data.x, data.y);
        Tmp.v2.trns(data.rotation - 90f + a, length / 2f).add(data.x, data.y);
        PMDamage.collideLine(
            damage, b.team,
            hitEffect, status, statusDuration,
            Tmp.v1.x, Tmp.v1.y,
            Tmp.v1.angleTo(Tmp.v2), Tmp.v1.dst(Tmp.v2),
            collidesGround, collidesAir, collidesTiles
        );
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof SweepLaserData data){
            float fin = Mathf.curve(b.fin(), 0f, sweepTime);
            float fout = 1f - Mathf.curve(b.fin(), blastTime);
            float w = width * fout;
            float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
            Tmp.v1.trns(data.rotation - 90f + a, -length / 2f * fin).add(data.x, data.y);
            Tmp.v2.trns(data.rotation - 90f + a, length / 2f * fin).add(data.x, data.y);

            Lines.stroke(w, color);
            PMDrawf.baseTri(
                Tmp.v1.x, Tmp.v1.y,
                w, w * 2f,
                data.rotation + 90 + a
            );
            PMDrawf.baseTri(
                Tmp.v2.x, Tmp.v2.y,
                w, w * 2f,
                data.rotation - 90 + a
            );

            Lines.line(
                Tmp.v1.x, Tmp.v1.y,
                Tmp.v2.x, Tmp.v2.y
            );

            if(fin < 1){
                Lines.line(
                    b.x, b.y,
                    Tmp.v1.x, Tmp.v1.y
                );
                Fill.circle(
                    Tmp.v1.x, Tmp.v1.y,
                    radius
                );
                Lines.line(
                    b.x, b.y,
                    Tmp.v2.x, Tmp.v2.y
                );
                Fill.circle(
                    Tmp.v2.x, Tmp.v2.y,
                    radius
                );
            }

            if(fout < 1){
                Tmp.c1.set(color).a(color.a * fout);
                Tmp.c2.set(edgeColor).a(edgeColor.a * fout);
                PMDrawf.border(
                    Tmp.v1.x, Tmp.v1.y,
                    Tmp.v2.x, Tmp.v2.y,
                    0.4f,
                    Tmp.c1, Tmp.c2
                );
            }
        }
    }
}