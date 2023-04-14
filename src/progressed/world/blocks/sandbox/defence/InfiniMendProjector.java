package progressed.world.blocks.sandbox.defence;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class InfiniMendProjector extends MendProjector{
    static final Vec2 configs = new Vec2();

    public InfiniMendProjector(String name){
        super(name);
        requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        health = 400;
        configurable = saveConfig = true;
        hasPower = hasItems = false;
        suppressable = false;
        range = 80f;
        reload = 30f;
        healPercent = 2f;

        config(Vec2.class, (InfiniMendBuild tile, Vec2 values) -> {
            tile.repairTime = values.x;
            tile.setRange = values.y;
        });
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.repairTime);
        stats.remove(Stat.range);
        stats.remove(Stat.boostEffect);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        //skip the stuff added in MendProjector
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
    }

    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){ //Don't run drawPlanConfig
        TextureRegion reg = getPlanRegion(plan, list);
        Draw.rect(reg, plan.drawx(), plan.drawy(), !rotate || !rotateDraw ? 0 : plan.rotation * 90);

        if(plan.worldContext && player != null && teamRegion != null && teamRegion.found()){
            if(teamRegions[player.team().id] == teamRegion) Draw.color(player.team().color);
            Draw.rect(teamRegions[player.team().id], plan.drawx(), plan.drawy());
            Draw.color();
        }
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        float realRange = plan.config != null ? ((Vec2)plan.config).y : range;

        Drawf.dashCircle(plan.drawx(), plan.drawy(), realRange, baseColor);

        indexer.eachBlock(player.team(), plan.drawx(), plan.drawy(), realRange, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }

    float extractHealPercent(float repairTime){
        if(repairTime == 0) return 1f; //No dividing by 0 on my watch.
        return 1f / (repairTime * 60f / reload);
    }

    public class InfiniMendBuild extends MendBuild{
        public float repairTime = 100f / healPercent * reload / 60f, setRange = range;

        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += heat * delta();

            phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);

            if(optionalEfficiency > 0 && timer(timerUse, useTime)){
                consume();
            }

            if(charge >= reload){
                charge = 0f;

                float healPercent = extractHealPercent(repairTime);
                indexer.eachBlock(this, setRange, Building::damaged, other -> {
                    other.heal(other.maxHealth() * healPercent * efficiency);
                    other.recentlyHealed();
                    Fx.healBlockFull.at(other.x, other.y, other.block.size, baseColor, other.block);
                });
            }
        }

        @Override
        public void drawSelect(){
            indexer.eachBlock(this, setRange, other -> true, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));

            Drawf.dashCircle(x, y, setRange, baseColor);
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.black5, t -> {
                t.marginLeft(6f).marginRight(6f).right();
                t.field(String.valueOf(repairTime), text -> {
                    float newRepairTime = repairTime;
                    if(Strings.canParsePositiveFloat(text)){
                        newRepairTime = Strings.parseFloat(text);
                    }
                    configure(configs.set(newRepairTime, setRange));
                }).width(120).get().setFilter(TextFieldFilter.floatsOnly);
                t.add(Core.bundle.get("unit.seconds")).left();

                t.row();
                t.field(String.valueOf(setRange / 8f), text -> {
                    float newRange = setRange;
                    if(Strings.canParsePositiveFloat(text)){
                        newRange = Strings.parseFloat(text) * 8f;
                    }
                    configure(configs.set(repairTime, newRange));
                }).width(120).get().setFilter(TextFieldFilter.floatsOnly);
                t.add(Core.bundle.get("unit.blocks")).left();
            });
        }

        @Override
        public float range(){
            return setRange;
        }

        @Override
        public Object config(){
            return configs.set(repairTime, setRange).cpy();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(repairTime);
            write.f(setRange);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            repairTime = read.f();
            setRange = read.f();
        }
    }
}
