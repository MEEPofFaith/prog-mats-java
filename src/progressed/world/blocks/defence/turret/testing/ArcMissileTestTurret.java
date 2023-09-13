package progressed.world.blocks.defence.turret.testing;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.bullet.pseudo3d.ArcBulletType.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.*;
import progressed.util.*;

public class ArcMissileTestTurret extends ArcBulletTestTurret{
    public ArcMissileTestTurret(String name){
        super(name);

        range = 80 * 8;
        shootType = new ArcMissileBulletType(){{
            //homingPower = 1f;
            accel = 0.1f;
        }};
        shotZ = 0f;
        shotTilt = 90f;
        shoot = new ShootPattern();
        inaccuracy = 0f;
        velocityRnd = 0f;
    }

    public class ArcMissileTestTurretBuild extends ArcBulletTestTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            ArcMissileBulletType bullet = (ArcMissileBulletType)peekAmmo();

            if(predictTarget && pos instanceof Hitboxc h){
                targetPos.set(Math3D.intercept(this, h, bullet.accel));
            }else{
                targetPos.set(pos);
            }

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        public void drawAimDebug(){
            if(!drawAimDebug) return;
            Draw.z(Layer.bullet);
            //Aiming Display
            float dst = Math.min(Mathf.dst(x, y, targetPos.x, targetPos.y), range);
            ArcMissileBulletType m = (ArcMissileBulletType)shootType;
            float time = Mathf.sqrt((2 * dst) / m.accel);
            float zVel = -0.5f * -m.gravity * time;
            Draw3D.drawAimDebug(x, y, shotZ, zVel, rotation, realTilt, inaccuracy);
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                shootAngle = rotation + angleOffset + Mathf.range(inaccuracy + type.inaccuracy);

            float dst = Math.min(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y), range);
            ArcMissileBulletType m = (ArcMissileBulletType)type;
            float time = Mathf.sqrt((2 * dst) / m.accel);
            float zVel = -0.5f * -m.gravity * time;
            handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, 1f, 1f, new ArcBulletData(shotZ, zVel, m.gravity).setAccel(shootAngle, m.accel), null, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

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
