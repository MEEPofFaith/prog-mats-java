package progressed.world.blocks;

import arc.func.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.ui.*;

import static mindustry.Vars.*;

public class PMItemSelection{
    private static float scrollPos = 0f;

    public static <T extends UnlockableContent> Cell<ScrollPane> buildTable(Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer){
        return buildTable(table, items, holder, consumer, true, true);
    }
    
    public static <T extends UnlockableContent> Cell<ScrollPane> buildTable(Table table, Seq<T> items, Prov<T> holder, Cons<T> consumer, boolean closeSelect, boolean returnOriginal){

        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        Table cont = new Table();
        cont.defaults().size(40).top();

        int i = 0;

        for(T item : items){
            if(!item.unlockedNow()) continue;

            ImageButton button = cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> {
                if(closeSelect) control.input.config.hideConfig();
            }).group(group).get();
            button.changed(() -> consumer.get(button.isChecked() ? item : returnOriginal ? holder.get() : null));
            button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
            button.update(() -> button.setChecked(holder.get() == item));

            if(i++ % 4 == 3){
                cont.row();
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
        return table.add(pane).maxHeight(Scl.scl(40 * 5)).top().left();
    }
}
