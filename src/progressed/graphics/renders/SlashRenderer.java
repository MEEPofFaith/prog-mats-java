package progressed.graphics.renders;

import arc.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import blackhole.graphics.*;
import progressed.graphics.PMShaders.*;

import static arc.Core.*;

public class SlashRenderer{
    private static int maxCount = 4;
    private static SlashShader slashShader;
    private static final Seq<SlashData> slashes = new Seq<>();
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

    public static void addSlash(float x, float y, float a, float off){
        slashes.add(new SlashData(x, y, a, off));
    }

    public static void draw(){
        Draw.draw(BHLayer.begin - 0.1f, () -> {
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin();
        });

        Draw.draw(BHLayer.end + 1f, () -> {
            buffer.end();

            if(slashes.size > maxCount) createShader();

            float[] slashArray = new float[slashes.size * 4];
            for(int i = 0; i < slashes.size; i++){
                SlashData slash = slashes.get(i);
                slashArray[i * 4] = slash.x;
                slashArray[i * 4 + 1] = slash.y;
                slashArray[i * 4 + 2] = Mathf.mod(slash.angle, Mathf.PI2)   ;
                slashArray[i * 4 + 3] = slash.offset;
            }
            slashShader.slashes = slashArray;
            buffer.blit(slashShader);
            slashes.clear();
        });
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

        public SlashData(float x, float y, float angle, float offset){
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.offset = offset;
        }
    }
}
