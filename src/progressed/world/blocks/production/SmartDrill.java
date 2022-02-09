package progressed.world.blocks.production;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.production.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class SmartDrill extends Drill{
    public SmartDrill(String name){
        super(name);

        configurable = true;

        config(Item.class, (SmartDrillBuild tile, Item item) -> {
            countOre(tile.tile);
            tile.dominantItem = item;
            tile.dominantItems = oreCount.get(item);
        });
        configClear((SmartDrillBuild tile) -> tile.dominantItem = null);
    }

    public float drawWorldText(String text, float x, float y, boolean valid, boolean underline){
        if(renderer.pixelator.enabled()) return 0;

        Color color = valid ? Pal.accent : Pal.remove;
        return PMDrawf.text(x, y + size * tilesize / 2f + 3, color, text);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);

        Tile tile = world.tile(x, y);
        if(tile == null) return;

        countOre(tile);

        if(itemArray.any()){
            float items = itemArray.size - 1f;

            for(int i = 0; i < itemArray.size; i++){
                Item item = itemArray.get(i);
                int count = oreCount.get(item, 0);
                float mul = (items - i) / items,
                    tx = x * tilesize + offset,
                    ty = (y * tilesize + offset) + 8f * items * (items == 0 ? 0f : mul);
                float width = drawWorldText(Core.bundle.formatFloat(
                    "bar.drillspeed",
                    60f / (drillTime + hardnessDrillMultiplier * item.hardness) * count,
                    2
                ), tx, ty, valid, i == itemArray.size - 1);
                float dx = tx - width / 2f - 4f,
                    dy = ty + size * tilesize / 2f + 5,
                    s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(item.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(item.fullIcon, dx, dy, s, s);
            }
        }else{
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && t.drop().hardness > tier);
            Item item = to == null ? null : to.drop();
            if(item != null){
                drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, valid);
            }
        }
    }

    protected void countOre(Tile tile){
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for(Tile other : tile.getLinkedTilesAs(this, tempTiles)){
            if(canMine(other)){
                oreCount.increment(getDrop(other), 0, 1);
            }
        }

        for(Item item : oreCount.keys()){
            itemArray.add(item);
        }

        itemArray.sort(item -> -60f / (drillTime + hardnessDrillMultiplier * item.hardness) * oreCount.get(item, 0));

        if(itemArray.size == 0){
            return;
        }

        returnItem = itemArray.peek();
        returnCount = oreCount.get(itemArray.peek(), 0);
    }

    public class SmartDrillBuild extends DrillBuild{
        @Override
        public void placed(){
            super.placed();

            //Don't mine something you can't mine.
            countOre(tile);
            if(!itemArray.contains(i -> i == dominantItem)) configure(null);
        }

        @Override
        public void onProximityUpdate(){
            //no
        }

        @Override
        public void updateTile(){
            if(!isDrilling()){ //Literally the only thing changed from the original code. Wind down for deselecting.
                timeDrilled += warmup * delta();
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if(timer(timerDump, dumpTime)){
                dump(items.has(dominantItem) ? dominantItem : null);
            }

            timeDrilled += warmup * delta();

            if(items.total() < itemCapacity && dominantItems > 0 && consValid()){

                float speed = 1f;

                if(cons.optionalValid()){
                    speed = liquidBoostIntensity;
                }

                speed *= efficiency(); // Drill slower when not at full power

                lastDrillSpeed = (speed * dominantItems * warmup) / (drillTime + hardnessDrillMultiplier * dominantItem.hardness);
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;

                if(Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            float delay = drillTime + hardnessDrillMultiplier * dominantItem.hardness;

            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity){
                offload(dominantItem);

                progress %= delay;

                drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }

        public boolean isDrilling(){
            return dominantItem != null;
        }

        @Override
        public void buildConfiguration(Table table){
            countOre(tile);

            ItemSelection.buildTable(SmartDrill.this, table, itemArray, () -> dominantItem, this::configure);
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();

            countOre(tile);

            float items = itemArray.size - 1f;

            for(int i = 0; i < itemArray.size; i++){
                Item item = itemArray.get(i);
                int count = oreCount.get(item, 0);
                float mul = (items - i) / items,
                    ty = y + 8f * items * (items == 0 ? 0f : mul);
                float width = drawWorldText(Core.bundle.formatFloat(
                    "bar.drillspeed",
                    60f / (drillTime + hardnessDrillMultiplier * item.hardness) * count,
                    2
                ), x, ty, true, i == itemArray.size - 1);
                float dx = x - width / 2f - 4f,
                    dy = ty + size * tilesize / 2f + 5,
                    s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(item.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(item.fullIcon, dx, dy, s, s);
            }
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(this == other){
                deselect();
                configure(null);
                return false;
            }

            return true;
        }
    }
}
