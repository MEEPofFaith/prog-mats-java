package progressed.type;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class ParalyzeStatusEffect extends StatusEffect{
    public float cooldown, rotationRand;
    public boolean hidden = true;
    public Color effectColor = Pal.lancerLaser;

    public ParalyzeStatusEffect(String name){
        super(name);
        color = Pal.lancerLaser;
    }

    @Override
    public void update(Unit unit, float time){
        float strength = Mathf.clamp(time / cooldown);
        if(strength > 0f && Mathf.chanceDelta(1f)){
            for(WeaponMount mount : unit.mounts){
                Weapon weapon = mount.weapon;
                if(weapon.rotate){
                    mount.rotation += Mathf.range(weapon.rotateSpeed * rotationRand * strength);
                }
            }
        }
        
        if(damage > 0f){
            unit.damageContinuousPierce(damage);
        }else if(damage < 0f){ //heal unit
            unit.heal(-1f * damage * Time.delta);
        }

        if(effect != Fx.none && Mathf.chanceDelta(effectChance)){
            Tmp.v1.rnd(unit.type.hitSize /2f);
            effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, effectColor);
        }
    }
}
