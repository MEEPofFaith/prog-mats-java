package progressed.util;

import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;

import static arc.math.Mathf.*;

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

    public static float cbrt(float x){
        return (float)Math.cbrt(x);
    }

    /** Copied from {@link Predict#quad(float, float, float)} */
    public static Vec2 quad(float a, float b, float c){
        Vec2 sol = null;
        if(Math.abs(a) < 1e-6){
            if(Math.abs(b) < 1e-6){
                sol = Math.abs(c) < 1e-6 ? tmp.set(0, 0) : null;
            }else{
                tmp.set(-c / b, -c / b);
            }
        }else{
            float disc = b * b - 4 * a * c;
            if(disc >= 0){
                disc = Mathf.sqrt(disc);
                a = 2 * a;
                sol = tmp.set((-b - disc) / a, (-b + disc) / a);
            }
        }
        return sol;
    }

    /** @return Smallest positive nonzero solution of a quadratic. */
    public static float quadPos(float a, float b, float c){
        Vec2 ts = quad(a, b, c);
        if(ts != null){
            float t0 = ts.x, t1 = ts.y;
            float t = Math.min(t0, t1);
            if(t <= 0) t = Math.max(t0, t1);
            if(t > 0){
                return t;
            }
        }
        return 0f;
    }

    public static float solve(float a, float b, float c){
        if(a == 0){
            return -c / b;
        }else{
            Vec2 t = quad(a, b, c);
            return Math.max(t.x, t.y);
        }
    }

    // https://math.stackexchange.com/questions/785/is-there-a-general-formula-for-solving-quartic-degree-4-equations
    public static float[] quartic(float a, float b, float c, float d, float e){
        float p1 = 2*c*c*c - 9*b*c*d + 27*a*d*d + 27*b*b*e - 72*a*c*e;
        float p2 = c*c - 3*b*d + 12*a*e;
        float p3 = p1 + sqrt(-4*p2*p2*p2 + p1*p1);
        float p4 = cbrt(p3/2);
        float p5 = p2/(3*a*p4) + (p4/(3*a));

        float p6 = sqrt((b*b)/(4*a*a) - (2*c)/(3*a) + p5);
        float p7 = (b*b)/(2*a*a) - (4*c)/(3*a) - p5;
        float p8 = (-(b*b*b)/(a*a*a) + (4*b*c)/(a*a) - (8*d)/a) / (4*p6);

        return new float[]{
            -(b/(4*a)) - (p6/2) - (sqrt(p7-p8)/2),
            -(b/(4*a)) - (p6/2) + (sqrt(p7-p8)/2),
            -(b/(4*a)) - (p6/2) - (sqrt(p7+p8)/2),
            -(b/(4*a)) - (p6/2) + (sqrt(p7+p8)/2)
        };
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
