package progressed.entities.bullet.physical;

import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;

public class TargetBulletType extends BulletType{
    public float tDamage;
    public StatusEffect tStatus = StatusEffects.none;
    public float tStatusDuration = 6f * 10f;

    public TargetBulletType(float speed, float damage, float tDamage){
        super(speed, damage);
        this.tDamage = tDamage;

        pierce = true;
    }

    public TargetBulletType(float speed, float tDamage){
        this(speed, 0f, tDamage);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float initialHealth){
        super.hitEntity(b, entity, initialHealth);

        if(b.data == entity && entity instanceof Unitc u){
            u.damage(tDamage);
            if(tStatus != StatusEffects.none){
                u.apply(tStatus, tStatusDuration);
            }
        }
    }
}