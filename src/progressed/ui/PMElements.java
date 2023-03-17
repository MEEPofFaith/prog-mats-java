package progressed.ui;

import arc.*;
import arc.flabel.*;

public class PMElements{
    public static FLabel infinity(){
        return new FLabel("{wave}{rainbow}" + Core.bundle.get("pm-infinity"));
    }

    public static FLabel infiniteDamage(){
        return new FLabel("{wave}{rainbow}" + Core.bundle.get("pm-infinite-damage"));
    }

    public static FLabel everything(){
        return new FLabel("{wave}{rainbow}" + Core.bundle.get("pm-everything"));
    }
}
