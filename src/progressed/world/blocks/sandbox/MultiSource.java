package progressed.world.blocks.sandbox;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.ui.*;

import static mindustry.Vars.*;

public class MultiSource extends Block{
    public TextureRegion cross;
    public TextureRegion[] center = new TextureRegion[2];

    public MultiSource(String name){
        super(name);
        requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        update = solid = saveConfig = noUpdateDisabled = true;
        hasItems = hasLiquids = true;
        configurable = outputsLiquid = true;
        displayFlow = false;
        liquidCapacity = 10000f;
        group = BlockGroup.transportation;

        config(Integer.class, (MultiSourceBuild tile, Integer p) -> tile.data.set(p));
        configClear((MultiSourceBuild tile) -> tile.data.clear());
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.remove("items");
        bars.remove("liquid");
    }

    @Override
    public void load(){
        super.load();

        cross = Core.atlas.find(name + "-cross");
        center[0] = Core.atlas.find(name + "-center-0");
        center[1] = Core.atlas.find(name + "-center-1");
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(cross, req.drawx(), req.drawy());
        if(req.config instanceof Integer input){
            Point2 data = Point2.unpack(input);
            drawRequestConfigCenter(req, content.item((short)data.x), name + "-center-0");
            drawRequestConfigCenter(req, content.liquid((short)data.y), name + "-center-1");
        }
    }
    
    @Override
    public boolean canReplace(Block other){
        if(other.alwaysReplace) return true;
        return other.replaceable && (other != this || rotate) && this.group != BlockGroup.none && (other.group == BlockGroup.transportation || other.group == BlockGroup.liquids) &&
            (size == other.size || (size >= other.size && ((subclass != null && subclass == other.subclass) || group.anyReplace)));
    }

    public class MultiSourceBuild extends Building{
        protected SourceData data = new SourceData();

        @Override
        public void placed() {
            super.placed();
            cdump = 1;
        }

        @Override
        public void draw(){
            super.draw();

            Draw.rect(cross, x, y);

            if(data.item != null){
                Draw.color(data.item.color);
                Draw.rect(center[0], x, y);
                Draw.color();
            }
            
            if(data.liquid != null){
                Draw.color(data.liquid.color);
                Draw.rect(center[1], x, y);
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            if(data.item != null){
                items.set(data.item, 100);
                for(int i = 0; i < 100; i++) dump(data.item);
                items.set(data.item, 0);
            }

            if(data.liquid == null){
                liquids.clear();
            }else{
                liquids.add(data.liquid, liquidCapacity);
                dumpLiquid(data.liquid);
                liquids.clear();
            }
        }

        @Override
        public void buildConfiguration(Table table){
            ImageButtonStyle style = new ImageButtonStyle(Styles.clearTransi);
            style.imageDisabledColor = Color.gray;
            Cell<ImageButton> b = table.button(Icon.cancel, style, () -> data.clear()).top().size(40f);
            b.get().setDisabled(data::invalid);

            table.table(t -> {
                PMItemSelection.buildTable(t, content.items(), () -> data.item, this::configure, false, true).top();
                t.row();
                t.image(Tex.whiteui).size(40f * 4f, 8f).color(Color.gray).left().top();
                t.row();
                PMItemSelection.buildTable(t, content.liquids(), () -> data.liquid, this::configure, false, true).top();
            });
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(this == other){
                deselect();
                return false;
            }

            return true;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return false;
        }

        @Override
        public Integer config(){
            return data.pack();
        }

        @Override
        public void configure(Object value){
            if(value instanceof Item i){
                if(data.item == i){
                    data.item = null;
                }else{
                    data.set(i);
                }
            }else if(value instanceof Liquid l){
                if(data.liquid == l){
                    data.liquid = null;
                }else{
                    data.set(l);
                }
            }
            //save last used config
            block.lastConfig = data;
            Call.tileConfig(player, self(), value);
        }

        @Override
        public void configureAny(Object value){
            if(value instanceof Item i){
                if(data.item == i){
                    data.item = null;
                }else{
                    data.set(i);
                }
            }else if(value instanceof Liquid l){
                if(data.liquid == l){
                    data.liquid = null;
                }else{
                    data.set(l);
                }
            }
            Call.tileConfig(player, self(), value);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(data.pack());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            data.set(read.i());
        }
    }

    public static class SourceData{
        protected Item item;
        protected Liquid liquid;

        public SourceData(Item item, Liquid liquid){
            this.item = item;
            this.liquid = liquid;
        }
        
        public SourceData(){}

        public void set(Item item, Liquid liquid){
            this.item = item;
            this.liquid = liquid;
        }

        public void set(Item item){
            this.item = item;
        }

        public void set(Liquid liquid){
            this.liquid = liquid;
        }

        public void set(Point2 data){
            set(content.item(data.x), content.liquid(data.y));
        }

        public void set(int data){
            set(Point2.unpack(data));
        }

        public Point2 toPoint2(){
            return new Point2(item == null ? -1 : item.id, liquid == null ? -1 : liquid.id);
        }

        public int pack(){
            return toPoint2().pack();
        }

        public boolean invalid(){
            return item == null && liquid == null;
        }

        public void clear(){
            item = null;
            liquid = null;
        }
    }
}