package progressed.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

public class PMElements{
    public static Element imageStack(TextureRegion base, TextureRegion stacked, Color stackedColor){
        Stack stack = new Stack();

        Image i = new Image(stacked);
        if(stackedColor != null) i.setColor(stackedColor);

        stack.add(new Image(base));
        stack.add(i);

        return stack;
    }
}