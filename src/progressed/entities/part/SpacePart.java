package progressed.entities.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.game.EventType.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class SpacePart extends DrawPart{
    private static ObjectMap<Float, Seq<SpaceData>> draws;

    /** Appended to unit/weapon/block name and drawn. */
    public String suffix = "";
    /** Overrides suffix if set. */
    public @Nullable String name;
    public TextureRegion region;
    public float layer = -1;

    public SpacePart(String suffix){
        this.suffix = suffix;

        if(draws == null){
            draws = new ObjectMap<>();

            Events.run(Trigger.drawOver, () -> {
                for(Float layer : draws.keys()){
                    Seq<SpaceData> datas = draws.get(layer);
                    Draw.draw(layer, () -> {
                        renderer.effectBuffer.begin(Color.clear);
                        for(SpaceData data : datas){
                            Draw.rect(data.region, data.x, data.y, data.rot);
                        }
                        renderer.effectBuffer.end();
                        renderer.effectBuffer.blit(PMShaders.smallSpaceShader);
                    });
                }
                draws.clear();
            });
        }
    }

    @Override
    public void draw(PartParams params){
        float l = layer > 0 ? layer : Draw.z();
        Seq<SpaceData> data = draws.get(l, Seq::new);
        data.add(new SpaceData(params.x, params.y, params.rotation - 90f, region));
    }

    @Override
    public void load(String name){
        String realName = this.name == null ? name + suffix : this.name;

        region = Core.atlas.find(realName);
    }

    private static class SpaceData{
        float x, y, rot;
        TextureRegion region;

        private SpaceData(float x, float y, float rot, TextureRegion region){
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.region = region;
        }
    }
}
