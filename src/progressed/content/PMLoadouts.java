package progressed.content;

import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.world.blocks.storage.*;
import progressed.content.blocks.*;

public class PMLoadouts{
    public static Schematic
    basicCripple;

    public static void load(){
        basicCripple = Schematics.readBase64("bXNjaAF4nGNgYmBiZmDJS8xNZeB1SizOTFZwLsosKMhJZeBOSS1OBrJLMvPzGBgY2HISk1JzihmYomMZGcQKivLTdXMTS4p1k/OLUnWToXoYGBgZwIARACq+FxI=");
        Vars.schematics.getLoadouts().get((CoreBlock)PMBlocks.coreShatter, Seq::new).add(basicCripple);
    }
}
