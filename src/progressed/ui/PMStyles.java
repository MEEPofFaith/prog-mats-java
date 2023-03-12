package progressed.ui;

import arc.scene.ui.ImageButton.*;
import mindustry.ui.*;

import static mindustry.gen.Tex.*;

public class PMStyles{
    public static ImageButtonStyle squareTogglei, boxTogglei;

    public static void load(){
        squareTogglei = new ImageButtonStyle(){{
            over = buttonOver;
            up = button;
            checked = down = buttonDown;
        }};

        boxTogglei = new ImageButtonStyle(Styles.squarei){{
            checked = Styles.flatDown;
        }};
    }
}
