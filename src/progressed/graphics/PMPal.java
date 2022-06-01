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

    heal = valueOf("84f491"),
    overdrive = valueOf("feb380"),

    outline = valueOf("404049"),

    magma = valueOf("ff9c5a"),
    cyanLaser = valueOf("d1efff"),
    cyanFlame = valueOf("9fc9f5"),

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

    apotheosisLaserColors = {valueOf("9A27C455"), valueOf("9A27C4aa"), apotheosisLaser, white},
    pissbeamColors = {valueOf("c4b42755"), valueOf("c4b427aa"), valueOf("e5c85e"), white},
    cyanFlameColors = {valueOf("8ca9e8").a(0.55f), valueOf("9fc9f5").a(0.7f), valueOf("bbddfc").a(0.8f), cyanLaser, white.cpy()};

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
