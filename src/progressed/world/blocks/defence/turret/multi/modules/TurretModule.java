package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
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
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.consumers.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.*;
import progressed.world.meta.*;

public class TurretModule implements Cloneable{
    public String name, localizedName;
    /** Automatically set */
    public short mountID;

    public float deployTime = 120f;
    public float range = 80f;
    public boolean hasLiquids, hasPower;
    public float powerUse;

    public boolean targetAir = true, targetGround = true, targetHealing;
    public boolean accurateDelay;
    public float reloadTime = 30f;
    public float rotateSpeed = 5f;
    public float shootCone = 8f;

    public float shootLength = -1f;
    public int shots = 1;
    public float spread;
    public float shootShake;
    public float xRand;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public float burstSpacing = 0;
    /** <= 1 does nothing.
     * Switches to alternate shooting if > 1. */
    public int barrels = 1;
    public float inaccuracy = 0f;
    public float velocityInaccuracy = 0f;

    public boolean acceptCoolant = true;
    public float liquidCapacity = 10f;
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

    public ModuleSize size = ModuleSize.small;

    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;
    public float elevation = -1f;
    public float layerOffset;

    public TextureRegion region, heatRegion, liquidRegion, topRegion;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public Sortf unitSort = UnitSorts.closest;

    public Func4<ModularTurretBuild, TurretModule, Float, Float, TurretMount> mountType = TurretMount::new;

    public final MountBars bars = new MountBars();
    public final Consumers consumes = new Consumers();

    public TurretModule(String name){
        this.name = name;

        localizedName = Core.bundle.get("pmturretmodule." + name + ".name", name);
    }

    public void init(){
        //small = 1, medium = 2, large = 3
        if(elevation < 0) elevation = size();
        if(shootLength < 0) shootLength = size() * Vars.tilesize / 2f;

        if(acceptCoolant && !consumes.has(ConsumeType.liquid)){
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }

        setBars();

        consumes.init();
    }

    public void load(){
        region = Core.atlas.find("prog-mats-" + name);
        heatRegion = Core.atlas.find("prog-mats-" + name + "-heat");
        liquidRegion = Core.atlas.find("prog-mats-" + name + "-liquid");
        topRegion = Core.atlas.find("prog-mats-" + name + "-top");
    }

    public void setBars(){
        if(hasLiquids){
            Func<TurretMount, Liquid> current = mount -> mount.liquids == null ? Liquids.water : mount.liquids.current();
            bars.add("liquid", (entity, mount) -> new Bar(
                () -> mount.liquids.get(current.get(mount)) <= 0.001f ? Core.bundle.get("bar.liquid") : current.get(mount).localizedName,
                () -> current.get(mount).barColor(),
                () -> mount == null || mount.liquids == null ? 0f : mount.liquids.get(current.get(mount)) / liquidCapacity
            ));
        }

        if(hasPower){
            bars.add("power", (entity, mount) -> new Bar(
                () -> Core.bundle.get("bar.power"),
                () -> Pal.powerBar,
                () -> entity.power.status
            ));
        }
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public int size(){
        return size.ordinal() + 1;
    }

    public void onProximityAdded(TurretMount mount){}

    protected boolean validateTarget(TurretMount mount){
        return !Units.invalidateTarget(mount.target, mount.canHeal() ? Team.derelict : mount.parent.team, mount.x, mount.y) || mount.parent.isControlled() || mount.parent.logicControlled();
    }

    public boolean isActive(TurretMount mount){
        return isDeployed(mount) && (mount.target != null || mount.wasShooting) && mount.parent.enabled;
    }

    public void targetPosition(TurretMount mount, Posc pos){
        if(!hasAmmo(mount) || pos == null) return;
        BulletType bullet = peekAmmo(mount);

        var offset = Tmp.v1.setZero();

        //when delay is accurate, assume unit has moved by chargeTime already
        if(accurateDelay && pos instanceof Hitboxc h){
            offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
        }

        Tmp.v2.trns(mount.rotation, shootLength);
        mount.targetPos.set(PMUtls.intercept(mount.x, mount.y, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

        if(mount.targetPos.isZero()){
            mount.targetPos.set(pos);
        }
    }

    public boolean isDeployed(TurretMount mount){
        return mount.progress >= deployTime;
    }

    public void update(TurretMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);

        if(!isDeployed(mount)) return;

        if(!validateTarget(mount)) mount.target = null;

        mount.wasShooting = false;

        mount.recoil = Mathf.lerpDelta(mount.recoil, 0f, restitution);
        mount.heat = Mathf.lerpDelta(mount.heat, 0f, cooldown);

        ModularTurretBuild parent = mount.parent;
        if(hasAmmo(mount)){
            if(Float.isNaN(mount.reload)) mount.reload = 0;

            if(validateTarget(mount)){
                boolean canShoot = true;

                if(parent.isControlled()){ //player behavior
                    mount.targetPos.set(parent.unit.aimX(), parent.unit.aimY());
                    canShoot = parent.unit.isShooting();
                }else if(parent.logicControlled()){ //logic behavior
                    canShoot = parent.logicShooting;
                }else{ //default AI behavior
                    targetPosition(mount, mount.target);

                    if(Float.isNaN(mount.rotation)) mount.rotation = 0;
                }

                float targetRot = Angles.angle(mount.x, mount.y, mount.targetPos.x, mount.targetPos.y);

                if(shouldTurn(mount)){
                    turnToTarget(mount, targetRot);
                }

                if(Angles.angleDist(mount.rotation, targetRot) < shootCone && canShoot){
                    mount.wasShooting = true;
                    updateShooting(mount);
                }
            }
        }

        if(acceptCoolant){
            updateCooling(mount);
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

    public void updateShooting(TurretMount mount){
        mount.reload += mount.parent.delta() * peekAmmo(mount).reloadMultiplier * reloadSpeedScl(mount);

        if(mount.reload >= reloadTime && !mount.charging){
            BulletType type = peekAmmo(mount);

            shoot(mount, type);

            mount.reload %= reloadTime;
        }
    }

    public void shoot(TurretMount mount, BulletType type){
        ModularTurretBuild parent = mount.parent;
        Vec2 tr = Tmp.v1;
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation;

        //when charging is enabled, use the charge shoot pattern
        if(chargeTime > 0){
            useAmmo(mount);

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
                tr.trns(rot, shootLength);
                bullet(mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy));
                mount.charging = false;
            });

            //when burst spacing is enabled, use the burst pattern
        }else{
            for(int i = 0; i < shots; i++){
                int ii = i;
                if(burstSpacing > 0.0001f){
                    Time.run(burstSpacing * i, () -> {
                        if(parent.dead || !hasAmmo(mount)) return;
                        basicShoot(mount, type, ii);
                    });
                }else{
                    if(parent.dead || !hasAmmo(mount)) return;
                    basicShoot(mount, type, i);
                }
            }

        }
    }

    public void basicShoot(TurretMount mount, BulletType type, int count){
        Vec2 tr = Tmp.v1;
        float rot = mount.rotation;

        if(barrels > 1){
            float i = (mount.shotCounter % barrels) - (barrels - 1)/2f;

            tr.trns(rot - 90, spread * i + Mathf.range(xRand), shootLength);
            bullet(mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy));
        }else{
            tr.trns(rot, shootLength, Mathf.range(xRand));
            bullet(mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (count - (int)(shots / 2f)) * spread);
        }

        useAmmo(mount);
    }

    protected void bullet(TurretMount mount, BulletType type, float angle){
        float x = mount.x, y = mount.y;
        Vec2 tr = Tmp.v1;
        float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, mount.targetPos.x, mount.targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

        type.create(mount.parent, mount.parent.team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);

        mount.recoil = recoilAmount;
        mount.heat = 1f;
        mount.shotCounter++;
        effects(mount);
    }

    protected void effects(TurretMount mount){
        float x = mount.x, y = mount.y;
        Vec2 tr = Tmp.v1;

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

        Tmp.v2.trns(mount.rotation - 90, ammoEjectX, ammoEjectY);
        ammoUseEffect.at(mount.x + Tmp.v2.x, mount.y + Tmp.v2.y, mount.rotation * scl);
    }

    /** Consume ammo and return a type. */
    public BulletType useAmmo(TurretMount mount){
        if(mount.parent.cheating()) return peekAmmo(mount);

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

    public void turnToTarget(TurretMount mount, float targetRot){
        mount.rotation = Angles.moveToward(mount.rotation, targetRot, rotateSpeed * mount.parent.delta() * reloadSpeedScl(mount));
    }

    public float reloadSpeedScl(TurretMount mount){
        return 1f;
    }

    public void findTarget(TurretMount mount){
        if(!hasAmmo(mount)) return;
        float x = mount.x,
            y = mount.y;
        if(targetAir && !targetGround){
            mount.target = Units.bestEnemy(mount.parent.team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
        }else{
            mount.target = Units.bestTarget(mount.parent.team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> targetGround, unitSort);

            if(mount.target == null && mount.canHeal()){
                mount.target = Units.findAllyTile(mount.parent.team, x, y, range, b -> b.damaged() && b != mount.parent);
            }
        }
    }

    public void draw(TurretMount mount){
        Vec2 tr = Tmp.v1;
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation;

        Draw.z(Layer.turret + layerOffset);

        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> PMDrawf.build(x, y, region, rot - 90, mount.progress / deployTime));
            return;
        }

        tr.trns(rot, -mount.recoil);

        Drawf.shadow(region, x + tr.x, y + tr.y - elevation, rot - 90);
        Draw.rect(region, x + tr.x, y + tr.y, rot - 90);

        if(heatRegion.found() && mount.heat > 0.001f){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + tr.x, y + tr.y, rot - 90);
            Draw.blend();
            Draw.color();
        }

        if(liquidRegion.found())
            Drawf.liquid(liquidRegion, x + tr.x, y + tr.y, mount.liquids.total() / liquidCapacity, mount.liquids.current().color, rot - 90);

        if(topRegion.found())
            Draw.rect(topRegion, x + tr.x, y + tr.y, rot - 90);
    }

    public void display(Table table, TurretMount mount){
        table.table(t -> {
            t.left();
            t.add(new Image(region)).size(8 * 4);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        });

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4f);

            displayBars(table, mount);
        }).growX();
    }

    public void displayBars(Table table, TurretMount mount){
        for(Func2<ModularTurretBuild, TurretMount, Bar> bar : bars.list()){
            try{
                table.add(bar.get(mount.parent, mount)).growX();
                table.row();
            }catch(ClassCastException e){
                break;
            }
        }
    }

    public TurretModule copy(){
        try{
            return (TurretModule)clone();
        }catch(CloneNotSupportedException suck){
            throw new RuntimeException("very good language design", suck);
        }
    }

    public int acceptStack(Item item, int amount, TurretMount mount){
        return 0;
    }

    public boolean acceptItem(Item item, TurretMount mount){
        return false;
    }

    public boolean acceptLiquid(Liquid liquid, TurretMount mount){
        return isDeployed(mount) && hasLiquids && consumes.liquidfilters.get(liquid.id) && mount.liquids.get(liquid) < liquidCapacity;
    }

    public void handleItem(Item item, TurretMount mount){}

    /** @return amount of liquid accepted */
    public float handleLiquid(Liquid liquid, float amount, TurretMount mount){
        float added = Math.min(amount, liquidCapacity - mount.liquids.get(liquid));
        mount.liquids.add(liquid, added);
        return amount - added;
    }

    public float powerUse(TurretMount mount){
        return Mathf.num(isActive(mount)) * powerUse;
    }

    public void writeAll(Writes write, TurretMount mount){
        write.s(mountID);
        write.b(version());
        if(mount.liquids != null) mount.liquids.write(write);
        write(write, mount);
    }

    public void readAll(Reads read, TurretMount mount){
        byte revision = read.b();
        if(mount.liquids != null) mount.liquids.read(read);
        read(read, revision, mount);
    }

    public void write(Writes write, TurretMount mount){
        write.f(mount.progress);
        write.f(mount.reload);
        write.f(mount.rotation);
    }

    public void read(Reads read, byte revision, TurretMount mount){
        mount.progress = read.f();
        mount.reload = read.f();
        mount.rotation = read.f();
    }

    public byte version(){
        return 0;
    }

    /** Modular Turrets have mounts of 3 sizes. */
    public static enum ModuleSize{
        small, medium, large
    }
}