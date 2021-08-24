package progressed.world.blocks.defence.turret.apotheosis;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.apotheosis.ApotheosisNexus.*;

import static mindustry.Vars.*;

public class ApotheosisChargeTower extends Block{
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

        config(Integer.class, (ApotheosisChargeTowerBuild tile, Integer value) -> {
            Building other = world.build(value);
            int pos = tile.pos();
            if(other instanceof ApotheosisNexusBuild o){
                if(o.chargers.contains(pos)){
                    o.chargers.removeValue(pos);
                    tile.nexus = null;
                }else{
                    if(tile.nexus != null){
                        tile.nexus.chargers.removeValue(pos);
                    }
                    o.chargers.add(pos);
                    tile.nexus = o;
                }
            }
        });

        configClear((ApotheosisChargeTowerBuild tile) -> {
           if(tile.nexus != null){
               tile.nexus.chargers.removeValue(tile.pos());
               tile.nexus = null;
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

    public class ApotheosisChargeTowerBuild extends Building{
        public ApotheosisNexusBuild nexus;

        public boolean isActive(){
            return nexus != null && nexus.isActive();
        }

        @Override
        public void draw(){
            super.draw();
            if(nexus != null) Lines.line(x, y, nexus.x, nexus.y);
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            Drawf.circles(x, y, range * tilesize);

            for(int x = (int)(tile.x - range - 2); x <= tile.x + range + 2; x++){
                for(int y = (int)(tile.y - range - 2); y <= tile.y + range + 2; y++){
                    Building link = world.build(x, y);

                    if(link instanceof ApotheosisNexusBuild){
                        if(nexus == link){
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
            if(nexus != null && !nexus.added) nexus = null;
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
    }
}