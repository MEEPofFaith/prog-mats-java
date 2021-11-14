package progressed.world.blocks.storage;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class CoreLink extends Block{
    public CoreLink(String name){
        super(name);
        hasItems = true;
        solid = true;
        update = true;
        destructible = true;
        separateItemCapacity = true;
        group = BlockGroup.transportation;
        flags = EnumSet.of(BlockFlag.storage);
        //allowResupply = true;
        //will be false until my dynamic resupply pr is accepted
        envEnabled = Env.any;
        highUnloadPriority = true;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    public class LinkBuild extends Building{
        public Building linkedCore;

        @Override
        public void created(){
            super.created();

            linkedCore = team.core();
        }

        @Override
        public void updateTile(){
            if(linkedCore == null){
                linkedCore = team.core();
            }

            if(linkedCore != null){
                items = linkedCore.items;
            }
        }

        @Override
        public boolean consValid(){
            return super.consValid() && (power == null || power.status >= 1);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return consValid() && linkedCore != null && linkedCore.acceptItem(source, item);
        }

        @Override
        public void handleItem(Building source, Item item){
            if(linkedCore != null){
                if(linkedCore.items.get(item) >= ((CoreBuild)linkedCore).storageCapacity){
                    if(Mathf.chance(0.3)){
                        Fx.coreBurn.at(this);
                    }
                }
                ((CoreBuild)linkedCore).noEffect = true;
                linkedCore.handleItem(source, item);
            }
        }

        @Override
        public boolean canUnload(){
            return super.canUnload() && consValid();
        }

        @Override
        public void itemTaken(Item item){
            if(linkedCore != null){
                linkedCore.itemTaken(item);
            }
        }

        @Override
        public int removeStack(Item item, int amount){
            if(linkedCore != null && team == state.rules.defaultTeam && state.isCampaign()){
                int result = super.removeStack(item, amount);
                state.rules.sector.info.handleCoreItem(item, -result);
                return result;
            }

            return 0;
        }

        @Override
        public int getMaximumAccepted(Item item){
            return linkedCore != null ? linkedCore.getMaximumAccepted(item) : 0;
        }

        @Override
        public int explosionItemCap(){
            //Items are teleported in from the core, the link itself does not contain items.
            return 0;
        }

        @Override
        public void drawSelect(){
            //outlines around core
            if(linkedCore != null){
                linkedCore.drawSelect();
            }

            //outlines around self
            Draw.color(Pal.darkMetal, Pal.accent, efficiency());
            for(int i = 0; i < 4; i++){
                Point2 p = Geometry.d8edge[i];
                float offset = -Math.max(block.size - 1, 0) / 2f * tilesize;
                Draw.rect("block-select", x + offset * p.x, y + offset * p.y, i * 90);
            }
        }

        @Override
        public void overwrote(Seq<Building> previous){
            //only add prev items when core is not linked
            if(linkedCore == null){
                for(Building other : previous){
                    if(other.items != null && other.items != items){
                        items.add(other.items);
                    }
                }

                items.each((i, a) -> items.set(i, Math.min(a, itemCapacity)));
            }
        }

        @Override
        public boolean canPickup(){
            return false;
        }
    }
}