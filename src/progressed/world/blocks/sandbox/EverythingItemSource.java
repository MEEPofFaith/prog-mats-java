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

public class EverythingItemSource extends Block{
    public float ticksPerItemColor = 90f;

    public TextureRegion strobeRegion, centerRegion;

    public EverythingItemSource(String name){
        super(name);
        requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.empty);

        health = 999999999;
        hasItems = true;
        update = true;
        solid = true;
        group = BlockGroup.transportation;
        noUpdateDisabled = true;
        envEnabled = Env.any;
    }

    @Override
    public void load(){
        super.load();
        strobeRegion = Core.atlas.find(name + "-strobe", "prog-mats-source-strobe");
        centerRegion = Core.atlas.find(name + "-center", "center");
    }

    public class EverythingItemSourceBuild extends Building{
        @Override
        public void draw(){
            super.draw();

            float speed = Core.settings.getInt("pm-strobespeed") / 2f;
            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
            Draw.rect(strobeRegion, x, y);

            Draw.color(Tmp.c1.lerp(PMPal.itemColors, Time.time / (ticksPerItemColor * PMPal.itemColors.length) % 1f));
            Draw.rect(centerRegion, x, y);
            Draw.color();
        }

        @Override
        public void updateTile(){
            Vars.content.items().each(i -> {
                items.set(i, 1);
                dump(i);
                items.set(i, 0);
            });
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return false;
        }
    }
}