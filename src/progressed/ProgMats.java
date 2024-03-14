package progressed;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import progressed.content.*;
import progressed.content.blocks.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.gen.entities.*;
import progressed.graphics.*;
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
    boolean hasProc;

    public ProgMats(){
        super();
        Events.on(ClientLoadEvent.class, e -> {
            loadSettings();
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
        }
    }

    @Override
    public void init(){
        if(!headless){
            if(OS.username.equals("MEEP")) experimental = true;

            LoadedMod progM = mods.locateMod("prog-mats");
            Func<String, String> getModBundle = value -> bundle.get("mod." + value);

            progM.meta.displayName = "[#4a6de5]" + progM.meta.displayName + "[]";
            progM.meta.description = getModBundle.get(progM.meta.name + ".description");

            StringBuilder contributors = new StringBuilder(getModBundle.get(progM.meta.name + ".author"));
            contributors.append("\n\n").append("[#4a6de5]Contributors:[]");
            int i = 0;
            while(bundle.has("mod." + progM.meta.name + "-contributor." + i)){
                contributors.append("\n        ").append(getModBundle.get(progM.meta.name + "-contributor." + i));
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


            if(!TUEnabled()){ //TU already does this, don't double up
                renderer.minZoom = Math.min(renderer.minZoom, 0.667f); //Zoom out farther
                renderer.maxZoom = Math.max(renderer.maxZoom, 24f); //Get a closer look at yourself

                Events.on(WorldLoadEvent.class, e -> {
                    //reset
                    hasProc = Groups.build.contains(b -> b.block.privileged); //Check for world procs
                    renderer.minZoom = 0.667f;
                    renderer.maxZoom = 24f;
                });

                Events.run(Trigger.update, () -> {
                    if(state.isGame()){ //Zoom range
                        if(hasProc){
                            if(control.input.logicCutscene){ //Dynamically change zoom range to not break cutscene zoom
                                renderer.minZoom = 1.5f;
                                renderer.maxZoom = 6f;
                            }else{
                                renderer.minZoom = 0.667f;
                                renderer.maxZoom = 24f;
                            }
                        }
                    }
                });
            }
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
        PMPlanets.load();
        PMTechTree.load();
        PMLoadouts.load();
    }

    void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.pm-title"), "prog-mats-settings-icon", t -> {
            t.pref(new Separator("pm-graphics-settings"));
            t.sliderPref("pm-sword-opacity", 100, 20, 100, 5, s -> s + "%");
            t.sliderPref("pm-zone-opacity", 100, 0, 100, 5, s -> s + "%");
            t.checkPref("pm-tesla-range", true);
            t.pref(new Separator("pm-other-settings"));
            t.checkPref("pm-farting", false, b -> Sounds.wind3.play(Interp.pow2In.apply(Core.settings.getInt("sfxvol") / 100f) * 5f));
        });
    }

    public static boolean farting(){
        return settings.getBool("pm-farting", false);
    }

    static boolean TUEnabled(){
        return PMUtls.modEnabled("test-utils");
    }

    static class Separator extends Setting{
        float height;

        public Separator(String name){
            super(name);
        }

        public Separator(float height){
            this("");
            this.height = height;
        }

        @Override
        public void add(SettingsTable table){
            if(name.isEmpty()){
                table.image(Tex.clear).height(height).padTop(3f);
            }else{
                table.table(t -> {
                    t.add(title).padTop(3f);
                }).get().background(Tex.underline);
            }
            table.row();
        }
    }
}
