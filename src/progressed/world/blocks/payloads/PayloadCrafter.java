package progressed.world.blocks.payloads;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.ui.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

public class PayloadCrafter extends BlockProducer{
    private float scrollPos;

    public Seq<Missile> products;
    public boolean hasTop = true, build = true;

    public int[] capacities = {};

    public PayloadCrafter(String name){
        super(name);

        configurable = logicConfigurable = true;

        config(Block.class, (PayloadCrafterBuild tile, Block block) -> {
            if(tile.recipe != block) tile.progress = 0f;
            if(canProduce(block)){
                tile.recipe = block;
            }
        });

        configClear((PayloadCrafterBuild tile) -> {
            tile.recipe = null;
            tile.progress = 0f;
        });
    }

    @Override
    public void init(){
        consumes.add(new DynamicConsumePower());

        capacities = new int[content.items().size];
        products.each(r -> {
            for(ItemStack stack : r.requirements){
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        });

        if(products.contains(m -> m.prev != null)) acceptsPayload = true;

        super.init();
    }

    @Override
    public void load(){
        super.load();

        inRegion = Core.atlas.find(name + "-in", Core.atlas.find("factory-in-" + size, "prog-mats-factory-in-" + size));
        outRegion = Core.atlas.find(name + "-out", Core.atlas.find("factory-out-" + size, "prog-mats-factory-out-" + size));
        if(!hasTop) topRegion = Core.atlas.find("clear");
    }

    @Override
    public TextureRegion[] icons(){
        if(products.contains(b -> b.prev != null)){
            return new TextureRegion[]{region, inRegion, outRegion, topRegion};
        }
        return new TextureRegion[]{region, outRegion, topRegion};
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.powerUse);

        stats.add(Stat.input, PMStatValues.payloadProducts(products));
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.remove("progress");
        bars.add("progress", (PayloadCrafterBuild entity) -> new Bar("bar.progress", Pal.ammo, () -> entity.recipe() == null ? 0f : (entity.progress / ((Missile)(entity.recipe())).constructTime)));
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(region, req.drawx(), req.drawy());
        if(products.contains(b -> b.prev != null)) Draw.rect(inRegion, req.drawx(), req.drawy(), req.rotation * 90);
        Draw.rect(outRegion, req.drawx(), req.drawy(), req.rotation * 90);
        Draw.rect(topRegion, req.drawx(), req.drawy());
    }

    public boolean canProduce(Block b){
        if(b instanceof Missile m){
            return (!m.requiresUnlock || m.unlockedNow()) && products.contains(m);
        }
        return false;
    }

    public class PayloadCrafterBuild extends BlockProducerBuild{
        public @Nullable Block recipe;
        public boolean produce;

        @Override
        public @Nullable Block recipe(){
            return recipe;
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config) return recipe;
            if(sensor == LAccess.progress) return progress;
            return super.senseObject(sensor);
        }

        @Override
        public void updateTile(){
            var recipe = recipe();
            produce = recipe instanceof Missile m && consValid() && (m.prev != null ? (payload != null && hasArrived() && payload.block() == m.prev) : payload == null);

            if(recipe instanceof Missile m){
                if(payload != null && payload.block() != m.prev){
                    moveOutPayload();
                }
            }else if(payload != null && !products.contains(b -> b.prev == payload.block())){
                moveOutPayload();
            }

            if(recipe instanceof Missile m && m.prev != null && payload != null && payload.block() != m){
                moveInPayload(false);
            }

            if(produce){
                progress += edelta();

                Missile m = (Missile)recipe;
                if(progress >= m.constructTime){
                    consume();
                    payload = new BuildPayload(recipe, team);
                    payVector.setZero();
                    progress %= 1f;
                }
            }else if(recipe == null || !consValid()){
                progress = 0f;
            }

            heat = Mathf.lerpDelta(heat, Mathf.num(produce), 0.15f);
            time += heat * delta();
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            if(curInput()){
                for(int i = 0; i < 4; i++){
                    if(blends(i) && i != rotation){
                        Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                    }
                }
            }

            Draw.rect(outRegion, x, y, rotdeg());

            if(recipe != null){
                Draw.draw(Layer.blockBuilding, () -> {
                    if(build){
                        Draw.color(Pal.accent);

                        for(TextureRegion region : recipe.getGeneratedIcons()){
                            Shaders.blockbuild.region = region;
                            Shaders.blockbuild.progress = progress / ((Missile)recipe).constructTime;

                            Draw.rect(region, x, y, recipe.rotate ? rotdeg() : 0);
                            Draw.flush();
                        }

                        Draw.color();
                    }else{
                        Drawf.construct(this, recipe.fullIcon, 0, progress / ((Missile)recipe).constructTime, heat, time);
                    }
                });

                if(build){
                    Draw.z(Layer.blockBuilding + 0.01f);
                    Draw.color(Pal.accent, heat);

                    Lines.lineAngleCenter(x + Mathf.sin(time, 10f, Vars.tilesize / 2f * recipe.size + 1f), y, 90, recipe.size * Vars.tilesize + 1f);

                    Draw.reset();
                }
            }
            Draw.z(Layer.blockBuilding + 1f);
            Draw.rect(topRegion, x, y);

            drawPayload();
        }

        @Override
        public void drawSelect(){
            // Do not
        }

        public boolean curInput(){
            return recipe instanceof Missile m && m.prev != null;
        }

        @Override
        public int getMaximumAccepted(Item item){
            return capacities[item.id];
        }

        @Override
        public void buildConfiguration(Table table){
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            Table cont = new Table();
            cont.defaults().size(40);

            int i = 0;

            for(Block b : content.blocks()){
                if(b instanceof Missile m && products.contains(m)){
                    ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {}).group(group).get();
                    button.update(() -> button.setChecked(recipe == m));

                    if(m.requiresUnlock && !m.unlockedNow()){
                        button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("clear"));
                        button.replaceImage(PMElements.imageStack(m.uiIcon, Icon.tree.getRegion(), Color.red));
                        button.getImageCell().tooltip("@pm-missing-research");
                    }else{
                        button.changed(() -> configure(button.isChecked() ? m : null));
                        button.getStyle().imageUp = new TextureRegionDrawable(m.uiIcon);
                        button.getImageCell().tooltip(m.localizedName);
                    }

                    if(i++ % 4 == 3){
                        cont.row();
                    }
                }
            }

            //add extra blank spaces so it looks nice
            if(i % 4 != 0){
                int remaining = 4 - (i % 4);
                for(int j = 0; j < remaining; j++){
                    cont.image(Styles.black6);
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setScrollYForce(scrollPos);
            pane.update(() -> scrollPos = pane.getScrollY());

            pane.setOverscroll(false, false);
            table.add(pane).maxHeight(Scl.scl(40 * 5));
        }

        @Override
        public Object config(){
            return recipe;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.payload == null && recipe instanceof Missile m && payload instanceof BuildPayload p && p.block() == m.prev;
        }

        @Override
        public boolean shouldAmbientSound(){
            return super.shouldAmbientSound() && produce;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(recipe == null ? -1 : recipe.id);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                recipe = Vars.content.block(read.s());
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }

    protected class DynamicConsumePower extends ConsumePower{
        public DynamicConsumePower(){
            super();
        }

        @Override
        public float requestedPower(Building entity){
            if(entity instanceof PayloadCrafterBuild s && s.recipe() instanceof Missile m){
                return m.powerUse;
            }

            return super.requestedPower(entity);
        }
    }
}
