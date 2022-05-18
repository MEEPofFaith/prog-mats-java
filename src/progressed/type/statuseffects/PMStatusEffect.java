package progressed.type.statuseffects;

import arc.graphics.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.graphics.*;

public class PMStatusEffect extends StatusEffect{
    /** Outlined icon color.*/
    public Color outlineColor = PMPal.outline;

    public PMStatusEffect(String name){
        super(name);
    }

    @Override
    public void createIcons(MultiPacker packer){
        Outliner.outlineRegion(packer, fullIcon, outlineColor, name, 3);

        super.createIcons(packer);
    }
}
