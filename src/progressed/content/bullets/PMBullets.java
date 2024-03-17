package progressed.content.bullets;

import arc.graphics.*;
import blackhole.entities.bullet.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.bullet.unit.*;
import progressed.entities.effect.*;

public class PMBullets{
    public static BulletType

    magnetCopper, magnetTitanium, magenetTenelium,

    pixel,

    syringe,

    smallFlare, mediumFlare, largeFlare,

    pillarField,
    
    blackHole,
    
    excaliburLaser, sentinelLaser,

    harmanuke,

    burstDriverOrb;

    public static void load(){
        magnetCopper = new MagnetBulletType(2.5f, 12f){{
            lifetime = 300f;
            scaledForce = 0.3f;
            attractRange = 12f * 8f;
            frontColor = Color.valueOf("EAC2A9");
            backColor = Color.valueOf("B8705C");
        }};

        magnetTitanium = new MagnetBulletType(2.8f, 25f){{
            lifetime = 400f;
            scaledForce = 0.8f;
            attractRange = 15f * 8f;
            frontColor = Color.valueOf("A4B8FA");
            backColor = Color.valueOf("7575C8");
        }};

        magenetTenelium = new MagnetBulletType(3.4f, 45f){{
            lifetime = 500f;
            scaledForce = 1.7f;
            attractRange = 21f * 8f;
            frontColor = Color.valueOf("B0BAC0");
            backColor = Color.valueOf("6E7080");
        }};

        pixel = new BitBulletType(2f, 5f){{
            lifetime = 90f;
            splashDamage = 27f;
            splashDamageRadius = 40f;
            hitSize = size = 8f;
            homingPower = 0.01f;
            homingRange = 160f;
            knockback = 3f;
            weaveScale = 10f;
            weaveMag = 2f;
            trailDelay = 7.5f;
        }};

        syringe = new InjectorBulletType(3f, 7f){{
            nanomachines = true;
            vaccines = new Vaccine[]{
                new Vaccine(PMStatusEffects.vcFrenzy),
                new Vaccine(PMStatusEffects.vcDisassembly),
                new Vaccine(PMStatusEffects.vcWeaken),
                new Vaccine(PMStatusEffects.vcCorvus)
            };
            width = height = 8f;
            lifetime = 50f;
            ammoMultiplier = 4;
        }};

        smallFlare = new SignalFlareBulletType(8f, 60f, PMUnitTypes.flareSmall){{
            size = 4f;
            spinSpeed = 3f;
        }};

        mediumFlare = new SignalFlareBulletType(7f, 70f, PMUnitTypes.flareMedium){{
            size = 8f;
            spinSpeed = 5f;
        }};

        largeFlare = new SignalFlareBulletType(6f, 80f, PMUnitTypes.flareLarge){{
            size = 12f;
            spinSpeed = 8f;
        }};

        pillarField = new PillarFieldBulletType(){{
            lifetime = 90f;
            radius = 5f * 8f;
            amount = 10;
        }};

        blackHole = new BlackHoleBulletType(0.5f, 1400f / 30f){{
            lifetime = 630f;
            lightRadius = 8f;
            lightOpacity = 0.7f;
            int times = 25;
            float life = EnergyFx.kugelblitzGrow.lifetime;
            chargeEffect = new MultiEffect(
                new WrapEffect(new RepeatEffect(EnergyFx.kugelblitzCharge, (life - EnergyFx.kugelblitzCharge.lifetime - 1f) / times, times), Color.black, 48f),
                EnergyFx.kugelblitzGrow
            );
            chargeEffect.lifetime = life;
            despawnEffect = EnergyFx.blackHoleDespawn;
        }};

        excaliburLaser = new CrossLaserBulletType(1500f){{
            length = 800f;
            width = 26f;
            growTime = 10f;
            fadeTime = 40f;
            lifetime = 70f;
            crossLength = 300f;
            crossWidth = 18f;
            colors = new Color[]{
                Color.valueOf("E8D174").a(0.4f),
                Pal.surge,
                Color.white
            };
        }};

        sentinelLaser = new LaserBlastBulletType(12f, 150f){{
            lifetime = 36f;
            splashDamage = 450f;
            splashDamageRadius = 6f * 8f;
            scaledSplashDamage = true;
            buildingDamageMultiplier = 0.3f;
            length = 8f;
            width = 3f;
            trailLength = 12;
            makeFire = true;
            trailColor = hitColor = Pal.lancerLaser;
            hitEffect = EnergyFx.sentinelBlast;
            hitSound = Sounds.shockBlast;
            hitSoundVolume = 4f;
        }};

        harmanuke = new BasicBulletType(){{
            sprite = "large-bomb";
            width = height = 120/4f;

            backColor = Color.red;
            frontColor = Color.white;
            mixColorTo = Color.white;

            hitSound = Sounds.plasmaboom;

            hitShake = 4f;

            lifetime = 70f;

            despawnEffect = EnergyFx.redBomb;
            hitEffect = Fx.massiveExplosion;
            keepVelocity = false;
            spin = 2f;

            shrinkX = shrinkY = 0.7f;

            speed = 0f;
            collides = false;

            splashDamage = 1000f;
            splashDamageRadius = 80f;
        }};

        burstDriverOrb = new BurstDriverOrb();

        MinigunBullets.load();
        SniperBullets.load();
        ModuleBullets.load();
        PayloadBullets.load();
    }
}
