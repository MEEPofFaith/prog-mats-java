package progressed.world.draw;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;
import progressed.world.blocks.crafting.AccelerationCrafter.*;

/** DrawPlasma, but based off of an {@link AcceleratingCrafterBuild}'s speed instead of warmup. */
public class DrawSpeedPlasma extends DrawPlasma{
    @Override
    public void draw(Building build){
        if(!(build instanceof AcceleratingCrafterBuild b)) return;

        float warmup = Interp.pow3In.apply(b.getSpeed());
        Draw.blend(Blending.additive);
        for(int i = 0; i < regions.length; i++){ //Haha draw code stolen from Impact Reactor
            float r = ((float)regions[i].width * Draw.scl - 3f + Mathf.absin(Time.time, 2f + i * 1f, 5f - i * 0.5f));

            Draw.color(plasma1, plasma2, (float)i / regions.length);
            Draw.alpha((0.3f + Mathf.absin(Time.time, 2f + i * 2f, 0.3f + i * 0.05f)) * warmup);
            Draw.rect(regions[i], b.x, b.y, r, r, b.totalActivity * (12 + i * 6f));
        }
        Draw.blend();
        Draw.color();
    }

    @Override
    public void drawLight(Building build){
        if(!(build instanceof AcceleratingCrafterBuild b)) return;

        Drawf.light(
            build.x, build.y,
            (110f + Mathf.absin(5, 5f)) * b.getSpeed(),
            Tmp.c1.set(plasma2).lerp(plasma1, Mathf.absin(7f, 0.2f)),
            0.8f * b.getSpeed()
        );
    }
}
