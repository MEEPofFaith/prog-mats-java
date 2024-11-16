package progressed.util;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;

import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public class Math3D{
    private static final Vec2 vec = new Vec2();
    private static final Vec2 vresult = new Vec2();
    private static final Vec3 axis = new Vec3();
    private static final Vec3 rim = new Vec3();

    /** Properly rotates and tilts up a 3D vector.
     * @param vec3 Vec3 to write output to.
     * @param length Length of the vector.
     * @param yaw Angle of the main angle.
     * @param yawOffset Rotational offset from the main angle.
     * @param pitch Pitch. Tilts around the axis 90* of the main angle.
     */
    public static Vec3 rotate(Vec3 vec3, float length, float yaw, float yawOffset, float pitch){
        return vec3.set(Angles.trnsx(yawOffset, length), Angles.trnsy(yawOffset, length), 0f)
            .rotate(Vec3.Y, pitch).rotate(Vec3.Z, -yaw);
    }

    public static int linePointCounts(float x1, float y1, float z1, float x2, float y2, float z2){
        return (int)(dst(x1, y1, z1, x2, y2, z2) / tilesize / tilesize);
    }

    public static float[] linePoints(float x1, float y1, float z1, float x2, float y2, float z2, int pointCount){
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
     * Calculates of intercept of a stationary and moving target. Do not call from multiple threads!
     * @param srcx X of shooter
     * @param srcy Y of shooter
     * @param dstx X of target
     * @param dsty Y of target
     * @param dstvx X velocity of target (subtract shooter X velocity if needed)
     * @param dstvy Y velocity of target (subtract shooter Y velocity if needed)
     * @param ba constant acceleration of the bullet
     * @param bv initial velocity of the bullet
     * @return the intercept location
     */
    public static Vec2 intercept(float srcx, float srcy, float dstx, float dsty, float dstvx, float dstvy, float ba, float bv){
        dstvx /= Time.delta;
        dstvy /= Time.delta;
        float dx = dstx - srcx,
            dy = dsty - srcy;
        float uv = dstvx * dstvx + dstvy * dstvy,
            ud = dx * dstvx + dy * dstvy;

        // Get quartic components
        float a = -(ba * ba) / 4;
        float b = -ba * bv;
        float c = uv - (bv * bv);
        float d = 2 * ud;
        float e = dx * dx + dy * dy;

        // Solve
        float[] ts = PMMathf.quartic(a, b, c, d, e);

        // Find smallest positive solution
        Vec2 sol = vresult.set(dstx, dsty);
        float min = Float.MAX_VALUE;
        for(float t : ts){
            if(t >= 0 && t < min) min = t;
        }
        if(min < Float.MAX_VALUE) sol.set(dstx + dstvx * min, dsty + dstvy * min);

        return sol;
    }

    public static Vec2 intercept(Position src, Position dst, float ba, float bv){
        float ddx = 0, ddy = 0;
        if(dst instanceof Hitboxc h){
            ddx += h.deltaX();
            ddy += h.deltaY();
        }
        if(src instanceof Hitboxc h){
            ddx -= h.deltaX();
            ddy -= h.deltaY();
        }
        if(ddx == 0 && ddy == 0) return vresult.set(dst); //Don't bother performing unnecessary math if no prediction is needed.
        return intercept(src.getX(), src.getY(), dst.getX(), dst.getY(), ddx, ddy, ba, bv);
    }

    public static Vec2 inaccuracy(float inaccuracy){
        PMMathf.randomCirclePoint(vec, inaccuracy);
        return vec;
    }

    /** Returns only a single float if dst is beyond where it can reach.
     * Otherwise, the float array has two terms: 1st is the lower trajectory, 2nd is the higher trajectory */
    public static float[] shootAngle(float dst, float g, float v){
        float v2 = v * v;
        float in = ((v2*v2) / (g*g * dst*dst)) - 1;

        if(in < 0) return new float[]{Mathf.pi / 4};

        float a = sqrt(in);
        float b = v2 / (g * a);

        return new float[]{
            (float)Math.atan(b + a),
            (float)Math.atan(b - a)
        };
    }

    //See my notebook for half the calculation. Oh wait, you don't have access to it because I physically hold it.
    //And apparently neither do I; I forgot to bring it with me to my dorm.
    public static float homingPitch(float x1, float y1, float z1, float x2, float y2, float v2, float a, float g){
        float dst = Mathf.dst(x1, y1, x2, y2);

        float p1 = 2 * g * z1;
        float p2 = 2 * a * dst;
        float p3 = -a * z1 - g * dst;
        float p4 = (p3 * p3 - p1 * p2) / v2; //v2 = v * v

        //Also thanks to asimplebeginner for helping with the second half of the calculation.
        float A = p2 - p1;
        float B = p3;
        float C = p4 - p1;

        float D = sign(B) * sqrt(0.25f * A * A + B * B);
        float E = (float)(Math.atan(0.5f * A / B)) / 2f;

        return (float)(-Math.asin((C - 0.5f * A) / D)) / 2 - E;
    }

    public static float dst(float x, float y, float z){
        return sqrt(x * x + y * y + z * z);
    }

    public static float dst(float x1, float y1, float z1, float x2, float y2, float z2){
        float xd = x2 - x1;
        float yd = y2 - y1;
        float zd = z2 - z1;
        return sqrt(xd * xd + yd * yd + zd * zd);
    }

    /**
     * See DriveBelt#drawBelt in AvantTeam/ProjectUnityPublic
     * @author Xelo
     */
    public static float tubeStartAngle(float x1, float y1, float x2, float y2, float rad1, float rad2){
        if(x1 == x2 && y1 == y2) return 0f;

        float d = Mathf.dst(x2 - x1,y2 - y1);
        float f = sqrt(d * d - sqr(rad2 - rad1));
        float a = rad1 > rad2 ? atan2(rad1 - rad2, f) : (rad1 < rad2 ? pi - atan2(rad2 - rad1, f) : halfPi);
        Tmp.v1.set(x2 - x1, y2 - y1).scl(1f / d); //normal
        Tmp.v2.set(Tmp.v1).rotateRad(pi - a).scl(-rad2).add(x2, y2); //tangent

        return Angles.angle(x2, y2, Tmp.v2.x, Tmp.v2.y);
    }
}
