package progressed.world.blocks.power;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class BeamDiode extends Block{
    public int range = 5;

    public Color laserColor1 = Color.white;
    public Color laserColor2 = Color.valueOf("ffd9c2");
    public float pulseScl = 7, pulseMag = 0.05f;
    public float laserWidth = 0.4f;

    public TextureRegion laser, arrow;
    public TextureRegion[] laserEnds = new TextureRegion[2];

    public BeamDiode(String name){
        super(name);
        rotate = true;
        update = true;
        solid = true;
        insulated = true;
        group = BlockGroup.power;
        noUpdateDisabled = true;
        schematicPriority = 10;
        envEnabled |= Env.space;
    }

    @Override
    public void load(){
        super.load();

        arrow = Core.atlas.find(name + "-arrow");
        laser = Core.atlas.find(name + "-beam", "power-beam");
        laserEnds[0] = Core.atlas.find(name + "-beam-end-out", "power-beam-end");
        laserEnds[1] = Core.atlas.find(name + "-beam-end-in", "power-beam-end");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.powerRange, range, StatUnit.blocks);
    }

    @Override
    public void init(){
        super.init();

        updateClipRadius((range + 1) * tilesize);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("back", (BeamDiodeBuild entity) -> new Bar("bar.input", Pal.powerBar, () -> bar(entity.links[1])));
        addBar("front", (BeamDiodeBuild entity) -> new Bar("bar.output", Pal.powerBar, () -> bar(entity.links[0])));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(fullIcon, plan.drawx(), plan.drawy());
        Draw.rect(arrow, plan.drawx(), plan.drawy(), !rotate ? 0 : plan.rotation * 90);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        for(int i = 0; i < 2; i++){
            int maxLen = range + size/2;
            Building dest = null;
            var dir = Geometry.d4[Mathf.mod(rotation + 2 * i, 4)];
            int dx = dir.x, dy = dir.y;
            int offset = size/2;
            for(int j = 1 + offset; j <= range + offset; j++){
                var other = world.build(x + j * dir.x, y + j * dir.y);

                //hit insulated wall
                if(other != null && other.isInsulated()){
                    break;
                }

                if(other != null && other.block.hasPower && other.team == Vars.player.team() && !(other.block instanceof PowerNode)){
                    maxLen = j;
                    dest = other;
                    break;
                }
            }

            Drawf.dashLine(Pal.placing,
                x * tilesize + dx * (tilesize * size / 2f + 2),
                y * tilesize + dy * (tilesize * size / 2f + 2),
                x * tilesize + dx * (maxLen) * tilesize,
                y * tilesize + dy * (maxLen) * tilesize
            );

            if(dest != null){
                Drawf.square(dest.x, dest.y, dest.block.size * tilesize/2f + 2.5f, 0f);
            }
        }
    }

    // battery % of the graph on either side, defaults to zero
    public float bar(Building tile){
        return (tile != null && tile.block.hasPower) ? tile.power.graph.getLastPowerStored() / tile.power.graph.getTotalBatteryCapacity() : 0f;
    }

    public class BeamDiodeBuild extends Building{
        //current links, 0 in front, 1 behind
        public Building[] links = new Building[2];
        public Tile[] dests = new Tile[2];
        public int lastChange = -2;

        @Override
        public void updateTile(){
            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateDirections();
            }

            if(tile == null || links[0] == null || links[1] == null || !links[1].block.hasPower || !links[0].block.hasPower || links[1].team != team || links[0].team != team) return;

            PowerGraph backGraph = links[1].power.graph;
            PowerGraph frontGraph = links[0].power.graph;
            if(backGraph == frontGraph) return;

            // 0f - 1f of battery capacity in use
            float backStored = backGraph.getBatteryStored() / backGraph.getTotalBatteryCapacity();
            float frontStored = frontGraph.getBatteryStored() / frontGraph.getTotalBatteryCapacity();

            // try to send if the back side has more % capacity stored than the front side
            if(backStored > frontStored){
                // send half of the difference
                float amount = backGraph.getBatteryStored() * (backStored - frontStored) / 2;
                // prevent sending more than the front can handle
                amount = Mathf.clamp(amount, 0, frontGraph.getTotalBatteryCapacity() * (1 - frontStored));

                backGraph.transferPower(-amount);
                frontGraph.transferPower(amount);
            }
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            updateDirections();
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y, 0);
            Draw.rect(arrow, x, y, rotate ? rotdeg() : 0);

            if(Mathf.zero(Renderer.laserOpacity)) return;

            Draw.z(Layer.power);
            Draw.alpha(Renderer.laserOpacity);
            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for(int i = 0; i < 2; i++){
                if(dests[i] != null){
                    int dst = Math.max(Math.abs(dests[i].x - tile.x),  Math.abs(dests[i].y - tile.y));
                    //don't draw lasers for adjacent blocks
                    if(dst > 1 + size/2){
                        var point = Geometry.d4[Mathf.mod(rotation + 2 * i, 4)];
                        float poff = tilesize/2f;
                        Draw.color(laserColor1, laserColor2, (1f - links[i].power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
                        Drawf.laser(laser, laserEnds[1 - i], laserEnds[i], x + poff*size*point.x, y + poff*size*point.y, dests[i].worldx() - poff*point.x, dests[i].worldy() - poff*point.y, w);
                    }
                }
            }

            Draw.reset();
        }

        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }

        public void updateDirections(){
            for(int i = 0; i < 2; i ++){
                var dir = Geometry.d4[Mathf.mod(rotation + 2 * i, 4)];
                links[i] = null;
                dests[i] = null;
                int offset = size / 2;
                //find first block with power in range
                for(int j = 1 + offset; j <= range + offset; j++){
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    //hit insulated wall
                    if(other != null && other.isInsulated()){
                        break;
                    }

                    if(other != null && other.block.hasPower && other.team == team){
                        links[i] = other;
                        dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }
            }
        }
    }
}
