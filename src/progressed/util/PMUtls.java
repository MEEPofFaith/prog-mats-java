package progressed.util;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.entities.bullet.energy.*;

public class PMUtls{
    public static final Rand rand = new Rand();
    
    public static float bulletDamage(BulletType b, float lifetime){
        float damage = b.damage + b.splashDamage; //Base Damage

        damage += b.lightningDamage * b.lightning * b.lightningLength; //Lightning Damage

        if(b.fragBullet != null){
            damage += bulletDamage(b.fragBullet, b.fragBullet.lifetime) * b.fragBullets; //Frag Bullet Damage
        }

        if(b instanceof ContinuousLaserBulletType){ //Continuous Damage
            return damage * lifetime / 5f;
        }else if(b instanceof BlackHoleBulletType){
            return damage * lifetime / 2f;
        }else{
            return damage;
        }
    }

    public static ItemStack[] randomizedItems(int[] repeatAmounts, int minAmount, int maxAmount){
        Seq<ItemStack> stacks = new Seq<>();

        Vars.content.items().each(item -> {
            int repeats = repeatAmounts[Mathf.random(repeatAmounts.length - 1)];
            if(repeats > 0){
                for(int i = 0; i < repeats; i++){
                    stacks.add(new ItemStack(item, Mathf.random(minAmount, maxAmount)));
                }
            }
        });

        stacks.shuffle();
        return stacks.toArray(ItemStack.class);
    }
    
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

    public static ItemStack[] researchRequirements(ItemStack[] requirements){
        return researchRequirements(requirements, 1f);
    }

    /** Adds ItemStack arrayws together. Combines duplicate items into one stack. */
    public static ItemStack[] addItemStacks(ItemStack[][] stacks){
        Seq<ItemStack> rawStacks = new Seq<>();
        for(ItemStack[] arr : stacks){
            for(ItemStack stack : arr){
                rawStacks.add(stack);
            }
        }
        Seq<Item> items = new Seq<>();
        IntSeq amounts = new IntSeq();
        rawStacks.each(s -> {
            if(!items.contains(s.item)){
                items.add(s.item);
                amounts.add(s.amount);
            }else{
                int index = items.indexOf(s.item);
                amounts.incr(index, s.amount);
            }
        });
        ItemStack[] result = new ItemStack[items.size];
        items.each(i -> {
            int index = items.indexOf(i);
            result[index] = new ItemStack(i, amounts.get(index));
        });
        return result;
    }

    public static float equalArcLen(float r1, float r2, float length){
        return (r1 / r2) * length;
    }

    public static int boolArrToInt(boolean[] arr){
        int i = 0;
        for(boolean value : arr){
            if(value) i++;
        }
        return i;
    }

    public static float moveToward(float from, float to, float speed, float min, float max){
        float target = Mathf.clamp(to, min, max);
        if(Math.abs(target - from) < speed) return target;
        if(from > target){
            return from - speed;
        }
        if(from < target){
            return from + speed;
        }

        return from;
    }

    public static float multiLerp(float[] values, float progress){ //No idea how this works, just stole it from Color
        int l = values.length;
        float s = Mathf.clamp(progress);
        float a = values[(int)(s * (l - 1))];
        float b = values[Mathf.clamp((int)(s * (l - 1) + 1), 0, l - 1)];

        float n = s * (l - 1) - (int)(s * (l - 1));
        float i = 1f - n;
        return a * i + b * n;
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

    public static void uhOhSpeghettiOh(String ohno){
        throw new RuntimeException(ohno);
    }
}
