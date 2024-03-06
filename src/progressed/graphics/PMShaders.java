package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.scene.ui.layout.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMShaders{
    public static MaterializeShader materialize;
    public static VerticalBuildShader vertBuild;
    public static BlockBuildCenterShader blockBuildCenter;
    public static TractorConeShader tractorCone;
    public static AlphaShader alphaShader;
    public static DimShader dimShader;
    public static GravitationalLensingShader blackHole;
    public static AccretionDiskShader accretionDisk;
    public static PassThroughShader passThrough;

    public static void init(){
        materialize = new MaterializeShader();
        vertBuild = new VerticalBuildShader();
        blockBuildCenter = new BlockBuildCenterShader();
        tractorCone = new TractorConeShader();
        alphaShader = new AlphaShader();
        dimShader = new DimShader();
        passThrough = new PassThroughShader();

        createBlackholeShader();
    }

    public static void createBlackholeShader(){
        if(blackHole != null){
            GravitationalLensingShader.len *= 2;
            blackHole.dispose();
            accretionDisk.dispose();
        }

        Shader.prependFragmentCode = "#define MAX_COUNT " + GravitationalLensingShader.len + "\n";
        blackHole = new GravitationalLensingShader();
        accretionDisk = new AccretionDiskShader();
        Shader.prependFragmentCode = "";
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

    public static class TractorConeShader extends PMLoadShader{
        public float cx, cy;
        public float time, spacing, thickness;

        TractorConeShader(){
            super("screenspace", "tractorcone");
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", time / Scl.scl(1f));
            setUniformf("u_offset",
                Core.camera.position.x - Core.camera.width / 2,
                Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);

            setUniformf("u_spacing", spacing / Scl.scl(1f));
            setUniformf("u_thickness", thickness / Scl.scl(1f));
            setUniformf("u_cx", cx / Scl.scl(1f));
            setUniformf("u_cy", cy / Scl.scl(1f));
        }

        public void setCenter(float cx, float cy){
            this.cx = cx;
            this.cy = cy;
        }
    }

    public static class AlphaShader extends PMLoadShader{
        public float alpha = 1f;

        AlphaShader(){
            super("screenspace", "postalpha");
        }

        @Override
        public void apply(){
            setUniformf("u_alpha", alpha);
        }
    }

    public static class DimShader extends PMLoadShader{
        public float alpha;

        DimShader(){
            super("screenspace", "dim");
        }

        @Override
        public void apply(){
            setUniformf("u_alpha", alpha);
        }
    }

    public static class GravitationalLensingShader extends PMLoadShader{
        public static int len = 4;
        public float[] blackholes;

        GravitationalLensingShader(){
            super("screenspace", "gravitationallensing");
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);

            setUniformi("u_blackholecount", blackholes.length / 4);
            setUniform4fv("u_blackholes", blackholes, 0, blackholes.length);
        }
    }

    public static class AccretionDiskShader extends PMLoadShader{
        public float[] blackholes;
        public float[] colors;

        AccretionDiskShader(){
            super("screenspace", "accretiondisk");
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);

            setUniformi("u_blackholecount", blackholes.length / 4);
            setUniform4fv("u_blackholes", blackholes, 0, blackholes.length);
            setUniform4fv("u_colors", colors, 0, colors.length);
        }
    }

    static class PassThroughShader extends PMLoadShader{
        public PassThroughShader(){
            super("screenspace", "passThrough");
        }
    }

    static class PMLoadShader extends Shader{
        PMLoadShader(String vert, String frag){
            super(
                files.internal("shaders/" + vert + ".vert"),
                tree.get("shaders/" + frag + ".frag")
            );
        }

        PMLoadShader(String frag){
            this("default", frag);
        }
    }
}
