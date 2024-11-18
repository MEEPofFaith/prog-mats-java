package progressed.graphics.draw3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import progressed.graphics.*;

import static arc.math.Mathf.*;

public class Fill3D{
    private static final Color tmpCol = new Color();
    public static final Vec2 vec = new Vec2();
    private static final Vec3 axis = new Vec3();
    private static final Vec3 rim = new Vec3();

    public static void tube(float x, float y, float rad, float z2, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        Vec2 pos = Perspective.drawPos(x, y, z2);
        float angle = tubeStartAngle(x, y, pos.x, pos.y, rad, rad * Perspective.scale(x, y, z2));

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;

            pos = Perspective.drawPos(x1, y1, z2);
            float x3 = pos.x,
                y3 = pos.y;
            pos = Perspective.drawPos(x2, y2, z2);
            float x4 = pos.x,
                y4 = pos.y;

            float cLerp1 = 1f - Angles.angleDist(a, 45f) / 180f,
                cLerp2 = 1f - Angles.angleDist(a + space, 45f) / 180f;
            float bc1f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp1).toFloatBits(),
                tc1f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp1).toFloatBits(),
                bc2f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp2).toFloatBits(),
                tc2f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp2).toFloatBits();

            Fill.quad(x1, y1, bc1f, x2, y2, bc2f, x4, y4, tc2f, x3, y3, tc1f);
        }
    }

    public static void tube(float x, float y, float rad, float z2, Color baseColor, Color topColor){
        tube(x, y, rad, z2, baseColor, baseColor, topColor, topColor);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z2, float rad, Color baseColor, Color topColor, float offset){
        //Draw
        float scl = Perspective.scale(x2, y2, z2);
        int verts = Lines.circleVertices(rad * scl);
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);
        Vec2 pos = Perspective.drawPos(x2, y2, z2);
        float startAngle = tubeStartAngle(pos.x, pos.y, x1, y1, rad * scl, rad);
        float[] castVerts = castVertices(x1, y1, rotation, startAngle, tilt, rad, verts);
        float[] diskVerts = diskVertices(x2, y2, z2, rotation, startAngle, tilt, rad, verts);
        float hAlpha = Perspective.alpha(x2, y2, z2 * offset);
        float baseCol = Tmp.c1.set(baseColor).mulA(hAlpha).toFloatBits();
        float topCol = Tmp.c1.set(topColor).mulA(hAlpha).toFloatBits();
        for(int i = 0; i < verts - 1; i++){
            int i2 = i + 1;
            float bx1 = castVerts[i * 2],
                by1 = castVerts[i * 2 + 1],
                bx2 = castVerts[i2 * 2],
                by2 = castVerts[i2 * 2 + 1];
            Tmp.v1.set(Perspective.drawPos(diskVerts[i * 3], diskVerts[i * 3 + 1], diskVerts[i * 3 + 2]));
            Tmp.v2.set(Perspective.drawPos(diskVerts[i2 * 3], diskVerts[i2 * 3 + 1], diskVerts[i2 * 3 + 2]));
            if(offset > 0f){
                Tmp.v1.lerp(bx1, by1, offset);
                Tmp.v2.lerp(bx2, by2, offset);
            }

            Fill.quad(
                bx1, by1, baseCol,
                bx2, by2, baseCol,
                Tmp.v2.x, Tmp.v2.y, topCol,
                Tmp.v1.x, Tmp.v1.y, topCol
            );
        }
        //Debug
        Draw.z(PMLayer.skyBloom + 10);
        Lines.stroke(4);
        Draw.color(Color.black);
        Lines.line(x1, y1, castVerts[0], castVerts[1]);
        Lines3D.line(x2, y2, z2, diskVerts[0], diskVerts[1], diskVerts[2]);
        Lines3D.line(castVerts[0], castVerts[1], 0, diskVerts[0], diskVerts[1], diskVerts[2]);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z, float rad, Color baseColor, Color topColor){
        slantTube(x1, y1, x2, y2, z, rad, baseColor, topColor, 0f);
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
