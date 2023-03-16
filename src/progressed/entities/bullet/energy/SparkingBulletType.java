package progressed.entities.bullet.energy;

import arc.audio.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;

public class SparkingBulletType extends BasicBulletType{
    protected static boolean empHit;

    public float empRadius = 3f * 8f, empInterval = 20f;
    public float empDamage = -1f;
    public float slowDown = 0.5f, slowDownDuration =  10f * 60f;
    public boolean hitUnits = true;
    public float unitDamageScl = 0.5f;
    public Effect hitPowerEffect = MissileFx.hitEmpSpark, chainEffect = Fx.chainEmp;
    public Sound empSound = Sounds.spark;

    public SparkingBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
    }

    public SparkingBulletType(float speed, float damage){
        super(speed, damage);
    }

    public SparkingBulletType(){
        super();
    }

    @Override
    public void load(){
        super.load();

        if(empDamage < 0) empDamage = damage * 2;
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(b.timer.get(3, empInterval)){
            empHit = false;
            Vars.indexer.allBuildings(b.x, b.y, empRadius, other -> {
                if(other.team != b.team && other.power != null){
                    var absorber = Damage.findAbsorber(b.team, b.x, b.y, other.x, other.y);
                    if(absorber != null){
                        other = absorber;
                    }

                    if(other.power != null && other.power.graph.getLastPowerProduced() > 0f){
                        other.applySlowdown(slowDown, slowDownDuration);
                        other.damage(empDamage);
                        hitPowerEffect.at(other.x, other.y, b.angleTo(other), hitColor);
                        chainEffect.at(b.x, b.y, 0, hitColor, other);
                        empHit = true;
                    }
                }
            });

            if(hitUnits){
                Units.nearbyEnemies(b.team, b.x, b.y, empRadius, other -> {
                    if(other.team != b.team && other.hittable()){
                        var absorber = Damage.findAbsorber(b.team, b.x, b.y, other.x, other.y);
                        if(absorber != null){
                            return;
                        }

                        hitPowerEffect.at(other.x, other.y, b.angleTo(other), hitColor);
                        chainEffect.at(b.x, b.y, 0, hitColor, other);
                        other.damage(empDamage * unitDamageScl);
                        other.apply(status, statusDuration);
                        empHit = true;
                    }
                });
            }
            if(empHit) empSound.at(b);
        }
    }
}
