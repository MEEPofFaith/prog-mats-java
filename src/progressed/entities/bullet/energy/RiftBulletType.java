package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import progressed.entities.*;
import progressed.graphics.*;

public class RiftBulletType extends SweepLaserBulletType{
    public float riftHeight = 0.5f;
    public Color edgeColor;

    public RiftBulletType(float damage){
        super();
        this.damage = damage;
        color = Color.black;
        collidesGround = collidesTiles = true;
        collidesAir = false;
    }

    @Override
    public void init(){
        super.init();

        if(edgeColor == null) edgeColor = color.cpy().a(0f);
    }

    @Override
    public void sweepBlast(Bullet b, SweepLaserData d){
        float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
        Tmp.v1.trns(d.rotation - 90f + a, -length / 2f).add(d.x, d.y);
        Tmp.v2.trns(d.rotation - 90f + a, length / 2f).add(d.x, d.y);
        Effect.shake(hitShake, hitShake, d.x, d.y);
        hitSound.at(d.x, d.y, hitSoundPitch, hitSoundVolume);
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
        if(b.data instanceof SweepLaserData d){
            float fin = Mathf.curve(b.fin(), extendTime, sweepTime);
            float fout = 1f - Mathf.curve(b.fin(), blastTime);
            float w = width * fout;
            float a = Mathf.randomSeedRange(b.id * 2L, angleRnd);
            Tmp.v1.trns(d.rotation - 90f + a, -length / 2f * fin).add(d.x, d.y);
            Tmp.v2.trns(d.rotation - 90f + a, length / 2f * fin).add(d.x, d.y);

            Lines.stroke(w, color);
            PMDrawf.baseTri(
                Tmp.v1.x, Tmp.v1.y,
                w, w * 2f,
                d.rotation + 90 + a
            );
            PMDrawf.baseTri(
                Tmp.v2.x, Tmp.v2.y,
                w, w * 2f,
                d.rotation - 90 + a
            );

            Lines.line(
                Tmp.v1.x, Tmp.v1.y,
                Tmp.v2.x, Tmp.v2.y
            );

            //Line
            float lfin = Mathf.curve(b.fin(), 0f, extendTime);
            float lfout = 1f - Mathf.curve(b.fin(), sweepTime, retractTime);
            float lscl = lfin * lfout;
            if(lscl > 0.01f){
                float lx1 = Mathf.lerp(b.x, Tmp.v1.x, lscl),
                    ly1 = Mathf.lerp(b.y, Tmp.v1.y, lscl),
                    lx2 = Mathf.lerp(b.x, Tmp.v2.x, lscl),
                    ly2 = Mathf.lerp(b.y, Tmp.v2.y, lscl);
                Lines.line(
                    b.x, b.y,
                    lx1, ly1
                );
                Fill.circle(
                    lx1, ly1,
                    radius
                );
                Lines.line(
                    b.x, b.y,
                    lx2, ly2
                );
                Fill.circle(
                    lx2, ly2,
                    radius
                );
            }

            if(fout < 1){
                Tmp.c1.set(color).a(color.a * fout);
                Tmp.c2.set(edgeColor).a(edgeColor.a * fout);
                PMDrawf.border(
                    Tmp.v1.x, Tmp.v1.y,
                    Tmp.v2.x, Tmp.v2.y,
                    riftHeight,
                    Tmp.c1, Tmp.c2
                );
            }
        }
    }
}