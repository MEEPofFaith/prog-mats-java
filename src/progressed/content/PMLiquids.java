package progressed.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.type.*;
import progressed.graphics.*;

public class PMLiquids{
    public static Liquid
    magma, sludge;

    public static void load() {
        magma = new Liquid("magma", Color.valueOf("F58859")){{
            effect = StatusEffects.melting;
            flammability = temperature = 2f;
            viscosity = 0.3f;
            lightColor = color;
            hideDetails = false;
            hidden = true;
        }};

        sludge = new CellLiquid("sludge", PMPal.sludge){{
            effect = PMStatusEffects.sludgeIncineration;
            flammability = temperature = 2f;
            viscosity = 0.3f;
            lightColor = color;
            moveThroughBlocks = true;
            incinerable = false;
            blockReactive = false;
            canStayOn.addAll(Liquids.water, Liquids.oil, Liquids.cryofluid);
            hideDetails = false;
            hidden = true;

            colorFrom = Color.valueOf("d2701e");
            colorTo = Color.valueOf("a21019");
        }};
    }
}
