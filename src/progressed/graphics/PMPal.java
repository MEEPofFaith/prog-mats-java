package progressed.graphics;

import arc.graphics.*;

import static arc.graphics.Color.*;
import static mindustry.Vars.*;

public class PMPal{
    //Single colors
    public static Color

    darkBrown = valueOf("4d2c0c"),

    heal = valueOf("84f491"),
    overdrive = valueOf("feb380"),

    outline = valueOf("404049"),

    magma = valueOf("ff9c5a"),

    pixelFront = valueOf("FF84C1"),
    pixelBack = valueOf("EF4A9D"),

    missileBasic = valueOf("D4816B"),
    missileFrag = valueOf("9CB664"),

    nexusLaser = valueOf("CE5EE5"),
    nexusLaserDark = valueOf("9A27C4");

    //Color sets
    public static Color[]
    itemColors,
    liquidColors;

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
