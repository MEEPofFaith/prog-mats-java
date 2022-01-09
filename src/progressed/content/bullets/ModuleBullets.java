package progressed.content.bullets;

import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;

public class ModuleBullets implements ContentList{
    public static BulletType

    shotgunCopper, shotgunDense, shotgunTitanium, shotunThorium,

    swarmDense;

    @Override
    public void load(){
        shotgunCopper = new BasicBulletType(4f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 120f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            ammoMultiplier = 2;
        }};

        shotgunDense = new BasicBulletType(6f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 4;
            lifetime = 120f;
        }};

        shotgunTitanium = new BasicBulletType(5f, 16f){{
            width = 8;
            height = 10;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 3;
            lifetime = 120;
        }};

        shotunThorium = new BasicBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 3;
            lifetime = 120f;
        }};

        swarmDense = new MissileBulletType(4.2f, 13){{
            homingPower = 0.12f;
            width = 8f;
            height = 8f;
            shrinkX = shrinkY = 0f;
            drag = -0.005f;
            homingRange = 80f;
            splashDamageRadius = 35f;
            splashDamage = 27f;
            lifetime = 62f;
            trailColor = Pal.bulletYellowBack;
            backColor = Pal.bulletYellowBack;
            frontColor = Pal.bulletYellow;
            hitEffect = Fx.blastExplosion;
            despawnEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
        }};
    }
}