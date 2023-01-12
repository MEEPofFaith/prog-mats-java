package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;

import static arc.Core.*;
import static arc.math.Mathf.*;

public class DrawPseudo3D{
    private static final Color tmpCol = new Color();
    
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

    public static void tube(float x, float y, float rad, float height, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        float angle = tubeStartAngle(x, y, xHeight(x, height), yHeight(y, height), rad, rad * hScale(height));

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
            
            float cLerp1 = 1f - Angles.angleDist(a, 45f) / 180f,
                cLerp2 = 1f - Angles.angleDist(a + space, 45f) / 180f;
            float bc1f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp1).toFloatBits(),
                tc1f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp1).toFloatBits(),
                bc2f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp2).toFloatBits(),
                tc2f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp2).toFloatBits();

            Fill.quad(x1, y1, bc1f, x2, y2, bc2f, x4, y4, tc2f, x3, y3, tc1f);
        }
    }

    public static void tube(float x, float y, float rad, float height, Color baseColor, Color topColor){
        tube(x, y, rad, height, baseColor, baseColor, topColor, topColor);
    }

    public static void cylinder(float x, float y, float rad, float height, Color baseColor, Color topColor){
        int vert = Lines.circleVertices(rad);

        Draw.color(baseColor);
        Fill.poly(x, y, vert, rad);
        Draw.color();

        tube(x, y, rad, height, baseColor, topColor);

        Draw.color(topColor);
        Fill.poly(xHeight(x, height), yHeight(y, height), vert, rad * (1 + height));
        Draw.color();
    }

    public static void ring(float x, float y, float baseRad, float rad, float outRad, float height, Color inColor, Color outColor){
        if(rad < baseRad) return;

        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        float angle = 90f * baseRad / rad,
            angleFrom = Angles.angle(camera.position.x, camera.position.y, x, y);

        float c1f = inColor.toFloatBits();
        float c2f = outColor.toFloatBits();

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = xHeight(x + rad * cos, height),
                y1 = yHeight(y + rad * sin, height),
                x2 = xHeight(x + rad * cos2, height),
                y2 = yHeight(y + rad * sin2, height),
                x3 = xHeight(x + outRad * cos, height),
                y3 = yHeight(y + outRad * sin, height),
                x4 = xHeight(x + outRad * cos2, height),
                y4 = yHeight(y + outRad * sin2, height);

            float z = Draw.z();
            if(Angles.within(angleFrom, a, Angles.angleDist(angleFrom + 180, angle + space))){
                Draw.z(z - 0.01f);
            }else{
                Draw.z(z + 0.01f);
            }

            Fill.quad(x1, y1, c1f, x2, y2, c1f, x4, y4, c2f, x3, y3, c2f);

            Draw.z(z);
        }
    }

    public static void slantCylinder(float x1, float y1, float x2, float y2, float rad, float height, Color baseColor, Color topColor){
        int vert = Lines.circleVertices(rad);

        Draw.color(baseColor);
        Fill.poly(x1, y1, vert, rad);
        Draw.color();

        float space = 360f / vert;
        float angle = tubeStartAngle(x1, y1, xHeight(x2, height), yHeight(y2, height), rad, rad * hScale(height));

        float c1f = baseColor.toFloatBits();
        float c2f = topColor.toFloatBits();

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float dx1 = x1 + rad * cos,
                dy1 = y1 + rad * sin,
                dx2 = x1 + rad * cos2,
                dy2 = y1 + rad * sin2;

            float dx3 = xHeight(x2 + rad * cos, height),
                dy3 = yHeight(y2 + rad * sin, height),
                dx4 = xHeight(x2 + rad * cos2, height),
                dy4 = yHeight(y2 + rad * sin2, height);

            Fill.quad(dx1, dy1, c1f, dx2, dy2, c1f, dx4, dy4, c2f, dx3, dy3, c2f);
        }

        Draw.color(topColor);
        Fill.poly(xHeight(x2, height), yHeight(y2, height), vert, rad * (1 + height));
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
        return (x - camera.position.x) * height * Vars.renderer.getDisplayScale();
    }

    public static float yOffset(float y, float height){
        return (y - camera.position.y) * height * Vars.renderer.getDisplayScale();
    }

    public static float hScale(float height){
        return 1f + hMul(height);
    }

    public static float hMul(float height){
        return height * Vars.renderer.getDisplayScale();
    }

    /**
     * See DriveBelt#drawBelt in AvantTeam/ProjectUnityPublic
     * @author Xelo
     */
    static float tubeStartAngle(float x1, float y1, float x2, float y2, float rad1, float rad2){
        if(x1 == x2 && y1 == y2) return 0f;

        float d = dst(x2 - x1,y2 - y1);
        float f = sqrt(d * d - sqr(rad2 - rad1));
        float a = rad1 > rad2 ? atan2(rad1 - rad2, f) : (rad1 < rad2 ? pi - atan2(rad2 - rad1, f) : halfPi);
        Tmp.v1.set(x2 - x1, y2 - y1).scl(1f / d); //normal
        Tmp.v2.set(Tmp.v1).rotateRad(pi - a).scl(-rad2).add(x2, y2); //tangent

        return Angles.angle(x2, y2, Tmp.v2.x, Tmp.v2.y);
    }
}
