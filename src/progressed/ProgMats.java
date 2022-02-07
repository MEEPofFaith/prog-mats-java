package progressed;

import arc.*;
import arc.func.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import progressed.content.*;
import progressed.content.blocks.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.ui.*;
import progressed.ui.dialogs.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.modular.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ProgMats extends Mod{
    public static ModuleSwapDialog swapDialog;
    public static PMSettings settings = new PMSettings();

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

        if(!headless){
            Events.on(ContentInitEvent.class, e -> {
                float clip = PMModules.maxClip;
                ((ModularTurret)(PMBlocks.council)).setClip(clip);
                ((ModularTurret)(PMBlocks.congress)).setClip(clip);
                ((ModularTurret)(PMBlocks.pantheon)).setClip(clip);
            });
        }
    }

    @Override
    public void init(){
        if(!headless){
            if(OS.username.equals("MEEP")){
                enableConsole = true;
                experimental = true;
            }
            renderer.minZoom = Math.min(renderer.minZoom, 0.667f); //Zoom out farther
            renderer.maxZoom = Math.max(renderer.maxZoom, 24f); //Get a closer look at yourself

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

                    Events.run(Trigger.newGame, () -> {
                        Time.run(coreLandDuration, () -> {
                            CoreBuild core = player.bestCore();
                            if(core != null){
                                OtherFx.fard.at(core);
                                PMSounds.gigaFard.at(core.x, core.y, 1f, 10f);
                            }
                        });
                    });
                }
            });
        }
    }

    @Override
    public void loadContent(){
        PMStatusEffects.load();
        PMLiquids.load();
        PMUnitTypes.load();
        PMItems.load();
        PMBullets.load();
        PMWeathers.load();
        PMBlocks.load();
        PMTechTree.load();
    }
}
