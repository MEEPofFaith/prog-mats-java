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
    private Shader blackHoleShader;

    private FrameBuffer zonesBuffer = new FrameBuffer();
    private Seq<BlackHoleZone> circles = new Seq<>(BlackHoleZone.class);
    private int circleIndex = 0;

    public void add(float x, float y, float inRadius, float outRadius, Color color){
        if(inRadius > outRadius || outRadius <= 0) return;

        float res = Color.toFloatBits(color.r, color.g, color.b, 1);

        if(circles.size <= circleIndex) circles.add(new BlackHoleZone(x, y, res, inRadius, outRadius));
        circleIndex++;
    }

    public void draw(){
        //TODO add a setting. This is probably pretty intensive to render.

        zonesBuffer.resize(Core.graphics.getWidth() / scaling, Core.graphics.getHeight() / scaling);

        Draw.color();
        zonesBuffer.begin(Color.clear);
        Draw.sort(false);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);
        //apparently necessary
        Blending.normal.apply();

        for(int i = 0; i < circleIndex; i++){
            BlackHoleZone cir = circles.get(i);
            float x = cir.x,
                y = cir.y,
                inRadius = cir.inRadius,
                scl = cir.outRadius / inRadius,
                centerf = cir.color,
                edgef = Tmp.c1.abgr8888(cir.color).a(0).toFloatBits();
            int sides = Lines.circleVertices(cir.outRadius);
            float space = 360f / sides;

            for(int j = 0; j < sides; j++){
                float px1 = Angles.trnsx(space * (float)i, inRadius),
                    py1 = Angles.trnsy(space * (float)i, inRadius),
                    px2 = Angles.trnsx(space * (float)(i + 1), inRadius),
                    py2 = Angles.trnsy(space * (float)(i + 1), inRadius);

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
        Draw.sort(true);
        zonesBuffer.end();
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();

        circles.clear();
        circleIndex = 0;
    }

    public void captureAll(){
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
