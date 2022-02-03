package progressed.content;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class PMLiquids{
    public static Liquid
    magma;

    public static void load() {
        magma = new Liquid("magma"){
            {
                flammability = temperature = 1000f;
                viscosity = 0.3f;
                color = lightColor = Color.valueOf("F58859");
                hideDetails = false;
            }

            @Override
            public boolean isHidden(){
                return true;
            }
        };
    }
}