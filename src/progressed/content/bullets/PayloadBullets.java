package progressed.content.bullets;

import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.unit.*;
import progressed.graphics.*;

public class PayloadBullets implements ContentList{
    public static BulletType

    barrageLaunch, downpourLaunch, rapierLaunch,

    arbalestBasic, arbalestIncend,

    firestormMissile, //not payload, but it fits with the other missiles so whatever
    strikedownBasic, strikedownRecursive,
    trinityBasic, trinityCluster;

    @Override
    public void load(){
        barrageLaunch = new SentryBulletType(PMUnitTypes.barrage);
        downpourLaunch = new SentryBulletType(PMUnitTypes.downpour);
        rapierLaunch = new SentryBulletType(PMUnitTypes.rapier);

        arbalestBasic = new RocketBulletType(4f, 45f, "prog-mats-basic-rocket"){{
            lifetime = 70f;
            acceleration = 0.03f;
            backSpeed = thrustDelay = 0f;
            trailWidth = thrusterSize = 6f / 4f;
            trailParam = thrusterSize * 2f * 1.5f;
            trailOffset = thrusterOffset = 43f / 4f;
            rotOffset = 90f;
            hitEffect = despawnEffect = PMFx.missileExplosion;
            trailInterval = 1f;
            trailEffect = PMFx.smokeTrail;
            trailLength = 6;
            drawSize = 60f * 80f;

            splashDamage = 526f;
            splashDamageRadius = 52f;
            homingPower = 0.2f;
            homingDelay = 5f;
            homingRange = 100f * 8f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        arbalestIncend = new RocketBulletType(4f, 37f, "prog-mats-incendiary-rocket"){{
            lifetime = 70f;
            acceleration = 0.03f;
            backSpeed = thrustDelay = 0f;
            trailWidth = thrusterSize = 6f / 4f;
            trailParam = thrusterSize * 2f * 1.5f;
            trailOffset = thrusterOffset = 43f / 4f;
            rotOffset = 90f;
            hitEffect = despawnEffect = PMFx.missileExplosion;
            trailInterval = 1f;
            trailEffect = PMFx.smokeTrail;
            trailLength = 6;
            drawSize = 60f * 80f;

            splashDamage = 276f;
            splashDamageRadius = 88f;
            makeFire = true;
            status = PMStatusEffects.incendiaryBurn;
            statusDuration = 15f * 60f;
            homingPower = 0.2f;
            homingDelay = 5f;
            homingRange = 100f * 8f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        firestormMissile = new StrikeBulletType(2.4f, 28f, "prog-mats-storm-missile"){{
            splashDamage = 72f;
            splashDamageRadius = 30f;
            lifetime = 90f;
            homingPower = 0.035f;
            homingRange = 200f;
            ammoMultiplier = 4f;
            hitSound = Sounds.bang;
            collidesAir = false;
            hitShake = 2f;
            despawnEffect = PMFx.smallBoom;
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
            hitSound = Sounds.explosionbig;
            hitShake = 5f;
            despawnEffect = PMFx.missileExplosion;
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

        strikedownRecursive = new StrikeBulletType(2f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 200f;
            splashDamageRadius = 48f;
            homingPower = 0.05f;
            homingRange = 330f;
            lifetime = 135f;
            hitSound = Sounds.explosionbig;
            hitShake = 5f;
            despawnEffect = PMFx.missileExplosion;
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

            splitBullets = 3;
            splitVelocityMin = 0.8f;
            splitVelocityMax = 1.2f;
            splitBullet = new StrikeBulletType(3f, 80f, "prog-mats-recursive-missile"){{
                splashDamage = 100f;
                splashDamageRadius = 40f;
                homingPower = 0.07f;
                homingRange = 330f;
                lifetime = 90f;
                hitSound = Sounds.explosionbig;
                hitShake = 5f;
                despawnEffect = PMFx.missileExplosion;
                blockEffect = PMFx.missileBlocked;

                targetColor = PMPal.missileFrag;

                autoDropRadius = 15f;
                stopRadius = 10f;
                stopDelay = 55f;
                dropDelay = 35f;
                fallEngineSize = 8f;
                elevation = 260f;
                riseTime = 0f;
                fallTime = 20f;
                trailSize = 0.7f;
                fallSpin = 120f;
                randRot = true;

                unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;

                splitBullets = 3;
                splitVelocityMin = 0.8f;
                splitVelocityMax = 1.2f;
                splitBullet = new StrikeBulletType(4f, 80f, "prog-mats-recursive-missile"){{
                    splashDamage = 50f;
                    splashDamageRadius = 32f;
                    homingPower = 0.1f;
                    homingRange = 330f;
                    lifetime = 70f;
                    hitSound = Sounds.explosionbig;
                    hitShake = 5f;
                    despawnEffect = PMFx.missileExplosion;
                    blockEffect = PMFx.missileBlocked;

                    targetColor = PMPal.missileFrag;

                    autoDropRadius = 15f;
                    stopRadius = 10f;
                    stopDelay = 35f;
                    dropDelay = 25f;
                    fallEngineSize = 8f;
                    elevation = 230f;
                    riseTime = 0f;
                    fallTime = 15f;
                    trailSize = 0.7f;
                    fallSpin = 110f;
                    randRot = true;

                    unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
                }};
            }};
        }};

        trinityBasic = new StrikeBulletType(1f, 300f, "prog-mats-basic-nuke"){{
            splashDamage = 27000f;
            splashDamageRadius = 240f;
            homingPower = 0.05f;
            homingRange = 2200f;
            lifetime = 3000f;
            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            fartVolume = 200f;
            despawnEffect = PMFx.nuclearExplosion;
            blockEffect = PMFx.missileBlockedLarge;

            targetColor = PMPal.missileBasic;

            autoDropRadius = 30f;
            stopRadius = 20f;
            riseEngineSize = 24f;
            fallEngineSize = 14f;
            elevation = 900f;
            riseTime = 240f;
            fallTime = 90f;
            trailSize = 2f;
            riseSpin = 720f;
            fallSpin = 180f;
            targetRadius = 2f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};

        trinityCluster = new StrikeBulletType(1.25f, 0f, "prog-mats-cluster-nuke"){{
            homingPower = 0.05f;
            homingRange = 2200f;
            lifetime = 2400f;
            hitSound = Sounds.none;
            hitShake = 0f;
            despawnEffect = hitEffect = Fx.none;

            splitBullets = 20;
            splitBullet = new StrikeBulletType(1f, 80f, "prog-mats-recursive-missile"){{
                splashDamage = 3000f;
                splashDamageRadius = 40f;
                lifetime = 150f;
                hitSound = Sounds.explosionbig;
                hitShake = 5f;
                despawnEffect = PMFx.missileExplosion;
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
            splitVelocityMin = 0.1f;
            splitVelocityMax = 1f;
            splitLifeMin = 0.5f;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 30f;
            stopRadius = 20f;
            riseEngineSize = 24f;
            elevation = 900f;
            riseTime = 240f;
            fallTime = -1f;
            trailSize = 2f;
            riseSpin = 720f;
            targetRadius = 2f;

            unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
        }};
    }
}