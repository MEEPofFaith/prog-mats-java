package progressed.graphics;

import arc.graphics.*;

import static arc.graphics.Color.*;
import static mindustry.Vars.*;

public class PMPal{
    //Single colors
    public static Color

    lightGray = valueOf("B0BAC0"),
    midGray = valueOf("989AA4"),
    darkGray = valueOf("6E7080"),
    darkBrown = valueOf("4d2c0c"),

    heal = Color.valueOf("84f491"),
    overdrive = valueOf("feb380"),

    outline = valueOf("404049"),

    magma = valueOf("ff9c5a"),

    pixelFront = valueOf("FF84C1"),
    pixelBack = valueOf("EF4A9D"),

    missileBasic = valueOf("D4816B"),
    missileFrag = valueOf("9CB664"),

    apotheosisLaser = valueOf("CE5EE5"),
    apotheosisLaserDark = valueOf("9A27C4"),
    pissbeam = valueOf("e5c85e"),
    pissbeamDark = valueOf("c4b427");

    //Color sets
    public static Color[]
    itemColors,
    liquidColors,

    apotheosisLaserColors = {Color.valueOf("9A27C455"), Color.valueOf("9A27C4aa"), apotheosisLaser, white},
    pissbeamColors = {valueOf("c4b42755"), valueOf("c4b427aa"), valueOf("e5c85e"), white};

    public static void init(){
        int items = content.items().size;
        itemColors = new Color[items + 1];
        for(int i = 0; i < items; i++){
            itemColors[i] = content.item(i).color;
        }
        itemColors[items] = content.items().first().color;

        int liquids = content.liquids().size;
        liquidColors = new Color[liquids + 1];
        for(int i = 0; i < liquids; i++){
            liquidColors[i] = content.liquid(i).color;
        }
        liquidColors[liquids] = content.liquids().first().color;
    }
}
