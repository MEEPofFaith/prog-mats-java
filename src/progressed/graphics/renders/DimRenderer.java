package progressed.graphics.renders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import progressed.graphics.*;

/** Based on LightRenderer. Dims entire screen. */
public class DimRenderer{
    private static final int scaling = 4;

    private float[] vertices = new float[24];
    private FrameBuffer buffer = new FrameBuffer();
    private Seq<Runnable> lights = new Seq<>();
    private Seq<FadeCircle> circles = new Seq<>(FadeCircle.class);
    private int circleIndex = 0;
    private TextureRegion circleRegion;
    private float dimAlpha = 0;

    {
        Events.on(WorldLoadEvent.class, e -> dimAlpha = 0);
    }

    public void updateAlpha(float dimAlpha){
        this.dimAlpha = Mathf.clamp(Math.max(this.dimAlpha, dimAlpha));
    }

    public void add(Runnable run){
        lights.add(run);
    }

    public void add(float x, float y, float radius, float opacity){
        if(circles.size <= circleIndex) circles.add(new FadeCircle());

        //pool circles to prevent runaway GC usage from lambda capturing
        var light = circles.items[circleIndex];
        light.set(x, y, radius, opacity);

        circleIndex ++;
    }

    public void add(float x, float y, TextureRegion region, float opacity){
        add(x, y, region, 0f, opacity);
    }

    public void add(float x, float y, TextureRegion region, float rotation, float opacity){
        float xscl = Draw.xscl, yscl = Draw.yscl;
        add(() -> {
            Draw.alpha(opacity);
            Draw.scl(xscl, yscl);
            Draw.rect(region, x, y, rotation);
            Draw.scl();
        });
    }

    public void line(float x, float y, float x2, float y2, float stroke, float alpha){
        add(() -> {
            Draw.alpha(alpha);

            float rot = Mathf.angleExact(x2 - x, y2 - y);
            TextureRegion ledge = Core.atlas.find("circle-end"), lmid = Core.atlas.find("circle-mid");

            float color = Draw.getColor().toFloatBits();
            float u = lmid.u;
            float v = lmid.v2;
            float u2 = lmid.u2;
            float v2 = lmid.v;

            Vec2 v1 = Tmp.v1.trnsExact(rot + 90f, stroke);
            float lx1 = x - v1.x, ly1 = y - v1.y,
                lx2 = x + v1.x, ly2 = y + v1.y,
                lx3 = x2 + v1.x, ly3 = y2 + v1.y,
                lx4 = x2 - v1.x, ly4 = y2 - v1.y;

            vertices[0] = lx1;
            vertices[1] = ly1;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx2;
            vertices[7] = ly2;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx3;
            vertices[13] = ly3;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx4;
            vertices[19] = ly4;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);

            Vec2 v3 = Tmp.v2.trnsExact(rot, stroke);

            u = ledge.u;
            v = ledge.v2;
            u2 = ledge.u2;
            v2 = ledge.v;

            vertices[0] = lx4;
            vertices[1] = ly4;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx3;
            vertices[7] = ly3;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx3 + v3.x;
            vertices[13] = ly3 + v3.y;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx4 + v3.x;
            vertices[19] = ly4 + v3.y;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);

            vertices[0] = lx2;
            vertices[1] = ly2;
            vertices[2] = color;
            vertices[3] = u;
            vertices[4] = v;
            vertices[5] = 0;

            vertices[6] = lx1;
            vertices[7] = ly1;
            vertices[8] = color;
            vertices[9] = u;
            vertices[10] = v2;
            vertices[11] = 0;

            vertices[12] = lx1 - v3.x;
            vertices[13] = ly1 - v3.y;
            vertices[14] = color;
            vertices[15] = u2;
            vertices[16] = v2;
            vertices[17] = 0;

            vertices[18] = lx2 - v3.x;
            vertices[19] = ly2 - v3.y;
            vertices[20] = color;
            vertices[21] = u2;
            vertices[22] = v;
            vertices[23] = 0;

            Draw.vert(ledge.texture, vertices, 0, vertices.length);
        });
    }

    public void draw(){
        if(circleRegion == null) circleRegion = Core.atlas.find("circle-shadow");
        if(dimAlpha <= 0.001f){
            lights.clear();
            circleIndex = 0;
            return;
        }

        buffer.resize(Core.graphics.getWidth()/scaling, Core.graphics.getHeight()/scaling);

        Draw.color();
        buffer.begin(Color.clear);
        Draw.sort(false);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);
        //apparently necessary
        Blending.normal.apply();

        for(Runnable run : lights){
            run.run();
        }
        for(int i = 0; i < circleIndex; i++){
            var cir = circles.items[i];
            Draw.alpha(cir.alpha);
            Draw.rect(circleRegion, cir.x, cir.y, cir.radius * 2, cir.radius * 2);
        }
        Draw.reset();
        Draw.sort(true);
        buffer.end();
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();
        PMShaders.dimShader.alpha = dimAlpha;
        buffer.blit(PMShaders.dimShader);

        lights.clear();
        circleIndex = 0;
        dimAlpha = 0;
    }

    static class FadeCircle{
        float x, y, radius, alpha;

        public void set(float x, float y, float radius, float alpha){
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.alpha = alpha;
        }
    }
}
