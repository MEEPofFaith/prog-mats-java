package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
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
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.multi.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.meta.*;

public class TurretModule implements Cloneable{
    public String name, localizedName;
    /** Automatically set */
    public short mountID;

    public float deployTime;
    public float range;
    public boolean hasLiquids, hasPower;
    public float powerUse;

    public boolean targetAir = true, targetGround = true, targetHealing;
    public boolean accurateDelay;
    public float reloadTime = 30f;
    public float rotateSpeed = 6f;
    public float shootCone = 8f;
    public float shootLength = -1f;

    public boolean acceptCoolant = true;
    public float liquidCapacity = 10f;
    public float coolantUsage = 0.2f;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;

    public float chargeTime = -1f;

    public ModuleSize size = ModuleSize.small;

    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float cooldown = 0.02f;
    public float elevation = -1f;
    public float layerOffset;
    public Color heatColor = Pal.turretHeat;
    public TextureRegion region;
    public TextureRegion heatRegion;

    /** Usually shares sprite with the module payload, so default to false. */
    public boolean outlineIcon;
    public Color outlineColor = Color.valueOf("404049");

    public Sortf unitSort = UnitSorts.closest;

    public Func3<TurretModule, Float, Float, TurretMount> mountType = TurretMount::new;

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
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }

        setBars();

        consumes.init();
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

    protected boolean validateTarget(ModularTurretBuild parent, TurretMount mount){
        return !Units.invalidateTarget(mount.target, mount.canHeal(parent) ? Team.derelict : parent.team, mount.x, mount.y) || parent.isControlled() || parent.logicControlled();
    }

    public boolean isActive(ModularTurretBuild parent, TurretMount mount){
        return (mount.target != null || mount.wasShooting) && parent.enabled;
    }

    public void targetPosition(ModularTurretBuild parent, TurretMount mount, Posc pos){
        if(!mount.hasAmmo(parent) || pos == null) return;
        BulletType bullet = mount.peekAmmo();

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

    public void update(ModularTurretBuild parent, TurretMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);

        if(mount.progress < deployTime) return;

        if(!validateTarget(parent, mount)) mount.target = null;

        mount.wasShooting = false;

        mount.recoil = Mathf.lerpDelta(mount.recoil, 0f, restitution);
        mount.heat = Mathf.lerpDelta(mount.heat, 0f, cooldown);

        if(mount.hasAmmo(parent)){
            if(Float.isNaN(mount.reload)) mount.rotation = 0;

            if(validateTarget(parent, mount)){
                boolean canShoot = true;

                if(parent.isControlled()){ //player behavior
                    mount.targetPos.set(parent.unit.aimX(), parent.unit.aimY());
                    canShoot = parent.unit.isShooting();
                }else if(parent.logicControlled()){ //logic behavior
                    canShoot = parent.logicShooting;
                }else{ //default AI behavior
                    targetPosition(parent, mount, mount.target);

                    if(Float.isNaN(mount.rotation)) mount.rotation = 0;
                }

                float targetRot = Angles.angle(mount.x, mount.y, mount.targetPos.x, mount.targetPos.y);

                if(shouldTurn(mount)){
                    turnToTarget(parent, mount, targetRot);
                }

                if(Angles.angleDist(mount.rotation, targetRot) < shootCone && canShoot){
                    mount.wasShooting = true;
                    updateShooting(parent, mount);
                }
            }
        }

        if(acceptCoolant){
            updateCooling(parent, mount);
        }
    }

    public void updateShooting(ModularTurretBuild parent, TurretMount mount){
        mount.reload += parent.delta() * mount.peekAmmo().reloadMultiplier;

        if(mount.reload >= reloadTime && !mount.charging){
            BulletType type = mount.peekAmmo();

            shoot(parent, mount, type);

            mount.reload %= reloadTime;
        }
    }

    public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
        //TODO
    }

    public void updateCooling(ModularTurretBuild parent, TurretMount mount){
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

    public boolean shouldTurn(TurretMount mount){
        return !mount.charging;
    }

    public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
        mount.rotation = Mathf.approachDelta(mount.rotation, targetRot, rotateSpeed * parent.delta());
    }

    public void findTarget(ModularTurretBuild parent, TurretMount mount){
        if(!mount.hasAmmo(parent)) return;
        float x = mount.x,
            y = mount.y;
        if(targetAir && !targetGround){
            mount.target = Units.bestEnemy(parent.team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
        }else{
            mount.target = Units.bestTarget(parent.team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> targetGround, unitSort);

            if(mount.target == null && mount.canHeal(parent)){
                mount.target = Units.findAllyTile(parent.team, x, y, range, b -> b.damaged() && b != parent);
            }
        }
    }

    public void draw(ModularTurretBuild parent, TurretMount mount){
        Vec2 tr = Tmp.v1;
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation;

        Draw.z(Layer.turret + layerOffset);

        if(mount.progress < deployTime){
            Drawf.construct(x, y, region, 0f, mount.progress / deployTime, 1f, mount.progress);
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
    }

    public void display(Table table, ModularTurretBuild parent, TurretMount mount){
        table.table(t -> {
            t.left();
            t.add(new Image(region)).size(8 * 4);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        });

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4f);

            displayBars(table, parent, mount);
        }).growX();
    }

    public void displayBars(Table table, ModularTurretBuild parent, TurretMount mount){
        for(Func2<ModularTurretBuild, TurretMount, Bar> bar : bars.list()){
            try{
                table.add(bar.get(parent, mount)).growX();
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

    public void load(){
        region = Core.atlas.find(name);
        heatRegion = Core.atlas.find(name + "-heat");
    }

    public boolean acceptItem(Item item, TurretMount mount){
        return false;
    }

    public boolean acceptLiquid(Liquid liquid, TurretMount mount){
        return hasLiquids && consumes.liquidfilters.get(liquid.id) && mount.liquids.get(liquid) < liquidCapacity;
    }

    public void handleItem(Item item, TurretMount mount){}

    /** @return amount of liquid accepted */
    public float handleLiquid(Liquid liquid, float amount, TurretMount mount){
        float added = Math.min(amount, liquidCapacity - mount.liquids.get(liquid));
        mount.liquids.add(liquid, added);
        return amount - added;
    }

    public float powerUse(ModularTurretBuild parent, TurretMount mount){
        return Mathf.num(isActive(parent, mount)) * powerUse;
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