package progressed.entities.part.pseudo3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class RingPart extends DrawPart{
    /** Progress function for determining height. */
    public PartProgress heightProg = PartProgress.constant(1f);
    /** Progress function for determining radius. */
    public PartProgress radProg = PartProgress.warmup;
    /** Progress function for determining alpha. */
    public PartProgress alphaProg = PartProgress.warmup;
    public float rad = 8f, height = 1f;
    public float layer = Layer.flyingUnit + 0.5f;
    /** Whether this part is bloomed even if outside bloom layers. */
    public boolean alwaysBloom = false;
    public Blending blending = Blending.normal;
    public Color inColor = PMPal.nexusLaser, outColor;
    public float inRad = 16f, outRad = 24f;

    @Override
    public void draw(PartParams params){
        if(outColor == null){
            outColor = inColor.cpy().a(0f);
        }

        float rx = params.x, ry = params.y;

        float z = Draw.z();
        if(layer > 0){
            Draw.z(layer + Draw3D.layerOffset(rx, ry));
        }else{
            Draw.z(z + Draw3D.layerOffset(rx, ry));
        }

        float alpha = alphaProg.get(params);
        float radScl = radProg.get(params);
        float heightScl = heightProg.get(params);
        if(radScl <= 0.001f) return;

        if(alwaysBloom){
            PMDrawf.bloom(() -> drawRing(rx, ry, alpha, radScl, heightScl));
        }else{
            drawRing(rx, ry, alpha, radScl, heightScl);
        }

        Draw.z(z);
    }

    public void drawRing(float x, float y, float alpha, float radScl, float heightScl){
        Tmp.c1.set(inColor).mulA(alpha);
        Tmp.c2.set(outColor).mulA(alpha);

        Draw.blend(blending);
        Draw3D.ring(x, y, rad, inRad * radScl, outRad * radScl, height * heightScl, Tmp.c1, Tmp.c2);
        Draw.blend();
    }

    @Override
    public void load(String name){
    }
}
