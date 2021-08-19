package progressed.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.audio.*;
import mindustry.*;

//Don't mind me just stealing from BetaMindy.
public class PMSounds{
    public static Sound pixelShoot, pixelHit, harbingerCharge, harbingerBlast, popeshadowCharge, popeshadowBlast, swordStab, sentenelCharge;
    public static final String[] soundFiles = {"pixelShoot", "pixelHit", "harbingerCharge", "harbingerBlast", "popeshadowCharge", "popeshadowBlast", "swordStab", "sentenelCharge"};
    private static int num = 0;
    
    public static void load(){
        num = 0;
        pixelShoot = l();
        pixelHit = l();
        harbingerCharge = l();
        harbingerBlast = l();
        popeshadowCharge = l();
        popeshadowBlast = l();
        swordStab = l();
        sentenelCharge = l();
    }

    public static void dispose(){
        num = 0;
        pixelShoot = d();
        pixelHit = d();
        harbingerCharge = d();
        harbingerBlast = d();
        popeshadowCharge = d();
        popeshadowBlast = d();
        swordStab = d();
    }

    protected static Sound l(){
        return loadSound(soundFiles[num++]);
    }

    protected static Sound d(){
        return disposeSound(soundFiles[num++]);
    }

    protected static Sound loadSound(String soundName){
        if(!Vars.headless){
            String name = "sounds/" + soundName;
            String path = name + ".ogg";

            Sound sound = new Sound();

            AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundLoader.SoundParameter(sound));
            desc.errored = Throwable::printStackTrace;

            return sound;
        }else{
            return new Sound();
        }
    }

    protected static Sound disposeSound(String soundName){
        if(!Vars.headless){
            String name = "sounds/" + soundName;
            String path = name + ".ogg";

            if(Core.assets.isLoaded(path, Sound.class)){
                Core.assets.unload(path);
            }
        }

        return null;
    }
}