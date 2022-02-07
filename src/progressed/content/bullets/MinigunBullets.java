package progressed.content.bullets;

import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;

public class MinigunBullets{
    public static BulletType

    standardCopperMini, standardDenseMini, standardHomingMini, standardIncendiaryMini, standardThoriumMini;

    public static void load(){
        standardCopperMini = new BasicBulletType(2.5f, 19f){{
            width = 5f;
            height = 7f;
            lifetime = 110f;
        }};

        standardDenseMini = new BasicBulletType(3.5f, 42f){{
            width = 5.5f;
            height = 9f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 4;
            lifetime = 76f;
        }};

        standardHomingMini = new BasicBulletType(3f, 24f){{
            width = 4f;
            height = 6f;
            homingPower = 0.07f;
            reloadMultiplier = 1.3f;
            ammoMultiplier = 5;
            lifetime = 92f;
        }};

        standardIncendiaryMini = new BasicBulletType(3.2f, 21f){{
            width = 5f;
            height = 8f;
            frontColor = Pal.lightishOrange;
            backColor = Pal.lightOrange;
            inaccuracy = 5f;
            makeFire = true;
            lifetime = 86f;
            hitEffect = despawnEffect = new MultiEffect(Fx.hitBulletSmall, Fx.fireHit);
        }};

        standardThoriumMini = new BasicBulletType(4f, 54f){{
            width = 6f;
            height = 11f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 4f;
            lifetime = 66f;
        }};
    }
}
