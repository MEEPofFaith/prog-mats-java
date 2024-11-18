package progressed.util;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import progressed.graphics.perspective.*;

import static arc.math.Mathf.*;

public class Math3D{
    private static final Vec2 vresult = new Vec2();

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
        PMMathf.randomCirclePoint(Fill3D.vec, inaccuracy);
        return Fill3D.vec;
    }

    //See my notebook for half the calculation. Oh wait, you don't have access to it because I physically hold it.
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

}
