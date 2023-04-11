package progressed.ui;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.ui.fragments.HintsFragment.*;
import mindustry.world.*;
import progressed.*;
import progressed.content.blocks.*;
import progressed.world.blocks.production.UnitMinerDepot.*;

import static mindustry.Vars.*;

public class PMHints{
    ObjectSet<String> events = new ObjectSet<>();
    ObjectSet<Block> placedBlocks = new ObjectSet<>();

    public void load(){
        Vars.ui.hints.hints.add(PMHint.values()).as();

        Events.on(BlockBuildEndEvent.class, event -> {
            if(!event.breaking && event.unit == player.unit()){
                placedBlocks.add(event.tile.block());
            }
        });

        Events.on(ResetEvent.class, e -> {
            placedBlocks.clear();
            events.clear();
        });

        Events.on(BuildingCommandEvent.class, e -> {
            if(e.building instanceof UnitMinerDepotBuild){
                events.add("minercontrol");
            }
        });
    }

    /** Methods copied from {@link DefaultHint}. */
    public enum PMHint implements Hint{
        minerControl(() -> state.rules.defaultTeam.data().getBuildings(PMBlocks.unitMinerDepot).size > 0, () -> ProgMats.hints.events.contains("factorycontrol"));

        String text;
        int visibility = visibleAll;
        Hint[] dependencies = {};
        boolean finished, cached;
        Boolp complete, shown = () -> true;

        PMHint(Boolp complete){
            this.complete = complete;
        }

        PMHint(int visiblity, Boolp complete){
            this(complete);
            this.visibility = visiblity;
        }

        PMHint(Boolp shown, Boolp complete){
            this(complete);
            this.shown = shown;
        }

        PMHint(int visiblity, Boolp shown, Boolp complete){
            this(complete);
            this.shown = shown;
            this.visibility = visiblity;
        }

        @Override
        public boolean finished(){
            if(!cached){
                cached = true;
                finished = Core.settings.getBool("pm-" + name() + "-hint-done", false);
            }
            return finished;
        }

        @Override
        public void finish(){
            Core.settings.put("pm-" + name() + "-hint-done", finished = true);
        }

        @Override
        public String text(){
            if(text == null){
                text = Vars.mobile && Core.bundle.has("hint.pm-" + name() + ".mobile") ? Core.bundle.get("hint.pm-" + name() + ".mobile") : Core.bundle.get("hint.pm-" + name());
                if(!Vars.mobile) text = text.replace("tap", "click").replace("Tap", "Click");
            }
            return text;
        }

        @Override
        public boolean complete(){
            return complete.get();
        }

        @Override
        public boolean show(){
            return shown.get() && (dependencies.length == 0 || !Structs.contains(dependencies, d -> !d.finished()));
        }

        @Override
        public int order(){
            return ordinal();
        }

        @Override
        public boolean valid(){
            return (Vars.mobile && (visibility & visibleMobile) != 0) || (!Vars.mobile && (visibility & visibleDesktop) != 0);
        }
    }
}
