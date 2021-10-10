package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;

public class LiquidDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;
    public float constructTime = 60f;

    public TextureRegion liquidRegion, tankBase, tankTop, tankFull;
    public TextureRegion[] tankRegions = new TextureRegion[3];

    public LiquidDroneStation(String name){
        super(name);

        hasItems = false;
        hasLiquids = true;
        liquidCapacity = 300;
        outputsLiquid = true;
        selectColor = Liquids.cryofluid.color;
    }

    @Override
    public void load(){
        super.load();

        tankBase = Core.atlas.find("prog-mats-liquid-cargo-bottom");
        tankTop = Core.atlas.find("prog-mats-liquid-cargo-top");
        tankFull = Core.atlas.find("prog-mats-liquid-cargo-full");
        liquidRegion = Core.atlas.find("prog-mats-liquid-cargo-liquid");

        tankRegions[0] = tankBase;
        tankRegions[1] = Core.atlas.find("prog-mats-liquid-cargo-decal");
        tankRegions[2] = tankTop;
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, tankFull};
    }

    public class LiquidDroneStationBuild extends DroneStationBuild{
        public boolean constructing, open;
        public float build;

        @Override
        public void updateTile(){
            updateLoading();

            if(liquids.total() > 0.01f){
                dumpLiquid(liquids.current());
            }

            if(!loading){
                if(isOrigin()){
                    build = Mathf.approach(build, constructTime, edelta());
                    constructing = build < constructTime;
                }else{
                    if(liquids.total() <= 0.01f){
                        build = Mathf.approach(build, 0f, edelta());
                        constructing = build > 0f;
                    }
                }
            }
            open = isOrigin() ? build >= constructTime : build <= 0;
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            super.loadCargo(d);
            d.cargo.load(new LiquidStack(liquids.current(), liquids.currentAmount()));
            d.cargo.liquidCapacity = liquidCapacity;
            liquids.clear();
            build = 0f;
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            super.takeCargo(d);
            if(d.cargo.hasLiquid()) liquids.add(d.cargo.liquidCargo.liquid, d.cargo.liquidCargo.amount);
            d.cargo.empty();
            build = constructTime;
        }

        @Override
        public boolean ready(){
            return active || connected && open && (isOrigin() ? liquids.total() >= liquidCapacity * transportThreshold : liquids.total() <= liquidCapacity);
        }

        @Override
        public void draw(){
            super.draw();

            if(constructing){
                Draw.draw(Layer.blockBuilding, () -> {
                    Draw.color(isOrigin() ? Pal.accent : Pal.remove);
                    for(TextureRegion region : tankRegions){
                        Shaders.blockbuild.region = region;
                        Shaders.blockbuild.progress = build / constructTime;

                        Draw.rect(region, x, y, 0);
                        Draw.flush();
                    }
                });
            }

            if(build >= constructTime){
                Draw.z(loading ? (lowFlier ? Layer.flyingUnitLow : Layer.flyingUnit) - 1 : Layer.blockOver);
                Draw.rect(tankBase, x + loadVector.x, y + loadVector.y);

                if(liquids.total() > 0.01f){
                    Drawf.liquid(liquidRegion, x + loadVector.x, y + loadVector.y, liquids.total() / liquidCapacity, liquids.current().color);
                }

                Draw.rect(tankTop, x + loadVector.x, y + loadVector.y);
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return isOrigin() && (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && !loading && !constructing;
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);

            table.button(Icon.trash, () -> liquids.clear()).tooltip("@pm-drone-dump-liquid").size(40);
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