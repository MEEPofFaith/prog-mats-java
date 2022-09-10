package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.SoundLoader.*;
import arc.audio.*;
import mindustry.*;

/**
 * For how to make this. Just copy over and adjust the code.
 * @author GlennFolker
 */
public class PMSounds{
    public static Sound

    pixelShoot = new Sound(),
    pixelHit = new Sound(),
    rockExplode = new Sound(),
    harbingerCharge = new Sound(),
    harbingerBlast = new Sound(),
    nuclearExplosion = new Sound(),
    pulseBeam = new Sound(),
    gigaFard = new Sound();

    public static void load() {
        if(Vars.headless) return;

        pixelShoot = loadSound("pixel-shoot");
        pixelHit = loadSound("pixel-hit");
        rockExplode = loadSound("rock-explode");
        harbingerCharge = loadSound("harbinger-charge");
        harbingerBlast = loadSound("harbinger-blast");
        nuclearExplosion = loadSound("nuclear-explosion");
        pulseBeam = loadSound("pulse-beam");
        gigaFard = loadSound("giga-fard");
    }

    protected static Sound loadSound(String soundName){
        String name = "sounds/" + soundName;
        String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";

        Sound sound = new Sound();

        AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;

        return sound;
    }
}
