package progressed.graphics.renders;

import arc.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import blackhole.graphics.*;
import progressed.graphics.PMShaders.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SlashRenderer{
    private static int maxCount = 4;
    private static SlashShader slashShader;
    private static final Seq<SlashData> slashes = new Seq<>(SlashData.class);
    private static int slashIndex = 0;
    private static FrameBuffer buffer;

    private static void createShader(){
        if(maxCount >= 1021) return; //Exceeds maximum number of registers for a single shader

        if(slashShader != null){
            maxCount = Math.min(1021, maxCount * 2);
            slashShader.dispose();
        }

        Shader.prependFragmentCode = "#define MAX_COUNT " + maxCount + "\n";
        slashShader = new SlashShader();
        Shader.prependFragmentCode = "";
    }

    public static void init(){
        createShader();
        buffer = new FrameBuffer();
    }

    public static void addSlash(float x, float y, float a, float off, float length, float width, float color){
        if(off <= 0.001f) return;
        if(slashes.size <= slashIndex) slashes.add(new SlashData());

        //Pool slashes
        var slash = slashes.items[slashIndex];
        slash.set(x, y, a, off, length, width, color);

        slashIndex++;
    }

    public static void draw(){
        Draw.draw(BHLayer.begin - 0.1f, () -> {
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin();
        });

        Draw.draw(BHLayer.end + 1f, () -> {
            buffer.end();

            while(slashes.size > maxCount) createShader();

            float[] slashArray = new float[slashIndex * 4];
            for(int i = 0; i < slashIndex; i++){
                SlashData slash = slashes.get(i);
                slashArray[i * 4] = slash.x;
                slashArray[i * 4 + 1] = slash.y;
                slashArray[i * 4 + 2] = Mathf.mod(Mathf.halfPi - slash.angle, Mathf.PI2);
                slashArray[i * 4 + 3] = slash.offset;
            }
            slashShader.slashes = slashArray;
            buffer.blit(slashShader);

            if(renderer.bloom != null){
                renderer.bloom.capture();
                drawSlashes();
                renderer.bloom.render();
            }else{
                drawSlashes();
            }

            slashIndex = 0;
        });
    }

    private static void drawSlashes(){
        for(int i = 0; i < slashIndex; i++){
            SlashData slash = slashes.items[i];
            float ang = slash.angle * Mathf.radDeg;
            Tmp.v1.trns(ang, slash.length);
            Tmp.v2.trns(ang + 90f, slash.width);

            Draw.color(slash.color);
            Fill.quad(
                slash.x + Tmp.v1.x, slash.y + Tmp.v1.y,
                slash.x + Tmp.v2.x, slash.y + Tmp.v2.y,
                slash.x - Tmp.v1.x, slash.y - Tmp.v1.y,
                slash.x - Tmp.v2.x, slash.y - Tmp.v2.y
            );
        }
    }

    private static class SlashShader extends PMLoadShader{
        public float[] slashes;

        private SlashShader(){
            super("screenspace", "slash");
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);

            setUniformi("u_slashescount", slashes.length / 4);
            setUniform4fv("u_slashes", slashes, 0, slashes.length);
        }
    }

    private static class SlashData{
        public float x, y, angle, offset;
        public float length, width, color;

        public void set(float x, float y, float angle, float offset, float length, float width, float color){
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.offset = offset;
            this.length = length;
            this.width = width;
            this.color = color;
        }
    }
}
