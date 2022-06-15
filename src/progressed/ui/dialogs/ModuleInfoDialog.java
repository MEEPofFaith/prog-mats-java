package progressed.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.dialogs.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.payloads.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

/** Similar to {@link ContentInfoDialog} but with a {@link BaseModule} instead of a {@link UnlockableContent} */
public class ModuleInfoDialog extends BaseDialog{
    public ModuleInfoDialog(){
        super("@info.title");

        addCloseButton();
    }

    public void show(BaseModule module){
        cont.clear();

        ModulePayload payload = module.getPayload();
        Table table = new Table();
        table.margin(10);

        //initialize stats if they haven't been yet
        payload.checkStats();

        table.table(title1 -> {
            title1.image(payload.uiIcon).size(iconXLarge).scaling(Scaling.fit);
            title1.add("[accent]" + module.localizedName + (Core.settings.getBool("console") ? "\n[gray]" + module.name : "")).padLeft(5);
        });

        table.row();

        if(payload.description != null){
            var any = module.mStats.toMap().size > 0;

            if(any){
                table.add("@category.purpose").color(Pal.accent).fillX().padTop(10);
                table.row();
            }

            table.add("[lightgray]" + module.displayDescription()).wrap().fillX().padLeft(any ? 10 : 0).width(500f).padTop(any ? 0 : 10).left();
            table.row();

            if(!module.mStats.useCategories && any){
                table.add("@category.general").fillX().color(Pal.accent);
                table.row();
            }
        }

        ModuleStats stats = module.mStats;

        for(StatCat cat : stats.toMap().keys()){
            OrderedMap<Stat, Seq<StatValue>> map = stats.toMap().get(cat);

            if(map.size == 0) continue;

            if(stats.useCategories){
                table.add("@category." + cat.name).color(Pal.accent).fillX();
                table.row();
            }

            for(Stat stat : map.keys()){
                table.table(inset -> {
                    inset.left();
                    inset.add("[lightgray]" + stat.localized() + ":[] ").left().top();
                    Seq<StatValue> arr = map.get(stat);
                    for(StatValue value : arr){
                        value.display(inset);
                        inset.add().size(10f);
                    }

                }).fillX().padLeft(10);
                table.row();
            }
        }

        if(payload.details != null){
            table.add("[gray]" + (payload.unlocked() || !payload.hideDetails ? payload.details : Iconc.lock + " " + Core.bundle.get("unlock.incampaign"))).pad(6).padTop(20).width(400f).wrap().fillX();
            table.row();
        }

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }
}
