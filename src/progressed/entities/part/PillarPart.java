package progressed.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
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
    public float x, y, rad, height;
    public float layer = Layer.flyingUnit + 0.5f;
    public Color bottomColor = Pal.lancerLaser, topColor;



    @Override
    public void draw(PartParams params){
        if(topColor == null){
            topColor = bottomColor.cpy().a(0f);
        }

        float z = Draw.z();
        if(layer > 0) Draw.z(layer);

        Tmp.v1.set(x * Draw.xscl, y * Draw.yscl).rotateRadExact((params.rotation - 90) * Mathf.degRad);
        float
            rx = params.x + Tmp.v1.x,
            ry = params.y + Tmp.v1.y;

        float alpha = alphaProg.get(params);
        Tmp.c1.set(bottomColor).a(bottomColor.a * alpha);
        Tmp.c2.set(topColor).a(topColor.a * alpha);

        Draw3D.cylinder(rx, ry, rad * radProg.get(params), height * heightProg.get(params), Tmp.c1, Tmp.c2);

        Draw.z(z);
    }

    @Override
    public void load(String name){

    }
}
