package progressed.content.bullets;

import arc.graphics.*;
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

public class PMBullets{
    public static BulletType

    magnetCopper, magnetTitanium, magenetTenelium,

    pixel,

    syringe,

    smallFlare, mediumFlare, largeFlare,

    pillarField,
    
    blackHole, cataclysm, absorbed,
    
    harbingerLaser, excaliburLaser, sentinelLaser,

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
            backMove = false;
            lightRadius = 8f;
            lightOpacity = 0.7f;
            //TODO merge
            chargeEffect = new MultiEffect(EnergyFx.kugelblitzChargeBegin, EnergyFx.kugelblitzCharge);
        }};

        cataclysm = new BlackHoleCataclysmType();

        absorbed = new BulletType(0f, 0f){
            @Override
            public void despawned(Bullet b){
                //Do nothing
            }

            @Override
            public void hit(Bullet b, float x, float y){
                //Do nothing
            }

            @Override
            public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
                //do nothing
            }

            @Override
            public void update(Bullet b){
                //Do nothing
            }
        };

        harbingerLaser = new LaserBulletType(Float.POSITIVE_INFINITY){
            {
                colors = new Color[]{Color.valueOf("F3E97966"), Color.valueOf("F3E979"), Color.white};
                length = 900f;
                width = 75f;
                lifetime = 130;
                lightColor = colors[1];
                ammoMultiplier = 1;

                lightningSpacing = 20f;
                lightningLength = 15;
                lightningLengthRand = 10;
                lightningDelay = 0.5f;
                lightningDamage = (float)Double.MAX_VALUE;
                lightningAngleRand = 45f;
                lightningColor = colors[1];

                sideAngle = 25f;
                sideWidth = width / 8f;
                sideLength = length / 1.5f;

                chargeEffect = EnergyFx.harbingerCharge;
            }

            @Override
            public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
                super.hitTile(b, build, x, y, initialHealth, direct);
                if(build.team != b.team) build.kill();
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                super.hitEntity(b, other, initialHealth);
                if(((Teamc)other).team() != b.team) ((Healthc)other).kill();
            }
        };

        excaliburLaser = new CrossLaserBulletType(5000f){{
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
            splashDamage = 950f;
            splashDamageRadius = 6f * 8f;
            buildingDamageMultiplier = 0.3f;
            length = 8f;
            width = 3f;
            trailLength = 12;
            makeFire = true;
            hittable = false;
            trailColor = hitColor = Pal.lancerLaser;
            hitEffect = EnergyFx.sentinelBlast;
            hitSound = Sounds.largeExplosion;
            hitSoundVolume = 4f;
            chargeEffect = EnergyFx.aimChargeBegin;

            //Calculate ahead of time for Sentinel's init() checking the bullet's range.
            range = calculateRange();
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
        ModuleBullets.load();
        SniperBullets.load();
        PayloadBullets.load();
    }
}
