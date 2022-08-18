package progressed.entities.part;

import arc.graphics.g2d.*;
import arc.util.*;
import progressed.graphics.*;

public class RingPart extends PillarPart{
    public float inRad = 16f, outRad = 24f;

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

        float radScl = radProg.get(params);
        DrawPsudo3D.ring(rx, ry, rad, inRad * radScl, outRad * radScl, height * heightProg.get(params), Tmp.c1, Tmp.c2);

        Draw.z(z);
    }

    @Override
    public void load(String name){

    }
}
