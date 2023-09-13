package progressed.world.blocks.defence.turret.testing;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.graphics.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.entities.bullet.pseudo3d.ArcBulletType.*;
import progressed.graphics.*;

public class ArcBulletTestTurret extends FreeTurret{
    public float shotZ = 180f * Vars.tilesize;
    public float shotTilt = 45;
    public boolean drawAimDebug = false;
    public boolean swing = false;
    public float swingScl = 2f;

    public ArcBulletTestTurret(String name){
        super(name);

        shootType = new ArcBasicBulletType(25f);
        reload = 10f;
        shootY = 0f;
        shootCone = 360f;
        inaccuracy = 30f;
        velocityRnd = 0.3f;
        //rotateSpeed = 1f;
        shoot = new ShootSpread(5, 0f);
    }

    public class ArcBulletTestTurretBuild extends FreeTurretBuild{
        public float realTilt = shotTilt;

        @Override
        public void updateTile(){
            if(swing){
                realTilt = Time.time * swingScl;
            }else{
                realTilt = shotTilt;
            }

            super.updateTile();
        }

        @Override
        public void draw(){
            super.draw();
            drawAimDebug();
        }

        public void drawAimDebug(){
            if(!drawAimDebug) return;
            Draw.z(Layer.effect + 0.01f);
            //Aiming Display
            Draw3D.drawAimDebug(x, y, shotZ, shootType.speed * Vars.tilesize, rotation, realTilt, inaccuracy);
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                //shootAngle = rotation + angleOffset + Mathf.range(inaccuracy + type.inaccuracy);
                shootAngle = rotation + angleOffset;

            //float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y) / type.range, minRange / type.range, range() / type.range) : 1f;

            //handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);
            float velRnd = (1f - velocityRnd) + Mathf.random(velocityRnd);
            float zVel = ArcBulletData.calcVel(Tmp.v1, type.speed * velRnd, shootAngle, realTilt, Mathf.range(inaccuracy + type.inaccuracy));
            handleBullet(type.create(this, team, bulletX, bulletY, Tmp.v1.angle(), -1f, Tmp.v1.len() / type.speed, 1f, new ArcBulletData(shotZ, zVel), null, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

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
