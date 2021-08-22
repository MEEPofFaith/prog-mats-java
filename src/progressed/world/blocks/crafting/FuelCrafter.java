package progressed.world.blocks.crafting;

import arc.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;
import progressed.world.meta.*;

/** A smelter uses fuel to craft. Attribute tiles make it use less fuel. */
public class FuelCrafter extends GenericCrafter{
    public Attribute attribute = Attribute.heat;
    public Item fuelItem;
    public int fuelPerItem = 10, fuelPerCraft = -1;
    public int fuelCapacity = 30;
    /** 1 affinity = this amount removed from fuel use */
    public float fuelUseReduction = 1.5f;

    public FuelCrafter(String name){
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        int amount = Math.max(fuelPerCraft - Mathf.round(sumAttribute(attribute, x, y) * fuelUseReduction), 0);
        drawPlaceText(Core.bundle.format("bar.pm-fuel-use",
            amount > 0 ? amount : Core.bundle.get("bar.pm-fuel-unneeded")), x, y, valid);
    }

    @Override
    public void init(){
        if(fuelItem == null){
            throw new RuntimeException(name + " doesn't have a fuel item!");
        }

        if(fuelPerCraft < 0) fuelPerCraft = fuelPerItem;

        super.init();
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("pm-fuel", (FuelCrafterBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-fuel", entity.fuelNeeded() > 0 ? entity.fuel : Core.bundle.get("bar.pm-fuel-unneeded"), entity.fuelNeeded() > 0 ? ("/" + entity.fuelNeeded()) : ""),
            () -> fuelItem.color,
            () -> entity.fuelNeeded() > 0 ? (float)entity.fuel / (float)fuelCapacity : 1f
        ));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.input);

        stats.add(Stat.input, PMStatValues.fuel(this));
    }

    public class FuelCrafterBuild extends GenericCrafterBuild{
        public int fuel;
        public float attrsum;

        @Override
        public boolean shouldConsume(){
            return fuel >= fuelNeeded() && super.shouldConsume();
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            attrsum = sumAttribute(attribute, tile.x, tile.y);
        }

        @Override
        public void consume(){
            fuel -= fuelNeeded();
            super.consume();
        }

        public int fuelNeeded(){
            return Math.max(fuelPerCraft - Mathf.round(attrsum * fuelUseReduction), 0);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) || item == fuelItem && fuel + fuelPerItem <= fuelCapacity && fuelNeeded() > 0f;
        }

        @Override
        public void handleItem(Building source, Item item){
            if(item == fuelItem && fuel + fuelPerItem <= fuelCapacity && fuelNeeded() > 0f){
                fuel += fuelPerItem;
                return;
            }

            super.handleItem(source, item);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(fuel);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            fuel = read.i();
        }
    }
}