package progressed.entities.bullet.energy;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.*;

public class CrossLaserBulletType extends LaserBulletType{
    public float growTime = 5f, fadeTime = 5f;
    public float crossLength, crossWidth, crossSection = 0.7f;
    public float midLen = 0.3f, border = 3.5f, crossBorder = 2f;

    public CrossLaserBulletType(float damage){
        super(damage);
        laserEffect = Fx.none;
        collidesGround = collidesAir = true;
        largeHit = true;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        b.data = new CrossLaserData();
        Time.run(growTime, () -> {
            for(int i = 0; i < 2; i++){
                Tmp.v1.trns(b.rotation(), b.fdata * crossSection);
                float x = b.x + Tmp.v1.x, y = b.y + Tmp.v1.y;
                float resultLength = PMDamage.findLaserLength(x, y, b.rotation() + 90f * Mathf.signs[i], b.team, crossLength);
                Damage.collideLine(b, b.team, b.type.hitEffect, x, y, b.rotation() + 90f * Mathf.signs[i], resultLength, largeHit);
                ((CrossLaserData)b.data).addLength(resultLength, i);
            }
        });
    }

    @Override
    public void draw(Bullet b){
        CrossLaserData data = (CrossLaserData)b.data;

        float fin = Mathf.curve(b.fin(), 0f, growTime / b.lifetime);
        float cfin = Mathf.curve(b.fin(), growTime / b.lifetime, growTime / b.lifetime * 2f);
        float fout = 1 - Mathf.curve(b.fin(), (b.lifetime - fadeTime) / b.lifetime, 1f);

        float frac = b.fdata / length;
        float l = b.fdata * fin;
        float w = width / 2f * fin * frac;
        float mid = l * midLen;
        float cross = l * crossSection;

        //Main Laser
        for(int i = 0; i < colors.length; i++){
            float bord = border * i * fin * frac;
            Draw.color(colors[i], fout);

            // Main
            //Mid
            Tmp.v1.trns(b.rotation(), mid, -(w - bord));
            Tmp.v1.add(b.x, b.y);
            Tmp.v2.trns(b.rotation(), mid, w - bord);
            Tmp.v2.add(b.x, b.y);
            //End
            Tmp.v3.trns(b.rotation(), l);
            Tmp.v3.add(b.x, b.y);
            
            Fill.tri(b.x, b.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            Fill.tri(Tmp.v3.x, Tmp.v3.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        }

        //Main Light
        Tmp.v1.trns(b.rotation(), l);
        Tmp.v1.add(b.x, b.y);
        Drawf.light(b.team, b.x, b.y, Tmp.v1.x, Tmp.v1.y, w * 2f * fin * fout, colors[1], 0.6f);

        //Cross laser
        if(data.valid()){
            float cblock = (data.left() / crossLength + data.right() / crossLength) / 2f;
            float[] cl = {data.left() * cfin, data.right() * cfin};
            float[] cw = {crossWidth / 2f * cfin * cblock, crossWidth / 2f * cfin * cblock};

            for(int i = 0; i < colors.length; i++){
                float[] cbord = {crossBorder * i * cfin * cblock, crossBorder * i * cfin * cblock};
                Draw.color(colors[i], fout);

                for(int j = 0; j < 2; j++){
                    //Point
                    Tmp.v1.trns(b.rotation(), cross, cl[j] * Mathf.signs[j]);
                    Tmp.v1.add(b.x, b.y);
                    //Base
                    Tmp.v2.trns(b.rotation(), cross + cw[j] - cbord[j]);
                    Tmp.v2.add(b.x, b.y);
                    Tmp.v3.trns(b.rotation(), cross - cw[j] + cbord[j]);
                    Tmp.v3.add(b.x, b.y);
                    
                    Fill.tri(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y);
                }
            }

            // Cross Beam Light
            for(int i = 0; i < 2; i++){
                //Point
                Tmp.v1.trns(b.rotation(), cross, cl[i] * Mathf.signs[i]);
                Tmp.v1.add(b.x, b.y);
                //Base
                Tmp.v2.trns(b.rotation(), cross);
                Tmp.v2.add(b.x, b.y);
    
                Drawf.light(b.team, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, cw[i] * 2f * fin * fout, colors[1], 0.6f);
          }
        }
    }

    @Override
    public void drawLight(Bullet b){
        // Light is drawn in draw(b)
    }

    public static class CrossLaserData{
        public float[] lengths;

        public CrossLaserData(){
            lengths = new float[2];
        }

        public void addLength(float length, int index){
            lengths[index] = length;
        }

        public boolean valid(){
            return lengths != null;
        }

        public float left(){
            return lengths[0];
        }

        public float right(){
            return lengths[1];
        }
    }
}