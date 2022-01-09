package progressed.content.bullets;

import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.unit.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadBullets implements ContentList{
    public static BulletType

    barrageLaunch, downpourLaunch, rapierLaunch,

    arbalestBasic, arbalestIncend, arbalestBomber,

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
            trailEffect = PMFx.rocketTrail;
            trailLength = 6;
            drawSize = 60f * 80f;
            layer = Layer.turret + 0.015f;
            riseStart = thrusterGrowth;
            riseEnd = thrusterGrowth + 10f;
            targetLayer = Layer.bullet - 1;

            splashDamage = 526f;
            splashDamageRadius = 8f * tilesize;
            homingPower = 0.2f;
            homingDelay = 5f;
            homingRange = 100f * tilesize;

            unitSort = UnitSorts.strongest;
        }};

        arbalestIncend = new RocketBulletType(4f, 37f, "prog-mats-incendiary-rocket"){
            {
                lifetime = 70f;
                acceleration = 0.03f;
                backSpeed = thrustDelay = 0f;
                trailWidth = thrusterSize = 6f / 4f;
                trailParam = thrusterSize * 2f * 1.5f;
                trailOffset = thrusterOffset = 43f / 4f;
                rotOffset = 90f;
                hitEffect = despawnEffect = PMFx.missileExplosion;
                trailInterval = 1f;
                trailEffect = PMFx.rocketTrail;
                trailLength = 6;
                drawSize = 60f * 80f;
                layer = Layer.turret + 0.015f;
                riseStart = thrusterGrowth;
                riseEnd = thrusterGrowth + 10f;
                targetLayer = Layer.bullet - 1;

                splashDamage = 276f;
                splashDamageRadius = 88f;
                makeFire = true;
                status = PMStatusEffects.incendiaryBurn;
                statusDuration = 15f * 60f;
                homingPower = 0.2f;
                homingDelay = 5f;
                homingRange = 100f * tilesize;

                unitSort = UnitSorts.strongest;
            }

            @Override
            public void hit(Bullet b, float x, float y){
                PMFx.flameRing.at(x, y, b.rotation(), hitColor);
                super.hit(b, x, y);
            }
        };

        arbalestBomber = new RocketBulletType(4f, 12f, "prog-mats-bomber-rocket"){
            {
                lifetime = 70f;
                acceleration = 0.03f;
                backSpeed = thrustDelay = 0f;
                trailWidth = thrusterSize = 6f / 4f;
                trailParam = thrusterSize * 2f * 1.5f;
                trailOffset = thrusterOffset = 43f / 4f;
                rotOffset = 90f;
                despawnEffect = PMFx.missileExplosion;
                trailInterval = 1f;
                trailEffect = PMFx.rocketTrail;
                trailLength = 6;
                drawSize = 60f * 80f;
                layer = Layer.turret + 0.015f;
                riseStart = thrusterGrowth;
                riseEnd = thrusterGrowth + 10f;
                targetLayer = Layer.bullet - 1;

                collidesTiles = false;
                pierce = true;
                splashDamage = 135f;
                splashDamageRadius = 6.5f * tilesize;
                homingPower = 0.25f;
                homingDelay = 5f;
                homingRange = 100f * tilesize;

                bombBullet = new BombBulletType(74f, 4.5f * tilesize){{
                    lifetime = 15f;
                    width =  8f;
                    height = 10f;
                    hitEffect = Fx.flakExplosion;
                    status = StatusEffects.blasted;
                    statusDuration = 60f;
                    collidesAir = true;
                    frontColor = Pal.sapBullet;
                    backColor = Pal.sapBulletBack;
                    layer = Layer.turret + 0.014f;
                }};
                bombInterval = 2f;

                unitSort = UnitSorts.weakest; //Target, bomb, and destroy low health units :)))
            }

            @Override
            public void hit(Bullet b, float x, float y){
                //no
            }

            @Override
            public void despawned(Bullet b){
                hitEffect.at(b.x, b.y, b.rotation(), hitColor);
                hitSound.at(b.x, b.y, hitSoundPitch, hitSoundVolume);

                Effect.shake(hitShake, hitShake, b);

                Damage.damage(b.team, b.x, b.y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
            }
        };

        float firestormSize = 0.125f,
            trinitySize = 1f,
            strikedownRnd = 1.5f,
            trinityRnd = 3f;

        firestormMissile = new ArcMissileBulletType(2.4f, 28f, "prog-mats-storm-missile"){{
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

            shadowOffset = 150f;
            riseTime = 30f;
            fallTime = 20f;

            autoDropRadius = 0f;
            stopRadius = 8f;
            resumeSeek = false;
            riseEngineSize = fallEngineSize = 5f;
            trailSize = firestormSize;
            targetRadius = 0.5f;
        }};

        strikedownBasic = new ArcMissileBulletType(2f, 80f, "prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            homingPower = 0.05f;
            homingRange = 330f;
            lifetime = 360f;
            hitSound = Sounds.explosionbig;
            hitShake = 5f;
            despawnEffect = PMFx.missileExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileBasic;

            autoDropRadius = 15f;
            stopRadius = 10f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            shadowOffset = 300f;
            riseTime = 45f;
            fallTime = 30f;
            trailRnd = strikedownRnd;

            unitSort = UnitSorts.strongest;
        }};

        strikedownRecursive = new ArcMissileBulletType(2f, 80f, "prog-mats-recursive-missile"){{
            splashDamage = 200f;
            splashDamageRadius = 48f;
            homingPower = 0.05f;
            homingRange = 330f;
            lifetime = 270f;
            hitSound = Sounds.explosionbig;
            hitShake = 5f;
            despawnEffect = PMFx.missileExplosion;
            blockEffect = PMFx.missileBlocked;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 15f;
            stopRadius = 10f;
            riseEngineSize = 16f;
            fallEngineSize = 8f;
            elevation = 2f;
            shadowOffset = 300f;
            riseTime = 45f;
            fallTime = 30f;
            trailRnd = strikedownRnd;

            unitSort = UnitSorts.strongest;

            splitBullets = 3;
            splitVelocityMin = 0.8f;
            splitVelocityMax = 1.2f;
            splitBullet = new ArcMissileBulletType(2.5f, 80f, "prog-mats-recursive-missile"){{
                splashDamage = 100f;
                splashDamageRadius = 40f;
                homingPower = 0.07f;
                homingRange = 330f;
                lifetime = 135f;
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
                elevation = 2f;
                shadowOffset = 260f;
                riseTime = -1f;
                fallTime = 30f;
                randRot = true;

                unitSort = UnitSorts.strongest;

                splitBullets = 3;
                splitVelocityMin = 0.8f;
                splitVelocityMax = 1.2f;
                splitBullet = new ArcMissileBulletType(3f, 80f, "prog-mats-recursive-missile"){{
                    splashDamage = 50f;
                    splashDamageRadius = 32f;
                    homingPower = 0.1f;
                    homingRange = 330f;
                    lifetime = 96f;
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
                    elevation = 2f;
                    shadowOffset = 230f;
                    riseTime = -1f;
                    fallTime = 30f;
                    randRot = true;

                    unitSort = UnitSorts.strongest;
                }};
            }};
        }};

        trinityBasic = new ArcMissileBulletType(1f, 300f, "prog-mats-basic-nuke"){{
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
            elevation = 3f;
            shadowOffset = 900f;
            riseTime = 90f;
            fallTime = 75f;
            trailSize = trinitySize;
            trailRnd = trinityRnd;
            targetRadius = 2f;

            unitSort = UnitSorts.strongest;
        }};

        trinityCluster = new ArcMissileBulletType(1.25f, 0f, "prog-mats-cluster-nuke"){{
            homingPower = 0.05f;
            homingRange = 2200f;
            lifetime = 2400f;
            hitSound = Sounds.none;
            hitShake = 0f;
            despawnEffect = hitEffect = Fx.none;

            splitBullets = 20;
            splitBullet = new ArcMissileBulletType(1f, 80f, "prog-mats-recursive-missile"){{
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
                elevation = 3f;
                shadowOffset = 900f;
                riseTime = -1f;
                fallTime = 30;
            }};
            splitVelocityMin = 0.1f;
            splitVelocityMax = 1f;
            splitLifeMin = 0.5f;

            targetColor = PMPal.missileFrag;

            autoDropRadius = 30f;
            stopRadius = 20f;
            riseEngineSize = 24f;
            elevation = 3f;
            shadowOffset = 900f;
            riseTime = 90f;
            fallTime = -1f;
            trailSize = trinitySize;
            trailRnd = trinityRnd;
            targetRadius = 2f;

            unitSort = UnitSorts.strongest;
        }};
    }
}