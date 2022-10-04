package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;

public class HeightTrail extends Trail{
    private final FloatSeq points;
    private float lastX = -1, lastY = -1, lastAngle = -1, counter = 0f, lastW = 0f, lastH = 0f;

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
        out.lastAngle = lastAngle;
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
        if(points.size > 0){
            Draw.color(color);
            float[] items = points.items;
            int i = points.size - 4;
            float x1 = x(i), y1 = y(i), w1 = items[i + 2], w = w1 * width / (points.size/4) * i / 4f * 2f;
            if(w1 <= 0.001f) return;
            Draw.rect("hcircle", x1, y1, w, w, -Mathf.radDeg * lastAngle + 180f);
            Draw.reset();
        }
    }

    @Override
    public void draw(Color color, float width){
        Draw.color(color);
        float[] items = points.items;
        float lastAngle = this.lastAngle;
        float size = width / (points.size / 4);

        for(int i = 0; i < points.size; i += 4){
            float x1 = x(i), y1 = y(i), w1 = items[i + 2];
            float x2, y2, w2;

            //last position is always lastX/Y/W
            if(i < points.size - 4){
                x2 = x(i + 4);
                y2 = y(i + 4);
                w2 = items[i + 6];
            }else{
                x2 = DrawPseudo3D.xHeight(lastX, lastH);
                y2 = DrawPseudo3D.yHeight(lastY, lastH);
                w2 = lastW;
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
        //TODO fix longer trails at low FPS
        if((counter += Time.delta) >= 1f){
            if(points.size > length * 4){
                points.removeRange(0, 3);
            }

            points.add(x, y, width, height);

            counter %= 1f;
        }

        //update last position regardless, so it joins
        lastAngle = -Angles.angleRad(x, y, lastX, lastY);
        lastX = x;
        lastY = y;
        lastW = width;
        lastH = height;
    }

    public float x(int index){
        float[] items = points.items;
        return DrawPseudo3D.xHeight(items[index], items[index + 3]);
    }

    public float y(int index){
        float[] items = points.items;
        return DrawPseudo3D.yHeight(items[index + 1], items[index + 3]);
    }
}
