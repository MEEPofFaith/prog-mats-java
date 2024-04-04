package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.util.*;
import progressed.world.meta.*;

public class BlackHoleTurret extends PowerTurret{
    public BlackHoleTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(OrderedMap.of(this, shootType)));
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("pm-reload", (BlackHoleTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reloadCounter / reload)
        ));

        addBar("pm-charge", (BlackHoleTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-charge", PMUtls.stringsFixed(Mathf.clamp(entity.charge) * 100f)),
            () -> Color.navy,
            () -> entity.charge
        ));
    }

    public class BlackHoleTurretBuild extends PowerTurretBuild{
        @Override
        protected void updateReload(){
            if(charging()) return;
            super.updateReload();
        }

        @Override
        protected void updateCooling(){
            if(charging()) return;
            super.updateCooling();
        }

        @Override
        protected void shoot(BulletType type){
            float
                bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
                bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            if(shoot.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation, team.color, self());
            }

            shoot.shoot(totalShots, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets ++;
                if(delay > 0f){
                    Time.run(delay, () -> bullet(type, xOffset, yOffset, angle, mover));
                }else{
                    bullet(type, xOffset, yOffset, angle, mover);
                }
                totalShots ++;
            }, () -> barrelCounter++);

            if(consumeAmmoOnce) useAmmo();
        }
    }
}
