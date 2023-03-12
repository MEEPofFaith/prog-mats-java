package progressed.entities.units;

import arc.util.io.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.io.*;
import progressed.content.*;
import progressed.world.blocks.sandbox.units.TargetDummyBase.*;

public class TargetDummyUnit extends UnitEntity implements BuildingTetherc{
    public Building building;

    @Override
    public void update(){
        super.update();
        if(building == null || !building.isValid()){ //Don't despawn if the building is on another team
            Call.unitDespawn(self());
        }
    }

    @Override
    public Building building(){
        return building;
    }

    @Override
    public void building(Building building){
        this.building = building;
    }

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
    public void write(Writes write){
        super.write(write);
        TypeIO.writeBuilding(write, building);
    }

    @Override
    public void writeSync(Writes write){
        super.writeSync(write);
        TypeIO.writeBuilding(write, building);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        building = TypeIO.readBuilding(read);
    }

    @Override
    public void readSync(Reads read){
        super.readSync(read);
        building = TypeIO.readBuilding(read);
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(TargetDummyUnit.class);
    }
}
