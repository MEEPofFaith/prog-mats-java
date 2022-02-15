package progressed.entities.bullet.physical;

import arc.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.Wall.*;

import static mindustry.Vars.*;

public class TargetBulletType extends BasicBulletType{
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
        boolean tHit = false;

        if(entity instanceof Healthc h){
            if(b.data == entity){
                h.damage(tDamage * b.damageMultiplier());
                tHit = true;
            }else{
                h.damage(b.damage);
            }
        }

        if(entity instanceof Unit unit){
            Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
            if(impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
            unit.impulse(Tmp.v3);
            if(b.data == unit){
                unit.apply(tStatus, tStatusDuration);
                tHit = true;
            }else{
                unit.apply(status, statusDuration);
            }
        }

        //for achievements
        if(b.owner instanceof WallBuild && player != null && b.team == player.team() && entity instanceof Unit unit && unit.dead){
            Events.fire(Trigger.phaseDeflectHit);
        }

        if(tHit){
            tHitEffect.at(b.x, b.y);
            b.hit = true;
            b.remove();
        }
    }
}
