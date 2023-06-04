package progressed.entities.comp;

import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;
import progressed.world.blocks.sandbox.units.TargetDummyBase.*;

@EntityComponent
@EntityDef({Unitc.class, Healthc.class})
abstract class TargetDummyComp implements Unitc, Healthc{
    public @Nullable Building building;

    @Override
    public void update(){
        if(building == null || (!building.isPayload() && !building.isValid())){ //Don't despawn if the building is on another team
            Call.unitDespawn(self());
        }
    }

    @Override
    public void rawDamage(float damage){
        ((TargetDummyBaseBuild)building).dummyHit(damage);
    }
}
