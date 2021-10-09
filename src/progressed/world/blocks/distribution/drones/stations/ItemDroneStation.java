package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

public class ItemDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;
    public float constructTime = 60f;

    public TextureRegion containerRegion;

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

        containerRegion = Core.atlas.find("pm-item-cargo");
    }

    public class ItemDroneStationBuild extends DroneStationBuild{
        public boolean constructing;
        public float build, totalBuild, buildup;

        @Override
        public void updateTile(){
            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }

            if(isOrigin()){
                build += edelta();
                totalBuild += edelta();
                constructing = build < constructTime;
            }else{
                if(items.empty()){
                    build -= edelta();
                    totalBuild += edelta();
                }
                constructing = build > 0f;
            }
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
            build = 0f;
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            for(int i = 0; i < content.items().size; i++){
                items.add(content.items().get(i), d.cargo.itemCargo[i]);
            }
            d.cargo.empty();
            build = constructTime;
        }

        @Override
        public boolean ready(){
            return active || connected && !constructing && (isOrigin() ? items.total() >= itemCapacity * transportThreshold : items.total() <= itemCapacity);
        }

        @Override
        public void draw(){
            super.draw();

            if(buildup > 0.01){
                Draw.draw(Layer.blockOver, () -> {
                    Drawf.construct(x, y, containerRegion, team.color, 0f, build / constructTime, buildup, totalBuild);
                });
            }

            Draw.z(loading ? Layer.flyingUnit - 1 : Layer.blockOver);
            Draw.rect(containerRegion, x + loadVector.x, y + loadVector.y);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return isOrigin() && items.total() + 1 <= itemCapacity && !loading;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(build);
            write.bool(constructing);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            build = read.f();
            constructing = read.bool();
        }
    }
}