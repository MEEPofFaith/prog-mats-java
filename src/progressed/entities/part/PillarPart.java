package progressed.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class PillarPart extends DrawPart{
    /** Progress function for determining height. */
    public PartProgress heightProg = PartProgress.constant(1f);
    /** Progress function for determining radius. */
    public PartProgress radProg = PartProgress.warmup;
    /** Progress function for determining alpha. */
    public PartProgress alphaProg = PartProgress.warmup;
    public float rad = 8f, height = 1f;
    public float layer = Layer.flyingUnit + 0.5f;
    public Color colorFrom = Pal.lancerLaser, colorTo;

    @Override
    public void draw(PartParams params){
        if(colorTo == null){
            colorTo = colorFrom.cpy().a(0f);
        }

        float z = Draw.z();
        if(layer > 0) Draw.z(layer);

        float rx = params.x, ry = params.y;

        float alpha = alphaProg.get(params);
        Tmp.c1.set(colorFrom).a(colorFrom.a * alpha);
        Tmp.c2.set(colorTo).a(colorTo.a * alpha);

        DrawPsudo3D.cylinder(rx, ry, rad * radProg.get(params), height * heightProg.get(params), Tmp.c1, Tmp.c2);

        Draw.z(z);
    }

    @Override
    public void load(String name){

    }
}
