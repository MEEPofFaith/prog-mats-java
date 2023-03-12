package progressed.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import progressed.content.*;

import static mindustry.Vars.*;

public class UnitMinerDepot extends Block{
    public UnitType unitType = PMUnitTypes.draug;
    public float buildTime = 60f * 8f;

    public UnitMinerDepot(String name){
        super(name);

        solid = true;
        update = true;
        hasItems = true;
        configurable = true;
        clearOnDoubleTap = true;
        itemCapacity = 200;
        ambientSound = Sounds.respawning;
        flags = EnumSet.of(BlockFlag.drill); //Technically

        config(Item.class, (UnitMinerDepotBuild build, Item item) -> build.targetItem = item);
        configClear((UnitMinerDepotBuild build) -> build.targetItem = null);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("units", (UnitMinerDepotBuild e) -> new Bar(
            () ->
                Core.bundle.format("bar.unitcap",
                    Fonts.getUnicodeStr(unitType.name),
                    e.team.data().countType(unitType),
                    Units.getStringCap(e.team)
                ),
            () -> Pal.power,
            () -> (float)e.team.data().countType(unitType) / Units.getCap(e.team)
        ));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return super.canPlaceOn(tile, team, rotation) && Units.canCreate(team, unitType);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        if(!Units.canCreate(player.team(), unitType)){
            drawPlaceText(Core.bundle.get("bar.cargounitcap"), x, y, valid);
        }
    }

    public class UnitMinerDepotBuild extends Building{
        //needs to be "unboxed" after reading, since units are read after buildings.
        public int readUnitId = -1;
        public float buildProgress, totalProgress;
        public float warmup, readyness;
        public Unit unit;

        public Item targetItem;
        public ObjectMap<Item, Tile> oreTiles = new ObjectMap<>();
        public boolean oresFound;

        @Override
        public void updateTile(){
            //unit was lost/destroyed
            if(unit != null && (unit.dead || !unit.isAdded())){
                unit = null;
            }

            if(readUnitId != -1){
                unit = Groups.unit.getByID(readUnitId);
                if(unit != null || !net.client()){
                    readUnitId = -1;
                }
            }

            warmup = Mathf.approachDelta(warmup, efficiency, 1f / 60f);
            readyness = Mathf.approachDelta(readyness, unit != null ? 1f : 0f, 1f / 60f);

            if(!oresFound){
                oresFound = true;
                unitType.mineItems.each(i -> {
                    if(!oreTiles.containsKey(i)) oreTiles.put(i, indexer.findClosestOre(x, y, i));
                });
            }

            if(unit == null && Units.canCreate(team, unitType)){
                buildProgress += edelta() / buildTime;
                totalProgress += edelta();

                if(buildProgress >= 1f){
                    if(!net.client()){
                        unit = unitType.create(team);
                        if(unit instanceof BuildingTetherc bt){
                            bt.building(this);
                        }
                        unit.set(x, y);
                        unit.rotation = 90f;
                        unit.add();
                        //Call.cargoLoaderDroneSpawned(tile, unit.id);
                        spawned(unit.id); //TODO custom call
                    }
                }
            }

            dump();
        }

        public void spawned(int id){
            Fx.spawn.at(x, y);
            buildProgress = 0f;
            if(net.client()){
                readUnitId = id;
            }
        }

        @Override
        public boolean shouldConsume(){
            return unit == null;
        }

        @Override
        public boolean shouldActiveSound(){
            return shouldConsume() && warmup > 0.01f;
        }

        @Override
        public void draw(){
            Draw.rect(block.region, x, y);
            if(unit == null){
                Draw.draw(Layer.blockOver, () -> {
                    Drawf.construct(this, unitType.fullIcon, 0f, buildProgress, warmup, totalProgress);
                });
            }
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        @Override
        public float progress(){
            return buildProgress;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return Math.min(itemCapacity - items.total(), amount);
        }

        @Override
        public void buildConfiguration(Table table){
            Seq<Item> targets = unitType.mineItems.select(i -> indexer.hasOre(i));
            ItemSelection.buildTable(UnitMinerDepot.this, table, targets, () -> targetItem, this::configure);
        }

        @Override
        public Object config(){
            return targetItem;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(unit == null ? -1 : unit.id);
            TypeIO.writeItem(write, targetItem);

            write.i(oreTiles.size);
            for(var entry : oreTiles.entries()){
                write.s(entry.key.id);
                write.i(entry.value == null ? -1 : entry.value.pos());
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            readUnitId = read.i();
            targetItem = TypeIO.readItem(read);

            int size = read.i();
            for(int i = 0; i < size; i++){
                Item item = content.item(read.s());
                int pos = read.i();
                Tile ore = pos != -1 ? world.tile(pos) : null;
                if(item != null && ore != null) oreTiles.put(item, ore);
            }
        }
    }
}
