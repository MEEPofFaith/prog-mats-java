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
import progressed.graphics.*;
import progressed.ui.*;
import progressed.world.blocks.consumers.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

public class PayloadCrafter extends PayloadBlock{
    private float scrollPos;

    public Seq<Recipe> recipes = new Seq<>();
    public boolean hasTop = true;

    public PayloadCrafter(String name){
        super(name);

        update = true;
        rotate = true;
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

    public void recipes(Block... blocks){
        for(Block b : blocks){
            recipes.add(new Recipe(b));
        }
    }

    public void recipes(Recipe... newRecipes){
        for(Recipe r : newRecipes){
            recipes.add(r);
        }
    }

    @Override
    public void init(){
        if(recipes.contains(r -> r.powerUse > 0)){
            consumes.add(new PayloadCrafterConsumePower());
            hasPower = true;
        }
        if(recipes.contains(r -> r.buildCost != null)){
            consumes.add(new ConsumeItemDynamic((PayloadCrafterBuild e) -> e.recipe() != null && e.recipe().buildCost != null ? e.recipe().buildCost : ItemStack.empty));
            hasItems = true;
        }
        if(recipes.contains(r -> r.liquidCost != null)){
            consumes.add(new ConsumeLiquidDynamic((PayloadCrafterBuild e) -> e.recipe() != null ? e.recipe().liquidCost : null));
            hasLiquids = true;
        }
        if(recipes.contains(r -> r.inputBlock != null)) acceptsPayload = true;
        if(recipes.contains(r -> r.outputBlock != null)) outputsPayload = true;

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
        if(recipes.contains(Recipe::hasInputBlock)){
            return new TextureRegion[]{region, inRegion, outRegion, topRegion};
        }
        return new TextureRegion[]{region, outRegion, topRegion};
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.powerUse);

        stats.add(Stat.output, PMStatValues.payloadProducts(recipes));
    }

    @Override
    public void setBars(){
        super.setBars();

        if(hasLiquids){
            bars.remove("liquid");
            bars.add("liquid", (PayloadCrafterBuild entity) -> {
                Liquid l = entity.recipe() != null ? entity.recipe().getLiquidInput() : null;
                return new Bar(
                    () -> l != null ? l.localizedName : Core.bundle.get("bar.liquid"),
                    () -> l != null ? l.barColor() : Color.white,
                    () -> entity.liquids == null || l == null ? 0f : entity.liquids.get(l) / liquidCapacity
                );
            });
        }

        bars.add("progress", (PayloadCrafterBuild entity) -> new Bar(
            "bar.progress",
            Pal.ammo,
            () -> entity.recipe() == null ? 0f : (entity.progress / entity.recipe().craftTime)
        ));
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(region, req.drawx(), req.drawy());
        if(recipes.contains(r -> r.inputBlock != null)) Draw.rect(inRegion, req.drawx(), req.drawy(), req.rotation * 90);
        Draw.rect(outRegion, req.drawx(), req.drawy(), req.rotation * 90);
        Draw.rect(topRegion, req.drawx(), req.drawy());
    }

    public boolean canProduce(Block b){
        boolean hasRecipe = recipes.contains(r -> r.outputBlock == b);
        if(hasRecipe){
            Recipe recipe = recipes.find(r -> r.outputBlock == b);
            return recipe.requiresUnlock && recipe.outputBlock.unlockedNow();
        }
        return false;
    }

    public class PayloadCrafterBuild extends PayloadBlockBuild<BuildPayload>{
        public float progress, time, heat;
        public @Nullable Block recipe;
        public boolean produce;

        public @Nullable Recipe recipe(){
            return recipes.find(r -> r.outputBlock == recipe);
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config) return recipe;
            if(sensor == LAccess.progress) return progress;
            return super.senseObject(sensor);
        }

        @Override
        public void updateTile(){
            super.updateTile();
            Recipe recipe = recipe();
            produce = recipe != null && consValid() && (recipe.inputBlock != null ? (payload != null && hasArrived() && payload.block() == recipe.inputBlock) : payload == null);

            if(recipe != null){
                if(payload != null && payload.block() != recipe.inputBlock){
                    moveOutPayload();
                }
            }else if(payload != null && !recipes.contains(r -> r.inputBlock == payload.block())){
                moveOutPayload();
            }

            if(recipe != null && payload != null && payload.block() == recipe.inputBlock){
                moveInPayload(false);
            }

            if(produce && recipe != null){
                progress += edelta();

                if(progress >= recipe.craftTime){
                    craft(recipe);
                }
            }else if(recipe == null || !consValid()){
                progress = 0f;
            }

            heat = Mathf.lerpDelta(heat, Mathf.num(produce), 0.15f);
            time += heat * delta();
        }

        public void craft(Recipe recipe){
            consume();

            payload = new BuildPayload(recipe.outputBlock, team);
            payVector.setZero();
            progress %= 1f;
        }

        public float powerUse(){
            return recipe() != null ? recipe().powerUse : 0f;
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
                Recipe r = recipe();
                Draw.draw(Layer.blockBuilding, () -> {
                    if(r.blockBuild){
                        for(TextureRegion region : recipe.getGeneratedIcons()){
                            if(r.centerBuild){
                                PMDrawf.blockBuildCenter(x, y, region, recipe.rotate ? rotdeg() : 0, progress / r.craftTime);
                            }else{
                                PMDrawf.blockBuild(x, y, region, recipe.rotate ? rotdeg() : 0, progress / r.craftTime);
                            }
                        }
                    }else{
                        Drawf.construct(this, recipe.fullIcon, 0, progress / r.craftTime, heat, time);
                    }
                });

                if(r.blockBuild){
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

        public boolean curInput(){
            return recipe() != null && recipe().inputBlock != null;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return items != null && items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public int getMaximumAccepted(Item item){
            if(recipe() == null) return 0;
            for(ItemStack stack : recipe().buildCost){
                if(stack.item == item) return stack.amount * 2;
            }
            return 0;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return liquids != null && recipe() != null && recipe().hasLiquidInput(liquid);
        }

        @Override
        public void buildConfiguration(Table table){
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            Table cont = new Table();
            cont.defaults().size(40);

            int i = 0;

            for(Block b : content.blocks()){
                if(recipes.contains(r -> r.outputBlock == b)){
                    Cell<ImageButton> cell = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, () -> {}).group(group);
                    ImageButton button = cell.get();
                    Recipe r = recipes.find(rec -> rec.outputBlock == b);
                    button.update(() -> button.setChecked(recipe == b));

                    if(r.requiresUnlock && !r.outputBlock.unlockedNow()){
                        button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find("clear"));
                        button.replaceImage(PMElements.imageStack(b.uiIcon, Icon.tree.getRegion(), Color.red));
                        cell.tooltip("@pm-missing-research");
                    }else{
                        button.getStyle().imageUp = new TextureRegionDrawable(b.uiIcon);
                        cell.tooltip(b.localizedName);
                    }
                    button.changed(() -> configure(button.isChecked() ? b : null));

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
            Recipe r= recipe();
            return this.payload == null && r != null && payload instanceof BuildPayload p && p.block() == r.inputBlock;
        }

        @Override
        public void display(Table table){
            super.display(table);

            TextureRegionDrawable reg = new TextureRegionDrawable();

            table.row();
            table.table(t -> {
                t.left();
                t.image().update(i -> {
                    i.setDrawable(recipe == null ? Icon.cancel : reg.set(recipe.uiIcon));
                    i.setScaling(Scaling.fit);
                    i.setColor(recipe == null ? Color.lightGray : Color.white);
                }).size(32).padBottom(-4).padRight(2);
            }).left().get().label(() -> recipe == null ? "@none" : recipe.localizedName).wrap().width(230f).color(Color.lightGray);
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

    protected class PayloadCrafterConsumePower extends ConsumePower{
        @Override
        public float requestedPower(Building entity){
            return ((PayloadCrafterBuild)entity).powerUse();
        }
    }
}