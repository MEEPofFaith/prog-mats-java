package progressed.graphics.renders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.graphics.PMShaders.*;

import static arc.Core.*;
import static mindustry.Vars.renderer;

/** Renders the glowing area around black holes. (Do I even need this?) */
public class BlackHoleRenderer{
    private final Seq<BlackHoleZone> zones = new Seq<>(BlackHoleZone.class);
    private final FrameBuffer buffer;

    public BlackHoleRenderer(){ //TODO setting
        buffer = new FrameBuffer();

        Events.run(Trigger.draw, () -> {
            Draw.draw(Layer.min, () -> {
                buffer.resize(graphics.getWidth(), graphics.getHeight());
                buffer.begin();
            });

            Draw.draw(Layer.max, () -> {
                /*for(BlackHoleZone zone : zones){
                    Fill.light(
                        zone.x, zone.y,
                        Lines.circleVertices(zone.outRadius), zone.outRadius,
                        Tmp.c1.abgr8888(zone.color).a(0.5f), Tmp.c2.abgr8888(zone.color).a(0)
                    );
                }*/

                buffer.end();

                if(zones.size >= GravitationalLensingShader.len) PMShaders.createBlackholeShader();

                float[] blackholes = new float[zones.size * 4];
                float[] colors = new float[zones.size * 4];
                for(int i = 0; i < zones.size; i++){
                    BlackHoleZone zone = zones.get(i);
                    blackholes[i * 4] = zone.x;
                    blackholes[i * 4 + 1] = zone.y;
                    blackholes[i * 4 + 2] = zone.inRadius;
                    blackholes[i * 4 + 3] = zone.outRadius;

                    Tmp.c1.abgr8888(zone.color);
                    colors[i * 4] = Tmp.c1.r;
                    colors[i * 4 + 1] = Tmp.c1.g;
                    colors[i * 4 + 2] = Tmp.c1.b;
                    colors[i * 4 + 3] = Tmp.c1.a;
                }
                PMShaders.blackHole.blackholes = blackholes;
                buffer.blit(PMShaders.blackHole);

                PMShaders.accretionDisk.blackholes = blackholes;
                PMShaders.accretionDisk.colors = colors;
                buffer.begin();
                Draw.rect();
                buffer.end();

                Bloom bloom = renderer.bloom;
                if(bloom != null){
                    bloom.capture();
                    buffer.blit(PMShaders.accretionDisk);
                    bloom.render();
                }else{
                    buffer.blit(PMShaders.accretionDisk);
                }

                zones.clear();
            });
        });
    }

    public void add(float x, float y, float inRadius, float outRadius, Color color){
        if(inRadius > outRadius || outRadius <= 0) return;

        float res = Color.toFloatBits(color.r, color.g, color.b, 1);

        zones.add(new BlackHoleZone(x, y, res, inRadius, outRadius));
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
