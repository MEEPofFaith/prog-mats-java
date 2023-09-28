package progressed.world.blocks.defence.turret.testing;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.graphics.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ArcBulletTestTurret extends FreeTurret{
    public float shotZ = 180f * tilesize;
    public float shotTilt = 45;
    public boolean drawAimDebug = false;
    public boolean swing = false;
    public float swingScl = 2f;

    public ArcBulletTestTurret(String name){
        super(name);

        shootType = new ArcBasicBulletType(25f, 400f){{
            homingPower = 3f;
        }};
        reload = 10f;
        range = 45f * tilesize;
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
            Draw3D.drawAimDebug(x, y, shotZ, shootType.speed * tilesize, rotation, realTilt, inaccuracy);
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                shootAngle = rotation + angleOffset,
                velScl = 1f + Mathf.range(velocityRnd / 2f);

            ArcBulletType aType = (ArcBulletType)type;
            Vec2 inacc = Math3D.inaccuracy(inaccuracy);
            handleBullet(aType.create3D(this, team, x, y, shotZ, shootAngle + inacc.x, shotTilt + inacc.y, aType.gravity, velScl, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

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
