package progressed.world.blocks.storage;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class CoreLink extends Block{
    public float activationDelay = 60f;
    public float portalRad = -1f;
    public Effect activationEffect = PMFx.linkActivation;

    public CoreLink(String name){
        super(name);
        hasItems = true;
        solid = true;
        update = true;
        destructible = true;
        separateItemCapacity = true;
        group = BlockGroup.transportation;
        flags = EnumSet.of(BlockFlag.storage);
        //Will be false until my dynamic resupply pr is accepted, so units don't take from this when it doesn't have the power to support item transfer.
        //allowResupply = true;
        envEnabled = Env.any;
        highUnloadPriority = true;
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        if(portalRad < 0) portalRad = size * tilesize / 2f * 0.625f;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    public class LinkBuild extends Building{
        public Building linkedCore;
        public boolean activated;
        public float activationTime;

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

            activationTime += Time.delta * Mathf.sign(consValid());
            activationTime = Mathf.clamp(activationTime, 0f, activationDelay);

            if(!activated && isActive()){
                activated = true;
                activationEffect.at(x, y, team.color);
            }
            if(activated && !isActive()) activated = false;
        }

        @Override
        public void draw(){
            super.draw();

            if(activef() > 0.01f){
                Draw.z(Layer.bullet - 0.0001f);

                float rad = portalRad * Interp.pow2Out.apply(activef());
                float smallScl = 0.75f;
                float drift = 60f;
                Draw.color(team.color);
                PMDrawf.shiningCircle(id, Time.time,
                    x, y, rad,
                    3, 120f,
                    rad * 2f, rad,
                    drift
                );

                Draw.color(Color.black);
                PMDrawf.shiningCircle(id, Time.time,
                    x, y, rad * smallScl,
                    3, 120f,
                    rad * smallScl * 2f, rad * smallScl,
                    drift
                );
            }
        }

        @Override
        public boolean consValid(){
            return super.consValid() && (power == null || power.status >= 1);
        }

        public boolean isActive(){
            return activationTime >= activationDelay;
        }

        public float activef(){
            return Mathf.clamp(activationTime / activationDelay);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return consValid() && isActive() && linkedCore != null && linkedCore.acceptItem(source, item);
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
            return super.canUnload() && consValid() && isActive();
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
            Draw.color(Pal.darkMetal, Pal.accent, activef());
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