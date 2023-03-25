package progressed.entities.units;

import arc.util.io.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import progressed.ai.*;
import progressed.content.*;

public class BuildingTetherLegsUnit extends LegsUnit implements BuildingTetherc{
    public Building building;

    @Override
    public void update(){
        super.update();
        if(building == null || !building.isValid() || building.team != team){
            Call.unitDespawn(self());
        }
    }

    @Override
    public CoreBuild closestCore(){
        return controller instanceof DepotMinerAI ? null : super.closestCore();
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
        return PMUnitTypes.classID(BuildingTetherLegsUnit.class);
    }
}
