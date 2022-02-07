package progressed.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.weather.*;
import mindustry.world.meta.*;

/**
 * These are just experimental, and are not actually used anywhere.
 */
public class PMWeathers{
    public static Weather
    overdrive;

    public static void load(){
        overdrive = new ParticleWeather("overdrive"){
            {
                color = noiseColor = Color.valueOf("feb380");
                particleRegion = "particle";
                sizeMax = 16f;
                sizeMin = 1.3f;
                density = 8000f;
                attrs.set(Attribute.light, 1.5f);
                status = StatusEffects.overclock;

                sound = Sounds.techloop;
                soundVol = 0f;
                soundVolOscMag = 0.09f;
                soundVolOscScl = 1100f;
                soundVolMin = 0.01f;
            }

            @Override
            public void update(WeatherState state){
                Groups.build.each(b -> {
                    b.applyBoost(state.intensity * 4f, 120f);
                });
                
                super.update(state);
            }
        };
    }
}
