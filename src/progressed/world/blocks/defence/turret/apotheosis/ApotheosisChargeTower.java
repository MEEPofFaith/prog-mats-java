package progressed.world.blocks.defence.turret.apotheosis;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.apotheosis.ApotheosisNexus.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class ApotheosisChargeTower extends Block{
    protected static BuildPlan otherReq;

    public float range = 10f;
    public float damageBoost, radiusBoost, speedBoost, durationBoost;
    public float powerUse = 1f;

    public ApotheosisChargeTower(String name){
        super(name);

        configurable = true;
        canOverdrive = false;
        update = true;
        hasPower = true;
        solid = true;
        group = BlockGroup.turrets;
        schematicPriority = -1;

        config(Integer.class, (ApotheosisChargeTowerBuild tile, Integer value) -> {
            Building other = world.build(value);
            int pos = tile.pos();
            if(other instanceof ApotheosisNexusBuild o && o.within(tile, range * tilesize)){
                if(o.chargers.contains(pos)){
                    o.chargers.removeValue(pos);
                    tile.nexus = -1;
                }else{
                    if(tile.getNexus() != null){
                        tile.getNexus().chargers.removeValue(pos);
                    }
                    o.chargers.add(pos);
                    tile.nexus = o.pos();
                }
                tile.connected = false;
            }
        });

        config(Point2.class, (tile, value) -> configurations.get(Integer.class).get(tile, Point2.pack(value.x +  + tile.tileX(), value.y +  + tile.tileY())));

        configClear((ApotheosisChargeTowerBuild tile) -> {
           if(tile.getNexus() != null){
               tile.getNexus().chargers.removeValue(tile.pos());
               tile.nexus = -1;
               tile.connected = false;
           }
        });
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisChargeTowerBuild::isActive);
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, range * tilesize);
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list){
        otherReq = null;
        list.each(other -> {
            if(other.block instanceof ApotheosisNexus && req != other && req.config instanceof Point2 p && p.equals(other.x - req.x, other.y - req.y)){
                otherReq = other;
            }
        });

        if(otherReq != null){
            Lines.line(req.drawx(), req.drawy(), otherReq.drawx(), otherReq.drawy());
        }
    }

    public class ApotheosisChargeTowerBuild extends Building{
        public int nexus;
        public boolean connected;

        public boolean isActive(){
            return connected && getNexus() != null && getNexus().isActive();
        }

        @Override
        public void draw(){
            super.draw();
            if(getNexus() != null){
                Draw.color(connected ? team.color : Color.red);
                Lines.line(x, y, getNexus().x, getNexus().y);
            }
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            Drawf.circles(x, y, range * tilesize);

            for(int x = (int)(tile.x - range - 2); x <= tile.x + range + 2; x++){
                for(int y = (int)(tile.y - range - 2); y <= tile.y + range + 2; y++){
                    Building link = world.build(x, y);

                    if(link instanceof ApotheosisNexusBuild){
                        if(getNexus() == link){
                            Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                        }
                    }
                }
            }

            Draw.reset();
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(getNexus() != null && !getNexus().added) nexus = -1;
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(self() == other){
                configure(null);
                return true;
            }else if(other instanceof ApotheosisNexusBuild){
                configure(other.pos());
                return false;
            }
            return true;
        }

        public ApotheosisNexusBuild getNexus(){
            return world.build(nexus) instanceof ApotheosisNexusBuild b ? b : null;
        }

        public float chargef(){
            return getNexus() != null && connected ? getNexus().chargef() : 0f;
        }

        @Override
        public void pickedUp(){
            super.pickedUp();
            configure(null);
        }

        @Override
        public Object config(){
            return Point2.unpack(nexus).sub(tile.x, tile.y);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(nexus);
            write.bool(connected);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            nexus = read.i();
            connected = read.bool();
        }
    }
}