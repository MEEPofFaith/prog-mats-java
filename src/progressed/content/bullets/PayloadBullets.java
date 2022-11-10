package progressed.content.bullets;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
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

    barrageLaunch, downpourLaunch;

    public static RocketBulletType

    arbalestBasic, arbalestIncend; //TODO 3rd rocket

    public static BallisticMissileBulletType
    artemisBasic, artemisRecursive, //TODO 3rd missile
    paragonBasic, paragonCluster, //TODO 3rd nuke
    ohno;

    public static void load(){
        barrageLaunch = new SentryBulletType(PMUnitTypes.barrage);
        downpourLaunch = new SentryBulletType(PMUnitTypes.downpour);

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

        artemisBasic = new BallisticMissileBulletType("prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            buildingDamageMultiplier = 0.5f;
            hitShake = 5f;

            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        artemisRecursive = new BallisticMissileBulletType("prog-mats-recursive-missile"){{
            status = StatusEffects.none;

            height *= 1.5;
            zoneRadius = 5f * 8f;
            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileFrag;

            splitLifeMaxOffset = 30f;
            fragRandomSpread = 80f;
            fragBullets = 3;
            fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                status = StatusEffects.none;

                height *= 1.5;
                zoneRadius = 3.5f * 8f;
                trailLength = 20;
                trailWidth = 1f;
                trailColor = targetColor = PMPal.missileFrag;

                fragRandomSpread = 64f;
                fragBullets = 3;
                fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                    splashDamage = 260f;
                    splashDamageRadius = 48f;
                    buildingDamageMultiplier = 0.5f;
                    hitShake = 5f;

                    height *= 1.5;
                    zoneRadius = 2f * 8f;
                    trailLength = 15;
                    trailWidth = 1f;
                    trailColor = targetColor = PMPal.missileFrag;

                    blockEffect = MissileFx.missileBlockedSmall;
                }};
            }};
        }};

        paragonBasic = new BallisticMissileBulletType("prog-mats-basic-nuke"){{
            splashDamage = 27000f;
            splashDamageRadius = 240f;
            buildingDamageMultiplier = 0.5f;
            lifetime = 5f * 60f;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            fartVolume = 200f;
            despawnEffect = MissileFx.nuclearExplosion;
            blockEffect = MissileFx.missileBlockedLarge;

            height *= 4;
            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 1.5f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        paragonCluster = new BallisticMissileBulletType("prog-mats-cluster-nuke"){{
            status = StatusEffects.none;

            lifetime = 5f * 60f;

            height *= 6;
            zoneRadius = 12f * 8f;
            trailLength = 35;
            trailWidth = 1.5f;
            trailColor = targetColor = PMPal.missileFrag;

            splitTime = 0.65f;
            splitLifeMaxOffset = 45f;
            fragRandomSpread = 20f * 8f;
            fragBullets = 20;

            fragBullet = new BallisticMissileBulletType("prog-mats-cluster-nuke-split"){{
                splashDamage = 3500f;
                splashDamageRadius = 40f;
                buildingDamageMultiplier = 0.5f;
                lifetime = 5f * 60f;

                homingPower = 0.5f;
                homingRange = 30f * 8f;

                hitShake = 5f;
                despawnEffect = MissileFx.missileExplosion;
                blockEffect = MissileFx.missileBlocked;

                height *= 6;
                trailLength = 35;
                trailWidth = 1f;
                trailColor = targetColor = PMPal.missileFrag;
            }};
        }};

        ohno = (BallisticMissileBulletType)paragonCluster.copy();
        ohno.sprite = "prog-mats-sandbox-nuke";
        ohno.targetColor = ohno.trailColor = Color.red;
        ohno.rangeChange = 500 * 8;

        BallisticMissileBulletType stop = (BallisticMissileBulletType)artemisRecursive.copy();
        stop.speed = 1;

        BallisticMissileBulletType tooFar = (BallisticMissileBulletType)stop.fragBullet.fragBullet.copy();
        tooFar.buildingDamageMultiplier = 1f;
        stop.fragBullet.fragBullet = tooFar;

        RocketBulletType cease = (RocketBulletType)arbalestIncend.copy();
        cease.layer = Layer.bullet - 1;
        cease.homingRange = -1f;
        cease.fragBullets = 1;
        cease.fragLifeMin = 1.5f;
        cease.fragLifeMax = 3f;
        cease.fragBullet = stop;

        BallisticMissileBulletType enough = (BallisticMissileBulletType)paragonBasic.copy();
        enough.buildingDamageMultiplier = 1f;
        enough.fragBullets = 10;
        enough.fragBullet = cease;
        enough.height = ohno.height;

        ohno.fragBullet = enough;
        ohno.fragRandomSpread = 40f * 8f;
        ohno.splitLifeMaxOffset = 45f;
    }
}
