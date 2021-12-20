package progressed.type.statuseffects;

import arc.struct.*;
import mindustry.type.*;

public class ExclusiveStatusEffect extends PMStatusEffect{
    public Seq<StatusEffect> exclusives;

    public ExclusiveStatusEffect(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        //This effect gets replaced by the new effect.
        exclusives.each(s -> {
            transitions.put(s, ((unit, result, time) -> result.set(s, time)));
            opposites.add(s);
        });
    }
}