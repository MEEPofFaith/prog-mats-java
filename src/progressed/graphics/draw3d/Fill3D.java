package progressed.graphics.draw3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

import static arc.math.Mathf.*;

public class Fill3D{
    private static final Color tmpCol = new Color();
    public static final Vec2 vec = new Vec2();
    private static final Vec3 axis = new Vec3();
    private static final Vec3 rim = new Vec3();

    public static void quad(float x1, float y1, float z1, Color c1,
                            float x2, float y2, float z2, Color c2,
                            float x3, float y3, float z3, Color c3,
                            float x4, float y4, float z4, Color c4){
        Vec2 pos = Perspective.drawPos(x1, y1, z1);
        float dx1 = pos.x, dy1 = pos.y, a1 = Perspective.alpha(x1, y1, z1);
        pos = Perspective.drawPos(x2, y2, z2);
        float dx2 = pos.x, dy2 = pos.y, a2 = Perspective.alpha(x2, y2, z2);
        pos = Perspective.drawPos(x3, y3, z3);
        float dx3 = pos.x, dy3 = pos.y, a3 = Perspective.alpha(x3, y3, z3);
        pos = Perspective.drawPos(x4, y4, z4);
        float dx4 = pos.x, dy4 = pos.y, a4 = Perspective.alpha(x4, y4, z4);

        Fill.quad(
            dx1, dy1, tmpCol.set(c1).mulA(a1).toFloatBits(),
            dx2, dy2, tmpCol.set(c2).mulA(a2).toFloatBits(),
            dx3, dy3, tmpCol.set(c3).mulA(a3).toFloatBits(),
            dx4, dy4, tmpCol.set(c4).mulA(a4).toFloatBits()
        );
    }

    public static void quad(float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4){
        Color color = Draw.getColor();
        quad(x1, y1, z1, color, x2, y2, z2, color, x3, y3, z3, color, x4, y4, z4, color);
    }

    public static void tube(float x, float y, float rad, float z2, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        float scl = Perspective.scale(x, y, z2);
        if(scl < 0) return;

        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        Vec2 pos = Perspective.drawPos(x, y, z2);
        float angle = tubeStartAngle(x, y, pos.x, pos.y, rad, rad * scl);

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;
            
            float cLerp1 = 1f - Angles.angleDist(a, 45f) / 180f,
                cLerp2 = 1f - Angles.angleDist(a + space, 45f) / 180f;
            Color bc1f = Tmp.c1.set(baseColorLight).lerp(baseColorDark, cLerp1),
                tc1f = Tmp.c2.set(topColorLight).lerp(topColorDark, cLerp1),
                bc2f = Tmp.c3.set(baseColorLight).lerp(baseColorDark, cLerp2),
                tc2f = Tmp.c4.set(topColorLight).lerp(topColorDark, cLerp2);

            quad(
                x1, y1, 0, bc1f,
                x2, y2, 0, bc2f,
                x2, y2, z2, tc2f,
                x1, y1, z2, tc1f
            );
        }
    }

    public static void tube(float x, float y, float rad, float z2, Color baseColor, Color topColor){
        tube(x, y, rad, z2, baseColor, baseColor, topColor, topColor);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z2, float rad, Color baseColor, Color topColor){
        //Draw
        float scl = Perspective.scale(x2, y2, z2);
        if(scl < 0) return;

        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);
        Vec2 pos = Perspective.drawPos(x2, y2, z2);
        float startAngle = tubeStartAngle(pos.x, pos.y, x1, y1, rad * scl, rad);

        int verts = Lines.circleVertices(rad * scl);
        float[] castVerts = castVertices(x1, y1, rotation, startAngle, tilt, rad, verts);
        float[] diskVerts = diskVertices(x2, y2, z2, rotation, startAngle, tilt, rad, verts);
        int segments = Lines3D.linePointCount(x1, y1, 0, x2, y2, z2);

        for(int i = 0; i < verts; i++){
            float[] v1 = Lines3D.linePoints(
                castVerts[i * 2], castVerts[i * 2 + 1], 0,
                diskVerts[i * 3], diskVerts[i * 3 + 1], diskVerts[i * 3 + 2], segments
            );
            int i2 = i == verts - 1 ? 0 : i + 1;
            float[] v2 = Lines3D.linePoints(
                castVerts[i2 * 2], castVerts[i2 * 2 + 1], 0,
                diskVerts[i2 * 3], diskVerts[i2 * 3 + 1], diskVerts[i2 * 3 + 2], segments
            );
            for(int j = 0; j < segments - 3; j++){
                int jj = j * 3;
                Color c1 = Tmp.c1.set(baseColor).lerp(topColor, (float)jj / segments);
                Color c2 = Tmp.c2.set(baseColor).lerp(topColor, (float)(jj + 3) / segments);
                quad(
                    v1[jj], v1[jj + 1], v1[jj + 2], c1,
                    v2[jj], v2[jj + 1], v2[jj + 2], c1,
                    v2[jj + 3], v2[jj + 3 + 1], v2[jj + 3 + 2], c2,
                    v1[jj + 3], v1[jj + 3 + 1], v1[jj + 3 + 2], c2
                );
            }
        }
    }

    public static float[] diskVertices(float x, float y, float z, float rotation, float startAngle, float tilt, float rad, int verts){
        float[] diskVerts = new float[(verts + 1) * 3];
        float space = 360f / verts;
        axis.set(Vec3.Z).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        rim.set(rad, 0, 0).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        rim.rotate(axis, rotation - startAngle);

        for(int i = 0; i <= verts; i ++){
            diskVerts[i * 3] = x + rim.x;
            diskVerts[i * 3 + 1] = y + rim.y;
            diskVerts[i * 3 + 2] = z + rim.z;
            rim.rotate(axis, space);
        }
        return diskVerts;
    }

    public static float[] castVertices(float x, float y, float rotation, float startAngle, float tilt, float rad, int verts){
        float[] castVerts = new float[verts * 2];
        float space = 360f / (verts - 1f);
        float scl = 1f + sinDeg(tilt);

        for(int i = 0; i < verts; i++){
            float angle = startAngle + space * i - rotation;
            vec.trns(rotation, cosDeg(angle) * rad * scl, sinDeg(angle) * rad);
            castVerts[i * 2] = x + vec.x;
            castVerts[i * 2 + 1] = y + vec.y;
        }
        return castVerts;
    }

    /**
     * See DriveBelt#drawBelt in AvantTeam/ProjectUnityPublic
     * @author Xelo
     */
    public static float tubeStartAngle(float x1, float y1, float x2, float y2, float rad1, float rad2){
        if(x1 == x2 && y1 == y2) return 0f;

        float d = dst(x2 - x1,y2 - y1);
        float f = sqrt(d * d - sqr(rad2 - rad1));
        float a = rad1 > rad2 ? atan2(rad1 - rad2, f) : (rad1 < rad2 ? pi - atan2(rad2 - rad1, f) : halfPi);
        Tmp.v1.set(x2 - x1, y2 - y1).scl(1f / d); //normal
        Tmp.v2.set(Tmp.v1).rotateRad(pi - a).scl(-rad2).add(x2, y2); //tangent

        return Angles.angle(x2, y2, Tmp.v2.x, Tmp.v2.y);
    }
}
