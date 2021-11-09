package progressed.world.blocks.defence;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

public class SystemBooster extends Block{
    public float reload = 60f;
    public float speedBoost = 1.1f;
    public float powerPerBlock = 1f;
    public Color boostColor = Color.valueOf("feb380");

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
        int boosters;
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
            totalPowerUse = 0;
            power.graph.all.each(b -> b.block.canOverdrive, b -> {
                totalPowerUse += powerPerBlock;
            });
        }

        @Override
        public void drawSelect(){
            power.graph.all.each(b -> b.block.canOverdrive, b -> {
                Drawf.selected(b, Tmp.c1.set(boostColor).a(Mathf.absin(4f, 1f)));
            });
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
                s.add(Strings.autoFixed(powerPerBlock, 2));
                s.add(" " + Core.bundle.get("stat.pm-powersecondblock"));
            });
        }
    }
}