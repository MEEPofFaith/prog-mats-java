package progressed.type.weapons;

import arc.math.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

/** A weapon which sets the shot bullet's owner to the missile unit's shooter. */
public class MissileOwnerWeapon extends Weapon{
    @Override
    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover){
        if(!unit.isAdded()) return;

        mount.charging = false;
        float
            xSpread = Mathf.range(xRand),
            weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
            mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
            bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset),
            bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset),
            shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset,
            lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / bullet.range) : 1f,
            angle = angleOffset + shootAngle + Mathf.range(inaccuracy + bullet.inaccuracy);

        Teamc owner = unit.controller() instanceof MissileAI ai ? ai.shooter : unit; //Set the shooter to be the bullet's owner so that missile unit frags have the proper shooter
        mount.bullet = bullet.create(owner, unit.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, mount.aimX, mount.aimY);
        handleBullet(unit, mount, mount.bullet);
        if(owner != unit && bullet.killShooter && !unit.dead()) unit.kill(); //Since this unit technically isn't the owner of the bullet, handle killing the shooter here. Also bullets with a spawnUnit don't kill shooter anyways.

        if(!continuous){
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
        }

        ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
        bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
        bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, bullet.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        mount.heat = 1f;
    }
}
