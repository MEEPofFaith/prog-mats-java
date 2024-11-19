package progressed.graphics.draw3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import progressed.util.*;

import static mindustry.Vars.tilesize;

public class Lines3D{
    public static void line(float x1, float y1, float z1, float x2, float y2, float z2, int pointCount, boolean scale){
        float[] points = linePoints(x1, y1, z1, x2, y2, z2, pointCount);
        float lastAngle = 0;
        float stroke = Lines.getStroke() / 2f;
        Color color = Draw.getColor();
        for(int i = 0; i < points.length - 3; i += 3){
            float px1 = points[i], py1 = points[i + 1], pz1 = points[i + 2];
            float px2 = points[i + 3], py2 = points[i + 3 + 1], pz2 = points[i + 3 + 2];

            Vec2 pos = Perspective.drawPos(px1, py1, pz1);
            float dx1 = pos.x, dy1 = pos.y;
            pos = Perspective.drawPos(px2, py2, pz2);
            float dx2 = pos.x, dy2 = pos.y;

            float a2 = -Angles.angleRad(dx1, dy1, dx2, dy2);
            float a1 = i == 0 ? a2 : lastAngle;

            float w1 = scale ? Perspective.scale(px1, py1, pz1) : 1,
                w2 = scale ? Perspective.scale(px2, py2, pz2) : 1;

            float
                cx = Mathf.sin(a1) * stroke * w1,
                cy = Mathf.cos(a1) * stroke * w1,
                nx = Mathf.sin(a2) * stroke * w2,
                ny = Mathf.cos(a2) * stroke * w2;
            float c1 = Tmp.c1.set(color).mulA(Perspective.alpha(px1, py1, pz1)).toFloatBits(),
                c2 = Tmp.c1.set(color).mulA(Perspective.alpha(px2, py2, pz2)).toFloatBits();

            Fill.quad(
                dx1 - cx, dy1 - cy, c1,
                dx1 + cx, dy1 + cy, c1,
                dx2 + nx, dy2 + ny, c2,
                dx2 - nx, dy2 - ny, c2
            );

            lastAngle = a2;
        }
    }

    public static void line(float x1, float y1, float z1, float x2, float y2, float z2, boolean scale){
        line(x1, y1, z1, x2, y2, z2, linePointCount(x1, y1, z1, x2, y2, z2), scale);
    }

    public static void line(float x1, float y1, float z1, float x2, float y2, float z2){
        line(x1, y1, z1, x2, y2, z2, true);
    }

    public static void lineAngleBase(float x, float y, float z, float length, float rotation, float rotationOffset, float tilt){
        Math3D.rotate(Tmp.v31, length, rotation, rotationOffset, tilt);
        line(x, y, z, x + Tmp.v31.x, y + Tmp.v31.y, z + Tmp.v31.z);
    }

    public static int linePointCount(float x1, float y1, float z1, float x2, float y2, float z2){
        return (int)(Math3D.dst(x1, y1, z1, x2, y2, z2) / tilesize / tilesize);
    }

    public static float[] linePoints(float x1, float y1, float z1, float x2, float y2, float z2, int pointCount){
        if(z1 > z2){ //Always order from bottom to top. Needed for viewport check.
            float tx = x1, ty = y1, tz = z1;
            x1 = x2;
            y1 = y2;
            z1 = z2;
            x2 = tx;
            y2 = ty;
            z2 = tz;
        }

        float vz = Perspective.viewportZ();
        if(z2 > vz){ //If line goes above viewport, scale to viewport z.
            float scl = vz / (z2 - z1);
            x2 = x1 + (x2 - x1) * scl;
            y2 = y1 + (y2 - y1) * scl;
            z2 = vz;
            pointCount = Mathf.ceil(pointCount * scl);
        }

        float[] points = new float[pointCount * 3];
        float px = (x2 - x1) / (pointCount - 1);
        float py = (y2 - y1) / (pointCount - 1);
        float pz = (z2 - z1) / (pointCount - 1);

        for(int i = 0; i < pointCount; i++){
            points[i * 3] = x1 + px * i;
            points[i * 3 + 1] = y1 + py * i;
            points[i * 3 + 2] = z1 + pz * i;
        }

        return points;
    }
}
