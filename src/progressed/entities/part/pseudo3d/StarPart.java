package progressed.entities.part.pseudo3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static arc.Core.*;
import static arc.math.geom.Geometry.*;
import static progressed.graphics.Draw3D.*;

public class StarPart extends DrawPart{
    /** Progress function for determining height. */
    public PartProgress heightProg = PartProgress.constant(1f);
    /** Progress function for determining size. */
    public PartProgress sizeProg = PartProgress.warmup;
    public float spikeLen = 32f, spikeWidth = 8f, height = 1f;
    public float layer = Layer.flyingUnit + 0.5f;
    /** Whether this part is bloomed even if outside bloom layers. */
    public boolean alwaysBloom = false;
    public Blending blending = Blending.normal;
    public Color lightColor = PMPal.nexusLaser, midColor, darkColor = PMPal.nexusLaserDark;

    @Override
    public void draw(PartParams params){
        if(midColor == null){
            midColor = lightColor.cpy().lerp(darkColor, 0.5f);
        }

        float rx = params.x, ry = params.y;
        float heightScl = heightProg.get(params);
        float sizeScl = sizeProg.get(params);
        if(sizeScl <= 0.001f) return;

        float z = Draw.z();
        if(layer > 0){
            Draw.z(layer + Draw3D.layerOffset(rx, ry));
        }else{
            Draw.z(z + Draw3D.layerOffset(rx, ry));
        }

        if(alwaysBloom){
            PMDrawf.bloom(() -> drawStar(rx, ry, heightScl, sizeScl));
        }else{
            drawStar(rx, ry, heightScl, sizeScl);
        }

        Draw.z(z);
    }

    public void drawStar(float x, float y, float heightScl, float sizeScl){
        float relAng = Angles.angle(camera.position.x, camera.position.y, x, y);
        int start = Mathf.floorPositive(relAng / 90f); //0->90 = 0, 90->180 = 1, 180->270 = 2, 270->360 = 3
        int order = Mathf.signs[Mathf.mod(Mathf.floorPositive(relAng / 45f), 2)]; //0->45 = -1, 45->90 = 1

        Draw.blend(blending);
        drawSide(x, y, heightScl, sizeScl, start);
        drawSide(x, y, heightScl, sizeScl, start - order);
        drawSide(x, y, heightScl, sizeScl, start + order);
        drawSide(x, y, heightScl, sizeScl, start + 2);
        Draw.blend();
    }

    public void drawSide(float x, float y, float heightScl, float sizeScl, int side){
        float h = height * heightScl,
            th = h + (spikeWidth/2f * sizeScl * sizeScl),
            ph = h + (spikeLen * sizeScl * sizeScl);

        Draw.color(getColor(side));

        Point2 corner = d8edge(side);
        float x1 = xHeight(x + spikeWidth/4f * sizeScl * corner.x, h),
            y1 = yHeight(y + spikeWidth/4f * sizeScl * corner.y, h);

        //TODO properly calculate how far away the tops of these spikes should go
        for(int next : Mathf.zeroOne){
            float x2 = xHeight(x + spikeLen * sizeScl * d4x(side + next), h),
                y2 = yHeight(y + spikeLen * sizeScl * d4y(side + next), h),
                x3 = xHeight(x /*+ spikeWidth/4f * sizeScl * d4x(side + next)*/, th),
                y3 = yHeight(y /*+ spikeWidth/4f * sizeScl * d4y(side + next)*/, th);
            Fill.tri(x1, y1, x2, y2, x3, y3);
        }

        float px1 = xHeight(x, ph),
            py1 = yHeight(y, ph),
            px2 = xHeight(x + spikeWidth/2f * sizeScl * d4x(side), h),
            py2 = yHeight(y + spikeWidth/2f * sizeScl * d4y(side), h),
            px3 = xHeight(x + spikeWidth/2f * sizeScl * d4x(side + 1), h),
            py3 = yHeight(y + spikeWidth/2f * sizeScl * d4y(side + 1), h);

        Fill.tri(px1, py1, px2, py2, px3, py3);
    }

    public Color getColor(int side){
        return switch(Mathf.mod(side, 4)){
            case 0 -> lightColor;
            case 2 -> darkColor;
            default -> midColor;
        };
    }

    @Override
    public void load(String name){
    }
}
