package progressed.audio;

import arc.*;
import arc.audio.*;
import arc.util.*;

/** @author GlennFolker */
public class PitchedSoundLoop{
    private final Sound sound;
    private int id = -1;
    private float baseVolume;

    public PitchedSoundLoop(Sound sound, float baseVolume){
        this.sound = sound;
        this.baseVolume = baseVolume;
    }

    public void update(float x, float y, float volume, float pitch){
        if(baseVolume <= 0f) return;

        if(id < 0 && volume > 0.001f){
            id = sound.loop(sound.calcVolume(x, y) * volume * baseVolume, 1f, sound.calcPan(x, y));
        }else if(id >= 0){
            if(volume <= 0.001f){
                Core.audio.stop(id);
                id = -1;
                return;
            }

            Core.audio.set(id, sound.calcPan(x, y), sound.calcVolume(x, y) * volume * baseVolume);
            Core.audio.setPitch(id, pitch);
        }
    }

    public void stop(){
        if(id != -1){
            Core.audio.stop(id);
            id = -1;
            baseVolume = -1f;
        }
    }
}