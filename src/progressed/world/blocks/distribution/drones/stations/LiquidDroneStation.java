package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.entities.units.entity.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class LiquidDroneStation extends DroneStation{
    public float transportThreshold = 0.25f;
    public float constructTime = 60f;
    public float outputFlow = -1f;
    public int loadSize = 2;

    public TextureRegion liquidRegion, tankBase, tankTop, tankFull;
    public TextureRegion[] tankRegions = new TextureRegion[3];

    public LiquidDroneStation(String name){
        super(name);

        hasItems = false;
        hasLiquids = true;
        outputsLiquid = true;
        selectColor = Liquids.cryofluid.color;
        defName = "Liquid";
    }

    @Override
    public void init(){
        super.init();

        if(outputFlow < 0) outputFlow = liquidCapacity / 2f;
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
            super.updateTile();

            if(liquids.total() > 0.01f){
                dumpLiquid(liquids.current(), 1f);
                if(dumping && liquids.total() <= 0.01f){
                    dumping = false;
                }
            }

            if(!arrived){
                if(isOrigin()){
                    build = Mathf.approach(build, constructTime, edelta());
                    constructing = build < constructTime;
                }else if(liquids.total() <= 0.01f){
                    build = Mathf.approach(build, 0f, edelta());
                    constructing = build > 0f;
                }
            }
            open = isOrigin() ? build >= constructTime : build <= 0;
        }

        @Override
        public void dumpLiquid(Liquid liquid, float scaling){
            int dump = this.cdump;

            if(liquids.get(liquid) <= 0.0001f) return;

            if(!net.client() && state.isCampaign() && team == state.rules.defaultTeam) liquid.unlock();

            for(int i = 0; i < proximity.size; i++){
                incrementDump(proximity.size);
                Building other = proximity.get((i + dump) % proximity.size);
                other = other.getLiquidDestination(self(), liquid);

                if(other != null && other.team == team && other.block.hasLiquids && canDumpLiquid(other, liquid) && other.liquids != null){
                    transferLiquid(other, Math.min(liquids.get(liquid), outputFlow), liquid);
                }
            }
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            d.cargo.load(new LiquidStack(liquids.current(), liquids.currentAmount()));
            d.cargo.liquidCapacity = liquidCapacity;
            liquids.clear();
            build = 0f;
            constructing = true;
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            if(d.cargo.hasLiquid()) liquids.add(d.cargo.liquidCargo.liquid, d.cargo.liquidCargo.amount);
            d.cargo.empty();
        }

        @Override
        public void setLoading(DroneUnitEntity d){
            if(!arrived && !isOrigin()){
                build = constructTime;
            }
            super.setLoading(d);
        }

        @Override
        public boolean canDumpLiquid(Building to, Liquid liquid){
            return (!isOrigin() || dumping) && !loading;
        }

        @Override
        public boolean ready(){
            return active || connected && open && (isOrigin() ? liquids.total() >= liquidCapacity * transportThreshold : liquids.total() <= liquidCapacity);
        }

        @Override
        public void draw(){
            super.draw();

            float progress = build / constructTime;
            if(constructing){
                Draw.draw(Layer.blockBuilding, () -> {
                    for(TextureRegion region : tankRegions){
                        PMDrawf.blockBuild(x, y, region, isOrigin() ? Pal.accent : Pal.remove, 0, progress);
                    }
                });
            }


            Draw.z(loading ? (lowFlier ? Layer.flyingUnitLow : Layer.flyingUnit) - 1 : Layer.blockOver);
            if(progress > 0.01f) Drawf.shadow(x + loadVector.x, y + loadVector.y, loadSize * tilesize * 2f, progress);
            if(build >= constructTime){
                Draw.rect(tankBase, x + loadVector.x, y + loadVector.y);

                if(liquids.total() > 0.01f){
                    Drawf.liquid(liquidRegion, x + loadVector.x, y + loadVector.y, liquids.total() / liquidCapacity, liquids.current().color);
                }

                Draw.rect(tankTop, x + loadVector.x, y + loadVector.y);
            }
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return isOrigin() && (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && !loading && !constructing && !dumping;
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
