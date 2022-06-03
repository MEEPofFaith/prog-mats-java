package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;

public class DriftTrail extends Trail{
    private final FloatSeq points;
    private float lastX = -1, lastY = -1, lastAngle = -1, counter = 0f, lastW = 0f;

    public DriftTrail(int length){
        super(length);
        points = new FloatSeq(length * 6); //x, y, w, dx, dy, drag
    }

    public DriftTrail copy(){
        DriftTrail out = new DriftTrail(length);
        out.points.addAll(points);
        out.lastX = lastX;
        out.lastY = lastY;
        out.lastAngle = lastAngle;
        return out;
    }

    public void clear(){
        points.clear();
    }

    public int size(){
        return points.size / 6;
    }

    public void drawCap(Color color, float width){
        if(points.size > 0){
            Draw.color(color);
            float[] items = points.items;
            int i = points.size - 6;
            float x1 = items[i], y1 = items[i + 1], w1 = items[i + 2], w = w1 * width / (points.size / 6) * i / 6f * 2f;
            if(w1 <= 0.001f) return;
            Draw.rect("hcircle", x1, y1, w, w, -Mathf.radDeg * lastAngle + 180f);
            Draw.reset();
        }
    }

    public void draw(Color color, float width){
        Draw.color(color);
        float[] items = points.items;
        float lastAngle = this.lastAngle;
        float size = width / (points.size / 6);

        for(int i = 0; i < points.size; i += 6){
            float x1 = items[i], y1 = items[i + 1], w1 = items[i + 2];
            float x2, y2, w2;

            //last position is always lastX/Y/W
            if(i < points.size - 6){
                x2 = items[i + 6];
                y2 = items[i + 7];
                w2 = items[i + 8];
            }else{
                x2 = lastX;
                y2 = lastY;
                w2 = lastW;
            }

            float z2 = -Angles.angleRad(x1, y1, x2, y2);
            //end of the trail (i = 0) has the same angle as the next.
            float z1 = i == 0 ? z2 : lastAngle;
            if(w1 <= 0.001f || w2 <= 0.001f) continue;

            float
                cx = Mathf.sin(z1) * i/6f * size * w1,
                cy = Mathf.cos(z1) * i/6f * size * w1,
                nx = Mathf.sin(z2) * (i/6f + 1) * size * w2,
                ny = Mathf.cos(z2) * (i/6f + 1) * size * w2;

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
            if(points.size >= 6){
                points.removeRange(0, 5);
            }

            counter %= 1f;
        }
    }

    /** Drifts all points by their individual velocities. Factors in Time.delta. */
    public void drift(){
        float[] items = points.items;
        for(int i = 0; i < points.size; i += 6){
            items[i] += items[i + 3] * Time.delta;
            items[i + 1] += items[i + 4] * Time.delta;

            float scl = Math.max(1f - items[i + 5] * Time.delta, 0);
            items[i + 3] *= scl;
            items[i + 4] *= scl;
        }

        if(points.size > 6){
            int last = points.size - 6;
            lastAngle = -Angles.angleRad(
                items[last], items[last + 1],
                items[last - 6], items[last - 5]
            );
        }
    }

    /** Adds a new point to the trail at intervals. */
    public void update(float x, float y, Vec2 v){
        update(x, y, 1f, v);
    }

    /** Adds a new point to the trail at intervals. */
    public void update(float x, float y, float width, Vec2 v){
        update(x, y, width, v, 0f);
    }

    /** Adds a new point to the trail at intervals. */
    public void update(float x, float y, float width, Vec2 v, float drag){
        //drift before so that the new point isn't immediately drifted
        drift();

        if((counter += Time.delta) >= 0.99f){
            if(points.size > length * 6){
                points.removeRange(0, 5);
            }

            points.addAll(x, y, width, v.x, v.y, drag);

            counter = 0f;
        }

        //update last position regardless, so it joins
        lastAngle = -Angles.angleRad(x, y, lastX, lastY);
        lastX = x;
        lastY = y;
        lastW = width;
    }
}
