package progressed.entities.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.part.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class SpacePart extends DrawPart{
    /** Appended to unit/weapon/block name and drawn. */
    public String suffix = "";
    /** Overrides suffix if set. */
    public @Nullable String name;
    public TextureRegion region;
    public float layer = -1;

    public SpacePart(String suffix){
        this.suffix = suffix;
    }

    @Override
    public void draw(PartParams params){
        Draw.draw(layer > 0 ? layer : Draw.z(), () -> {
            renderer.effectBuffer.begin(Color.clear);
            Draw.rect(region, params.x, params.y, params.rotation - 90f);
            renderer.effectBuffer.end();
            renderer.effectBuffer.blit(PMShaders.smallSpaceShader);
        });
    }

    @Override
    public void load(String name){
        String realName = this.name == null ? name + suffix : this.name;

        region = Core.atlas.find(realName);
    }
}
