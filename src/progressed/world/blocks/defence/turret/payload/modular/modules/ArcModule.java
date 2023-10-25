package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.math.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.util.*;
import progressed.world.draw.*;

public class ArcModule extends ItemTurretModule{
    public ArcModule(String name){
        super(name);
        consumeAmmoOnce = false;
        recoil = 0f;
        shootY = 0f;
        shootEffect = smokeEffect = Fx.none;
        connectedPower = false;

        drawer = new DrawTurretModule();
    }

    @Override
    public void init(){
        for(Item c : ammoTypes.keys()){ //Check for invalid ammo
            if(!(ammoTypes.get(c) instanceof ArcMissileBulletType aType)){
                PMUtls.uhOhSpeghettiOh("Arc missile turret " + name + " has a non-arc missle bullet!");
            }else{
                aType.initDrawSize(range);
            }
        }

        super.init();
    }

    public class ArcModuleBuild extends ItemTurretModuleBuild{
        protected Seq<Posc> targets = new Seq<>();

        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            ArcMissileBulletType bullet = (ArcMissileBulletType)peekAmmo();

            if(predictTarget && pos instanceof Hitboxc h){
                targetPos.set(Math3D.intercept(this, h, bullet.accel, bullet.speed));
            }else{
                targetPos.set(pos);
            }

            if(!bullet.scaleLife) targetPos.sub(this).setLength(range).add(this);

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        protected void updateReload(){
            if(queuedBullets > 0) return;
            super.updateReload();
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;
            super.drawSelect();
            Drawf.dashCircle(x, y, minRange, Pal.accentBack);
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            //Select target
            if(!isControlled() && !logicControlled()){
                targets.clear();
                PMDamage.allNearbyEnemies(team, x, y, range, h -> {
                    if(h instanceof Unit u){
                        if(!u.dead() && unitFilter.get(u) && (u.isGrounded() || targetAir) && (!u.isGrounded() || targetGround) && dst(u) >= minRange) targets.add(u);
                    }else if(h instanceof Building b){
                        if(targetGround && buildingFilter.get(b) && dst(b) >= minRange) targets.add(b);
                    }
                });

                if(!targets.isEmpty()){
                    target = targets.random();
                    targetPosition(target);
                }
            }
            turnToTarget(angleTo(targetPos));

            //Shoot bullet - See ArcMissileTurret#bullet
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                xSpread = Mathf.range(xRand),
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                shootAngle = rotation + angleOffset + Mathf.range(inaccuracy + type.inaccuracy),
                velScl = 1f + Mathf.range(velocityRnd / 2f);

            float dst = Math.max(Math.min(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y), range()), minRange);
            ArcMissileBulletType m = (ArcMissileBulletType)type;
            float time = Mathf.sqrt((2 * dst) / m.accel); //TODO consider initial velocity
            float zVel = -0.5f * -m.gravity * time;
            handleBullet(m.create3DVel(this, team, bulletX, bulletY, 0f, shootAngle, zVel, m.accel * velScl, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

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

        @Override
        public float drawrot(){
            return 0;
        }
    }
}
