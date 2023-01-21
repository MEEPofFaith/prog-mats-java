package progressed.world.draw;

import arc.graphics.g2d.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;
import progressed.world.blocks.defence.turret.*;
import progressed.world.blocks.defence.turret.SwingContinuousTurret.*;

/** Passes the rotation speed fract into the life param for draw parts. */
public class DrawSwingTurret extends DrawTurret{
    public DrawSwingTurret(String basePrefix){
        this.basePrefix = basePrefix;
    }

    public DrawSwingTurret(){
    }

    @Override
    public void draw(Building build){
        SwingContinuousTurret turret = (SwingContinuousTurret)build.block;
        SwingContinuousTurretBuild tb = (SwingContinuousTurretBuild)build;

        Draw.rect(base, build.x, build.y);
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
            params.life = tb.rotateSpeedf;

            for(var part : parts){
                part.draw(params);
            }
        }
    }
}
