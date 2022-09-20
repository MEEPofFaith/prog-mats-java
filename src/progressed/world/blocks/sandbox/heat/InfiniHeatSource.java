package progressed.world.blocks.sandbox.heat;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.*;

public class InfiniHeatSource extends Block{
    public DrawBlock drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
    public float heatLerpTime = 60f;

    public InfiniHeatSource(String name){
        super(name);
        requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        health = ProgMats.sandboxBlockHealth;
        configurable = saveConfig = true;

        update = true;
        solid = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);

        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;

        config(Float.class, (InfinitHeatSourceBuild build, Float f) -> {
            build.targetHeat = f;
            build.oldHeat = build.smoothHeat;
        });
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    public class InfinitHeatSourceBuild extends Building implements HeatBlock{
        public float targetHeat = 20, smoothHeat, oldHeat;

        @Override
        public void updateTile(){
            super.updateTile();

            smoothHeat = Mathf.approachDelta(smoothHeat, targetHeat, Math.abs(targetHeat - oldHeat) * delta() / heatLerpTime);
        }

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.black5, t -> {
                t.marginLeft(6f).marginRight(6f).right();
                t.field(String.valueOf(targetHeat), text -> {
                    float newHeat = targetHeat;
                    if(Strings.canParsePositiveFloat(text)){
                        newHeat = Strings.parseFloat(text);
                    }
                    configure(newHeat);
                }).width(120).get().setFilter(TextFieldFilter.floatsOnly);
                t.add(Core.bundle.get("unit.heatunits")).left();
            });
        }

        @Override
        public Object config(){
            return targetHeat;
        }

        @Override
        public float warmup(){
            return 1f;
        }

        @Override
        public float heatFrac(){
            return smoothHeat / targetHeat;
        }

        @Override
        public float heat(){
            return smoothHeat;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(smoothHeat);
            write.f(targetHeat);
            write.f(oldHeat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            smoothHeat = read.f();
            targetHeat = read.f();
            oldHeat = read.f();
        }
    }
}
