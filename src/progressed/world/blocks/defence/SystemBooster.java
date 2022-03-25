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
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class SystemBooster extends Block{
    public float reload = 60f;
    public float speedBoost = 1.1f;
    public float basePowerUse, powerPerBlock = 0.01f;
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
        consumes.add(new DynamicConsumePower(b -> ((SystemBoosterBuild)b).totalPowerUse));

        super.init();

        clipSize = Math.max(clipSize, size * tilesize * 2f + 8f);
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
        stats.remove(Stat.powerUse);
        stats.add(Stat.powerUse, s -> {
            if(basePowerUse != 0){
                StatValues.number(basePowerUse * 60f, StatUnit.powerSecond).display(s);
                s.add(" + ");
            }
            s.add(Core.bundle.format("stat.pm-powersecondblock", powerPerBlock * 100f));
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("boost", (SystemBoosterBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-totalboost", Mathf.round(Math.max((entity.realBoost() * 100 - 100), 0))),
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
            totalPowerUse = 0f;
            boosted = 0;
            power.graph.consumers.each(b -> b.block.canOverdrive, b -> {
                ConsumePower consumePower = b.block.consumes.getPower();
                totalPowerUse += consumePower.requestedPower(b) * b.delta();
                boosted++;
            });
            totalPowerUse *= powerPerBlock;
            totalPowerUse += basePowerUse;
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
            Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
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

            Draw.z(Layer.shields);
            Lines.stroke(f * 4f * heat);
            Draw.alpha(0.9f);
            Lines.circle(x, y, (1f - f) * size * tilesize * 2f);
        }

        @Override
        public void display(Table table){
            super.display(table);

            table.row();
            table.label(() -> Core.bundle.format("pm-boostercount", boosters)).left().padLeft(18).fillX();
            table.row();
            table.label(() -> Core.bundle.format("pm-poweruse", Mathf.round(totalPowerUse * 60f))).left().fillX();
            table.row();
            table.label(() -> Core.bundle.format("pm-overdrivedcount", boosted)).left().padLeft(18).fillX();
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
}
