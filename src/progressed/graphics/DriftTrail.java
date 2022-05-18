package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/** Due to the amount of differences, I will not extend off of {@link Trail} for this. */
public class DriftTrail{
    public int length;

    private final Seq<DriftTrailData> points;
    private float lastX = -1, lastY = -1, lastAngle = -1, lastW = 0f, counter = 0f;

    public DriftTrail(int length){
        this.length = length;
        points = new Seq<>(length);
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
        return points.size;
    }

    public void drawCap(Color color, float width){
        if(points.size > 0){
            Draw.color(color);
            DriftTrailData d = points.peek();
            float x1 = d.x, y1 = d.y, w1 = d.w, w = w1 * width / points.size * (points.size - 1) * 2f;
            if(w1 <= 0.001f) return;
            Draw.rect("hcircle", x1, y1, w, w, -Mathf.radDeg * lastAngle + 180f);
            Draw.reset();
        }
    }

    public void draw(Color color, float width){
        Draw.color(color);
        float lastAngle = this.lastAngle;
        float size = width / points.size;

        for(int i = 0; i < points.size - 1; i++){
            DriftTrailData d1 = points.get(i), d2 = points.get(i + 1);
            float x1 = d1.x, y1 = d1.y, w1 = d1.w;
            float x2, y2, w2;

            //last position is always lastX/Y/W
            if(i < points.size){
                x2 = d2.x;
                y2 = d2.y;
                w2 = d2.w;
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
                cx = Mathf.sin(z1) * i * size * w1,
                cy = Mathf.cos(z1) * i * size * w1,
                nx = Mathf.sin(z2) * (i + 1) * size * w2,
                ny = Mathf.cos(z2) * (i + 1) * size * w2;

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
        if((counter += Time.delta) >= 0.99f){
            if(points.size >= 1){
                points.remove(0);
            }

            counter = 0f;
        }
    }

    /** Drifts all points by their individual velocities. */
    public void drift(){
        points.each(DriftTrailData::drift);

        if(points.size > 2){
            lastAngle = -Angles.angleRad(
                points.peek().x, points.peek().y,
                points.get(points.size - 2).x, points.get(points.size - 2).y
            );
        }
    }

    /** Adds a new point to the trail at intervals. */
    public void update(float x, float y, Vec2 v){
        update(x, y, 1f, v);
    }

    /** Adds a new point to the trail at intervals. */
    public void update(float x, float y, float width, Vec2 v){
        //drift before so that the new point isn't immediately drifted
        drift();

        if((counter += Time.delta) >= 0.99f){
            if(points.size > length){
                points.remove(0);
            }

            points.add(new DriftTrailData(x, y, width, v));

            counter = 0f;
        }

        //update last position regardless, so it joins
        lastAngle = -Angles.angleRad(x, y, lastX, lastY);
        lastX = x;
        lastY = y;
        lastW = width;
    }

    //I don't want to use a giant FloatSeq, just doesn't look as nice
    public static class DriftTrailData{
        public float x, y, w, dx, dy;

        public DriftTrailData(float x, float y, float w, Vec2 v){
            this.x = x;
            this.y = y;
            this.w = w;
            dx = v.x;
            dy = v.y;
        }

        public void drift(){
            x += dx * Time.delta;
            y += dy * Time.delta;
        }
    }
}
