package progressed.world.meta;

import arc.struct.*;
import mindustry.world.meta.*;

public class ModuleStats extends Stats{
    public ModuleStats(Stats base){
        for(StatCat cat : base.toMap().keys()){
            OrderedMap<Stat, Seq<StatValue>> map = base.toMap().get(cat);
            for(Stat stat : map.keys()){
                map.get(stat).each(s -> add(stat, s));
            }
        }
    }
}
