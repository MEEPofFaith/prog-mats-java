package progressed.graphics.trails;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import progressed.graphics.perspective.*;

public class ZTrail extends Trail{
    protected float lastZ = 0f;

    public ZTrail(int length){
        super(length);
        points = new FloatSeq(length * 4);
    }

    @Override
    public Trail copy(){
        ZTrail out = new ZTrail(length);
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastW = lastW;
        out.lastZ = lastZ;
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
            float[] items = points.items;
            int i = points.size - 4;
            float z1 = items[i - 4 + 3];
            Vec2 pos1 = Perspective.drawPos(items[i - 4], items[i - 4 + 1], z1);
            float x1 = pos1.x, y1 = pos1.y;
            Vec2 pos2 = Perspective.drawPos(lastX, lastY, lastZ);
            float x2 = pos2.x, y2 = pos2.y;
            float w1 = Perspective.scale(lastX, lastY, lastZ), w = w1 * width / (points.size / 4) * i / 4f * 2f;
            if(w1 <= 0.001f) return;
            if(fade){
                Draw.color(Tmp.c1.set(color).mulA(Perspective.alpha(lastX, lastY, lastZ)));
            }else{
                Draw.color(color);
            }
            Draw.rect("hcircle", x2, y2, w, w, Angles.angle(x1, y1, x2, y2));
            Draw.color();
        }
    }

    @Override
    public void drawCap(Color color, float width){
        drawCap(color, width, true);
    }

    public void draw(Color color, float width, boolean fade){
        float lastAngle = 0;
        float[] items = points.items;
        float size = width / (int)(points.size / 4);

        for(int i = 0; i < points.size; i += 4){
            float px1 = items[i], py1 = items[i + 1], z1 = items[i + 3];
            Vec2 pos1 = Perspective.drawPos(px1, py1, z1);
            float x1 = pos1.x, y1 = pos1.y, w1 = Perspective.scale(items[i], items[i + 1], z1);
            float x2, y2, px2, py2, w2, z2;

            //last position is always lastX/Y/W
            if(i < points.size - 4){
                px2 = items[i + 4];
                py2 = items[i + 4 + 1];
                z2 = items[i + 4 + 3];
                Vec2 pos2 = Perspective.drawPos(px2, py2, z2);
                x2 = pos2.x;
                y2 = pos2.y;
                w2 = Perspective.scale(items[i + 4], items[i + 4 + 1], z2);
            }else{
                px2 = lastX;
                py2 = lastY;
                z2 = lastZ;
                Vec2 pos2 = Perspective.drawPos(px2, py2, z2);
                x2 = pos2.x;
                y2 = pos2.y;
                w2 = Perspective.scale(lastX, lastY, z2);
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
                c1 = Tmp.c1.set(color).mulA(Perspective.alpha(px1, py1, z1)).toFloatBits();
                c2 = Tmp.c1.set(color).mulA(Perspective.alpha(px2, py2, z2)).toFloatBits();
            }

            Fill.quad(
                x1 - cx, y1 - cy, c1,
                x1 + cx, y1 + cy, c1,
                x2 + nx, y2 + ny, c2,
                x2 - nx, y2 - ny, c2
            );

            lastAngle = a2;
        }

        Draw.color();
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
        lastZ = height;
    }
}
