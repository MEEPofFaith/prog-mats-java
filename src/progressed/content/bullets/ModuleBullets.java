package progressed.content.bullets;

import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.entities.bullet.physical.*;

public class ModuleBullets implements ContentList{
    public static BulletType

    shotgunCopper, shotgunDense, shotgunTitanium, shotgunThorium,

    shotgunCopperCrit, shotgunDenseCrit, shotgunTitaniumCrit, shotgunThoriumCrit,

    waterShotMini, cryoShotMini, slagShotMini, oilShotMini,

    swarmIncendiary, swarmBlast;

    @Override
    public void load(){
        shotgunCopper = new BasicBulletType(5f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 240f;
            ammoMultiplier = 1;
        }};

        shotgunDense = new BasicBulletType(6.5f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 3;
            lifetime = 240f;
        }};

        shotgunTitanium = new BasicBulletType(6f, 16f){{
            width = 8;
            height = 10;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            ammoMultiplier = 2;
            lifetime = 240;
        }};

        shotgunThorium = new BasicBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            ammoMultiplier = 3;
            lifetime = 240f;
        }};

        shotgunCopperCrit = new CritBulletType(5f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 240f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            hitEffect = Fx.hitBulletSmall;
            ammoMultiplier = 1;
            trailLength = 3;
            pierce = pierceBuilding = false;

            critChance = 0.04f;
            critMultiplier = 2f;
            critEffect = PMFx.miniCrit;
        }};

        shotgunDenseCrit = new CritBulletType(6.5f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 3;
            lifetime = 240f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierce = pierceBuilding = false;

            critChance = 0.01f;
            critMultiplier = 7f;
            critEffect = PMFx.miniCrit;
        }};

        shotgunTitaniumCrit = new CritBulletType(6f, 16f){{
            width = 8;
            height = 10;
            ammoMultiplier = 2;
            lifetime = 240;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierce = pierceBuilding = false;

            critChance = 0.03f;
            critMultiplier = 3f;
            critEffect = PMFx.miniCrit;
        }};

        shotgunThoriumCrit = new CritBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            ammoMultiplier = 3;
            lifetime = 240f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierce = pierceBuilding = false;

            critChance = 0.02f;
            critMultiplier = 4f;
            critEffect = PMFx.miniCrit;
        }};

        waterShotMini = new LiquidBulletType(Liquids.water){{
            lifetime = 40f;
            speed = 3f;
            knockback = 0.3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
        }};

        cryoShotMini = new LiquidBulletType(Liquids.cryofluid){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
        }};

        slagShotMini = new LiquidBulletType(Liquids.slag){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
            damage = 2.5f;
        }};

        oilShotMini = new LiquidBulletType(Liquids.oil){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
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