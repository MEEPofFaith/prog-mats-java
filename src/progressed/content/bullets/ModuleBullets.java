package progressed.content.bullets;

import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;
import progressed.entities.bullet.explosive.*;
import progressed.graphics.*;

public class ModuleBullets{
    public static BulletType

    firestormMissile;

    public static void load(){
        firestormMissile = new BallisticMissileBulletType("prog-mats-firestorm-missile"){{
            lifetime = 75f;
            splashDamage = 170f;
            splashDamageRadius = 32f;
            buildingDamageMultiplier = 0.3f;
            hitShake = 3f;
            ammoMultiplier = 12;

            height = 32f;
            trailLength = 15;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileBasic;
            hitSound = Sounds.explosion;

            hitEffect = MissileFx.smallBoom;
            blockEffect = MissileFx.missileBlockedSmall;
        }};
    }
}
