package progressed.world.blocks.sandbox.items;

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

public class MaterialSourceVoid extends Block{
    public float ticksPerItemColor = 90f;

    public TextureRegion strobeRegion;
    public TextureRegion[] center = new TextureRegion[2];

    public MaterialSourceVoid(String name){
        super(name);
        requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        update = solid = true;
        hasItems = hasLiquids = acceptsItems = true;
        displayFlow = false;
        liquidCapacity = 10000f;
        group = BlockGroup.transportation;
        noUpdateDisabled = true;
        envEnabled = Env.any;
    }

    @Override
    public void load(){
        super.load();
        strobeRegion = Core.atlas.find(name + "-strobe", "prog-mats-source-strobe");
        center[0] = Core.atlas.find(name + "-center-0");
        center[1] = Core.atlas.find(name + "-center-1");
    }

    @Override
    public void setBars(){
        super.setBars();

        removeBar("items");
        removeBar("liquid");
    }

    public class MaterialSourceVoidBuild extends Building{
        @Override
        public void draw(){
            super.draw();

            float speed = Core.settings.getInt("pm-strobespeed") / 2f;
            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
            Draw.rect(strobeRegion, x, y);

            Draw.color(Tmp.c1.lerp(PMPal.itemColors, Time.time / (ticksPerItemColor * PMPal.itemColors.length) % 1f));
            Draw.rect(center[0], x, y);
            Draw.color(Tmp.c1.lerp(PMPal.liquidColors, Time.time / (ticksPerItemColor * PMPal.liquidColors.length) % 1f));
            Draw.rect(center[1], x, y);
            Draw.color();
        }

        @Override
        public void updateTile(){
            Vars.content.items().each(i -> {
                items.set(i, 1);
                dump(i);
                items.set(i, 0);
            });

            Vars.content.liquids().each(l -> {
                liquids.add(l, liquidCapacity);
                dumpLiquid(l);
                liquids.clear();
            });
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return enabled;
        }

        @Override
        public void handleItem(Building source, Item item){}

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return enabled;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){}
    }
}
