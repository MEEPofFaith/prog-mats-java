package progressed.content;

import arc.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import progressed.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SettingAdder{
    public SettingsTable progm;

    public void init(){
        BaseDialog dialog = new BaseDialog("Progressed Materials");
        dialog.addCloseButton();

        progm = new SettingsTable();
        progm.sliderPref("pm-swordopacity", 100, 20, 100, 5, s -> s + "%");
        progm.sliderPref("pm-strobespeed", 3, 1, 20, 1, s -> PMUtls.stringsFixed(s / 2f));
        progm.checkPref("pm-tesla-range", true);
        progm.checkPref("pm-farting", false, b -> Sounds.wind3.play(Core.settings.getInt("sfxvol"))); //haha yes LOUD FART

        dialog.cont.center().add(progm);

        Events.on(ResizeEvent.class, event -> {
           if(dialog.isShown() && Core.scene.getDialog() == dialog){
               dialog.updateScrollFocus();
           }
        });

        ui.settings.shown(() -> {
            Table settingUi = (Table)((Group)((Group)(ui.settings.getChildren().get(1))).getChildren().get(0)).getChildren().get(0); //This looks so stupid lol
            settingUi.row();
            settingUi.button(bundle.get("setting.pm-title"), Styles.cleart, dialog::show);
        });
    }
}