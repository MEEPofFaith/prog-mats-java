package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.payloads.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.multi.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;

public class TurretModule implements Cloneable{
    public String name;
    /** Automatically set */
    public short mountID;

    public float deployTime;
    public float range;
    public boolean hasLiquid;

    public boolean targetAir = true, targetGround = true, targetHealing;
    public boolean accurateDelay;

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

    public TurretModule(String name){
        this.name = name;
    }

    public void init(){
        //small = 1, medium = 2, large = 3
        if(elevation < 0) elevation = size.ordinal() + 1;
    }

    public void createIcons(MultiPacker packer){
        if(outlineIcon) Outliner.outlineRegion(packer, region, outlineColor, name);
    }

    public void targetPosition(ModularTurretBuild parent, TurretMount mount, Posc pos){
        if(!mount.hasAmmo(parent) || pos == null) return;
        BulletType bullet = mount.peekAmmo();

        var offset = Tmp.v1.setZero();

        //when delay is accurate, assume unit has moved by chargeTime already
        if(accurateDelay && pos instanceof Hitboxc h){
            offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
        }

        mount.targetPos.set(PMUtls.intercept(mount.x, mount.y, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

        if(mount.targetPos.isZero()){
            mount.targetPos.set(pos);
        }
    }

    public void update(ModularTurretBuild parent, TurretMount mount){
        mount.progress = Mathf.approachDelta(mount.progress, deployTime, 1f);

        if(mount.progress < deployTime) return;

        mount.recoil = Mathf.lerpDelta(mount.recoil, 0f, restitution);
        mount.heat = Mathf.lerpDelta(mount.heat, 0f, cooldown);
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
        return false;
    }

    public boolean acceptPayload(BuildPayload payload, TurretMount mount){
        return false;
    }

    public void handleItem(Item item, TurretMount mount){}

    public void handleLiquid(Liquid liquid, TurretMount mount){}

    public void handlePayload(BuildPayload payload, TurretMount mount){}

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