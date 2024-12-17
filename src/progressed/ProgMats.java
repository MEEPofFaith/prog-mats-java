package progressed;

import arc.*;
import arc.func.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import progressed.content.*;
import progressed.content.blocks.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.gen.entities.*;
import progressed.graphics.*;
import progressed.graphics.draw3d.*;
import progressed.graphics.renders.*;
import progressed.ui.*;
import progressed.ui.dialogs.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.payload.modular.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ProgMats extends Mod{
    public static ModuleSwapDialog swapDialog;
    public static PMHints hints = new PMHints();

    public ProgMats(){
        super();
        Events.on(ClientLoadEvent.class, e -> {
            PMSettings.init();
            PMPal.init();
            hints.load();
        });

        // Load all assets once they're added into Vars.tree
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            if(!headless){
                PMShaders.init();
                PMSounds.load();
            }
        }));

        if(!headless){
            Events.on(ContentInitEvent.class, e -> content.blocks().each(b -> b instanceof ModularTurret, (ModularTurret b) -> b.setClip(PMModules.maxClip)));

            settings.put("tu-disable-zoom", true);
        }
    }

    @Override
    public void init(){
        if(!headless){
            if(OS.username.equals("MEEP")) experimental = true;

            LoadedMod progM = mods.locateMod("prog-mats");
            Func<String, String> getModBundle = value -> bundle.get("mod." + progM.meta.name + "." + value);

            progM.meta.displayName = "[#4a6de5]" + progM.meta.displayName + "[]";
            progM.meta.description = getModBundle.get("description");
            progM.meta.subtitle = getModBundle.get("subtitle");

            StringBuilder contributors = new StringBuilder(getModBundle.get("author"));
            contributors.append("\n\n").append("[#4a6de5]Contributors:[]");
            int i = 0;
            while(bundle.has("mod." + progM.meta.name + ".contributor-" + i)){
                contributors.append("\n        ").append(getModBundle.get("contributor-" + i));
                i++;
            }
            progM.meta.author = contributors.toString();

            Events.on(ClientLoadEvent.class, e -> {
                PMStyles.load();
                swapDialog = new ModuleSwapDialog();
                PMRenders.init();
                Draw3D.init();

                if(farting()){
                    PMSounds.overrideSounds();

                    Events.run(Trigger.newGame, () -> {
                        if(settings.getBool("skipcoreanimation")) return;
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

            setupZoom();
        }
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        PMStatusEffects.load();
        PMLiquids.load();
        PMUnitTypes.load();
        PMItems.load();
        PMBullets.load();
        PMBlocks.load();
        PMTechTree.load();
        PMLoadouts.load();
    }

    public static boolean farting(){
        return settings.getBool("pm-farting", false);
    }

    private static void setupZoom(){
        //TODO Adjust minZoom based on fov and scale to never result in a negative viewport z.
        renderer.minZoom = Math.min(renderer.minZoom, 0.667f); //Zoom out farther
        renderer.maxZoom = Math.max(renderer.maxZoom, 24f); //Get a closer look at yourself

        Events.run(Trigger.update, () -> {
            if(state.isGame()){ //Zoom range
                if(control.input.logicCutscene){ //Dynamically change zoom range to not break cutscene zoom
                    renderer.minZoom = 1.5f;
                    renderer.maxZoom = 6f;
                }else{
                    renderer.minZoom = 0.667f;
                    renderer.maxZoom = 24f;
                }
            }
        });
    }
}
