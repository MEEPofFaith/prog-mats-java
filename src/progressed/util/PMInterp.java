package progressed.util;

import arc.math.*;

public class PMInterp{
    public static Interp
        flightArc = a -> Interp.sineOut.apply(Interp.slope.apply(a)),
        sineInverse = a -> a < 0.5f ? (Interp.sineOut.apply(a * 2) / 2f) : (0.5f + Interp.sineIn.apply(a * 2 - 1) / 2f);
}
