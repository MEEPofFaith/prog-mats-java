package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static arc.math.Mathf.*;

public class DrawPseudo3D{
    /** Translates horizontal distance in world units to camera offset height. Somewhat arbitrary. */
    public static final float horiToVerti = 1f/48f/Vars.tilesize;
    private static final Color tmpCol = new Color();
    private static final Vec3 tiltVec = new Vec3();
    
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
        Fill.poly(x, y, vert, rad); //TODO polygon with shading
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

    public static void slantTube(float x1, float y1, float x2, float y2, float rad, float height, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);

        float space = 360f / vert;
        float angle = tubeStartAngle(x1, y1, xHeight(x2, height), yHeight(y2, height), rad, rad * hScale(height));

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

            float cLerp1 = Angles.angleDist(a, 45f) / 180f,
                cLerp2 = Angles.angleDist(a + space, 45f) / 180f;
            float bc1f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp1).toFloatBits(),
                tc1f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp1).toFloatBits(),
                bc2f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp2).toFloatBits(),
                tc2f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp2).toFloatBits();

            Fill.quad(dx1, dy1, bc1f, dx2, dy2, bc2f, dx4, dy4, tc2f, dx3, dy3, tc1f);
        }
    }

    public static void lineAngleBase(float x, float y, float height, float length, float rotation, float rotationOffset, float tilt){
        rotate(Tmp.v31, length, rotation, rotationOffset, tilt);
        float h2 = height + Tmp.v31.z;
        float x1 = xHeight(x, height);
        float y1 = yHeight(y, height);
        float x2 = xHeight(x + Tmp.v31.x, h2);
        float y2 = yHeight(y + Tmp.v31.y, h2);
        Lines.line(x1, y1, x2, y2);
    }

    public static void drawAimDebug(float x, float y, float height, float length, float rotation, float tilt, float spread){
        Lines.stroke(3f);
        Draw.color(Color.blue); //Down
        lineAngleBase(x, y, height, length, rotation, 0f, tilt - spread);
        Lines.stroke(6f);
        Draw.color(Pal.accent); //Center
        lineAngleBase(x, y, height, length, rotation, 0f, tilt);
        Lines.stroke(3f);
        Draw.color(Color.red); //Right
        lineAngleBase(x, y, height, length, rotation, -spread, tilt);
        Draw.color(Color.lime); //Left
        lineAngleBase(x, y, height, length, rotation, spread, tilt);
        Draw.color(Color.orange); //Up
        lineAngleBase(x, y, height, length, rotation, 0f, tilt + spread);
    }

    public static void rotate(Vec3 vec3, float length, float rotation, float rotationOffset, float tilt){
        tiltVec.set(0, 1, 0).rotate(Vec3.Z, -rotation);
        vec3.set(length, 0, 0).rotate(Vec3.Z, -rotationOffset).rotate(Vec3.Z, -rotation).rotate(tiltVec, tilt);
    }

    public static float xHeight(float x, float height){
        if(height <= 0) return x;
        return x + xOffset(x, height);
    }

    public static float yHeight(float y, float height){
        if(height <= 0) return y;
        return y + yOffset(y, height);
    }

    public static float xOffset(float x, float height){
        return (x - camera.position.x) * hMul(height);
    }

    public static float yOffset(float y, float height){
        return (y - camera.position.y) * hMul(height);
    }

    public static float hScale(float height){
        return 1f + hMul(height);
    }

    public static float hMul(float height){
        return height(height) * Vars.renderer.getDisplayScale();
    }

    public static float height(float height){
        return height * horiToVerti;
    }

    public static float layerOffset(float x, float y){
        float max = Math.max(camera.width, camera.height);
        return -dst(x, y, camera.position.x, camera.position.y) / max / 1000f;
    }

    public static float layerOffset(float cx, float cy, float tx, float ty){
        float angleTo = Angles.angle(cx, cy, tx, ty),
            angleCam = Angles.angle(cx, cy, camera.position.x, camera.position.y);
        float angleDist = Angles.angleDist(angleTo, angleCam);
        float max = Math.max(camera.width, camera.height);

        return layerOffset(cx, cy) + dst(cx, cy, tx, ty) * cosDeg(angleDist) / max / 1000f;
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
