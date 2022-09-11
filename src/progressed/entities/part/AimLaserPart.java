package progressed.entities.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;

public class AimLaserPart extends DrawPart{
    /** Progress function for determining length. */
    public PartProgress progress = PartProgress.warmup;
    /** Progress function for determining alpha. */
    public PartProgress alpha = PartProgress.constant(1f);
    public Blending blending = Blending.normal;
    public float layer = -1f, layerOffset;
    public float x, y;
    public float width = 1f, length = 4f * 8f;
    public Color colorFrom = Color.red, colorTo;

    @Override
    public void draw(PartParams params){
        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        Draw.z(Draw.z() + layerOffset);

        Draw.blend(blending);
        float a = alpha.getClamp(params);
        float c1 = Tmp.c1.set(colorFrom).mulA(a).toFloatBits(), c2 = Tmp.c2.set(colorTo).mulA(a).toFloatBits();
        float rot = params.rotation - 90f;
        Tmp.v1.trns(rot, x, y);
        float rx = params.x + Tmp.v1.x,
            ry = params.y + Tmp.v1.y;
        float cos = Mathf.cosDeg(rot) * width / 2f, sin = Mathf.sinDeg(rot) * width / 2f;
        Tmp.v1.trns(params.rotation, length * progress.getClamp(params)).add(rx, ry);
        Fill.quad(
            rx + cos, ry + sin, c1,
            rx - cos, ry - sin, c1,
            Tmp.v1.x - cos, Tmp.v1.y - sin, c2,
            Tmp.v1.x + cos, Tmp.v1.y + sin, c2
        );

        Draw.blend();
        Draw.z(z);
    }

    @Override
    public void load(String name){
        //No textures to load

        if(colorTo == null) colorTo = colorFrom.cpy().a(0);
    }
}
