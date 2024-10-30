package progressed.type.statuseffects;

import arc.*;
import arc.graphics.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;

public class PMStatusEffect extends StatusEffect{
    public PMStatusEffect(String name){
        super(name);
        outline = true;
    }

    @Override
    public void createIcons(MultiPacker packer){ //Yoink from generator.
        //color image
        Pixmap base = Core.atlas.getPixmap(uiIcon).crop();
        Pixmap tint = base;
        base.each((x, y) -> tint.setRaw(x, y, Color.muli(tint.getRaw(x, y), color.rgba())));

        //outline the image
        Pixmap container = new Pixmap(tint.width + 6, tint.height + 6);
        container.draw(base, 3, 3, true);
        base = container.outline(Pal.gray, 3);
        packer.add(PageType.ui, name, base);
    }
}
