package progressed.type.weapons;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.audio.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.content.effects.*;
import progressed.content.effects.UtilFx.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class BeamWeapon extends Weapon{
    public Color beamColor = PMPal.cyanLaser;
    public float beamLayer = Layer.bullet, beamScl = 0.25f, maxBeamDst = 30f;
    public Effect beamEffect = EnergyFx.cyanBeamSpark;
    public TextureRegion beamStart, beamEnd, beam;

    public BeamWeapon(String name){
        super(name);
        mountType = BeamMount::new;

        continuous = true;
        rotate = true;
        inaccuracy = velocityRnd = 0;
    }

    public BeamWeapon(){
        this("");
    }

    @Override
    public void load(){
        super.load();
        beamStart = Core.atlas.find(name + "-beam-start", "prog-mats-rainbow-laser-end");
        beamEnd = Core.atlas.find(name + "-beam-end", "prog-mats-rainbow-laser-end");
        beam = Core.atlas.find(name + "-beam", "prog-mats-rainbow-laser");
    }

    @Override
    public float range(){
        return maxBeamDst * 2f;
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        boolean can = unit.canShoot();
        float lastReload = mount.reload;
        mount.reload = Math.max(mount.reload - Time.delta * unit.reloadMultiplier, 0);
        mount.recoil = Mathf.approachDelta(mount.recoil, 0, unit.reloadMultiplier / recoilTime);
        mount.warmup = Mathf.lerpDelta(mount.warmup, can && mount.shoot ? 1f : 0f, shootWarmupSpeed);
        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);

        //rotate if applicable
        if(rotate && (mount.rotate || mount.shoot) && can){
            float axisX = unit.x + Angles.trnsx(unit.rotation - 90,  x, y),
                axisY = unit.y + Angles.trnsy(unit.rotation - 90,  x, y);

            mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - unit.rotation;
            mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, rotateSpeed * Time.delta);
            if(rotationLimit < 360){
                float dst = Angles.angleDist(mount.rotation, 0f);
                if(dst > rotationLimit/2f){
                    mount.rotation = Angles.moveToward(mount.rotation, 0, dst - rotationLimit/2f);
                }
            }
        }else if(!rotate){
            mount.rotation = baseRotation;
            mount.targetRotation = unit.angleTo(mount.aimX, mount.aimY);
        }

        float
            weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
            mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
            bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
            bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY),
            shootAngle = bulletRotation(unit, mount, bulletX, bulletY);

        //find a new target
        if(!controllable && autoTarget){
            if((mount.retarget -= Time.delta) <= 0f){
                mount.target = findTarget(unit, mountX, mountY, bullet.range, bullet.collidesAir, bullet.collidesGround);
                mount.retarget = mount.target == null ? targetInterval : targetSwitchInterval;
            }

            if(mount.target != null && checkTarget(unit, mount.target, mountX, mountY, bullet.range)){
                mount.target = null;
            }

            boolean shoot = false;

            if(mount.target != null){
                shoot = mount.target.within(mountX, mountY, bullet.range + Math.abs(shootY) + (mount.target instanceof Sized s ? s.hitSize()/2f : 0f)) && can;

                if(predictTarget){
                    Vec2 to = Predict.intercept(unit, mount.target, bullet.speed);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }else{
                    mount.aimX = mount.target.x();
                    mount.aimY = mount.target.y();
                }
            }

            mount.shoot = mount.rotate = shoot;

            //note that shooting state is not affected, as these cannot be controlled
            //logic will return shooting as false even if these return true, which is fine
        }

        //update continuous state (this is the only part throughout the entirety of this method that is different from original code)
        BeamMount bMount = (BeamMount)mount;
        if(continuous && mount.bullet != null){
            if(!mount.bullet.isAdded() || mount.bullet.time >= mount.bullet.lifetime || mount.bullet.type != bullet){
                mount.bullet = null;
                bMount.stroke = Mathf.approachDelta(bMount.stroke, 0f, 0.15f);
            }else{
                mount.bullet.rotation(weaponRotation + 90);
                float dst = mount.bullet.fin() * maxBeamDst,
                    bx = bulletX + Angles.trnsx(weaponRotation + 90f, dst),
                    by = bulletY + Angles.trnsy(weaponRotation + 90f, dst);
                mount.bullet.set(bx, by);
                mount.reload = reload;
                mount.recoil = 1f;
                bMount.dst = dst;
                unit.vel.add(Tmp.v1.trns(unit.rotation + 180f, mount.bullet.type.recoil));
                if(!headless){
                    if(shootSound != Sounds.none){
                        if(mount.sound == null) mount.sound = new SoundLoop(shootSound, 1f);
                        mount.sound.update(bulletX, bulletY, true);
                    }

                    if((bMount.beam -= Time.delta) < 0){
                        bMount.beam = 2;
                        beamEffect.at(mount.bullet, weaponRotation + 90f);
                    }
                    bMount.stroke = Mathf.approachDelta(bMount.stroke, 1f, 0.15f);
                }
            }
        }else{
            //heat decreases when not firing
            mount.heat = Math.max(mount.heat - Time.delta * unit.reloadMultiplier / cooldownTime, 0);
            bMount.stroke = Mathf.approachDelta(bMount.stroke, 0f, 0.15f);

            if(mount.sound != null){
                mount.sound.update(bulletX, bulletY, false);
            }
        }

        //flip weapon shoot side for alternating weapons
        boolean wasFlipped = mount.side;
        if(otherSide != -1 && alternate && mount.side == flipSprite && mount.reload <= reload / 2f && lastReload > reload / 2f){
            unit.mounts[otherSide].side = !unit.mounts[otherSide].side;
            mount.side = !mount.side;
        }

        //shoot if applicable
        if(mount.shoot && //must be shooting
            can && //must be able to shoot
            (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo || unit.team.rules().infiniteAmmo) && //check ammo
            (!alternate || wasFlipped == flipSprite) &&
            mount.warmup >= minWarmup && //must be warmed up
            unit.vel.len() >= minShootVelocity && //check velocity requirements
            mount.reload <= 0.0001f && //reload has to be 0
            Angles.within(rotate ? mount.rotation : unit.rotation + baseRotation, mount.targetRotation, shootCone) //has to be within the cone
        ){
            shoot(unit, mount, bulletX, bulletY, shootAngle);

            mount.reload = reload;

            if(useAmmo){
                unit.ammo--;
                if(unit.ammo < 0) unit.ammo = 0;
            }
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        super.draw(unit, mount);

        BeamMount bMount = (BeamMount)mount;
        if(bMount.stroke > 0.001f){
            float z = Draw.z();
            Draw.z(beamLayer);

            float
                weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
                mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
                shootX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
                shootY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY),
                bx = shootX + Angles.trnsx(weaponRotation + 90f, bMount.dst),
                by = shootY + Angles.trnsy(weaponRotation + 90f, bMount.dst);

            Draw.color(beamColor);
            Drawf.laser(beam, beamStart, beamEnd, shootX, shootY, bx, by, bMount.stroke * beamScl);

            Draw.z(z);
        }
    }

    public static class BeamMount extends WeaponMount{
        float beam, stroke, dst;

        public BeamMount(Weapon weapon){
            super(weapon);
        }
    }
}
