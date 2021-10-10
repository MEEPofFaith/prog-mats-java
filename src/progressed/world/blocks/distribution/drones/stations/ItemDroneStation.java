package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

public class ItemDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;
    public float constructTime = 60f;

    public TextureRegion container;

    public ItemDroneStation(String name){
        super(name);

        hasLiquids = false;
        hasItems = true;
        itemCapacity = 100;
        selectColor = Color.yellow;
    }

    @Override
    public void load(){
        super.load();

        container = Core.atlas.find("prog-mats-item-cargo");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, container};
    }

    public class ItemDroneStationBuild extends DroneStationBuild{
        public boolean constructing, open;
        public float build, totalBuild, buildup;

        @Override
        public void updateTile(){
            updateLoading();

            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }

            if(!loading){
                if(isOrigin()){
                    build = Mathf.approach(build, constructTime, edelta());
                    constructing = build < constructTime;
                }else{
                    if(items.empty()){
                        build = Mathf.approach(build, 0f, edelta());
                        constructing = build > 0f;
                    }
                }
                totalBuild += edelta() * buildup;
            }
            open = isOrigin() ? build >= constructTime : build <= 0;
            buildup = Mathf.lerpDelta(buildup, Mathf.num(constructing), 0.15f);
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            super.loadCargo(d);
            int[] it = new int[content.items().size];
            for(int i = 0; i < content.items().size; i++){
                it[i] = items.get(content.items().get(i));
            }
            d.cargo.load(it);
            items.clear();
            build = 0;
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            super.takeCargo(d);
            for(int i = 0; i < content.items().size; i++){
                items.add(content.items().get(i), d.cargo.itemCargo[i]);
            }
            d.cargo.empty();
            build = constructTime;
        }

        @Override
        public boolean ready(){
            return active || connected && open && (isOrigin() ? items.total() >= itemCapacity * transportThreshold : items.total() <= itemCapacity);
        }

        @Override
        public void draw(){
            super.draw();

            if(buildup > 0.01){
                Draw.draw(Layer.blockOver + 1, () -> {
                    Drawf.construct(x, y, container, isOrigin() ? Pal.accent : Pal.remove, 0f, Math.max(build / constructTime, 0.02f), buildup, totalBuild);
                });
            }

            if(build >= constructTime){
                Draw.z(loading ? (lowFlier ? Layer.flyingUnitLow : Layer.flyingUnit) - 1 : Layer.blockOver);
                Draw.rect(container, x + loadVector.x, y + loadVector.y);
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return isOrigin() && items.total() + 1 <= itemCapacity && !loading && !constructing;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(build);
            write.bool(constructing);
            write.bool(open);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            build = read.f();
            constructing = read.bool();
            open = read.bool();
        }
    }
}