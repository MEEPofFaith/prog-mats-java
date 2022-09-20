package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.SoundLoader.*;
import arc.audio.*;
import arc.files.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

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
    funiBoom = new Sound(),
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
        funiBoom = loadSound("funi-boom");
        gigaFard = loadSound("giga-fard");
    }

    public static void overrideSounds(){
        if(Vars.headless) return;

        content.blocks().each(b -> b.destroySound = Sounds.wind3);
        content.blocks().each(b -> b instanceof PowerGenerator, (PowerGenerator b) -> b.explodeSound = funiBoom);
        content.units().each(u -> u.deathSound = Sounds.wind3);

        Sounds.press.load(soundFile("press-boom"));
    }

    protected static Sound loadSound(String soundName){
        String path = soundPath(soundName);

        Sound sound = new Sound();

        AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundParameter(sound));
        desc.errored = Throwable::printStackTrace;

        return sound;
    }
    protected static String soundPath(String soundName){
        String name = "sounds/" + soundName;
        return Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";
    }

    protected static Fi soundFile(String soundName){
        return Vars.tree.get(soundPath(soundName));
    }
}
