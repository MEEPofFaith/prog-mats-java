package progressed.world.blocks.crafting;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;

public class AccelerationCrafter extends GenericCrafter{
    public float accelerationSpeed = 0.03f, decelerationSpeed = 0.05f;
    public Interp interp = Interp.smoother;

    public Cons<AcceleratingCrafterBuild> onCraft = tile -> {};

    public AccelerationCrafter(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("speed", (AcceleratingCrafterBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-craft-speed", Mathf.round(entity.getSpeed() * 100f)),
            () -> Pal.surge,
            entity::getSpeed
        ));
    }

    public class AcceleratingCrafterBuild extends GenericCrafterBuild{
        public float speed, totalActivity;

        @Override
        public void updateTile(){
            float s = getSpeed();
            if(consumes.has(ConsumeType.item) && consumes.getItem().valid(this) || !consumes.has(ConsumeType.item)){
                progress += getProgressIncrease(craftTime) * s;
                totalProgress += delta() * s;
            }
            totalActivity += delta() * s;

            if(Mathf.chanceDelta(updateEffectChance * s)){
                updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            }

            if(canConsume()){
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                float e = efficiency;
                if(speed <= e){
                    speed = Mathf.approachDelta(speed, e, accelerationSpeed * e);
                }else{
                    speed = Mathf.approachDelta(speed, e, decelerationSpeed);
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                speed = Mathf.approachDelta(speed, 0f, decelerationSpeed);
            }

            if(progress >= 1f){
                craft();
                onCraft.get(this);
            }

            dumpOutputs();
        }

        public float getSpeed(){
            return interp.apply(speed);
        }

        @Override
        public float getProgressIncrease(float baseTime){
            return 1f / baseTime * delta();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(speed);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            speed = read.f();
        }
    }
}
