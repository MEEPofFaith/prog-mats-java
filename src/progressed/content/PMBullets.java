package progressed.content;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.bullet.*;
import progressed.graphics.*;

public class PMBullets implements ContentList{
    public static BulletType
    flameMagma, blazeMagma, infernoMagma,

    standardCopperMini, standardDenseMini, standardHomingMini, standardIncendiaryMini, standardThoriumMini,

    sniperBoltSilicon, sniperBoltTitanium, sniperBoltThorium, sniperBoltSurge, sniperBoltTechtaniteFrag, sniperBoltTechtanite,

    magnetCopper, magnetTitanium, magnetTechtanite,

    pixel,

    barrageLaunch, downpourLaunch, rapierLaunch,

    syringe,

    smallFlare, mediumFlare, largeFlare,
    
    blackHole, cataclysm, absorbed,
    
    empParticle,
    
    firestormMissile,

    recursionTwo, recursionOne,
    
    strikedownBasic, strikedownEmp, strikedownRecursive,
    
    arbiterBasic, arbiterClusterFrag, arbiterCluster,
    
    harbingerLaser, excaliburLaser, sentinelLaser,

    burstBolt;

    @Override
    public void load(){
        flameMagma = new MagmaBulletType(50f, 18f){{
            shake = 1f;
        }};

        blazeMagma = new MagmaBulletType(75f, 36f){{
            shake = 2f;
        }};

        infernoMagma = new MagmaBulletType(62.5f, 13f){{
            shake = 0.1f;
        }};

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
        }};

        standardThoriumMini = new BasicBulletType(4f, 54f){{
            width = 6f;
            height = 11f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            ammoMultiplier = 4f;
            lifetime = 66f;
        }};

        sniperBoltSilicon = new CritBulletType(12f, 300f){{
            lifetime = 48f;
            knockback = 4f;
            width = 5f;
            height = 8f;
            pierceCap = 20;
            reloadMultiplier = 1.2f;
            critChance = 0.2f;
            critMultiplier = 2f;
            bouncing = true;
        }};

        sniperBoltTitanium = new CritBulletType(13f, 500f){{
            lifetime = 45f;
            knockback = 5f;
            width = 7f;
            height = 12f;
            pierceCap = 26;
            reloadMultiplier = 1.7f;
            critChance = 0.15f;
            critMultiplier = 2.5f;
        }};

        sniperBoltThorium = new CritBulletType(12f, 800f){{
            lifetime = 51f;
            knockback = 5f;
            width = 8f;
            height = 14f;
            pierceCap = 30;
            critChance = 0.1f;
            critMultiplier = 4.5f;
        }};

        sniperBoltSurge = new CritBulletType(14f, 1000f){{
            frontColor = Color.valueOf("F3E979");
            backColor = hitColor = Color.valueOf("D99F6B");
            lifetime = 42f;
            knockback = 5f;
            width = 10f;
            height = 19f;
            trailLength = 11;
            pierceCap = 40;
            knockback = 7f;
            reloadMultiplier = 0.7f;
            lightning = 5;
            lightningLength = 3;
            lightningLengthRand = 2;
            lightningDamage = 40f;
            critChance = 0.20f;
            critMultiplier = 5f;
        }};

        sniperBoltTechtaniteFrag = new CritBulletType(13f, 160f){{
            lifetime = 23f;
            knockback = 3f;
            width = 6f;
            height = 14f;
            pierceCap = 12;
            critMultiplier = 3f;
            critEffect = PMFx.sniperCritMini;
        }};

        sniperBoltTechtanite = new CritBulletType(13f, 800f){
            final float fragInacc = 5f;

            {
                lifetime = 23f;
                knockback = 4f;
                width = 9f;
                height = 16f;
                pierceCap = 13;
                fragBullets = 5;
                fragBullet = sniperBoltTechtaniteFrag;
                fragVelocityMin = 0.8f;
                fragVelocityMax = 1.2f;
                fragCone = 30f;
                critChance = 0.1f;
                critMultiplier = 3f;
                despawnHitEffects = false;
            }

            @Override
            public void hit(Bullet b, float x, float y){
                b.hit = true;
                if(!((CritBulletData)b.data).despawned){
                    hitEffect.at(x, y, b.rotation(), hitColor);
                    hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
                }

                Effect.shake(hitShake, hitShake, b);
            }

            @Override
            public void despawned(Bullet b){ //Only frag on despawn
                CritBulletData data = (CritBulletData)b.data;
                for(int i = 0; i < fragBullets; i++){
                    float a = b.rotation() + ((fragCone / fragBullets) * (i - (fragBullets - 1f) / 2f)) + Mathf.range(fragInacc);
                    if(fragBullet instanceof CritBulletType critB){
                        Bullet bullet = critB.create(b.owner, b.team, b.x, b.y, a, -1f, Mathf.random(fragVelocityMin, fragVelocityMax), 1f, new CritBulletData(data.crit, data.trail.copy()));
                        if(b.collided.size > 0) bullet.collided.add(b.collided.peek());
                    }
                }
                Sounds.missile.at(b, Mathf.random(0.9f, 1.1f));

                super.despawned(b);
            }
        };

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

        magnetTechtanite = new MagnetBulletType(3.4f, 45f){{
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

        barrageLaunch = new SentryBulletType(PMUnitTypes.barrage);
        downpourLaunch = new SentryBulletType(PMUnitTypes.downpour);
        rapierLaunch = new SentryBulletType(PMUnitTypes.rapier);

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

        blackHole = new BlackHoleBulletType(0.5f, 1400f / 30f){{
            lifetime = 630f;
            backMove = false;
            lightRadius = 8f;
            lightOpacity = 0.7f;
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
            public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
                //do nothing
            }

            @Override
            public void update(Bullet b){
                //Do nothing
            }
        };

        empParticle = new ParticleBulletType(){{
            particleSound = Sounds.spark;
            hitColor = trailColor = Pal.lancerLaser;
            status = PMStatusEffects.emp;
            statusDuration = 60f * 10f;
        }};
        
        firestormMissile = new StrikeBulletType(2.4f, 28f, "prog-mats-storm-missile"){{
            splashDamage = 72f;
            splashDamageRadius = 30f;
            lifetime = 90f;
            homingPower = 0.035f;
            homingRange = 200f;
            ammoMultiplier = 4f;
            hitSound = Sounds.explosion;
            collidesAir = false;
            hitShake = 2f;
            despawnEffect = PMFx.missileBoom;
            blockEffect = PMFx.missileBlockedSmall;
            
            targetColor = PMPal.missileBasic;

            elevation = 150f;
            riseTime = 30f;
            fallTime = 20f;
            weaveWidth = 12f;
            weaveSpeed = 0.3f;

            autoDropRadius = 0f;
            stopRadius = 8f;
            resumeSeek = false;
            riseEngineSize = fallEngineSize = 5f;
            trailSize = 0.2f;
            targetRadius = 0.5f;
        }};

        strikedownBasic = new StrikeBulletType(2f, 80f, "prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            homingPower = 0.05f;
            homingRange = 330f;
            lifetime = 180f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileBasic;

            autoDropRadius = 15f;
            stopRadius = 10f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 300f;
            riseTime = 45f;
            fallTime = 25f;
            trailSize = 0.7f;
            riseSpin = 300f;
            fallSpin = 135f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        strikedownEmp = new StrikeBulletType(3f, 80f, "prog-mats-emp-missile"){{
            splashDamage = 235f;
            splashDamageRadius = 48f;
            reloadMultiplier = 0.75f;
            homingPower = 0.075f;
            homingRange = 330f;
            lifetime = 120f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            fragBullets = 360;
            fragVelocityMin = 0.5f;
            fragBullet = empParticle;

            targetColor = PMPal.missileEmp;

            autoDropRadius = 35f;
            stopRadius = 10f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 300f;
            riseTime = 35f;
            fallTime = 15f;
            trailSize = 0.7f;
            riseSpin = 270f;
            fallSpin = 90f;
        }};

        recursionTwo = new StrikeBulletType(4f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 50f;
            splashDamageRadius = 32f;
            homingPower = 0.1f;
            homingRange = 330f;
            lifetime = 70f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 15f;
            stopRadius = 10f;
            stopDelay = 30f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 230f;
            riseTime = 25f;
            fallTime = 15f;
            trailSize = 0.7f;
            riseSpin = 190f;
            fallSpin = 110f;
            randRot = true;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        recursionOne = new StrikeBulletType(3f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 100f;
            splashDamageRadius = 40f;
            homingPower = 0.07f;
            homingRange = 330f;
            lifetime = 90f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 15f;
            stopRadius = 10f;
            stopDelay = 45f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 260f;
            riseTime = 35f;
            fallTime = 20f;
            trailSize = 0.7f;
            riseSpin = 230f;
            fallSpin = 120f;
            randRot = true;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;

            fragBullets = 3;
            fragVelocityMin = 0.8f;
            fragVelocityMax = 1.2f;
            fragBullet = recursionTwo;
        }};

        strikedownRecursive = new StrikeBulletType(2f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 200f;
            splashDamageRadius = 48f;
            homingPower = 0.05f;
            homingRange = 330f;
            lifetime = 135f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 15f;
            stopRadius = 10f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 300f;
            riseTime = 45f;
            fallTime = 25f;
            trailSize = 0.7f;
            riseSpin = 300f;
            fallSpin = 135f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;

            fragBullets = 3;
            fragVelocityMin = 0.8f;
            fragVelocityMax = 1.2f;
            fragBullet = recursionOne;
        }};

        arbiterBasic = new StrikeBulletType(1f, 300f, "prog-mats-basic-nuke"){{
            splashDamage = 27000f;
            splashDamageRadius = 240f;
            homingPower = 0.05f;
            homingRange = 2200f;
            lifetime = 5500f;
            hitSound = Sounds.explosionbig;
            hitShake = 30f;
            despawnEffect = PMFx.mushroomCloudExplosion;
            blockEffect = PMFx.missileBlockedLarge;

            targetColor = PMPal.missileBasic;

            autoDropRadius = 30f;
            stopRadius = 20f;
            riseEngineSize = 24f;
            fallEngineSize = 14f;
            trailSize = 0.7f;
            elevation = 900f;
            riseTime = 240f;
            fallTime = 90f;
            trailSize = 2f;
            riseSpin = 720f;
            fallSpin = 180f;
            targetRadius = 2f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        arbiterClusterFrag = new StrikeBulletType(1f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 3000f;
            splashDamageRadius = 40f;
            lifetime = 150f;
            hitSound = Sounds.bang;
            hitShake = 5f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileFrag;

            autoDropRadius = stopRadius = -1f;
            fallEngineSize = 8f;
            elevation = 900f;
            riseTime = -1f;
            fallTime = 75f;
            trailSize = 0.7f;
            fallSpin = 135f;
        }};

        arbiterCluster = new StrikeBulletType(1f, 0f, "prog-mats-cluster-nuke"){{
            homingPower = 0.05f;
            homingRange = 2200f;
            lifetime = 5500f;
            hitSound = Sounds.none;
            hitShake = 0f;
            despawnEffect = hitEffect = Fx.none;

            fragBullets = 20;
            fragBullet = arbiterClusterFrag;
            fragVelocityMin = 0.1f;
            fragVelocityMax = 1f;
            fragLifeMin = 0.5f;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 30f;
            stopRadius = 20f;
            riseEngineSize = 24f;
            trailSize = 0.7f;
            elevation = 900f;
            riseTime = 240f;
            fallTime = -1f;
            trailSize = 2f;
            riseSpin = 720f;
            targetRadius = 2f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        harbingerLaser = new LaserBulletType((float)Double.MAX_VALUE){
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
            }

            @Override
            public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
                super.hitTile(b, build, initialHealth, direct);
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

        sentinelLaser = new TeamLaserBlastBulletType(12f, 150f){{
            lifetime = 36f;
            splashDamage = 1870f;
            splashDamageRadius = 6f * 8f;
            length = 8f;
            width = 3f;
            trailLength = 12;
            makeFire = true;
            hittable = false;
            hitEffect = PMFx.sentinelBlast;
            hitSound = Sounds.explosionbig;
            hitSoundVolume = 4f;
        }};

        burstBolt = new BurstDriverItem();
    }
}