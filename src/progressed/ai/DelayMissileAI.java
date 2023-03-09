package progressed.ai;

import arc.math.*;
import mindustry.ai.types.*;
import mindustry.gen.*;
import progressed.type.unit.*;

public class DelayMissileAI extends MissileAI{
    @Override
    public void updateMovement(){
        unloadPayloads();

        float time = unit instanceof TimedKillc t ? t.time() : unit.type.homingDelay;

        if(time >= unit.type.homingDelay && shooter != null){
            unit.lookAt(shooter.aimX, shooter.aimY);
        }

        //move forward forever
        unit.moveAt(vec.trns(unit.rotation, unit.type.missileAccelTime <= 0f ? unit.speed() : Mathf.pow(Math.min(time / unit.type.missileAccelTime, 1f), 2f) * unit.speed()));

        //kill instantly on enemy building contact after delay
        if(time < unit.type.homingDelay) return;
        var build = unit.buildOn();
        if(build != null && build.team != unit.team && (build == target || !build.block.underBullets)){
            unit.kill();
        }
    }

    @Override
    public boolean retarget(){
        return (!(unit instanceof TimedKillc t) || !(t.time() < ((RocketUnitType)unit.type).targetDelay)) && super.retarget();
    }

    @Override
    public Teamc target(float x, float y, float range, boolean air, boolean ground){
        if(unit instanceof TimedKillc t && t.time() < ((RocketUnitType)unit.type).targetDelay) return null;

        return super.target(x, y, range, air, ground);
    }
}
