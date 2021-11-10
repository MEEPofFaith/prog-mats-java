package progressed.world.blocks.defence;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class SystemBooster extends Block{
    public float reload = 60f;
    public float speedBoost = 1.1f;
    public float basePowerUse, powerPerBlock = 1f;
    public Color boostColor = Color.valueOf("feb380");

    public TextureRegion topRegion;

    public SystemBooster(String name){
        super(name);
        solid = true;
        update = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
        emitLight = true;
        lightRadius = 50f;
    }

    @Override
    public void load(){
        super.load();

        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void init(){
        consumes.add(new BoosterConsumePower());

        super.init();
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("boost", (SystemBoosterBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-totalboost", Mathf.round(Math.max((entity.realBoost() * 100 - 100), 0)), entity.boosters),
            () -> Pal.accent,
            () -> entity.realBoost() / entity.maxBoost
        ));
    }

    public class SystemBoosterBuild extends Building{
        float heat;
        float charge = Mathf.random(reload);
        float totalPowerUse, maxBoost;
        int boosters, boosted;
        float smoothEfficiency;

        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency(), 0.08f);
            heat = Mathf.lerpDelta(heat, consValid() ? 1f : 0f, 0.08f);
            charge += heat * Time.delta;

            updateBoost();
            updatePowerUse();

            if(charge >= reload){
                charge = 0f;
                power.graph.all.each(b -> b.block.canOverdrive, b -> b.applyBoost(realBoost(), reload + 1f));
            }
        }

        public float realBoost(){
            return maxBoost * efficiency();
        }

        public void updateBoost(){
            maxBoost = 0;
            boosters = 0;
            power.graph.all.each(b -> b instanceof SystemBoosterBuild, (SystemBoosterBuild b) -> {
                maxBoost += ((SystemBooster)(b.block)).speedBoost - 1f;
                boosters++;
            });
            maxBoost++;
        }

        public void updatePowerUse(){
            totalPowerUse = basePowerUse;
            boosted = 0;
            power.graph.all.each(b -> b.block.canOverdrive, b -> {
                totalPowerUse += powerPerBlock;
                boosted++;
            });
        }

        @Override
        public void drawSelect(){
            power.graph.all.each(b -> b.block.canOverdrive, b -> {
                Drawf.selected(b, Tmp.c1.set(boostColor).a(Mathf.absin(4f, 1f)));
            });
        }

        @Override
        public void draw(){
            super.draw();

            Draw.color(boostColor);
            Draw.alpha(heat * Mathf.absin(Time.time, 10f, 1f) * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.alpha(1f);

            float f = 1f - (Time.time / 100f) % 1f;
            Lines.stroke(f * heat);
            power.links.each(i -> {
                Building b = world.build(i);
                float angle1 = Angles.angle(x, y, b.x, b.y),
                    vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                    len1 = size * tilesize / 2f - 1.5f;

                Lines.circle(
                    x + vx * len1,
                    y + vy * len1,
                    (1f - f) * 3f
                );
            });
        }

        @Override
        public void display(Table table){
            super.display(table);

            table.row();
            table.label(() -> Core.bundle.format("pm-poweruse", Strings.autoFixed(totalPowerUse * 60f, 2))).left().fillX().wrap();
            table.row();
            table.label(() -> Core.bundle.format("pm-overdrivedcount", boosted)).left().padLeft(18).fillX().wrap();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            heat = read.f();
        }
    }

    protected class BoosterConsumePower extends ConsumePower{
        @Override
        public float requestedPower(Building entity){
            return ((SystemBoosterBuild)entity).totalPowerUse;
        }

        @Override
        public void display(Stats stats){
            stats.add(Stat.powerUse, s -> {
                if(basePowerUse != 0){
                    StatValues.number(basePowerUse * 60f, StatUnit.powerSecond).display(s);
                    s.add(" + ");
                }
                s.add(Strings.autoFixed(powerPerBlock * 60f, 2));
                s.add(" " + Core.bundle.get("stat.pm-powersecondblock"));
            });
        }
    }
}