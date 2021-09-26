package progressed.content;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class PMItems implements ContentList{
    public static Item
        valexitite;

    @Override
    public void load(){
        valexitite = new Item("techtanite", Color.valueOf("B0BAC0")){{
            cost = 1.6f;
            hideDetails = false;
        }};
    }
}