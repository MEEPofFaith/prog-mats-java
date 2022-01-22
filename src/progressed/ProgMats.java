package progressed;

import arc.*;
import arc.func.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import progressed.content.*;
import progressed.content.blocks.*;
import progressed.content.bullets.*;
import progressed.graphics.*;
import progressed.ui.*;
import progressed.ui.dialogs.*;
import progressed.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ProgMats extends Mod{
    public static ModuleSwapDialog swapDialog;
    public static PMSettings settings = new PMSettings();

    private final ContentList[] pmContent = {
        new PMStatusEffects(),
        new PMLiquids(),
        new PMUnitTypes(),
        new PMItems(),
        new PMBullets(),
        new PMWeathers(),
        new PMBlocks(),
        new PMTechTree()
    };

    public ProgMats(){
        super();
        Events.on(ClientLoadEvent.class, e -> {
            settings.init();
            PMPal.init();
        });

        // Load all assets once they're added into Vars.tree
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            PMShaders.load();
            PMSounds.load();
        }));
    }

    @Override
    public void init(){
        if(!headless){
            enableConsole = true;
            experimental = true;
            renderer.minZoom = 0.667f; //Zoom out farther
            renderer.maxZoom = 24f; //Get a closer look at yourself

            LoadedMod progM = mods.locateMod("prog-mats");
            Func<String, String> getModBundle = value -> bundle.get("mod." + value);

            progM.meta.displayName = "[#FCC21B]" + progM.meta.displayName + "[]";
            progM.meta.description = getModBundle.get(progM.meta.name + ".description");

            StringBuilder contributors = new StringBuilder(getModBundle.get(progM.meta.name + ".author"));
            contributors.append("\n\n").append("[#FCC21B]Contributors:[]");
            int i = 0;
            while(bundle.has("mod." + progM.meta.name + "-contributor." + i)){
                contributors.append("\n        ").append(getModBundle.get(progM.meta.name + "-contributor." + i));
                i++;
            }
            progM.meta.author = contributors.toString();

            Events.on(ClientLoadEvent.class, e -> {
                PMUtls.godHood(PMUnitTypes.everythingUnit);
                PMStyles.load();
                swapDialog = new ModuleSwapDialog();

                if(Core.settings.getBool("pm-farting")){
                    content.blocks().each(b -> {
                        b.destroySound = Sounds.wind3;
                    });

                    content.units().each(u -> {
                       u.deathSound = Sounds.wind3;
                    });
                }
            });
        }
    }

    @Override
    public void loadContent(){
        for(ContentList list : pmContent){
            list.load();
        }
    }
}