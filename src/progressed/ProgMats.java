package progressed;

import arc.*;
import arc.func.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import progressed.content.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ProgMats extends Mod{
    public static SettingAdder settingAdder = new SettingAdder();

    private final ContentList[] pmContent = {
        new PMStatusEffects(),
        new PMLiquids(),
        new PMUnitTypes(),
        new PMItems(),
        new PMBullets(),
        new PMWeathers(),
        new PMPayloads(),
        new PMBlocks(),
        new PMTechTree()
    };

    public ProgMats(){
        super();
        PMSounds.load();

        Events.on(DisposeEvent.class, e -> {
            PMSounds.dispose();
        });

        Events.on(ClientLoadEvent.class, e -> {
            settingAdder.init();
        });
    }

    @Override
    public void init(){
        if(!headless){
            enableConsole = true;
            renderer.minZoom = 0.667f; //Zoom out farther
            renderer.maxZoom = 24f; //Get a closer look at yourself

            LoadedMod progM = mods.locateMod("prog-mats");
            Func<String, String> stringf = value -> Core.bundle.get("mod." + value);

            progM.meta.displayName = "[#FCC21B]" + progM.meta.displayName + "[]";
            progM.meta.author = stringf.get(progM.meta.name + ".author");
            progM.meta.version = "[#FCC21B]" + progM.meta.version + "[]";
            progM.meta.description = stringf.get(progM.meta.name + ".description");

            Events.on(ClientLoadEvent.class, e -> {
                PMUtls.godHood(PMUnitTypes.everythingUnit);

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

            Log.info("@: Loaded content list: @", getClass().getSimpleName(), list.getClass().getSimpleName());
        }
    }

    public static void print(Object... args){
        StringBuilder builder = new StringBuilder();
        if(args == null){
            builder.append("null");
        }else{
            for(int i = 0; i < args.length; i++){
                builder.append(args[i]);
                if(i < args.length - 1) builder.append(", ");
            }
        }

        Log.info("&lc&fb[PM]&fr @", builder.toString());
    }
}