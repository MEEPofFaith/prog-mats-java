package progressed.world.blocks.defence.turret.multi.modules.turret;

import arc.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;
import progressed.world.meta.*;

public class ItemTurretModule extends TurretModule{
    public ObjectMap<Item, BulletType> ammoTypes = new ObjectMap<>();

    public int maxAmmo = 20;

    public ItemTurretModule(String name, ModuleSize size){
        super(name, size);
    }

    public ItemTurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("ammo", (entity, mount) -> new Bar(
            "stat.ammo",
            Pal.ammo,
            () -> ((TurretMount)mount).totalAmmo / (float)maxAmmo
        ));
    }

    /** Initializes accepted ammo map. Format: [item1, bullet1, item2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = ObjectMap.of(objects);
    }

    /** Makes copies of all bullets and limits their range. */
    public void limitRange(){
        limitRange(1f);
    }

    /** Makes copies of all bullets and limits their range. */
    public void limitRange(float margin){
        for(var entry : ammoTypes.copy().entries()){
            var copy = entry.value.copy();
            copy.lifetime = (range + margin) / copy.speed;
            ammoTypes.put(entry.key, copy);
        }
    }

    @Override
    public void onProximityAdded(ModularTurretBuild parent, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return;

        if(parent.cheating() && m.ammo.size > 0){
            handleItem(ammoTypes.entries().next().key, mount);
        }
    }

    @Override
    public int acceptStack(Item item, int amount, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return 0;

        BulletType type = ammoTypes.get(item);

        if(type == null || !isDeployed(mount)) return 0;

        return Math.min((int)((maxAmmo - m.totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
    }

    @Override
    public void handleItem(Item item, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return;

        if(item == Items.pyratite){
            Events.fire(Trigger.flameAmmo);
        }

        BulletType type = ammoTypes.get(item);
        if(type == null) return;
        m.totalAmmo += type.ammoMultiplier;

        //find ammo entry by type
        for(int i = 0; i < m.ammo.size; i++){
            ModuleItemEntry entry = (ModuleItemEntry)m.ammo.get(i);

            //if found, put it to the right
            if(entry.item == item){
                entry.amount += type.ammoMultiplier;
                m.ammo.swap(i, m.ammo.size - 1);
                return;
            }
        }

        //must not be found
        m.ammo.add(new ModuleItemEntry(item, (int)type.ammoMultiplier));
    }

    @Override
    public boolean acceptItem(Item item, BaseMount mount){
        if(!(mount instanceof TurretMount m)) return false;
        return isDeployed(mount) && ammoTypes.get(item) != null && m.totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
    }

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);
        if(!(mount instanceof TurretMount m)) return;

        write.b(m.ammo.size);
        for(AmmoEntry entry : m.ammo){
            ModuleItemEntry i = (ModuleItemEntry)entry;
            write.s(i.item.id);
            write.s(i.amount);
        }
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);
        if(!(mount instanceof TurretMount m)) return;

        m.ammo.clear();
        m.totalAmmo = 0;
        int amount = read.ub();
        for(int i = 0; i < amount; i++){
            Item item = Vars.content.item(read.s());
            short a = read.s();

            //only add ammo if this is a valid ammo type
            if(item != null && ammoTypes.containsKey(item)){
                m.totalAmmo += a;
                m.ammo.add(new ModuleItemEntry(item, a));
            }
        }
    }

    public class ModuleItemEntry extends AmmoEntry{
        public Item item;

        ModuleItemEntry(Item item, int amount){
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type(){
            return ammoTypes.get(item);
        }
    }
}