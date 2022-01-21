package progressed.ui;

import arc.scene.ui.ImageButton.*;

import static mindustry.gen.Tex.*;

public class PMStyles{
    public static ImageButtonStyle squarei;

    public static void load(){
        squarei = new ImageButtonStyle(){{
            over = buttonSquareOver;
            up = buttonSquare;
            checked = down = buttonSquareDown;
        }};
    }
}