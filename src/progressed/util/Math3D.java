package progressed.util;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;

import static arc.math.Mathf.*;

public class Math3D{
    private static final Vec2 vec = new Vec2();
    private static final Vec2 vresult = new Vec2();
    private static final Vec3 axis = new Vec3();
    private static final Vec3 rim = new Vec3();

    /** Properly rotates and tilts up a 3D vector.
     * @param vec3 Vec3 to write output to.
     * @param length Length of the vector.
     * @param rotation Angle of the main angle.
     * @param rotationOffset Rotational offset from the main angle.
     * @param tilt 3D tilt. Tilts around the axis 90* of the main angle.
     */
    public static Vec3 rotate(Vec3 vec3, float length, float rotation, float rotationOffset, float tilt){
        return vec3.set(Angles.trnsx(rotationOffset, length), Angles.trnsy(rotationOffset, length), 0f)
            .rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
    }

    public static float[] diskVertices(float x, float y, float z, float rotation, float startAngle, float tilt, float rad, int verts){
        float[] diskVerts = new float[verts * 3];
        float space = 360f / (verts - 1f);
        axis.set(Vec3.Z).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        rim.set(rad, 0, 0).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        rim.rotate(axis, rotation - startAngle);

        for(int i = 0; i < verts * 3; i += 3){
            diskVerts[i] = x + rim.x;
            diskVerts[i + 1] = y + rim.y;
            diskVerts[i + 2] = z + rim.z;
            rim.rotate(axis, space);
        }
        return diskVerts;
    }

    public static float[] castVertices(float x, float y, float rotation, float startAngle, float tilt, float rad, int verts){
        float[] castVerts = new float[verts * 2];
        float space = 360f / (verts - 1f);
        float scl = 1f + Mathf.sinDeg(tilt);

        for(int i = 0; i < verts; i++){
            float angle = startAngle + space * i - rotation;
            vec.trns(rotation, Mathf.cosDeg(angle) * rad * scl, Mathf.sinDeg(angle) * rad);
            castVerts[i * 2] = x + vec.x;
            castVerts[i * 2 + 1] = y + vec.y;
        }
        return castVerts;
    }

    /**
     * Calculates of intercept of a stationary and moving target. Do not call from multiple threads!
     * @param srcx X of shooter
     * @param srcy Y of shooter
     * @param dstx X of target
     * @param dsty Y of target
     * @param dstvx X velocity of target (subtract shooter X velocity if needed)
     * @param dstvy Y velocity of target (subtract shooter Y velocity if needed)
     * @param accel constant acceleration of bullet
     * @return the intercept location
     */
    public static Vec2 intercept(float srcx, float srcy, float dstx, float dsty, float dstvx, float dstvy, float accel){
        //TODO come back once I learn parametrics
        return vresult.set(dstx, dsty);
    }

    public static Vec2 intercept(Position src, Position dst, float accel){
        float ddx = 0, ddy = 0;
        if(dst instanceof Hitboxc h){
            ddx += h.deltaX();
            ddy += h.deltaY();
        }
        if(src instanceof Hitboxc h){
            ddx -= h.deltaX();
            ddy -= h.deltaY();
        }
        return intercept(src.getX(), src.getY(), dst.getX(), dst.getY(), ddx, ddy, accel);
    }

    public static Vec2 inaccuracy(float inaccuracy){
        PMMathf.randomCirclePoint(vec, inaccuracy);
        return vec;
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
