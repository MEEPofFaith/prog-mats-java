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
import progressed.util.*;

import static mindustry.Vars.*;

public class PayloadCrafter extends BlockProducer{
    public Seq<Missile> products;
    public boolean hasTop = true;

    public int[] capacities = {};

    private float scrollPos;

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

        stats.add(Stat.input, t -> {
           t.row();

           products.each(p -> {
               t.image(p.fullIcon).padRight(4).right().top();
               t.add(p.localizedName).padRight(10).left().top();

               t.table(ct -> {
                   ct.left().defaults().padRight(3).left();

                   ct.table(it -> {
                       it.add(Stat.input.localized() + ": ");
                       for(ItemStack stack : p.requirements){
                           it.add(PMElements.itemImage(stack.item.uiIcon, () -> stack.amount == 0 ? "" : stack.amount + ""));
                       }
                   });

                   if(p.prev != null){
                       ct.row();
                       ct.table(pt -> {
                           pt.image(p.prev.fullIcon).padLeft(60f).padRight(4).right().top();
                           pt.add(p.prev.localizedName).padRight(10).left().top();
                       });
                   }
                   if(p.constructTime > 0){
                       ct.row();
                       ct.add(Stat.buildTime.localized() + ": " + PMUtls.stringsFixed(p.constructTime / 60f) + " " + StatUnit.seconds.localized());
                   }
                   if(p.powerUse > 0){
                       ct.row();
                       ct.add(Stat.powerUse.localized() + ": " + PMUtls.stringsFixed(p.powerUse * 60f) + " " + StatUnit.powerSecond.localized());
                   }
               }).padTop(-9).left().get().background(Tex.underline);

               t.row();
           });
        });
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
            boolean produce = recipe instanceof Missile m && consValid() && (m.prev != null ? (payload != null && hasArrived() && payload.block() == m.prev) : payload == null);

            if(recipe instanceof Missile m){
                if(payload != null && payload.block() != m.prev){
                    moveOutPayload();
                }
            }else{
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

            drawPayload();

            if(recipe != null){
                Draw.draw(Layer.blockOver, () -> Drawf.construct(this, recipe.fullIcon, 0, progress / ((Missile)recipe).constructTime, heat, time));
            }

            Draw.z(Layer.blockOver);

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
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
            pane.update(() -> {
                scrollPos = pane.getScrollY();
            });

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
