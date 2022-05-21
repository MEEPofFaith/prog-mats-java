package progressed.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import progressed.entities.units.*;
import progressed.type.unit.*;
import progressed.world.blocks.defence.turret.energy.SwordTurret.*;

public class SwordAI extends AIController{
    Rand curveRand = new Rand();
    float curveScl;
    float attackTimer;
    boolean curving, rotating, inRange;

    @Override
    public void init(){
        curveRand.setSeed(unit.id);
    }

    @Override
    public void updateMovement(){
        SwordTurretBuild swordTurret = sturret();
        if(swordTurret == null) return;

        if(swordTurret.isActive()){
            attackTimer += Time.delta;
            if(attackTimer >= sunit().orbitPos * stype().attackTimeOffset){
                attackTimer = sunit().orbitPos * stype().attackTimeOffset + 60f;
            }
        }else{
            attackTimer -= Time.delta;
            if(attackTimer <= 0){
                attackTimer = 0;
                curving = rotating = inRange = false;
            }
        }

        if(shouldAttack()){
            attack(swordTurret.targetPos, stype().attackRadius);
        }else{
            float count = Math.max(swordTurret.swordCount(), 1); //Should never be 0, just in case this is somehow run while it's 0.
            float pos = 360f / count * sunit().orbitPos - Time.time * stype().orbitSpeed + swordTurret.rotation + Mathf.randomSeed(swordTurret.id, 360f);
            Tmp.v1.trns(Mathf.mod(pos, 360f), stype().orbitRadius).add(swordTurret);
            moveTo(Tmp.v1, 1f, 20);
            unit.lookAt(swordTurret.rotation);
        }
    }

    @Override
    public void updateVisuals(){
        if(!shouldAttack()){
            unit.wobble();
        }
    }

    public void attack(Position target, float circleLength){
        vec.set(target).sub(unit);

        float ang = unit.angleTo(target);
        float diff = Angles.angleDist(ang, unit.rotation());

        if(vec.len() < circleLength) inRange = true;
        if(diff > 70f && vec.len() < circleLength) curving = true;

        if(curving){
            if(vec.len() < circleLength){
                unit.rotation += curveScl;
            }else{
                rotating = true;
                unit.lookAt(target);
                if(Angles.within(unit.rotation, unit.angleTo(target), 15f)){
                    curving = rotating = false;
                    curveScl = curveRand.range(stype().curveRnd);
                    sunit().clearCollided();
                }
            }
        }else{
            unit.lookAt(target);
        }

        vec.trns(unit.rotation, unit.speed());
        unit.moveAt(vec);
    }

    public float speed(){
        return rotating ? stype().neutralSpeed : inRange ? stype().speed * sturret().efficiency : shouldAttack() ? stype().travelSpeed * sturret().efficiency : stype().neutralSpeed;
    }
    public boolean shouldAttack(){
        return attackTimer > sunit().orbitPos * stype().attackTimeOffset;
    }

    public boolean shouldDamage(){
        return shouldAttack() && !rotating;
    }

    public SwordUnitType stype(){
        return sunit().stype();
    }

    public SwordUnit sunit(){
        return (SwordUnit)unit;
    }

    public SwordTurretBuild sturret(){
        return (SwordTurretBuild)sunit().building();
    }
}
