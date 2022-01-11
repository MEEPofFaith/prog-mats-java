package progressed.content.bullets;

import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;

public class ModuleBullets implements ContentList{
    public static BulletType

    shotgunCopper, shotgunDense, shotgunTitanium, shotunThorium,

    swarmIncendiary, swarmBlast;

    @Override
    public void load(){
        shotgunCopper = new BasicBulletType(4f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 240f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            ammoMultiplier = 2;
        }};

        shotgunDense = new BasicBulletType(6f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 4;
            lifetime = 240f;
        }};

        shotgunTitanium = new BasicBulletType(5f, 16f){{
            width = 8;
            height = 10;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 3;
            lifetime = 240;
        }};

        shotunThorium = new BasicBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 3;
            lifetime = 240f;
        }};

        swarmIncendiary = new MissileBulletType(4.2f, 19){{
            frontColor = Pal.lightishOrange;
            backColor = trailColor = Pal.lightOrange;
            homingPower = 0.12f;
            width = 4f;
            height = 4.5f;
            shrinkX = shrinkY = 0f;
            drag = -0.003f;
            homingRange = 80f;
            splashDamageRadius = 20f;
            splashDamage = 24f;
            ammoMultiplier = 5f;
            lifetime = 46f;
            hitEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
            makeFire = true;
            status = StatusEffects.burning;
        }};

        swarmBlast = new MissileBulletType(4.6f, 23){{
            frontColor = Pal.bulletYellow;
            backColor = trailColor = Pal.bulletYellowBack;
            homingPower = 0.15f;
            width = 4.5f;
            height = 5f;
            shrinkX = shrinkY = 0f;
            drag = -0.005f;
            homingRange = 80f;
            splashDamageRadius = 28f;
            splashDamage = 29f;
            ammoMultiplier = 5f;
            lifetime = 40f;
            hitEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
        }};
    }
}