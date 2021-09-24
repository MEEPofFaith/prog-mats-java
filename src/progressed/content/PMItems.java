package progressed.content;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class PMItems implements ContentList{
    public static Item
        fusium;

    @Override
    public void load(){
        fusium = new Item("techtanite", Color.valueOf("B0BAC0")){{
            cost = 1.6f;
            hideDetails = false;
        }};
    }
}