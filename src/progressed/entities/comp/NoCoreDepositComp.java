package progressed.entities.comp;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import mindustry.world.blocks.storage.CoreBlock.*;

@EntityComponent
abstract class NoCoreDepositComp implements Unitc, Teamc{
    @Replace
    public CoreBuild closestCore(){
        return null; //If closestCore is null, it cannot deposit items into it. Kinda hacky but there's no other option.
    }
}
