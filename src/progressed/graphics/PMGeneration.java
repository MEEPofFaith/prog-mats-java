package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;

public class PMGeneration{
    /**
     * Outlines a given textureRegion. Run in createIcons.
     * @author Sunny
     * */
    public static void outlineRegion(MultiPacker packer, TextureRegion tex, Color outlineColor, String name){
        if(tex == null) return;
        final int radius = 4;
        PixmapRegion region = Core.atlas.getPixmap(tex);
        Pixmap out = new Pixmap(region.width, region.height);
        Color color = new Color();
        for(int x = 0; x < region.width; x++){
            for(int y = 0; y < region.height; y++){

                region.get(x, y, color);
                out.set(x, y, color);
                if(color.a < 1f){
                    boolean found = false;
                    outer:
                    for(int rx = -radius; rx <= radius; rx++){
                        for(int ry = -radius; ry <= radius; ry++){
                            if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.within(rx, ry, radius) && color.set(region.get(rx + x, ry + y)).a > 0.01f){
                                found = true;
                                break outer;
                            }
                        }
                    }
                    if(found){
                        out.set(x, y, outlineColor);
                    }
                }
            }
        }
        packer.add(MultiPacker.PageType.main, name, out);
    }

    /**
     * Outlines a list of regions. Run in createIcons.
     * @author Sunny
     * */
    public static void outlineRegions(MultiPacker packer, TextureRegion[] textures, Color outlineColor, String name){
        for(int i = 0; i < textures.length; i++){
            outlineRegion(packer, textures[i], outlineColor, name + "-" + i);
        }
    }
}