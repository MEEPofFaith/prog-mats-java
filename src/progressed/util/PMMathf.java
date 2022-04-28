package progressed.util;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;

public class PMMathf{
    private static final Rand rand = new Rand();
    private static final Vec2 rv = new Vec2();

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
