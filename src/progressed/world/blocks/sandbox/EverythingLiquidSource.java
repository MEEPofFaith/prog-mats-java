package progressed.world.blocks.sandbox;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.graphics.*;

public class EverythingLiquidSource extends Block{
    public float ticksPerItemColor = 90f;

    public TextureRegion strobeRegion, centerRegion;

    public EverythingLiquidSource(String name){
        super(name);
        requirements(Category.liquid, BuildVisibility.sandboxOnly, ItemStack.empty);

        health = 999999999;
        update = true;
        solid = true;
        hasLiquids = true;
        liquidCapacity = 100f;
        outputsLiquid = true;
        noUpdateDisabled = true;
        displayFlow = false;
        group = BlockGroup.liquids;
        envEnabled = Env.any;
    }

    @Override
    public void load(){
        super.load();
        strobeRegion = Core.atlas.find(name + "-strobe", "prog-mats-source-strobe");
        centerRegion = Core.atlas.find(name + "-center", "center");
    }

    public class EverythingLiquidSourceBuild extends Building{
        @Override
        public void draw(){
            super.draw();

            float speed = Core.settings.getInt("pm-strobespeed") / 2f;
            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
            Draw.rect(strobeRegion, x, y);

            Draw.color(Tmp.c1.lerp(PMPal.liquidColors, Time.time / (ticksPerItemColor * PMPal.liquidColors.length) % 1f));
            Draw.rect(centerRegion, x, y);
            Draw.color();
        }

        @Override
        public void updateTile(){
            super.updateTile();

            Vars.content.liquids().each(l -> {
                liquids.add(l, liquidCapacity);
                dumpLiquid(l);
                liquids.clear();
            });
        }
    }
}