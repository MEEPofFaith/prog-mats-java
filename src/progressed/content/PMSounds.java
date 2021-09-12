package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.assets.loaders.SoundLoader.*;
import arc.audio.*;
import mindustry.*;

/**
 * @author GlennFolker
 * For how to make this. Just copy over and adjust the code.
 */
public class PMSounds{
    public static Sound
        pixelShoot = new Sound(),
        pixelHit = new Sound(),
        harbingerCharge = new Sound(),
        harbingerBlast = new Sound(),
        popeshadowCharge = new Sound(),
        popeshadowBlast = new Sound(),
        swordStab = new Sound(),
        sentenelCharge = new Sound(),
        nuclearExplosion = new Sound(),
        moonPiss = new Sound(),
        loudMoonPiss = new Sound();

    public static void load() {
        if(Vars.headless) return;

        pixelShoot = loadSound("pixelShoot");
        pixelHit = loadSound("pixelHit");
        harbingerCharge = loadSound("harbingerCharge");
        harbingerBlast = loadSound("harbingerBlast");
        popeshadowCharge = loadSound("popeshadowCharge");
        popeshadowBlast = loadSound("popeshadowBlast");
        swordStab = loadSound("swordStab");
        sentenelCharge = loadSound("sentenelCharge");
        nuclearExplosion = loadSound("nuclearExplosion");
        moonPiss = loadSound("moonPiss");
        loudMoonPiss = loadSound("extraLoudMoonPiss");
    }

    protected static Sound loadSound(String soundName) {
        String name = "sounds/" + soundName;
        String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";

        Sound sound = new Sound();

        AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;

        return sound;
    }
}