package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.bullet.physical.DelayBulletType.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static void load(){

    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
