package progressed.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import progressed.*;

public class PMPlanets{
    public static Planet
    theGrandPoo;

    public static void load(){
        // *wipes tear* It's beautiful.
        theGrandPoo = new Planet("the-grand-poo", Planets.serpulo, 0.25f){{
            bloom = true;
            accessible = false;
            visible = ProgMats.farting();
            orbitRadius = 2;
            lightColor = Color.valueOf("6e340a");
            atmosphereColor = Color.valueOf("663712");

            meshLoader = () -> new SunMesh(
                this, 4,
                5, 0.3, 1.7, 1.2, 1,
                1.1f,
                Color.valueOf("52230e"),
                Color.valueOf("472502"),
                lightColor,
                atmosphereColor,
                Color.valueOf("d6af11"),
                Color.valueOf("b5a528")
            );
        }};
    }
}
