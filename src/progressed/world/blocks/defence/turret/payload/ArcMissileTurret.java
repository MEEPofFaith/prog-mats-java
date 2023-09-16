package progressed.world.blocks.defence.turret.payload;

import arc.math.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.util.*;
import progressed.world.draw.*;

import static mindustry.Vars.*;

public class ArcMissileTurret extends SinglePayloadAmmoTurret{
    public ArcMissileTurret(String name){
        super(name);

        outlineIcon = false;
        outlinedIcon = 1;
        rotateSpeed = 3600f;

        drawer = new DrawPayloadTurret(false);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.shootRange);
        stats.add(Stat.shootRange, "@-@ @", StatValues.fixValue(minRange / tilesize), StatValues.fixValue(range / tilesize), StatUnit.blocks.localized());
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Pal.accentBack);
    }

    @Override
    public void init(){
        for(UnlockableContent c : ammoTypes.keys()){ //Check for invalid ammo
            if(!(ammoTypes.get(c) instanceof ArcMissileBulletType aType)){
                PMUtls.uhOhSpeghettiOh("Arc missile turret " + name + " has a non-arc missle bullet!");
            }else{
                aType.initDrawSize(range);
            }
        }

        super.init();
    }

    public class ArcMissileTurretBuild extends SinglePayloadAmmoTurretBuild{
        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            ArcMissileBulletType bullet = (ArcMissileBulletType)peekAmmo();

            if(predictTarget && pos instanceof Hitboxc h){
                targetPos.set(Math3D.intercept(this, h, bullet.accel));
            }else{
                targetPos.set(pos);
            }

            if(!bullet.scaleLife) targetPos.sub(this).setLength(range).add(this);

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        protected void findTarget(){
            float range = range();

            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded() && unitFilter.get(e) && dst(e) >= minRange, unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && unitFilter.get(e) && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround) && dst(e) >= minRange, b -> targetGround && buildingFilter.get(b) && dst(b) >= minRange, unitSort);

                if(target == null && canHeal()){
                    target = Units.findAllyTile(team, x, y, range, b -> b.damaged() && b != this);
                }
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            Drawf.dashCircle(x, y, minRange, Pal.accentBack);
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
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
            float time = Mathf.sqrt((2 * dst) / m.accel);
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
    }
}
