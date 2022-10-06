package progressed.content.bullets;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.unit.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadBullets{
    public static SentryBulletType

    barrageLaunch, downpourLaunch, rapierLaunch;

    public static RocketBulletType

    arbalestBasic, arbalestIncend, arbalestBomber;

    public static BallisticMissileBulletType
        artemisBasic, artemisRecursive,
    paragonBasic, paragonCluster,
    ohno;

    public static void load(){
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
            hitEffect = despawnEffect = MissileFx.missileExplosion;
            trailInterval = 1f;
            trailEffect = MissileFx.rocketTrail;
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
                hitEffect = despawnEffect = MissileFx.missileExplosion;
                trailInterval = 1f;
                trailEffect = MissileFx.rocketTrail;
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
                if(!b.absorbed) MissileFx.flameRing.at(x, y, b.rotation(), hitColor);
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
                despawnEffect = MissileFx.missileExplosion;
                trailInterval = 1f;
                trailEffect = MissileFx.rocketTrail;
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

        artemisBasic = new BallisticMissileBulletType("prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            hitShake = 5f;

            trailLength = 25;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        artemisRecursive = new BallisticMissileBulletType("prog-mats-recursive-missile"){{
            status = StatusEffects.none;

            height *= 1.5;
            zoneRadius = 5f * 8f;
            trailLength = 25;
            trailColor = targetColor = PMPal.missileFrag;

            splitTime = 1f / 3f;
            fragRandomSpread = 80f;
            fragBullets = 3;
            fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                status = StatusEffects.none;

                height *= 1.5;
                zoneRadius = 3.5f * 8f;
                trailLength = 20;
                trailColor = targetColor = PMPal.missileFrag;

                fragRandomSpread = 64f;
                fragBullets = 3;
                fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                    splashDamage = 260f;
                    splashDamageRadius = 48f;
                    hitShake = 5f;

                    height *= 1.5;
                    zoneRadius = 2.5f * 8f;
                    trailLength = 15;
                    trailColor = targetColor = PMPal.missileFrag;

                    blockEffect = MissileFx.missileBlockedSmall;
                }};
            }};
        }};

        paragonBasic = new BallisticMissileBulletType("prog-mats-basic-nuke"){{
            splashDamage = 27000f;
            splashDamageRadius = 240f;
            lifetime = 5f * 60f;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            fartVolume = 200f;
            despawnEffect = MissileFx.nuclearExplosion;
            blockEffect = MissileFx.missileBlockedLarge;

            height *= 5;
            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 4f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        paragonCluster = new BallisticMissileBulletType("prog-mats-cluster-nuke"){{
            status = StatusEffects.none;

            lifetime = 5f * 60f;

            height *= 5;
            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 4f;
            trailColor = targetColor = PMPal.missileFrag;

            splitTime = 1f / 2f;
            splitLifeMaxOffset = 45f;
            fragRandomSpread = 20f * 8f;
            fragBullets = 20;

            fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                splashDamage = 3000f;
                splashDamageRadius = 40f;
                lifetime = 5f * 60f;

                hitSound = Sounds.explosionbig;
                hitShake = 5f;
                despawnEffect = MissileFx.missileExplosion;
                blockEffect = MissileFx.missileBlocked;

                height *= 5;
                zoneRadius = 4f * 8f;
                trailLength = 35;
                trailColor = targetColor = PMPal.missileFrag;
            }};
        }};

        ohno = (BallisticMissileBulletType)paragonCluster.copy();
        ohno.sprite = "prog-mats-send-help";
        ohno.targetColor = ohno.trailColor = Color.red;

        BallisticMissileBulletType stop = (BallisticMissileBulletType)artemisRecursive.copy();
        stop.speed = 1;

        RocketBulletType cease = (RocketBulletType)arbalestIncend.copy();
        cease.layer = Layer.bullet - 1;
        cease.homingRange = -1f;
        cease.fragBullets = 1;
        cease.fragLifeMin = 1.5f;
        cease.fragLifeMax = 3f;
        cease.fragBullet = stop;

        BallisticMissileBulletType enough = (BallisticMissileBulletType)paragonBasic.copy();
        enough.fragBullets = 10;
        enough.fragBullet = cease;

        ohno.fragBullet = enough;
        ohno.fragRandomSpread = 40f * 8f;
    }
}
