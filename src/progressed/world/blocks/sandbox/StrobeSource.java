package progressed.world.blocks.sandbox;

import arc.*;
import arc.flabel.*;
import arc.math.*;
import arc.util.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import progressed.util.*;

public class StrobeSource extends StrobeNode{
    public float powerProduction = 2000000000f / 60f;
    public boolean boost;
    public float speedBoost;

    public StrobeSource(String name){
        super(name);
        outputsPower = true;
        consumesPower = false;
    }

    @Override
    public void setStats(){
        super.setStats();
        if(boost){
            stats.add(Stat.speedIncrease, t -> t.add(new FLabel("{wave}{rainbow}" + PMUtls.stringsFixed(100 * speedBoost) + StatUnit.percent.localized())));
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        if(boost){
            bars.add("pm-gay", (StrobeSourceBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.pm-gay", PMUtls.stringsFixed(speedBoost * 100f)),
                () -> Tmp.c1.set(laserColor1).lerp(laserColor3, Mathf.absin(Time.time * lerpSpeed, 1f, 1f)).shiftHue(Time.time * Core.settings.getInt("pm-strobespeed") / 2f),
                () -> speedBoost * 100f
            ));
        }
    }

    public class StrobeSourceBuild extends StrobeNodeBuild{
        @Override
        public float getPowerProduction(){
            return enabled ? powerProduction : 0f;
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(boost && timer(0, 60f)){
                power.graph.all.each(b -> b.applyBoost(speedBoost, 65f));
            }
        }
    }
}