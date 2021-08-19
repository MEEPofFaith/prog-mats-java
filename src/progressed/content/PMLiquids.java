package progressed.content;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class PMLiquids implements ContentList{
    public static Liquid
    magma;

    @Override
    public void load() {
        magma = new Liquid("magma"){
            {
                flammability = temperature = 1000f;
                viscosity = 0.8f;
                color = lightColor = Color.valueOf("F58859");
            }

            @Override
            public boolean isHidden(){
                return true;
            }
        };
    }
}