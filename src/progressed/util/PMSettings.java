package progressed.util;

import arc.*;
import arc.math.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMSettings{
    public static void init(){
        ui.settings.addCategory(bundle.get("setting.pm-title"), "prog-mats-settings-icon", t -> {
            t.pref(new Separator("pm-graphics-settings"));
            t.sliderPref("pm-sword-opacity", 100, 20, 100, 5, s -> s + "%");
            t.sliderPref("pm-zone-opacity", 100, 0, 100, 5, s -> s + "%");
            t.checkPref("pm-tesla-range", true);
            t.pref(new Separator("pm-other-settings"));
            t.checkPref("pm-farting", false, b -> Sounds.wind3.play(Interp.pow2In.apply(Core.settings.getInt("sfxvol") / 100f) * 5f));
        });
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
