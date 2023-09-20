package progressed.content.bullets;

import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.*;

public class ModuleBullets{
    public static BulletType

    firestormMissile;

    public static void load(){
        firestormMissile = new ArcMissileBulletType("prog-mats-firestorm-missile"){{
            lifetime = 75f;
            splashDamage = 170f;
            splashDamageRadius = 32f;
            buildingDamageMultiplier = 0.3f;
            hitShake = 3f;
            collidesAir = false;
            ammoMultiplier = 12;

            accel = 0.2f;
            gravity = 0.3f;
            trailLength = 15;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileBasic;
            hitSound = Sounds.explosion;

            hitEffect = MissileFx.smallBoom;
            absorbEffect = Pseudo3DFx.absorbedSmall;
        }};
    }
}
