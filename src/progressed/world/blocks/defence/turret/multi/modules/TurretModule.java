package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
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
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.consumers.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class TurretModule extends BaseModule{
    public float range = 80f;

    public boolean targetAir = true, targetGround = true, targetHealing;
    public boolean accurateDelay;
    public float reloadTime = 30f;
    public float rotateSpeed = 5f;
    public float shootCone = 8f;

    public float shootLength = -1f;
    public int shots = 1;
    public boolean countAfter;
    public float barrelSpacing, angleSpread;
    public float shootShake;
    public float xRand;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public float burstSpacing = 0;
    public int barrels = 1;
    public float inaccuracy, velocityInaccuracy;

    public boolean acceptCoolant = true;
    public float coolantUsage = 0.2f;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;

    public Color heatColor = Pal.turretHeat;
    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Sound shootSound = Sounds.shoot;
    public Effect ammoUseEffect = Fx.none;
    public boolean alternate = false;
    public float ammoEjectX = 1f, ammoEjectY = -1f;

    public float chargeTime = -1f;
    public int chargeEffects = 5;
    public float chargeMaxDelay = 10f;
    public Effect chargeEffect = Fx.none;
    public Effect chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;

    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;
    public float elevation = -1f;

    public TextureRegion heatRegion, liquidRegion, topRegion;

    public Sortf unitSort = UnitSorts.closest;

    public TurretModule(String name){
        super(name);
        mountType = TurretMount::new;
    }

    @Override
    public void init(){
        //small = 1, medium = 2, large = 3
        if(elevation < 0) elevation = size();
        if(shootLength < 0) shootLength = size() * Vars.tilesize / 2f;
        if(barrels < 1) barrels = 1; //Do not

        if(acceptCoolant && !consumes.has(ConsumeType.liquid)){
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }

        super.init();
    }

    @Override
    public void load(){
        super.load();
        heatRegion = Core.atlas.find("prog-mats-" + name + "-heat");
        liquidRegion = Core.atlas.find("prog-mats-" + name + "-liquid");
        topRegion = Core.atlas.find("prog-mats-" + name + "-top");
    }

    public boolean canHeal(TurretMount mount){
        return targetHealing && hasAmmo(mount) && peekAmmo(mount).collidesTeam && peekAmmo(mount).healPercent > 0;
    }

    protected boolean validateTarget(ModularTurretBuild parent, TurretMount mount){
        return !Units.invalidateTarget(mount.target, canHeal(mount) ? Team.derelict : parent.team, mount.x, mount.y) || parent.isControlled() || parent.logicControlled();
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return false;
        return isDeployed(m) && (m.target != null || m.wasShooting) && parent.enabled;
    }

    public void targetPosition(TurretMount mount, Posc pos){
        if(!hasAmmo(mount) || pos == null) return;
        BulletType bullet = peekAmmo(mount);

        var offset = Tmp.v1.setZero();

        //when delay is accurate, assume unit has moved by chargeTime already
        if(accurateDelay && pos instanceof Hitboxc h){
            offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
        }

        tr2.trns(mount.rotation, shootLength - mount.recoil);
        mount.targetPos.set(PMUtls.intercept(mount.x, mount.y, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

        if(mount.targetPos.isZero()){
            mount.targetPos.set(pos);
        }
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount mount){
        super.update(parent, mount);

        if(!isDeployed(mount) || !(mount instanceof TurretMount m)) return;

        if(!validateTarget(parent, m)) m.target = null;

        m.wasShooting = false;

        m.recoil = Mathf.lerpDelta(m.recoil, 0f, restitution);
        m.heat = Mathf.lerpDelta(m.heat, 0f, cooldown);

        if(hasAmmo(m)){
            if(Float.isNaN(m.reload)) m.reload = 0;

            if(validateTarget(parent, m)){
                boolean canShoot = true;

                if(parent.isControlled()){ //player behavior
                    m.targetPos.set(parent.unit.aimX(), parent.unit.aimY());
                    canShoot = parent.unit.isShooting();
                }else if(parent.logicControlled()){ //logic behavior
                    canShoot = parent.logicShooting;
                }else{ //default AI behavior
                    targetPosition(m, m.target);

                    if(Float.isNaN(m.rotation)) m.rotation = 0;
                }

                float targetRot = Angles.angle(m.x, m.y, m.targetPos.x, m.targetPos.y);

                if(shouldTurn(m)){
                    turnToTarget(parent, m, targetRot);
                }

                if(Angles.angleDist(m.rotation, targetRot) < shootCone && canShoot){
                    m.wasShooting = true;
                    updateShooting(parent, m);
                }
            }
        }

        if(acceptCoolant){
            updateCooling(m);
        }
    }

    public void updateCooling(TurretMount mount){
        if(mount.reload < reloadTime && !mount.charging){
            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
            Liquid liquid = mount.liquids.current();

            float used = Math.min(mount.liquids.get(liquid), maxUsed * Time.delta);
            mount.reload += used * liquid.heatCapacity * coolantMultiplier;
            mount.liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                coolEffect.at(mount.x + Mathf.range(size() * Vars.tilesize / 2f), mount.y + Mathf.range(size() * Vars.tilesize / 2f));
            }
        }
    }

    public void updateShooting(ModularTurretBuild parent, TurretMount mount){
        mount.reload += parent.delta() * peekAmmo(mount).reloadMultiplier * speedScl(parent, mount);

        if(mount.reload >= reloadTime && !mount.charging){
            BulletType type = peekAmmo(mount);

            shoot(parent, mount, type);

            mount.reload %= reloadTime;
        }
    }

    public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation;

        //when charging is enabled, use the charge shoot pattern
        if(chargeTime > 0){
            useAmmo(parent, mount);

            tr.trns(rot, shootLength);
            chargeBeginEffect.at(x + tr.x, y + tr.y, rot);
            chargeSound.at(x + tr.x, y + tr.y, 1);

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(parent.dead) return;
                    tr.trns(rot, shootLength);
                    chargeEffect.at(x + tr.x, y + tr.y, rot);
                });
            }

            mount.charging = true;

            Time.run(chargeTime, () -> {
                if(parent.dead) return;
                tr.trns(rot, shootLength - mount.recoil);
                bullet(parent, mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy));
                mount.charging = false;
            });
        }else{
            for(int i = 0; i < shots; i++){
                int ii = i;
                if(burstSpacing > 0.0001f){
                    Time.run(burstSpacing * i, () -> {
                        if(parent.dead || !hasAmmo(mount)) return;
                        basicShoot(parent, mount, peekAmmo(mount), ii);
                    });
                }else{
                    if(parent.dead || !hasAmmo(mount)) return;
                    basicShoot(parent, mount, peekAmmo(mount), i);
                }
            }

            if(countAfter) mount.shotCounter++;
        }
    }

    public void basicShoot(ModularTurretBuild parent, TurretMount mount, BulletType type, int count){
        float rot = mount.rotation;
        float b = (mount.shotCounter % barrels) - (barrels - 1) / 2f;

        tr.trns(rot - 90, barrelSpacing * b + Mathf.range(xRand), shootLength - mount.recoil);
        bullet(parent, mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (count - (int)(shots / 2f)) * angleSpread);

        useAmmo(parent, mount);
    }

    protected void bullet(ModularTurretBuild parent, TurretMount mount, BulletType type, float angle){
        float x = mount.x, y = mount.y;
        float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, mount.targetPos.x, mount.targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

        type.create(parent, parent.team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);

        mount.recoil = recoilAmount;
        mount.heat = 1f;
        if(!countAfter) mount.shotCounter++;
        effects(mount);
    }

    protected void effects(TurretMount mount){
        float x = mount.x, y = mount.y;

        Effect fshootEffect = shootEffect == Fx.none ? peekAmmo(mount).shootEffect : shootEffect;
        Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo(mount).smokeEffect : smokeEffect;

        fshootEffect.at(x + tr.x, y + tr.y, mount.rotation);
        fsmokeEffect.at(x + tr.x, y + tr.y, mount.rotation);
        shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

        if(shootShake > 0){
            Effect.shake(shootShake, shootShake, x, y);
        }

        mount.recoil = recoilAmount;
    }

    protected void ejectEffects(TurretMount mount){
        float scl = Mathf.sign(alternate && mount.shotCounter % 2 == 0);

        tr2.trns(mount.rotation - 90, ammoEjectX, ammoEjectY);
        ammoUseEffect.at(mount.x + tr2.x, mount.y + tr2.y, mount.rotation * scl);
    }

    /** Consume ammo and return a type. */
    public BulletType useAmmo(ModularTurretBuild parent, TurretMount mount){
        if(parent.cheating()) return peekAmmo(mount);

        AmmoEntry entry = mount.ammo.peek();
        entry.amount -= 1;
        if(entry.amount <= 0) mount.ammo.pop();
        mount.totalAmmo = Math.max(mount.totalAmmo - 1, 0);
        ejectEffects(mount);
        return entry.type();
    }

    /** @return the ammo type that will be returned if useAmmo is called. */
    public BulletType peekAmmo(TurretMount mount){
        return mount.ammo.peek().type();
    }

    /** @return whether the turret has ammo. */
    public boolean hasAmmo(TurretMount mount){
        return mount.ammo.size > 0;
    }

    public boolean shouldTurn(TurretMount mount){
        return !mount.charging;
    }

    public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
        mount.rotation = Angles.moveToward(mount.rotation, targetRot, rotateSpeed * parent.delta() * speedScl(parent, mount));
    }

    public void findTarget(ModularTurretBuild parent, TurretMount mount){
        if(!hasAmmo(mount)) return;
        float x = mount.x,
            y = mount.y;
        if(targetAir && !targetGround){
            mount.target = Units.bestEnemy(parent.team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
        }else{
            mount.target = Units.bestTarget(parent.team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> targetGround, unitSort);

            if(mount.target == null && canHeal(mount)){
                mount.target = Units.findAllyTile(parent.team, x, y, range, b -> b.damaged() && b != parent);
            }
        }
    }

    @Override
    public void draw(BaseMount mount){
        if(!(mount instanceof TurretMount m)) return;

        float x = m.x,
            y = m.y,
            rot = m.rotation;

        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.build(mount.x, mount.y, region, mount.rotation - 90, mount.progress / deployTime));
            return;
        }

        tr.trns(rot, -m.recoil);

        Drawf.shadow(region, x + tr.x, y + tr.y - elevation, rot - 90);
        Draw.rect(region, x + tr.x, y + tr.y, rot - 90);

        if(heatRegion.found() && m.heat > 0.001f){
            Draw.color(heatColor, m.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + tr.x, y + tr.y, rot - 90);
            Draw.blend();
            Draw.color();
        }

        if(liquidRegion.found())
            Drawf.liquid(liquidRegion, x + tr.x, y + tr.y, m.liquids.total() / liquidCapacity, m.liquids.current().color, rot - 90);

        if(topRegion.found())
            Draw.rect(topRegion, x + tr.x, y + tr.y, rot - 90);
    }

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);

        if(mount instanceof TurretMount m) write.f(m.reload);
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);

        if(mount instanceof TurretMount m) m.reload = read.f();
    }
}