package progressed.entities.bullet.energy;

import mindustry.content.*;
import progressed.content.*;
import progressed.graphics.*;

public class MagmaBulletType extends BeamBulletType{
    public MagmaBulletType(float damage, float radius){
        super(damage, radius);

        hitEffect = Fx.fireballsmoke;
        hitColor = PMPal.magma;
        makeFire = makePuddles = true;
        puddleLiquid = PMLiquids.magma;
        puddleAmount = 60f;
        status = StatusEffects.melting;
    }
}
