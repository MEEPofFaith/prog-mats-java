package progressed.entities.units;

import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

public class PMUnitType extends UnitType{
    public Seq<Weapon> bottomWeapons = new Seq<>(); //Literally stole this from Project Unity lmao

    public PMUnitType(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        Seq<Weapon> addBottoms = new Seq<>();
        for(Weapon w : weapons){
            if(bottomWeapons.contains(w) && w.otherSide != -1){
                addBottoms.add(weapons.get(w.otherSide));
            }
        }

        bottomWeapons.addAll(addBottoms.distinct());
    }

    @Override
    public void drawWeapons(Unit unit){
        float z = Draw.z();

        applyColor(unit);
        for(WeaponMount mount : unit.mounts){
            Weapon weapon = mount.weapon;
            if(bottomWeapons.contains(weapon)) Draw.z(z - 0.0001f);

            weapon.draw(unit, mount);
            Draw.z(z);
        }

        Draw.reset();
    }
}