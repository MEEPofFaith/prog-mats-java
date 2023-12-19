package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;

/** Renders gravitational lensing. Client only. */
public class BlackHoleRenderer{
    private static final int scaling = 4;

    private FrameBuffer allBuffer = new FrameBuffer();
    private boolean capturing = false;
    private Shader blackHoleShader = PMShaders.blackHoleShader;

    private FrameBuffer zonesBuffer = new FrameBuffer();
    private Seq<BlackHoleZone> zones = new Seq<>(BlackHoleZone.class);
    private int zoneIndex = 0;
    private Texture zonesTex;

    public void add(float x, float y, float inRadius, float outRadius, Color color){
        if(inRadius > outRadius || outRadius <= 0) return;

        float res = Color.toFloatBits(color.r, color.g, color.b, 1);

        if(zones.size <= zoneIndex) zones.add(new BlackHoleZone(x, y, res, inRadius, outRadius));
        zoneIndex++;
    }

    public void draw(){
        zonesBuffer.resize(Core.graphics.getWidth() / scaling, Core.graphics.getHeight() / scaling);

        Draw.color();
        Draw.sort(false);
        zonesBuffer.begin(Color.clear);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);
        //apparently necessary? idk I just copied this from LightRenderer
        Blending.normal.apply();

        for(int i = 0; i < zoneIndex; i++){
            BlackHoleZone cir = zones.get(i);
            float x = cir.x,
                y = cir.y,
                inRadius = cir.inRadius,
                scl = cir.outRadius / inRadius,
                centerf = cir.color,
                edgef = Tmp.c1.abgr8888(cir.color).a(0).toFloatBits();
            int sides = Lines.circleVertices(cir.outRadius);
            float space = 360f / sides;

            for(int j = 0; j < sides; j++){
                float px1 = Angles.trnsx(space * (float)j, inRadius),
                    py1 = Angles.trnsy(space * (float)j, inRadius),
                    px2 = Angles.trnsx(space * (float)(j + 1), inRadius),
                    py2 = Angles.trnsy(space * (float)(j + 1), inRadius);

                Draw.color(centerf);
                Fill.quad(x, y, x, y, x + px1, y + py1, x + px2, y + py2);
                Draw.color();
                Fill.quad(x + px1, y + py1, centerf,
                    x + px2, y + py2, centerf,
                    x + px2 * scl, y + py2 * scl, edgef,
                    x + px1 * scl, y + py1 * scl, edgef
                );
            }
        }

        Draw.reset();
        zonesBuffer.end();
        zonesTex = zonesBuffer.getTexture();
        Draw.sort(true);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();
        zonesBuffer.blit(PMShaders.none);

        zones.clear();
        zoneIndex = 0;
    }

    public void captureAll(){
        allBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        if(!capturing){
            capturing = true;
            allBuffer.begin();
        }
    }

    public void render(){
        if(capturing){
            capturing = false;
            allBuffer.end();
        }

        ((Texture)allBuffer.getTexture()).bind(1);
        allBuffer.blit(blackHoleShader);
    }

    static class BlackHoleZone{
        float x, y, color, inRadius, outRadius;

        public BlackHoleZone(float x, float y, float color, float inRadius, float outRadius){
            this.x = x;
            this.y = y;
            this.color = color;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
        }

        public void set(float x, float y, float color, float inRadius, float outRadius){
            this.x = x;
            this.y = y;
            this.color = color;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
        }
    }
}
