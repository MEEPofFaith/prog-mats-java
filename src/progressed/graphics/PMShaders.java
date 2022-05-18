package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMShaders{
    public static MaterializeShader materialize;
    public static VerticalBuildShader vertBuild;
    public static BlockBuildCenterShader blockBuildCenter;

    public static void init(){
        materialize = new MaterializeShader();
        vertBuild = new VerticalBuildShader();
        blockBuildCenter = new BlockBuildCenterShader();
    }

    public static class MaterializeShader extends PMLoadShader{
        public float progress, offset, time;
        public int shadow;
        public Color color = new Color();
        public TextureRegion region;

        MaterializeShader(){
            super("materialize");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_offset", offset);
            setUniformf("u_time", time);
            setUniformf("u_width", region.width);
            setUniformf("u_shadow", shadow);
            setUniformf("u_color", color);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class VerticalBuildShader extends PMLoadShader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public VerticalBuildShader(){
            super("vertbuild");
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

    public static class BlockBuildCenterShader extends PMLoadShader{
        public float progress;
        public TextureRegion region;
        public float time;

        BlockBuildCenterShader(){
            super("blockbuildcenter");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_time", time);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    static class PMLoadShader extends Shader{
        PMLoadShader(String frag){
            super(
                files.internal("shaders/default.vert"),
                tree.get("shaders/" + frag + ".frag")
            );
        }
    }
}
