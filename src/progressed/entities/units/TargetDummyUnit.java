package progressed.entities.units;

import mindustry.entities.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.world.blocks.sandbox.units.TargetDummyBase.*;

public class TargetDummyUnit extends BuildingTetherPayloadUnit{
    @Override
    public void damage(float amount){
        //apply armor and scaling effects
        rawDamage(Damage.applyArmor(amount, armor) / healthMultiplier);
    }

    @Override
    public void damagePierce(float amount, boolean withEffect){
        float pre = hitTime;
        rawDamage(amount / healthMultiplier);
        if(!withEffect){
            hitTime = pre;
        }
    }

    protected void rawDamage(float damage){
        ((TargetDummyBaseBuild)building).dummyHit(damage);
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(TargetDummyUnit.class);
    }
}
