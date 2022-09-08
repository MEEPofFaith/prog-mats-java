package progressed.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.draw.*;
import progressed.world.blocks.defence.turret.payload.*;
import progressed.world.blocks.defence.turret.payload.SinglePayloadAmmoTurret.*;

public class DrawPayloadTurret extends DrawTurret{
    public String regionSuffix = "";
    public TextureRegion inRegion, topRegion;

    public DrawPayloadTurret(String basePrefix){
        this.basePrefix = basePrefix;
    }

    public DrawPayloadTurret(){

    }

    @Override
    public void draw(Building build){
        SinglePayloadAmmoTurret turret = (SinglePayloadAmmoTurret)build.block;
        SinglePayloadAmmoTurretBuild tb = (SinglePayloadAmmoTurretBuild)build;

        Draw.rect(base, build.x, build.y);
        for(int i = 0; i < 4; i++){
            if(PayloadBlock.blends(tb, i)){
                Draw.rect(inRegion, tb.x, tb.y, (i * 90) - 180);
            }
        }
        drawPayload(tb);
        Draw.z(Layer.blockOver + 0.1f);
        Draw.rect(topRegion, build.x, build.y);

        Draw.color();

        Draw.z(Layer.turret - 0.5f);

        Drawf.shadow(preview, build.x + tb.recoilOffset.x - turret.elevation, build.y + tb.recoilOffset.y - turret.elevation, tb.drawrot());

        Draw.z(Layer.turret);

        drawTurret(turret, tb);
        drawHeat(turret, tb);

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
    public void load(Block block){
        if(!(block instanceof SinglePayloadAmmoTurret)) throw new ClassCastException("This drawer can only be used on single payload ammo turrets.");

        super.load(block);

        inRegion = Core.atlas.find(block.name + "-in", "factory-in-" + block.size + regionSuffix);
        topRegion = Core.atlas.find(block.name + "-top", "factory-top-" + block.size + regionSuffix);
    }
}
