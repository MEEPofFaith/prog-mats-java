package progressed.content.bullets;

import arc.graphics.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;
import progressed.entities.bullet.physical.*;

public class SniperBullets{
    public static BulletType

    sniperBoltSilicon, sniperBoltTitanium, sniperBoltThorium, sniperBoltSurge, sniperBoltTenliumFrag, sniperBoltTenelium;

    public static void load(){
        sniperBoltSilicon = new CritBulletType(12f, 150f){{
            lifetime = 48f;
            knockback = 4f;
            width = 5f;
            height = 8f;
            pierceCap = 20;
            reloadMultiplier = 1.2f;
            critChance = 0.1f;
            critMultiplier = 2f;
            bouncing = true;
        }};

        sniperBoltTitanium = new CritBulletType(13f, 250f){{
            lifetime = 45f;
            knockback = 5f;
            width = 7f;
            height = 12f;
            pierceCap = 26;
            reloadMultiplier = 1.7f;
            critChance = 0.08f;
            critMultiplier = 2.5f;
        }};

        sniperBoltThorium = new CritBulletType(12f, 400f){{
            lifetime = 51f;
            knockback = 5f;
            width = 8f;
            height = 14f;
            pierceCap = 30;
            critChance = 0.05f;
            critMultiplier = 4.5f;
        }};

        sniperBoltSurge = new CritBulletType(14f, 500f){{
            frontColor = Color.valueOf("F3E979");
            backColor = hitColor = Color.valueOf("D99F6B");
            lifetime = 42f;
            width = 10f;
            height = 19f;
            trailLength = 11;
            pierceCap = 40;
            knockback = 7f;
            reloadMultiplier = 0.7f;
            lightning = 5;
            lightningLength = 3;
            lightningLengthRand = 2;
            lightningDamage = 20f;
            critChance = 0.1f;
            critMultiplier = 5f;
        }};

        sniperBoltTenliumFrag = new CritBulletType(13f, 80f){{
            lifetime = 23f;
            knockback = 3f;
            width = 6f;
            height = 14f;
            pierceCap = 12;
            critMultiplier = 3f;
            critEffect = OtherFx.miniCrit;
        }};

        sniperBoltTenelium = new CritBulletType(13f, 400f){
            {
                lifetime = 23f;
                knockback = 4f;
                width = 9f;
                height = 16f;
                pierceCap = 13;
                critChance = 0.05f;
                critMultiplier = 3f;
                despawnHitEffects = false;

                setDefaults = false;
                fragOnHit = false;
                fragBullets = 5;
                fragVelocityMin = 0.8f;
                fragVelocityMax = 1.2f;
                fragRandomSpread = 30f;
                fragBullet = sniperBoltTenliumFrag;
            }

            @Override
            public void removed(Bullet b){
                super.removed(b);
                if(b.fdata != 1f) createFrags(b, b.x, b.y);
            }
        };
    }
}
