package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.util.*;

public class MortarLiquidTurretModule extends LiquidTurretModule{
    public MortarLiquidTurretModule(String name){
        super(name);
        predictTarget = false; //I don't know how to do this
    }

    public class MortarLiquidTurretModuleBuild extends LiquidTurretModuleBuild{
        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets--;
            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            ArcMissileBulletType m = (ArcMissileBulletType)type;
            Vec2 inacc = Math3D.inaccuracy(inaccuracy);

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                shootAngle = rotation + angleOffset + inacc.x,
                shootVAngle = shootAngle(m) + inacc.y,
                velScl = 1f + Mathf.range(velocityRnd / 2f);

            handleBullet(m.create3D(this, team, bulletX, bulletY, 0, shootAngle, shootVAngle, m.gravity, velScl, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

            (shootEffect == null ? type.shootEffect : shootEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            (smokeEffect == null ? type.smokeEffect : smokeEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));

            ammoUseEffect.at(
                x - Angles.trnsx(rotation, ammoEjectBack),
                y - Angles.trnsy(rotation, ammoEjectBack),
                rotation * Mathf.sign(xOffset)
            );

            if(shake > 0){
                Effect.shake(shake, shake, this);
            }

            curRecoil = 1f;
            if(recoils > 0){
                curRecoils[barrelCounter % recoils] = 1f;
            }
            heat = 1f;
            totalShots++;

            if(!consumeAmmoOnce){
                useAmmo();
            }
        }

        protected float shootAngle(ArcBulletType b){
            float[] angs = Math3D.shootAngle(Math.min(dst(targetPos), range()), b.gravity, b.speed);
            if(angs.length == 2){
                return angs[1];
            }else{
                return angs[0];
            }
        }
    }
}
