package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.ModulePayload.*;

public class TurretModule extends ReloadTurretModule{
    public boolean targetAir = true, targetGround = true, targetBlocks = true,
        targetEnemies = true, targetHealing;
    public boolean leadTargets = true, accurateDelay;
    public boolean moveWhileShooting = true;
    /** If true, ammo is only consumed once per shot regardless of bullet count. */
    public boolean consumeAmmoOnce = false;
    /** pattern used for bullets */
    public ShootPattern shoot = new ShootPattern();
    public float shootX = 0f, shootY = Float.NEGATIVE_INFINITY;
    public float shake;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public float inaccuracy, velocityRnd;

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Sound shootSound = Sounds.shoot;
    /** Sound emitted when shoot.firstShotDelay is >0 and shooting begins. */
    public Sound chargeSound = Sounds.none;
    /** Range for pitch of shoot sound. */
    public float soundPitchMin = 0.9f, soundPitchMax = 1.1f;
    public Effect ammoUseEffect = Fx.none;
    public float ammoEjectX, ammoEjectY;
    public boolean rotate = true;

    public Sortf unitSort = UnitSorts.closest;

    public TurretModule(String name, ModuleSize size){
        super(name, size);
        mountType = TurretMount::new;
        recoil = size();
        if(shootY == Float.NEGATIVE_INFINITY) shootY = size() * Vars.tilesize / 2f;
    }

    @Override
    public void init(){
        if(!targetEnemies) targetHealing = true; //At least shoot at *something*

        super.init();
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.inaccuracy, (int)inaccuracy, StatUnit.degrees);
        stats.add(Stat.reload, 60f / (reload) * shoot.shots, StatUnit.perSecond);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
    }

    public boolean canHeal(TurretMount mount){
        return targetHealing && hasAmmo(mount) && peekAmmo(mount).collidesTeam && peekAmmo(mount).healPercent > 0;
    }

    protected boolean validateTarget(ModularTurretBuild parent, TurretMount mount){
        return !Units.invalidateTarget(mount.target, canHeal(mount) ? Team.derelict : parent.team, mount.x, mount.y) || (playerControl && parent.isControlled()) || (logicControl && parent.logicControlled());
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        TurretMount m = (TurretMount)mount;
        return isDeployed(m) && (m.target != null || m.wasShooting);
    }

    @Override
    public boolean usePower(ModularTurretBuild parent, BaseMount mount){
        return super.usePower(parent, mount) && shouldReload(parent, (TurretMount)mount);
    }

    @Override
    public void targetPosition(BaseTurretMount m, Posc pos){
        TurretMount mount = (TurretMount)m;
        if(!hasAmmo(mount) || pos == null) return;
        BulletType bullet = peekAmmo(mount);

        var offset = Tmp.v1.setZero();

        if(leadTargets){
            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(shoot.firstShotDelay / Time.delta);
            }

            recoilOffset.trns(mount.rotation, shootY - mount.curRecoil);
            mount.targetPos.set(Predict.intercept(mount, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
        }

        if(mount.targetPos.isZero()){
            mount.targetPos.set(pos);
        }
    }

    @Override
    public float range(BaseMount mount){
        TurretMount m = (TurretMount)mount;
        if(peekAmmo(m) != null){
            return range + peekAmmo(m).rangeChange;
        }
        return range;
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount m){
        super.update(parent, m);
        if(!isDeployed(m)) return;

        TurretMount mount = (TurretMount)m;

        if(!validateTarget(parent, mount)) mount.target = null;

        if(mount.lastBullet != null && !mount.lastBullet.isAdded()) mount.lastBullet = null;

        mount.wasShooting = false;

        mount.curRecoil = Mathf.lerpDelta(mount.curRecoil, 0f, restitution);
        mount.heat = Mathf.lerpDelta(mount.heat, 0f, cooldown);

        if(hasAmmo(mount)){
            if(Float.isNaN(mount.reloadCounter)) mount.reloadCounter = 0;

            if(validateTarget(parent, mount)){
                boolean canShoot = true;

                if(playerControl && parent.isControlled()){ //player behavior
                    mount.targetPos.set(parent.unit.aimX(), parent.unit.aimY());
                    canShoot = parent.unit.isShooting();
                }else if(logicControl && parent.logicControlled()){ //logic behavior
                    canShoot = parent.logicShooting;
                }else{ //default AI behavior
                    targetPosition(mount, mount.target);

                    if(Float.isNaN(mount.rotation)) mount.rotation = 0;
                }

                float targetRot = Angles.angle(mount.x, mount.y, mount.targetPos.x, mount.targetPos.y);

                if(shouldTurn(mount)){
                    turnToTarget(parent, mount, targetRot);
                }

                if(mount.queuedBullets == 0 && (!rotate || Angles.angleDist(mount.rotation, targetRot) < shootCone) && canShoot){
                    mount.wasShooting = true;
                    updateShooting(parent, mount);
                }
            }
        }

        updateCharging(parent, mount);
    }

    @Override
    public void updateCooling(ModularTurretBuild parent, BaseTurretMount mount){
        if(((TurretMount)mount).charge <= 0f) super.updateCooling(parent, mount);
    }

    public void updateShooting(ModularTurretBuild parent, TurretMount mount){
        if(!shouldReload(parent, mount)) return;
        mount.reloadCounter += peekAmmo(mount).reloadMultiplier * delta(parent);

        if(mount.reloadCounter >= reload){
            BulletType type = peekAmmo(mount);

            shoot(parent, mount, type);

            mount.reloadCounter %= reload;
        }

        updateCooling(parent, mount);
    }

    public void updateCharging(ModularTurretBuild parent, TurretMount mount){
        if(charging(mount)){
            mount.charge += Time.delta;

            if(mount.charge >= shoot.firstShotDelay){
                mount.charge = 0;
            }
        }
    }

    public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
        float rot = mount.rotation,
            bulletX = mount.x + Angles.trnsx(rot, shootX, shootY),
            bulletY = mount.y + Angles.trnsy(rot, shootX, shootY);

        if(shoot.firstShotDelay > 0){
            chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
            type.chargeEffect.at(bulletX, bulletY, rot);
        }

        shoot.shoot(mount.totalShots, (xOffset, yOffset, angle, delay, mover) -> {
            mount.queuedBullets++;
            if(delay > 0f){
                Time.run(delay, () -> bullet(parent, mount, type, xOffset, yOffset, angle, mover));
            }else{
                bullet(parent, mount, type, xOffset, yOffset, angle, mover);
            }
            mount.totalShots++;
        });

        if(consumeAmmoOnce){
            useAmmo(parent, mount);
        }
    }

    protected void bullet(ModularTurretBuild parent, TurretMount mount, BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
        mount.queuedBullets--;

        if(parent.dead || (!consumeAmmoOnce && !hasAmmo(mount))) return;

        float
            rot = mount.rotation,
            bulletX = mount.x + Angles.trnsx(rot - 90, shootX + xOffset, shootY + yOffset),
            bulletY = mount.y + Angles.trnsy(rot - 90, shootX + xOffset, shootY + yOffset),
            shootAngle = rot + angleOffset + Mathf.range(inaccuracy);

        float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.targetPos.x, mount.targetPos.y) / type.range, minRange / type.range, range(mount) / type.range) : 1f;

        mount.lastBullet = type.create(parent, parent.team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, mount.targetPos.x, mount.targetPos.y);
        handleBullet(parent, mount, mount.lastBullet, xOffset, yOffset, shootAngle - rot);

        (shootEffect == null ? type.shootEffect : shootEffect).at(bulletX, bulletY, rot + angleOffset, type.hitColor);
        (smokeEffect == null ? type.smokeEffect : smokeEffect).at(bulletX, bulletY, rot + angleOffset, type.hitColor);
        shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));

        ammoUseEffect.at(
            mount.x + Angles.trnsx(rot, ammoEjectX),
            mount.y + Angles.trnsy(rot, ammoEjectY),
            rot * Mathf.sign(xOffset)
        );

        if(shake > 0){
            Effect.shake(shake, shake, mount);
        }

        mount.curRecoil = 1f;
        mount.heat = 1f;

        if(!consumeAmmoOnce){
            useAmmo(parent, mount);
        }
    }

    protected void handleBullet(ModularTurretBuild parent, TurretMount mount, Bullet bullet, float offsetX, float offsetY, float angleOffset){
        mount.lastBullet = bullet;
    }

    /** Consume ammo and return a type. */
    public BulletType useAmmo(ModularTurretBuild parent, TurretMount mount){
        if(parent.cheating()) return peekAmmo(mount);

        AmmoEntry entry = mount.ammo.peek();
        entry.amount -= 1;
        if(entry.amount <= 0) mount.ammo.pop();
        mount.totalAmmo = Math.max(mount.totalAmmo - 1, 0);
        return entry.type();
    }

    /** @return the ammo type that will be returned if useAmmo is called. */
    public BulletType peekAmmo(TurretMount mount){
        return mount.ammo.size == 0 ? null : mount.ammo.peek().type();
    }

    /** @return whether the turret has ammo. */
    @Override
    public boolean hasAmmo(TurretMount mount){
        return mount.ammo.size > 0;
    }

    public boolean shouldTurn(TurretMount mount){
        return !charging(mount) && (moveWhileShooting || mount.queuedBullets == 0);
    }

    public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
        if(rotate){
            mount.rotation = Angles.moveToward(mount.rotation, targetRot, rotateSpeed * edelta(parent));
        }else{
            mount.rotation = targetRot;
        }
    }

    @Override
    public boolean charging(TurretMount mount){
        return mount.queuedBullets > 0 && shoot.firstShotDelay > 0;
    }

    @Override
    public void findTarget(ModularTurretBuild parent, BaseTurretMount m){
        TurretMount mount = (TurretMount)m;
        if(!hasAmmo(mount)) return;
        float x = mount.x,
            y = mount.y;
        if(targetEnemies){
            if(targetAir && !targetGround){
                mount.target = Units.bestEnemy(parent.team, x, y, range(m), e -> !e.dead() && !e.isGrounded(), unitSort);
            }else{
                mount.target = Units.bestTarget(parent.team, x, y, range(m), e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> targetBlocks, unitSort);

                if(mount.target == null && canHeal(mount)){
                    mount.target = Units.findAllyTile(parent.team, x, y, range(m), b -> b.damaged() && b != parent);
                }
            }
        }else{
            mount.target = Units.findAllyTile(parent.team, x, y, range(m), b -> b.damaged() && b != parent);
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount m){
        BaseTurretMount mount = (BaseTurretMount)m;
        float x = mount.x,
            y = mount.y,
            rot = rotate ? mount.rotation - 90f : 0;

        if(mount.progress < deployTime){
            drawDeploy(parent, mount);
            return;
        }

        shootOffset.trns(rot + 90f, rotate ? -mount.curRecoil : 0f);

        Drawf.shadow(region, x + shootOffset.x - elevation, y + shootOffset.y - elevation, rot);
        applyColor(parent, mount);
        Draw.rect(region, x + shootOffset.x, y + shootOffset.y, rot);

        if(heatRegion.found() && mount.heat > 0.001f){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + shootOffset.x, y + shootOffset.y, rot);
            Draw.blend();
            Draw.color();
        }

        if(liquidRegion.found()){
            Drawf.liquid(liquidRegion, x + shootOffset.x, y + shootOffset.y, mount.liquids.currentAmount() / liquidCapacity, mount.liquids.current().color, rot);
        }

        if(topRegion.found()){
            Draw.z(Layer.turret + topLayerOffset);
            Draw.rect(topRegion, x + shootOffset.x, y + shootOffset.y, rot);
        }
        Draw.mixcol();
    }

    @Override
    public void drawPayload(ModulePayloadBuild payload){
        super.drawPayload(payload);

        if(topRegion.found()){
            Draw.rect(topRegion, payload.x, payload.y);
        }
    }

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);

        if(mount instanceof TurretMount m){
            write.f(m.charge);
        }
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);

        if(mount instanceof TurretMount m){
            m.charge = read.f();
        }
    }
}
