package progressed.world.blocks.defence.turret.multi.modules;

import arc.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import progressed.world.blocks.defence.turret.multi.*;

public class ItemTurretModule extends TurretModule{
    public ObjectMap<Item, BulletType> ammoTypes = new ObjectMap<>();

    public int maxAmmo = 30;

    public ItemTurretModule(String name){
        super(name);
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
    public void onProximityAdded(TurretMount mount){
        if(mount.parent.cheating() && mount.ammo.size > 0){
            handleItem(ammoTypes.entries().next().key, mount);
        }
    }

    @Override
    public int acceptStack(Item item, int amount, TurretMount mount){
        BulletType type = ammoTypes.get(item);

        if(type == null || !isDeployed(mount)) return 0;

        return Math.min((int)((maxAmmo - mount.totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
    }

    @Override
    public void handleItem(Item item, TurretMount mount){
        if(item == Items.pyratite){
            Events.fire(Trigger.flameAmmo);
        }

        BulletType type = ammoTypes.get(item);
        if(type == null) return;
        mount.totalAmmo += type.ammoMultiplier;

        //find ammo entry by type
        for(int i = 0; i < mount.ammo.size; i++){
            ModuleItemEntry entry = (ModuleItemEntry)mount.ammo.get(i);

            //if found, put it to the right
            if(entry.item == item){
                entry.amount += type.ammoMultiplier;
                mount.ammo.swap(i, mount.ammo.size - 1);
                return;
            }
        }

        //must not be found
        mount.ammo.add(new ModuleItemEntry(item, (int)type.ammoMultiplier));
    }

    @Override
    public boolean acceptItem(Item item, TurretMount mount){
        return isDeployed(mount) && ammoTypes.get(item) != null && mount.totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
    }

    @Override
    public void write(Writes write, TurretMount mount){
        super.write(write, mount);

        write.b(mount.ammo.size);
        for(AmmoEntry entry : mount.ammo){
            ModuleItemEntry i = (ModuleItemEntry)entry;
            write.s(i.item.id);
            write.s(i.amount);
        }
    }

    @Override
    public void read(Reads read, byte revision, TurretMount mount){
        super.read(read, revision, mount);

        mount.ammo.clear();
        mount.totalAmmo = 0;
        int amount = read.ub();
        for(int i = 0; i < amount; i++){
            Item item = Vars.content.item(read.s());
            short a = read.s();

            //only add ammo if this is a valid ammo type
            if(item != null && ammoTypes.containsKey(item)){
                mount.totalAmmo += a;
                mount.ammo.add(new ModuleItemEntry(item, a));
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