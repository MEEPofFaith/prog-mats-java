package progressed.world.draw;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.entities.part.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.PayloadAmmoTurret.*;
import mindustry.world.draw.*;
import progressed.graphics.*;

public class DrawPayloadAmmo extends DrawBlock{
    public PartProgress progress = PartProgress.warmup;
    public PartProgress matProgress = PartProgress.warmup;
    public boolean materialize = true, fade = true;
    public float layer = Layer.turret - 0.005f;
    public float x, y, xScl = 1f, yScl = 1f, rotation;
    public float moveX, moveY, growX, growY;
    public Seq<PartMove> moves = new Seq<>();

    @Override
    public void draw(Building build){
        PayloadTurretBuild tb = (PayloadTurretBuild)build;

        UnlockableContent pAmmo = tb.currentAmmo();
        if(pAmmo == null) return;

        float tProgress = tb.progress();
        var params = DrawPart.params.set(build.warmup(), 1f - tProgress, 1f - tProgress, tb.heat, tb.curRecoil, tb.charge, tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation);

        float prog = progress.getClamp(params);
        float mx = moveX * prog, my = moveY * prog, gx = xScl + growX * prog, gy = yScl + growY * prog;

        if(moves.size > 0){
            for(int i = 0; i < moves.size; i++){
                var move = moves.get(i);
                float p = move.progress.getClamp(params);
                mx += move.x * p;
                my += move.y * p;
            }
        }
        Tmp.v1.set((x + mx) * Draw.xscl, (y + my) * Draw.yscl).rotateRadExact((params.rotation - 90) * Mathf.degRad);

        float rx = params.x + Tmp.v1.x, ry = params.y + Tmp.v1.y, rot = params.rotation - 90f + rotation;

        Draw.scl(gx, gy);
        if(materialize){
            float matProg = matProgress.getClamp(params);;
            Draw.draw(layer, () -> {
                PMDrawf.materialize(rx, ry, pAmmo.fullIcon, tb.team.color, rot, 0.1f, matProg, -Time.time / 4f);
            });
        }else{
            Draw.z(layer);
            if(fade) Draw.alpha(matProgress.getClamp(params));
            Draw.rect(pAmmo.fullIcon, rx, ry, rot);
            Draw.alpha(1f);
        }
        Draw.scl();
    }

    @Override
    public void load(Block block){
        if(!(block instanceof PayloadAmmoTurret)) throw new ClassCastException("This drawer can only be used on payload ammo turrets.");

        //Nothing to load
    }
}
