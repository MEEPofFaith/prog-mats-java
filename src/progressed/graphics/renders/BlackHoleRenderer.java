package progressed.graphics.renders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static arc.Core.graphics;

/** Renders the glowing area around black holes. (Do I even need this?) */
public class BlackHoleRenderer{
    private final Seq<BlackHoleZone> zones = new Seq<>(BlackHoleZone.class);
    private final FrameBuffer pingPong1;
    private final FrameBuffer pingPong2;
    private int zoneIndex = 0;

    public BlackHoleRenderer(){
        pingPong1 = new FrameBuffer();
        pingPong2 = new FrameBuffer();

        Events.run(Trigger.draw, () -> {
            Draw.draw(Layer.min, () -> {
                pingPong1.resize(graphics.getWidth(), graphics.getHeight());
                pingPong2.resize(graphics.getWidth(), graphics.getHeight());

                pingPong1.begin();
            });

            Draw.draw(Layer.max, () -> {
                FrameBuffer from = pingPong1;

                for(int i = 0; i < zoneIndex; i++){
                    BlackHoleZone cir = zones.get(i);
                    Fill.light(cir.x, cir.y,
                        Lines.circleVertices(cir.outRadius), cir.outRadius,
                        Tmp.c1.abgr8888(cir.color).a(0.5f), Tmp.c2.abgr8888(cir.color).a(0)
                    );

                    from.end();
                    FrameBuffer to = from == pingPong1 ? pingPong2 : pingPong1;
                    to.begin();
                    PMShaders.blackHole.set(cir);
                    from.blit(PMShaders.blackHole);
                    from = to;
                }

                from.end();
                from.blit(PMShaders.passThrough);

                zones.clear();
                zoneIndex = 0;
            });
        });
    }

    public void add(float x, float y, float inRadius, float outRadius, Color color){
        if(inRadius > outRadius || outRadius <= 0) return;

        float res = Color.toFloatBits(color.r, color.g, color.b, 1);

        if(zones.size <= zoneIndex) zones.add(new BlackHoleZone(x, y, res, inRadius, outRadius));
        zoneIndex++;
    }

    public void draw(){
        Draw.color();
        Draw.sort(false);
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

                Fill.quad(x + px1, y + py1, centerf,
                    x + px2, y + py2, centerf,
                    x + px2 * scl, y + py2 * scl, edgef,
                    x + px1 * scl, y + py1 * scl, edgef
                );
            }
        }
        for(int i = 0; i < zoneIndex; i++){
            BlackHoleZone cir = zones.get(i);
            float x = cir.x,
                y = cir.y,
                inRadius = cir.inRadius;

            Draw.color(Color.black);
            Fill.circle(x, y, inRadius);
        }

        Draw.reset();
        Draw.sort(true);
        Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

        Draw.color();

        zones.clear();
        zoneIndex = 0;
    }

    public static class BlackHoleZone{
        public float x, y, color, inRadius, outRadius;

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
