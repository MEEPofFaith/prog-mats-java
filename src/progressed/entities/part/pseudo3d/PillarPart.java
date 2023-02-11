package progressed.entities.part.pseudo3d;

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
    public float radius = 8f, height = 1f;
    public float layer = Layer.flyingUnit + 0.5f;
    /** Whether this part is bloomed even if outside bloom layers. */
    public boolean alwaysBloom = false;
    public Blending blending = Blending.normal;
    public Color baseColorLight = PMPal.nexusLaser, baseColorDark = PMPal.nexusLaserDark, topColorLight, topColorDark;

    @Override
    public void draw(PartParams params){
        if(topColorLight == null) topColorLight = baseColorLight;
        if(topColorDark == null) topColorDark = baseColorDark;

        float rx = params.x, ry = params.y;

        float z = Draw.z();
        if(layer > 0){
            Draw.z(layer + DrawPseudo3D.layerOffset(rx, ry));
        }else{
            Draw.z(z + DrawPseudo3D.layerOffset(rx, ry));
        }

        float alpha = alphaProg.get(params);
        float radScl = radProg.get(params);
        float heightScl = heightProg.get(params);
        if(radScl <= 0.001f) return;

        if(alwaysBloom){
            PMDrawf.bloom(() -> drawCylinder(rx, ry, alpha, radScl, heightScl));
        }else{
            drawCylinder(rx, ry, alpha, radScl, heightScl);
        }

        Draw.z(z);
    }

    public void drawCylinder(float x, float y, float alpha, float radScl, float heightScl){
        Tmp.c1.set(baseColorDark).mulA(alpha);
        Tmp.c2.set(baseColorLight).mulA(alpha);
        Tmp.c3.set(topColorDark).mulA(alpha);
        Tmp.c4.set(topColorLight).mulA(alpha);

        Draw.blend(blending);
        DrawPseudo3D.tube(x, y, radius * radScl, height * heightScl, Tmp.c1, Tmp.c2, Tmp.c3, Tmp.c4);
        Draw.blend();
    }

    @Override
    public void load(String name){
    }
}
