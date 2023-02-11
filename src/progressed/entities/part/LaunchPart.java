package progressed.entities.part;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;

public class LaunchPart extends DrawPart{
    public PartProgress progress = PartProgress.warmup;
    public float layer = Layer.effect;
    public float start, length, spacing;
    public int arrows = 2;

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params);

        float w = spacing + spacing * (1f - prog);
        Lines.stroke(prog * 1.2f, Pal.accent);

        float z = Draw.z();
        Draw.z(layer);
        for(int sign : Mathf.signs){
            Tmp.v1.trns(params.rotation, start, w * sign);
            Lines.lineAngle(params.x + Tmp.v1.x, params.y + Tmp.v1.y, params.rotation, length);
        }

        Draw.scl(prog * 1.1f);
        for(int i = 0; i < arrows; i++){
            Tmp.v1.trns(params.rotation, start + length / (arrows + 1) * (i + 1));
            Draw.rect("bridge-arrow", params.x + Tmp.v1.x, params.y + Tmp.v1.y, params.rotation);
        }
        Draw.reset();
        Draw.z(z);
    }

    @Override
    public void load(String name){
        //Nothing to load
    }
}
