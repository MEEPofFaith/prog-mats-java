package progressed.entities.bullet;

import arc.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;

public class TargetBulletType extends BasicBulletType{
    static final UnitDamageEvent bulletDamageEvent = new UnitDamageEvent();

    public float tDamage;
    public StatusEffect tStatus = StatusEffects.none;
    public Effect tHitEffect = Fx.none;
    public float tStatusDuration = 10f * 60f;

    public TargetBulletType(float speed, float damage, float tDamage, String bulletSprite){
        super(speed, damage, bulletSprite);
        this.tDamage = tDamage;

        pierce = true;
    }

    public TargetBulletType(float speed, float damage, float tDamage){
        this(speed, damage, tDamage, "bullet");
    }

    public TargetBulletType(float speed, float tDamage){
        this(speed, 0f, tDamage);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float initialHealth){
        boolean wasDead = entity instanceof Unit u && u.dead;
        boolean tHit = b.data.equals(entity);

        if(entity instanceof Healthc h){
            float damage = tHit ? tDamage * b.damageMultiplier() : b.damage;

            if(pierceArmor){
                h.damagePierce(damage);
            }else{
                h.damage(damage);
            }
        }

        if(entity instanceof Unit unit){
            Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
            if(impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
            unit.impulse(Tmp.v3);
            unit.apply(tHit ? tStatus : status, tHit ? tStatusDuration : statusDuration);

            Events.fire(bulletDamageEvent.set(unit, b));
        }

        if(!wasDead && entity instanceof Unit unit && unit.dead){
            Events.fire(new UnitBulletDestroyEvent(unit, b));
        }

        if(tHit){
            tHitEffect.at(b.x, b.y);
            b.hit = true;
            b.remove();
        }
    }
}
