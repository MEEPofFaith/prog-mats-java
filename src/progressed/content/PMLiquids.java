package progressed.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.type.*;
import progressed.graphics.*;

public class PMLiquids{
    public static Liquid
    magma;

    public static void load() {
        magma = new CellLiquid("magma"){{
            effect = StatusEffects.melting;
            flammability = temperature = 2f;
            viscosity = 0.3f;
            capPuddles = false;
            hideDetails = false;
            hidden = true;

            color = colorFrom = lightColor = PMPal.magma;
            colorTo = Color.valueOf("bf682e");
        }};
    }
}
