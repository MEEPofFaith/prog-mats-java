package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;

import static progressed.graphics.DrawPseudo3D.*;

public class HeightTrail extends Trail{
    protected float lastH = 0f;

    public HeightTrail(int length){
        super(length);
        points = new FloatSeq(length * 4);
    }

    @Override
    public Trail copy(){
        HeightTrail out = new HeightTrail(length);
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastW = lastW;
        out.lastH = lastH;
        return out;
    }

    @Override
    public float width(){
        return lastW;
    }

    @Override
    public void clear(){
        points.clear();
    }

    @Override
    public int size(){
        return points.size / 4;
    }

    @Override
    public void drawCap(Color color, float width){
        if(points.size > 4){
            Draw.color(color);
            int i = points.size - 4;
            float x1 = x(i - 4), y1 = y(i - 4),
                x2 = x(i), y2 = y(i),
                w1 = w(i), w = w1 * width / (points.size / 4) * i / 4f * 2f;
            if(w1 <= 0.001f) return;
            Draw.rect("hcircle", x2, y2, w, w, Angles.angle(x1, y1, x2, y2));
            Draw.reset();
        }
    }

    @Override
    public void draw(Color color, float width){
        Draw.color(color);
        float lastAngle = 0;
        float size = width / (points.size / 4);

        for(int i = 0; i < points.size; i += 4){
            float x1 = x(i), y1 = y(i), w1 = w(i);
            float x2, y2, w2;

            //last position is always lastX/Y/W
            if(i < points.size - 4){
                x2 = x(i + 4);
                y2 = y(i + 4);
                w2 = w(i + 4);
            }else{
                x2 = xHeight(lastX, lastH);
                y2 = yHeight(lastY, lastH);
                w2 = lastW * hScale(lastH);
            }

            float z2 = -Angles.angleRad(x1, y1, x2, y2);
            //end of the trail (i = 0) has the same angle as the next.
            float z1 = i == 0 ? z2 : lastAngle;
            if(w1 <= 0.001f || w2 <= 0.001f) continue;

            float
                cx = Mathf.sin(z1) * i/4f * size * w1,
                cy = Mathf.cos(z1) * i/4f * size * w1,
                nx = Mathf.sin(z2) * (i/4f + 1) * size * w2,
                ny = Mathf.cos(z2) * (i/4f + 1) * size * w2;

            Fill.quad(
                x1 - cx, y1 - cy,
                x1 + cx, y1 + cy,
                x2 + nx, y2 + ny,
                x2 - nx, y2 - ny
            );

            lastAngle = z2;
        }

        Draw.reset();
    }

    /** Removes the last point from the trail at intervals. */
    public void shorten(){
        if((counter += Time.delta) >= 1f){
            if(points.size >= 4){
                points.removeRange(0, 3);
            }

            counter %= 1f;
        }
    }

    @Override
    public void update(float x, float y, float width){
        update(x, y, width, 0f);
    }

    public void update(float x, float y, float width, float height){
        if((counter += Time.delta) >= 1f){
            if(points.size > length * 4){
                points.removeRange(0, 3);
            }

            points.add(x, y, width, height);

            counter %= 1f;
        }

        //update last position regardless, so it joins
        lastX = x;
        lastY = y;
        lastW = width;
        lastH = height;
    }

    public float x(int index){
        float[] items = points.items;
        return xHeight(items[index], items[index + 3]);
    }

    public float y(int index){
        float[] items = points.items;
        return yHeight(items[index + 1], items[index + 3]);
    }

    public float w(int index){
        float[] items = points.items;
        return items[index + 2] * hScale(items[index + 3]);
    }
}
