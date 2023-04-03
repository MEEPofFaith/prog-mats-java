package progressed.util;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.entities.bullet.energy.*;

public class PMUtls{
    public static final Rand rand = new Rand();
    static final Seq<ItemStack> rawStacks = new Seq<>();
    static final Seq<Item> items = new Seq<>();
    static final IntSeq amounts = new IntSeq();
    
    public static float bulletDamage(BulletType b, float lifetime){
        if(b.spawnUnit != null){ //Missile unit damage
            Weapon uW = b.spawnUnit.weapons.first();
            return bulletDamage(uW.bullet, uW.bullet.lifetime) * uW.shoot.shots;
        }else{
            float damage = b.damage + b.splashDamage; //Base Damage
            damage += b.lightningDamage * b.lightning * b.lightningLength; //Lightning Damage

            if(b.fragBullet != null){
                damage += bulletDamage(b.fragBullet, b.fragBullet.lifetime) * b.fragBullets; //Frag Bullet Damage
            }

            if(b instanceof ContinuousBulletType cB){ //Continuous Damage
                return damage * lifetime / cB.damageInterval;
            }else if(b instanceof BlackHoleBulletType){
                return damage * lifetime / 2f;
            }else{
                return damage;
            }
        }
    }

    //Is this really necessary?
    public static String stringsFixed(float value){
        return Strings.autoFixed(value, 2);
    }

    /** Research costs for anything that isn't a block or unit */
    public static ItemStack[] researchRequirements(ItemStack[] requirements, float mul){
        ItemStack[] out = new ItemStack[requirements.length];
        for(int i = 0; i < out.length; i++){
            int quantity = 60 + Mathf.round(Mathf.pow(requirements[i].amount, 1.1f) * 20 * mul, 10);

            out[i] = new ItemStack(requirements[i].item, UI.roundAmount(quantity));
        }

        return out;
    }

    /** Adds ItemStack arrays together. Combines duplicate items into one stack. */
    public static ItemStack[] addItemStacks(Seq<ItemStack[]> stacks){
        rawStacks.clear();
        items.clear();
        amounts.clear();
        stacks.each(s -> {
            for(ItemStack stack : s){
                rawStacks.add(stack);
            }
        });
        rawStacks.sort(s -> s.item.id);
        rawStacks.each(s -> {
            if(!items.contains(s.item)){
                items.add(s.item);
                amounts.add(s.amount);
            }else{
                amounts.incr(items.indexOf(s.item), s.amount);
            }
        });
        ItemStack[] result = new ItemStack[items.size];
        for(int i = 0; i < items.size; i++){
            result[i] = new ItemStack(items.get(i), amounts.get(i));
        }
        return result;
    }

    /**
     * {@link Tile#relativeTo(int, int)} does not account for building rotation.
     * Taken from Goobrr/esoterum.
     * */
    public static int relativeDirection(Building from, Building to){
        if(from == null || to == null) return -1;
        if(from.x == to.x && from.y > to.y) return (7 - from.rotation) % 4;
        if(from.x == to.x && from.y < to.y) return (5 - from.rotation) % 4;
        if(from.x > to.x && from.y == to.y) return (6 - from.rotation) % 4;
        if(from.x < to.x && from.y == to.y) return (4 - from.rotation) % 4;
        return -1;
    }

    public static Item oreDrop(Tile tile){
        if(tile == null) return null;

        if(tile.block() != Blocks.air){
            return tile.wallDrop();
        }else{
            return tile.drop();
        }
    }

    public static String round(float f){
        if(f >= 1_000_000_000){
            return Strings.autoFixed(f / 1_000_000_000, 1) + UI.billions;
        }else if(f >= 1_000_000){
            return Strings.autoFixed(f / 1_000_000, 1) + UI.millions;
        }else if(f >= 1000){
            return Strings.autoFixed(f / 1000, 1) + UI.thousands;
        }else{
            return Strings.autoFixed(f, 2);
        }
    }

    public static void uhOhSpeghettiOh(String ohno){
        throw new RuntimeException(ohno);
    }
}
