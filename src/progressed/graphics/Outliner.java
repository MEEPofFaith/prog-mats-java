package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;

public class Outliner{
    /**
     * Outlines a given textureRegion. Run in createIcons.
     * @author Sunny
     * */
    public static void outlineRegion(MultiPacker packer, TextureRegion tex, Color outlineColor, String name, int radius){
        if(tex == null) return;
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
                            if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.within(rx, ry, radius + 0.5f) && color.set(region.get(rx + x, ry + y)).a > 0.01f){
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

    public static void outlineRegion(MultiPacker packer, TextureRegion tex, Color outlineColor, String name){
        outlineRegion(packer, tex, outlineColor, name, 4);
    }

    /**
     * Outlines a list of regions. Run in createIcons.
     * @author Sunny
     * */
    public static void outlineRegions(MultiPacker packer, TextureRegion[] textures, Color outlineColor, String name, int radius){
        for(int i = 0; i < textures.length; i++){
            outlineRegion(packer, textures[i], outlineColor, name + "-" + i, radius);
        }
    }

    public static void outlineRegions(MultiPacker packer, TextureRegion[] textures, Color outlineColor, String name){
        outlineRegions(packer, textures, outlineColor, name, 4);
    }
}