package progressed.world.blocks.defence.turret.payload;

import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.payloads.*;

public class SinglePayloadAmmoTurret extends PayloadAmmoTurret{
    float minLoadWarmup = 0f;

    public SinglePayloadAmmoTurret(String name){
        super(name);

        maxAmmo = 1;
        linearWarmup = true;

        //PayloadAmmoTurret doesn't hav this. (TODO: Remove this line after my PR, as it'll become redundant.)
        acceptsPayload = true;
    }

    public class SinglePayloadAmmoTurretBuild extends PayloadTurretBuild{
        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return shootWarmup <= minLoadWarmup && super.acceptPayload(source, payload);
        }
    }
}
