package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;

import static mindustry.Vars.*;

public class PMShaders{
    public static VerticalBuildShader vertBuild;

    public static void load(){
        vertBuild = new VerticalBuildShader();
    }

    public static class VerticalBuildShader extends Shader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public VerticalBuildShader(){
            super(
                Core.files.internal("shaders/default.vert"),
                tree.get("shaders/vertbuild.frag")
            );
        }

        @Override
        public void apply(){
            setUniformf("u_time", time);
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

}