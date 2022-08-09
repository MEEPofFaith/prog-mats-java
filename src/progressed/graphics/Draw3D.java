package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;

import static arc.Core.*;
import static arc.math.Mathf.*;

public class Draw3D{
    /**
     * @author sunny, modified by MEEP
     * */
    public static void wall(float x1, float y1, float x2, float y2, float baseHeight, float height, Color baseColor, Color topColor){
        float c1f = baseColor.toFloatBits();
        float c2f = topColor.toFloatBits();
        float x3 = xHeight(x1, height);
        float y3 = yHeight(y1, height);
        float x4 = xHeight(x2, height);
        float y4 = yHeight(y2, height);
        x1 += xOffset(x1, baseHeight);
        y1 += yOffset(y1, baseHeight);
        x2 += xOffset(x2, baseHeight);
        y2 += yOffset(y2, baseHeight);

        Fill.quad(x1, y1, c1f, x2, y2, c1f, x4, y4, c2f, x3, y3, c2f);
    }

    public static void wall(float x1, float y1, float x2, float y2, float height, Color baseColor, Color topColor){
        wall(x1, y1, x2, y2, 0f, height, baseColor, topColor);
    }

    public static void ring(float x, float y, float rad, float height, Color baseColor, Color topColor){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        float angle = startAngle(x, y, rad, height);

        float c1f = baseColor.toFloatBits();
        float c2f = topColor.toFloatBits();

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;

            float x3 = xHeight(x1, height),
                y3 = yHeight(y1, height),
                x4 = xHeight(x2, height),
                y4 = yHeight(y2, height);

            Fill.quad(x1, y1, c1f, x2, y2, c1f, x4, y4, c2f, x3, y3, c2f);
        }
    }

    public static void cylinder(float x, float y, float rad, float height, Color baseColor, Color topColor){
        int vert = Lines.circleVertices(rad);

        Draw.color(baseColor);
        Fill.poly(x, y, vert, rad);
        Draw.color();

        ring(x, y, rad, height, baseColor, topColor);

        Draw.color(topColor);
        Fill.poly(xHeight(x, height), yHeight(y, height), vert, rad * (1 + height));
        Draw.color();
    }

    public static float xHeight(float x, float height){
        if(height == 0) return x;
        return x + xOffset(x, height);
    }

    public static float yHeight(float y, float height){
        if(height == 0) return y;
        return y + yOffset(y, height);
    }

    public static float xOffset(float x, float height){
        return (x - camera.position.x) * height;
    }

    public static float yOffset(float y, float height){
        return (y - camera.position.y) * height;
    }

    /**
     * See DriveBelt#drawBelt in AvantTeam/ProjectUnityPublic
     * @author Xelo
     */
    static float startAngle(float x, float y, float rad, float height){
        float x2 = xHeight(x, height), y2 = yHeight(y, height), rad2 = rad * (1f + height);

        float d = dst(x2 - x,y2 - y);
        float f = sqrt(d * d - sqr(rad2 - rad));
        float a = rad > rad2 ? atan2(rad - rad2, f) : (rad < rad2 ? pi - atan2(rad2 - rad, f) : halfPi);
        Tmp.v1.set(x2 - x, y2 - y).scl(1f / d); //normal
        Tmp.v2.set(Tmp.v1).rotateRad(pi - a).scl(-rad2).add(x2, y2); //tangent

        return Angles.angle(x2, y2, Tmp.v2.x, Tmp.v2.y);
    }
}
