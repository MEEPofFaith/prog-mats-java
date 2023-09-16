package progressed.graphics.trails;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static progressed.graphics.Draw3D.*;

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

    public void drawCap(Color color, float width, boolean fade){
        if(points.size > 4){
            Draw.color(color);
            if(fade) Draw.alpha(Draw3D.scaleAlpha(lastH));
            int i = points.size - 4;
            float x1 = x(i - 4), y1 = y(i - 4),
                x2 = x(-1), y2 = y(-1),
                w1 = w(-1), w = w1 * width / (points.size / 4) * i / 4f * 2f;
            if(w1 <= 0.001f) return;
            Draw.rect("hcircle", x2, y2, w, w, Angles.angle(x1, y1, x2, y2));
            Draw.reset();
        }
    }

    @Override
    public void drawCap(Color color, float width){
        drawCap(color, width, true);
    }

    public void draw(Color color, float width, boolean fade){
        float lastAngle = 0;
        float[] items = points.items;
        float size = width / (points.size / 4);

        for(int i = 0; i < points.size; i += 4){
            float x1 = x(i), y1 = y(i), w1 = w(i), z1 = items[i + 3];
            float x2, y2, w2, z2;

            //last position is always lastX/Y/W
            if(i < points.size - 4){
                x2 = x(i + 4);
                y2 = y(i + 4);
                w2 = w(i + 4);
                z2 = items[i + 4 + 3];

            }else{
                x2 = xHeight(lastX, lastH);
                y2 = yHeight(lastY, lastH);
                w2 = lastW * hScale(lastH);
                z2 = lastH;
            }

            float a2 = -Angles.angleRad(x1, y1, x2, y2);
            //end of the trail (i = 0) has the same angle as the next.
            float a1 = i == 0 ? a2 : lastAngle;
            if(w1 <= 0.001f || w2 <= 0.001f) continue;

            float
                cx = Mathf.sin(a1) * i/4f * size * w1,
                cy = Mathf.cos(a1) * i/4f * size * w1,
                nx = Mathf.sin(a2) * (i/4f + 1) * size * w2,
                ny = Mathf.cos(a2) * (i/4f + 1) * size * w2;
            Tmp.c1.set(color);
            float c1 = Tmp.c1.toFloatBits(),
                c2 = Tmp.c1.toFloatBits();
            if(fade){
                c1 = Tmp.c1.set(color).mulA(Draw3D.scaleAlpha(z1)).toFloatBits();
                c2 = Tmp.c1.set(color).mulA(Draw3D.scaleAlpha(z2)).toFloatBits();
            }

            Fill.quad(
                x1 - cx, y1 - cy, c1,
                x1 + cx, y1 + cy, c1,
                x2 + nx, y2 + ny, c2,
                x2 - nx, y2 - ny, c2
            );

            lastAngle = a2;
        }

        Draw.reset();
    }

    @Override
    public void draw(Color color, float width){
        draw(color, width, true);
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
        if(index < 0) return xHeight(lastX, lastH);

        float[] items = points.items;
        return xHeight(items[index], items[index + 3]);
    }

    public float y(int index){
        if(index < 0) return yHeight(lastY, lastH);

        float[] items = points.items;
        return yHeight(items[index + 1], items[index + 3]);
    }

    public float w(int index){
        if(index < 0) return lastW * hScale(lastH);

        float[] items = points.items;
        return items[index + 2] * hScale(items[index + 3]);
    }
}
