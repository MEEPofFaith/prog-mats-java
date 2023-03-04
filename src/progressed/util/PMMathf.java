package progressed.util;

import arc.math.*;
import arc.math.geom.*;

public class PMMathf{
    private static final Vec2 bezOut = new Vec2(), p1 = new Vec2(), p2 = new Vec2(), p3 = new Vec2(), p4 = new Vec2(), tmp = new Vec2();

    /** @return whether x,y is inside the square with radius d centered at cx, cy. */
    public static boolean isInSquare(float cx, float cy, float d, float x, float y){
        return x > cx - d && x < cx + d && y > cy - d && y < cy + d;
    }

    public static float cornerDst(float r){
        return cornerDst(r, r);
    }

    public static float cornerDst(float w, float h){
        return (float)Math.sqrt(w * h * 2f);
    }

    public static Vec2 randomCirclePoint(Vec2 v, float radius){
        v.setToRandomDirection().setLength(radius * Mathf.sqrt(Mathf.random()));

        return v;
    }

    /** Pulled out of {@link Angles#moveToward(float, float, float)} */
    public static int angleMoveDirection(float from, float to){
        from = Mathf.mod(from, 360f);
        to = Mathf.mod(to, 360f);

        if(from > to == Angles.backwardDistance(from, to) > Angles.forwardDistance(from, to)){
            return -1;
        }else{
            return 1;
        }
    }

    /** Lerp from one angle to another. */
    public static float lerpAngle(float from, float to, float progress){
        return Angles.moveToward(from, to, progress * Angles.angleDist(from, to));
    }

    public static Vec2 bezier(float t, float x1, float y1, float x2, float y2, Vec2 h1, Vec2 h2){
        p1.set(x1, y1);
        p4.set(x2, y2);
        p2.set(p1).add(h1);
        p3.set(p4).add(h2);
        Bezier.cubic(bezOut, t, p1, p2, p3, p4, tmp);
        return bezOut;
    }

    public static Vec2 bezierDeriv(float t, float x1, float y1, float x2, float y2, Vec2 h1, Vec2 h2){
        p1.set(x1, y1);
        p4.set(x2, y2);
        p2.set(p1).add(h1);
        p3.set(p4).add(h2);
        Bezier.cubicDerivative(bezOut, t, p1, p2, p3, p4, tmp);
        return bezOut;
    }
}
