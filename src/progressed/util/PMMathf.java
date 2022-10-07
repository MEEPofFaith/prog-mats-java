package progressed.util;

import arc.math.*;
import arc.math.geom.*;

public class PMMathf{
    public static final Interp arc = a -> Interp.sineOut.apply(Interp.slope.apply(a));

    /** @return whether x,y is inside the square with radius d centered at cx, cy. */
    public static boolean isInSquare(float cx, float cy, float d, float x, float y){
        return x > cx - d && x < cx + d && y > cy - d && y < cy + d;
    }

    public static float cornerDst(float r){
        return (float)Math.sqrt(r * r * 2f);
    }

    public static Vec2 randomCirclePoint(Vec2 v, float radius){
        v.setToRandomDirection().setLength(radius * Mathf.sqrt(Mathf.random()));

        return v;
    }
}
