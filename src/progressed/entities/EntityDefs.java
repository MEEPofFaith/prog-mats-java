package progressed.entities;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import progressed.gen.entities.*;

class EntityDefs{
    @EntityDef({Unitc.class, TargetDummyc.class}) Object targetDummyUnit;
    @EntityDef({Unitc.class, Legsc.class, BuildingTetherc.class, NoCoreDepositc.class}) Object noCoreDepositBuildingTetherLegsUnit;
}
