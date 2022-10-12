package progressed.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.draw.*;
import progressed.world.blocks.defence.turret.payload.*;
import progressed.world.blocks.defence.turret.payload.SinglePayloadAmmoTurret.*;

public class DrawPayloadTurret extends DrawTurret{
    public String regionSuffix = "";
    public boolean drawTurret;
    public TextureRegion in;

    public DrawPayloadTurret(String basePrefix){
        this.basePrefix = basePrefix;
    }

    public DrawPayloadTurret(boolean drawTurret){
        this.drawTurret = drawTurret;
    }

    @Override
    public void draw(Building build){
        SinglePayloadAmmoTurret turret = (SinglePayloadAmmoTurret)build.block;
        SinglePayloadAmmoTurretBuild tb = (SinglePayloadAmmoTurretBuild)build;

        Draw.rect(base, build.x, build.y);
        for(int i = 0; i < 4; i++){
            if(PayloadBlock.blends(tb, i)){
                Draw.rect(in, tb.x, tb.y, (i * 90) - 180);
            }
        }
        drawPayload(tb);
        Draw.z(Layer.blockOver + 0.1f);
        if(drawTurret){
            Draw.rect(top, build.x, build.y);

            Draw.color();

            Draw.z(Layer.turret - 0.5f);

            Drawf.shadow(preview, build.x + tb.recoilOffset.x - turret.elevation, build.y + tb.recoilOffset.y - turret.elevation, tb.drawrot());

            Draw.z(Layer.turret);

            drawTurret(turret, tb);
            drawHeat(turret, tb);
        }else{
            if(top.found()) Draw.rect(top, build.x, build.y);
            Draw.rect(turret.region, build.x, build.y);
            drawHeat(turret, tb);

            Draw.z(Layer.turret);
        }

        if(parts.size > 0){
            if(outline.found()){
                //draw outline under everything when parts are involved
                Draw.z(Layer.turret - 0.01f);
                Draw.rect(outline, build.x + tb.recoilOffset.x, build.y + tb.recoilOffset.y, tb.drawrot());
                Draw.z(Layer.turret);
            }

            float progress = tb.progress();

            //TODO no smooth reload
            var params = DrawPart.params.set(build.warmup(), 1f - progress, 1f - progress, tb.heat, tb.curRecoil, tb.charge, tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation);

            for(var part : parts){
                part.draw(params);
            }
        }
    }

    public void drawPayload(SinglePayloadAmmoTurretBuild build){
        if(build.payload != null){
            build.updatePayload();

            Draw.z(Layer.blockOver);
            build.payload.draw();
        }
    }

    @Override
    public void drawHeat(Turret block, TurretBuild build){
        if(build.heat <= 0.00001f || !heat.found()) return;

        if(drawTurret){
            Drawf.additive(heat, block.heatColor.write(Tmp.c1).a(build.heat), build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.drawrot(), Layer.turretHeat);
        }else{
            Drawf.additive(heat, block.heatColor.write(Tmp.c1).a(build.heat), build.x, build.y, 0f, Layer.turretHeat);
        }
    }

    @Override
    public void load(Block block){
        if(!(block instanceof SinglePayloadAmmoTurret)) throw new ClassCastException("This drawer can only be used on single payload ammo turrets.");

        super.load(block);

        in = Core.atlas.find(block.name + "-in", "factory-in-" + block.size + regionSuffix);
        top = Core.atlas.find(block.name + "-top", drawTurret ? "factory-top-" + block.size + regionSuffix : "");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return top.found() ? new TextureRegion[]{base, in, top, preview} : new TextureRegion[]{base, in, preview};
    }
}
