package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.SoundLoader.*;
import arc.audio.*;
import mindustry.*;

/**
 * @author GlennFolker
 * For how to make this. Just copy over and adjust the code.
 */
public class PMSounds{
    public static Sound

    pixelShoot,
    pixelHit,
    rockExplode,
    harbingerCharge,
    harbingerBlast,
    riftSplit,
    rocketLaunch,
    nuclearExplosion,
    sentinelWarning,
    pulseBeam,
    moonPiss,
    loudMoonPiss,
    gigaFard ;

    public static void load() {
        pixelShoot = Vars.tree.loadSound("pixel-shoot");
        pixelHit = Vars.tree.loadSound("pixel-hit");
        rockExplode = Vars.tree.loadSound("rock-explode");
        harbingerCharge = Vars.tree.loadSound("harbinger-charge");
        harbingerBlast = Vars.tree.loadSound("harbinger-blast");
        riftSplit = Vars.tree.loadSound("rift-split");
        nuclearExplosion = Vars.tree.loadSound("nuclear-explosion");
        rocketLaunch = Vars.tree.loadSound("rocket");
        sentinelWarning = Vars.tree.loadSound("sentinel-warning");
        pulseBeam = Vars.tree.loadSound("pulse-beam");
        moonPiss = Vars.tree.loadSound("piss");
        loudMoonPiss = Vars.tree.loadSound("piss-loud");
        gigaFard = Vars.tree.loadSound("giga-fard");
    }
}
