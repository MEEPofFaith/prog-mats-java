package progressed.content.bullets;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.unit.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadBullets{
    public static SentryBulletType

    barrageLaunch, downpourLaunch;

    public static BulletType

    arbalestBasic, arbalestIncend, arbalestSplitter;

    public static BallisticMissileBulletType
    artemisBasic, artemisRecursive, //TODO 3rd missile
    paragonBasic, paragonCluster, //TODO 3rd nuke
    ohno;

    public static void load(){
        barrageLaunch = new SentryBulletType(PMUnitTypes.barrage);
        downpourLaunch = new SentryBulletType(PMUnitTypes.downpour);

        arbalestBasic = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;

            spawnUnit = new MissileUnitType("basic-rocket-b"){{
                speed = 8f;
                maxRange = 6f;
                lifetime = 3.1f * 60f;
                engineColor = trailColor = Pal.accent;
                engineLayer = Layer.effect;
                engineSize = 3.1f;
                engineOffset = 10f;
                rotateSpeed = 0.5f;
                trailLength = 18;
                missileAccelTime = 2f * 60f;
                lowAltitude = true;
                loopSound = Sounds.missileTrail;
                loopSoundVolume = 0.6f;
                deathSound = Sounds.largeExplosion;

                fogRadius = 6f;

                health = 210;

                weapons.add(new Weapon(){{
                    shootCone = 360f;
                    mirror = false;
                    reload = 1f;
                    deathExplosionEffect = MissileFx.missileExplosion;
                    shootOnDeath = true;
                    shake = 10f;
                    bullet = new ExplosionBulletType(526f, 8f * tilesize){{
                        hitColor = Pal.accent;
                        shootEffect = new MultiEffect(Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
                            lifetime = 10f;
                            strokeFrom = 4f;
                            sizeTo = 130f;
                        }});
                    }};
                }});

                abilities.add(new MoveEffectAbility(){{
                    effect = MissileFx.rocketTrailSmoke;
                    rotateEffect = true;
                    y = -9f;
                    color = Color.grays(0.6f).lerp(Pal.redLight, 0.5f).a(0.4f);
                    interval = 4f;
                }});
            }};
        }};

        arbalestIncend = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;

            spawnUnit = new MissileUnitType("incendiary-rocket-b"){{
                speed = 8f;
                maxRange = 6f;
                lifetime = 3.1f * 60f;
                engineColor = trailColor = Pal.remove;
                engineLayer = Layer.effect;
                engineSize = 3.1f;
                engineOffset = 10f;
                rotateSpeed = 0.5f;
                trailLength = 18;
                missileAccelTime = 2f * 60f;
                lowAltitude = true;
                loopSound = Sounds.missileTrail;
                loopSoundVolume = 0.6f;
                deathSound = Sounds.largeExplosion;

                fogRadius = 6f;

                health = 210;

                weapons.add(new Weapon(){{
                    shootCone = 360f;
                    mirror = false;
                    reload = 1f;
                    deathExplosionEffect = new MultiEffect(MissileFx.missileExplosion, MissileFx.flameRing);
                    shootOnDeath = true;
                    shake = 10f;
                    bullet = new ExplosionBulletType(526f, 8f * tilesize){{
                        hitColor = Pal.remove;
                        shootEffect = new MultiEffect(Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
                            lifetime = 10f;
                            strokeFrom = 4f;
                            sizeTo = 130f;
                        }});

                        makeFire = true;
                        status = PMStatusEffects.incendiaryBurn;
                    }};
                }});

                abilities.add(new MoveEffectAbility(){{
                    effect = MissileFx.rocketTrailSmoke;
                    rotateEffect = true;
                    y = -9f;
                    color = Color.grays(0.6f).lerp(Pal.redLight, 0.5f).a(0.4f);
                    interval = 4f;
                }});
            }};
        }};

        arbalestSplitter = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;
        }};

        artemisBasic = new BallisticMissileBulletType("prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            buildingDamageMultiplier = 0.5f;
            hitShake = 5f;

            height = 24f;
            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        artemisRecursive = new BallisticMissileBulletType("prog-mats-recursive-missile"){{
            status = StatusEffects.none;

            lifetime *= 1.5f;
            height = 36f;
            zoneRadius = 5f * 8f;
            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileFrag;
            posInterp = Interp.smoother;
            vertical = true;

            splitLifeMaxOffset = 30f;
            fragRandomSpread = 80f;
            fragBullets = 3;
            fragBullet = new BallisticMissileBulletType("prog-mats-recursive-missile-split"){{
                status = StatusEffects.none;

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
            lifetime = 3f * 60f;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            fartVolume = 200f;
            despawnEffect = MissileFx.nuclearExplosion;
            blockEffect = MissileFx.missileBlockedLarge;

            height = 70f;
            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 1.5f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        paragonCluster = new BallisticMissileBulletType("prog-mats-cluster-nuke"){{
            status = StatusEffects.none;

            lifetime = 5f * 60f;

            height = 160f;
            zoneRadius = 12f * 8f;
            trailLength = 35;
            trailWidth = 1.5f;
            trailColor = targetColor = PMPal.missileFrag;
            posInterp = Interp.smoother;
            vertical = true;

            splitTime = 0.65f;
            splitLifeMaxOffset = 45f;
            fragRandomSpread = 20f * 8f;
            fragBullets = 20;

            fragBullet = new BallisticMissileBulletType("prog-mats-cluster-nuke-split"){{
                splashDamage = 3500f;
                splashDamageRadius = 40f;
                buildingDamageMultiplier = 0.5f;

                homingPower = 0.5f;
                homingRange = 30f * 8f;

                hitShake = 5f;
                despawnEffect = MissileFx.missileExplosion;
                blockEffect = MissileFx.missileBlocked;

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

        BulletType cease = arbalestIncend.copy();
        cease.spawnUnit = new MissileUnitType("mistake-rocket-b"){{
            speed = 8f;
            maxRange = 6f;
            lifetime = 3.1f * 60f;
            engineColor = trailColor = Pal.remove;
            engineLayer = Layer.effect;
            engineSize = 3.1f;
            engineOffset = 10f;
            rotateSpeed = 0.5f;
            trailLength = 18;
            missileAccelTime = 2f * 60f;
            lowAltitude = true;
            loopSound = Sounds.missileTrail;
            loopSoundVolume = 0.6f;
            deathSound = Sounds.largeExplosion;

            fogRadius = 6f;

            health = 210;

            weapons.add(new Weapon(){{
                shootCone = 360f;
                mirror = false;
                reload = 1f;
                deathExplosionEffect = new MultiEffect(MissileFx.missileExplosion, MissileFx.flameRing);
                shootOnDeath = true;
                shake = 10f;
                bullet = new ExplosionBulletType(526f, 8f * tilesize){{
                    hitColor = Pal.remove;
                    shootEffect = new MultiEffect(Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
                        lifetime = 10f;
                        strokeFrom = 4f;
                        sizeTo = 130f;
                    }});

                    makeFire = true;
                    status = PMStatusEffects.incendiaryBurn;
                    fragBullets = 1;
                    fragLifeMin = 1.5f;
                    fragLifeMax = 3f;
                    fragBullet = stop;
                }};
            }});

            abilities.add(new MoveEffectAbility(){{
                effect = MissileFx.rocketTrailSmoke;
                rotateEffect = true;
                y = -9f;
                color = Color.grays(0.6f).lerp(Pal.redLight, 0.5f).a(0.4f);
                interval = 4f;
            }});
        }};

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
