package progressed.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.turret.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.turret.TurretModule.*;

public class DrawTurretModule extends DrawTurret{
    protected static final Rand rand = new Rand();

    public Seq<DrawPart> parts = new Seq<>();
    /** Overrides the liquid to draw in the liquid region. */
    public Liquid liquidDraw;
    public TextureRegion base, liquid, top, heat, preview, outline;

    @Override
    public void getRegionsToOutline(Block block, Seq<TextureRegion> out){
        for(var part : parts){
            part.getOutlines(out);
        }

        if(block.region.found() && !(block.outlinedIcon > 0 && block.getGeneratedIcons()[block.outlinedIcon].equals(block.region))){
            out.add(block.region);
        }
    }

    @Override
    public void draw(Building build){
        TurretModule turret = (TurretModule)build.block;
        TurretModuleBuild tb = (TurretModuleBuild)build;

        Draw.rect(base, build.x, build.y);
        Draw.color();

        Draw.z(Layer.turret - 0.5f);

        Drawf.shadow(preview, build.x + TurretModule.recoilOffset.x - turret.elevation, build.y + TurretModule.recoilOffset.y - turret.elevation, tb.drawrot());

        Draw.z(Layer.turret);

        drawTurret(turret, tb);
        drawHeat(turret, tb);

        if(parts.size > 0){
            if(outline.found()){
                //draw outline under everything when parts are involved
                Draw.z(Layer.turret - 0.01f);
                Draw.rect(outline, build.x + TurretModule.recoilOffset.x, build.y + TurretModule.recoilOffset.y, tb.drawrot());
                Draw.z(Layer.turret);
            }

            float progress = tb.progress();

            //TODO no smooth reload
            var params = DrawPart.params.set(build.warmup(), 1f - progress, 1f - progress, tb.heat, tb.curRecoil, tb.charge, tb.x + TurretModule.recoilOffset.x, tb.y + TurretModule.recoilOffset.y, tb.rotation);

            for(var part : parts){
                part.draw(params);
            }
        }
    }

    public void drawTurret(TurretModule block, TurretModuleBuild build){
        if(block.region.found()){
            Draw.rect(block.region, build.x + TurretModule.recoilOffset.x, build.y + TurretModule.recoilOffset.y, build.drawrot());
        }

        if(liquid.found()){
            Liquid toDraw = liquidDraw == null ? build.liquids.current() : liquidDraw;
            Drawf.liquid(liquid, build.x + TurretModule .recoilOffset.x, build.y + TurretModule.recoilOffset.y, build.liquids.get(toDraw) / block.liquidCapacity, toDraw.color.write(Tmp.c1).a(1f), build.drawrot());
        }

        if(top.found()){
            Draw.rect(top, build.x + TurretModule.recoilOffset.x, build.y + TurretModule.recoilOffset.y, build.drawrot());
        }
    }

    public void drawHeat(TurretModule block, TurretModuleBuild build){
        if(build.heat <= 0.00001f || !heat.found()) return;

        Drawf.additive(heat, block.heatColor.write(Tmp.c1).a(build.heat), build.x + TurretModule.recoilOffset.x, build.y + TurretModule.recoilOffset.y, build.drawrot(), Layer.turretHeat);
    }

    /** Load any relevant texture regions. */
    @Override
    public void load(Block block){
        if(!(block instanceof TurretModule)) throw new ClassCastException("This drawer can only be used on turrets.");

        preview = Core.atlas.find(block.name + "-preview", block.region);
        outline = Core.atlas.find(block.name + "-outline");
        liquid = Core.atlas.find(block.name + "-liquid");
        top = Core.atlas.find(block.name + "-top");
        heat = Core.atlas.find(block.name + "-heat");
        base = Core.atlas.find(block.name + "-base");

        for(var part : parts){
            part.turretShading = true;
            part.load(block.name);
        }

        if(!base.found() && block.minfo.mod != null) base = Core.atlas.find(block.minfo.mod.name + "-" + basePrefix + "block-" + block.size);
        if(!base.found()) base = Core.atlas.find(basePrefix + "block-" + block.size);
    }

    /** @return the generated icons to be used for this block. */
    @Override
    public TextureRegion[] icons(Block block){
        return top.found() ? new TextureRegion[]{base, preview, top} : new TextureRegion[]{base, preview};
    }
}
