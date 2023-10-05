package progressed.world.blocks.defence.turret.testing;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class ArcBulletScatterTestTurret extends ArcBulletTestTurret{
    public float bAccel = 0.3f;

    public ArcBulletScatterTestTurret(String name){
        super(name);


        shootType = new ArcBasicBulletType(15f, 30f);
        reload = 1f;
        shotTilt = -90f;
        inaccuracy = 45f;
        shoot = new ShootPattern(){{
            shots = 5;
        }};
    }

    public class ArcBulletScatterTestTurretBuild extends ArcBulletTestTurretBuild{
        @Override
        public void drawAimDebug(){
            if(!drawAimDebug) return;
            Draw.z(PMLayer.skyBloom + 2f);
            //Aiming Display
            Draw3D.drawAimDebug(x, y, shotZ, shootType.speed * tilesize, 0f, -90f, inaccuracy);
        }

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
            handleBullet(aType.create3DStraight(this, team, x, y, shotZ, shootAngle, Mathf.random(-90f, -90f + inaccuracy), aType.speed * velScl, bAccel), xOffset, yOffset, shootAngle - rotation);

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
