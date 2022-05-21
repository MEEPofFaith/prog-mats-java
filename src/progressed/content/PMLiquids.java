package progressed.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.type.*;

public class PMLiquids{
    public static Liquid
    magma;

    public static void load() {
        magma = new Liquid("magma"){{
            effect = StatusEffects.melting;
            flammability = temperature = 2f;
            viscosity = 0.3f;
            color = lightColor = Color.valueOf("F58859");
            hideDetails = false;
            hidden = true;
        }};
    }
}
