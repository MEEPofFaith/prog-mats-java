package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;
import progressed.util.*;

import static arc.Core.*;
import static arc.math.Mathf.*;

public class Draw3D{
    /** Arbitrary value that translates horizontal distance in world units to camera offset height. */
    public static final float horiToVerti = 1f/48f/Vars.tilesize;
    public static final float zFadeBegin = 300f, zFadeEnd = 5000f;
    public static final float scaleFadeBegin = 1.5f, scaleFadeEnd = 7f;
    private static final Color tmpCol = new Color();

    public static void tube(float x, float y, float rad, float height, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        float angle = Math3D.tubeStartAngle(x, y, xHeight(x, height), yHeight(y, height), rad, rad * hScale(height));

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

    public static void slantTube(float x1, float y1, float x2, float y2, float z2, float rad, Color baseColor, Color topColor, float offset){
        //Properly set the offset with scale
        offset = 1f - offset;
        float hAlpha = scaleAlpha(hScale(z2 * offset));
        offset *= hAlpha;
        offset = 1f - offset;

        //Draw
        int verts = Lines.circleVertices(rad * hScale(z2));
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);
        float startAngle = Math3D.tubeStartAngle(xHeight(x2, z2), yHeight(y2, z2), x1, y1, rad * hScale(z2), rad);
        float[] castVerts = Math3D.castVertices(x1, y1, rotation, startAngle, tilt, rad, verts);
        float[] diskVerts = Math3D.diskVertices(x2, y2, z2, rotation, startAngle, tilt, rad, verts);
        float baseCol = baseColor.toFloatBits();
        float topCol = topColor.toFloatBits();
        for(int i = 0; i < verts - 1; i++){
            int i2 = i + 1;
            float bx1 = castVerts[i * 2],
                by1 = castVerts[i * 2 + 1],
                bx2 = castVerts[i2 * 2],
                by2 = castVerts[i2 * 2 + 1];
            float tz1 = diskVerts[i * 3 + 2],
                tz2 = diskVerts[i2 * 3 + 2];
            float tx1 = xHeight(diskVerts[i * 3], tz1),
                ty1 = yHeight(diskVerts[i * 3 + 1], tz1),
                tx2 = xHeight(diskVerts[i2 * 3], tz2),
                ty2 = yHeight(diskVerts[i2 * 3 + 1], tz2);
            if(offset > 0f){
                tx1 = Mathf.lerp(tx1, bx1, offset);
                ty1 = Mathf.lerp(ty1, by1, offset);
                tx2 = Mathf.lerp(tx2, bx2, offset);
                ty2 = Mathf.lerp(ty2, by2, offset);
            }

            Fill.quad(
                bx1, by1, baseCol,
                bx2, by2, baseCol,
                tx2, ty2, topCol,
                tx1, ty1, topCol
            );
        }
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z, float rad, Color baseColor, Color topColor){
        slantTube(x1, y1, x2, y2, z, rad, baseColor, topColor, 0f);
    }

    public static void line(float x1, float y1, float z1, float x2, float y2, float z2){
        Lines.line(
            xHeight(x1, z1), yHeight(y1, z1),
            xHeight(x2, z2), yHeight(y2, z2)
        );
    }

    public static void lineAngleBase(float x, float y, float height, float length, float rotation, float rotationOffset, float tilt){
        Math3D.rotate(Tmp.v31, length, rotation, rotationOffset, tilt);
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

    public static void drawDiskDebug(float x1, float y1, float x2, float y2, float z2, float rad){
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);

        Tmp.v31.set(Vec3.Z).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        Tmp.v32.set(rad, 0, 0).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);

        Tmp.v32.rotate(Tmp.v31, Time.time * 2f);

        //Disk
        Lines.stroke(3f);
        Draw.color(Color.white);
        float[] verts = Math3D.diskVertices(x2, y2, z2, rotation, 0f, tilt, rad, Lines.circleVertices(rad * hScale(z2)));
        Lines.beginLine();
        for(int i = 0; i < verts.length; i += 3){
            float vZ = verts[i + 2];
            Lines.linePoint(xHeight(verts[i], vZ), yHeight(verts[i + 1], vZ));
        }
        Lines.endLine(true);
        //Stuff
        Draw.color(Color.yellow);
        line(x2, y2, z2, x2 + Tmp.v31.x, y2 + Tmp.v31.y, z2 + Tmp.v31.z);
        Draw.color(Color.purple);
        line(x2, y2, z2, x2 + Tmp.v32.x, y2 + Tmp.v32.y, z2 + Tmp.v32.z);
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

    public static float zAlpha(float z){
        return 1f - Mathf.curve(z, zFadeBegin, zFadeEnd);
    }

    public static float scaleAlpha(float z){
        return 1f - Mathf.curve(hMul(z), scaleFadeBegin, scaleFadeEnd);
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
}
