package progressed.world.blocks.defence.turret.testing;

import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import progressed.entities.bullet.pseudo3d.*;

public class ArcBulletScatterTestTurret extends ArcBulletTestTurret{
    public float bAccel = 0.3f;

    public ArcBulletScatterTestTurret(String name){
        super(name);


        shootType = new ArcBasicBulletType(15f, 30f){{
            isInheritive = true;
        }};
        reload = 1f;
        shotTilt = -90f;
        shoot = new ShootPattern(){{
            shots = 5;
        }};
    }

    public class ArcBulletTestTurretBuild extends FreeTurretBuild{


        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                shootAngle = Mathf.random(360),
                velScl = 1f + Mathf.range(velocityRnd / 2f);

            ArcBulletType aType = (ArcBulletType)type;
            handleBullet(aType.create3DStraight(this, team, x, y, shotZ, shootAngle, Mathf.random(-90f, -0.1f), aType.speed * velScl, bAccel), xOffset, yOffset, shootAngle - rotation);

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
    }
}
