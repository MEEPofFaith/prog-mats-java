package progressed.world.blocks.storage;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class CoreLink extends Block{
    public float activationDelay = 60f;
    public float portalRad = -1f, innerRadScl = 0.75f;
    public int clouds;
    public float minCloudSize = 1f, maxCloudSize = 1f;
    public Effect activationEffect = OtherFx.linkActivation;

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
        canOverdrive = false;
    }

    @Override
    public void init(){
        super.init();

        if(portalRad < 0) portalRad = size * tilesize / 2f * 0.625f;
    }

    @Override
    protected TextureRegion[] icons(){
        return teamRegion.found() ? new TextureRegion[]{region, teamRegions[Team.sharded.id]} : new TextureRegion[]{region};
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.itemCapacity);
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    public class LinkBuild extends Building{
        public Building linkedCore;
        public boolean activated, usePower;
        public float activationTime;

        @Override
        public void created(){
            super.created();

            linkedCore = team.core();
            if(linkedCore != null) items = linkedCore.items;
        }

        @Override
        public void updateTile(){
            if(linkedCore == null) linkedCore = team.core();
            if(linkedCore != null) items = linkedCore.items;

            ConsumePower cPower = findConsumer(c -> c instanceof ConsumePower);
            if(cPower != null){
                float u = cPower.usage * Time.delta;
                usePower = power.graph.getBatteryStored() >= u || power.graph.getPowerBalance() >= u;
            }else{
                usePower = true;
            }

            activationTime += Time.delta * Mathf.sign(shouldConsume());
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

                float scl = Interp.pow2Out.apply(activef());
                float rad = portalRad * scl;
                Draw.color(team.color);
                if(clouds > 0){
                    for(int i = 0; i < clouds; i++){
                        float angle = Time.time * 4f + Mathf.randomSeed((id + i) * 37L, 360f);
                        float cRad = Mathf.randomSeed((id + i) * 52L, minCloudSize, maxCloudSize) * scl;
                        Tmp.v1.trns(angle, rad).add(x, y);
                        Fill.circle(Tmp.v1.x, Tmp.v1.y, cRad);
                    }
                }
                PMDrawf.spinningCircle(
                    id, Time.time * 3f, Time.time,
                    x, y, rad,
                    3, 60f, 30f,
                    20f, rad * 1.125f, -40f
                );
                PMDrawf.spinningCircle(
                    id * 2, Time.time * 6f, Time.time,
                    x, y, rad,
                    3, 60f, 20f,
                    30f, rad * 1.25f, -60f
                );

                Draw.color(Color.black);
                Fill.circle(x, y, rad * innerRadScl);
            }
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume() && usePower;
        }

        public boolean isActive(){
            return activationTime >= activationDelay;
        }

        public float activef(){
            return Mathf.clamp(activationTime / activationDelay);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return isActive() && linkedCore != null && linkedCore.acceptItem(source, item);
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
            return super.canUnload() && linkedCore != null && canConsume() && isActive();
        }

        @Override
        public void itemTaken(Item item){
            if(linkedCore != null){
                linkedCore.itemTaken(item);
            }
        }

        @Override
        public int removeStack(Item item, int amount){
            if(linkedCore != null){
                int result = super.removeStack(item, amount);

                if(team == state.rules.defaultTeam && state.isCampaign()){
                    state.rules.sector.info.handleCoreItem(item, -result);
                }

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
        public boolean canPickup(){
            return false;
        }
    }
}
