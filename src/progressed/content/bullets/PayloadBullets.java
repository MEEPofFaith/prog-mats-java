package progressed.content.bullets;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import blackhole.entities.bullet.*;
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
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.entities.bullet.unit.*;
import progressed.graphics.*;
import progressed.type.unit.*;
import progressed.type.weapons.*;

import static mindustry.Vars.*;

public class PayloadBullets{
    public static SentryBulletType

    barrageLaunch, strikedownLaunch;

    public static BulletType

    arbalestBasic, arbalestIncend, arbalestSplitter;
    public static ArcMissileBulletType

    artemisBasic, artemisRecursive, artemisBombing,
    paragonBasic, paragonCluster, paragonEMP, paragonBlackHole,
    ohno;

    public static void load(){
        barrageLaunch = new SentryBulletType(PMUnitTypes.barrage);
        strikedownLaunch = new SentryBulletType(PMUnitTypes.strikedown);

        arbalestBasic = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;

            spawnUnit = new RocketUnitType("basic-rocket-b", true){{
                engineColor = trailColor = Pal.accent;

                weapons.add(new Weapon(){{
                    rotate = false;
                    shootCone = 360f;
                    mirror = false;
                    reload = 1f;
                    deathExplosionEffect = MissileFx.missileExplosion;
                    shootOnDeath = true;
                    shake = 10f;
                    shootSound = Sounds.none;
                    bullet = new ExplosionBulletType(526f, 8f * tilesize){{
                        hitColor = Pal.accent;
                        shootEffect = RocketUnitType.rocketShoot;
                    }};
                }});
            }};
        }};

        arbalestIncend = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;

            spawnUnit = new RocketUnitType("incendiary-rocket-b", true){{
                engineColor = trailColor = Pal.remove;

                weapons.add(new Weapon(){{
                    rotate = false;
                    shootCone = 360f;
                    mirror = false;
                    reload = 1f;
                    deathExplosionEffect = new MultiEffect(MissileFx.missileExplosion, MissileFx.flameRing);
                    shootOnDeath = true;
                    shake = 10f;
                    shootSound = Sounds.none;
                    bullet = new ExplosionBulletType(526f, 8f * tilesize){{
                        hitColor = Pal.remove;
                        shootEffect = RocketUnitType.rocketShoot;

                        makeFire = true;
                        status = PMStatusEffects.incendiaryBurn;
                    }};
                }});
            }};
        }};

        arbalestSplitter = new BulletType(0f, 0f){{
            ammoMultiplier = 1f;

            spawnUnit = new RocketUnitType("splitter-rocket-b", true){{
                engineColor = trailColor = PMPal.missileFrag;

                weapons.add(new MissileOwnerWeapon(){{
                    shootCone = 360f;
                    rotateSpeed = 0f;
                    mirror = false;
                    reload = 1f;
                    deathExplosionEffect = MissileFx.missileExplosion;
                    shootOnDeath = true;
                    shake = 10f;
                    shootSound = Sounds.missileLarge;
                    bullet = new ExplosionBulletType(150f, 8f * tilesize){{
                        hitColor = PMPal.missileFrag;
                        shootEffect = RocketUnitType.rocketShoot;

                        fragBullets = 3;
                        fragAngle = 180f;
                        fragRandomSpread = 120f;
                        fragBullet = new BulletType(){{
                            spawnUnit = new RocketUnitType("splitter-rocket-split", false){{
                                health = 120;
                                engineColor = trailColor = PMPal.missileFrag;
                                homingDelay = 20f;
                                missileAccelTime = 30f;
                                targetDelay = 30f;
                                lifetime /= 2;
                                rotateSpeed = 6f;

                                engineSize = 10f / 4f;
                                engineOffset = 33f/ 4f;
                                loopSoundVolume = 0.3f;

                                weapons.add(new MissileOwnerWeapon(){{
                                    shootCone = 360f;
                                    rotateSpeed = 0f;
                                    mirror = false;
                                    reload = 1f;
                                    deathExplosionEffect = MissileFx.missileExplosion;
                                    shootOnDeath = true;
                                    shake = 5f;
                                    shootSound = Sounds.missileSmall;
                                    bullet = new ExplosionBulletType(150f, 8f * tilesize){{
                                        hitColor = PMPal.missileFrag;

                                        fragBullets = 3;
                                        fragAngle = 180f;
                                        fragRandomSpread = 120f;
                                        fragBullet = new BulletType(){{
                                            spawnUnit = new RocketUnitType("splitter-rocket-bit", false){{
                                                health = 60;
                                                engineColor = trailColor = PMPal.missileFrag;
                                                homingDelay = 15f;
                                                missileAccelTime = 20f;
                                                targetDelay = 20f;
                                                lifetime /= 3;
                                                rotateSpeed = 10f;
                                                deathSound = Sounds.explosion;

                                                engineSize = 7f / 4f;
                                                engineOffset = 19f / 4f;
                                                loopSoundVolume = 0.1f;

                                                weapons.add(new MissileOwnerWeapon(){{
                                                    shootCone = 360f;
                                                    rotateSpeed = 0f;
                                                    mirror = false;
                                                    reload = 1f;
                                                    deathExplosionEffect = MissileFx.smallBoom;
                                                    shootOnDeath = true;
                                                    shake = 1f;
                                                    shootSound = Sounds.none;
                                                    bullet = new ExplosionBulletType(150f, 8f * tilesize){{
                                                        hitColor = PMPal.missileFrag;
                                                    }}; //Oh jeez that's a lot of closings
                                                }});
                                            }};
                                        }};
                                    }};
                                }});
                            }};
                        }};
                    }};
                }});
            }};
        }};

        artemisBasic = new ArcMissileBulletType("prog-mats-basic-missile"){{
            splashDamage = 750f;
            splashDamageRadius = 64f;
            buildingDamageMultiplier = 0.5f;
            hitShake = 5f;

            gravity = 0.12f;
            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        artemisRecursive = new ArcMissileBulletType("prog-mats-recursive-missile"){{
            status = StatusEffects.none;

            zoneRadius = 5f * 8f;
            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = PMPal.missileFrag;

            hitEffect = despawnEffect = Fx.none;
            hitSound = despawnSound = Sounds.none;

            accel = 0.015f;
            gravity = 0.05f;
            lifetimeScl = 0.33f;
            fragBullets = 3;
            fragBullet = new ArcMissileBulletType("prog-mats-recursive-missile-split"){{
                status = StatusEffects.none;

                zoneRadius = 3.5f * 8f;
                trailLength = 20;
                trailWidth = 1f;
                trailColor = targetColor = PMPal.missileFrag;

                hitEffect = despawnEffect = Fx.none;
                hitSound = despawnSound = Sounds.none;

                keepVelocity = true;
                gravity = 0.05f;
                arcFragDrift = 2f;
                targetDriftDrag = 0.02f;
                lifetimeScl = 0.5f;
                fragBullets = 3;
                fragBullet = new ArcMissileBulletType("prog-mats-recursive-missile-split"){{
                    splashDamage = 220f;
                    splashDamageRadius = 48f;
                    keepVelocity = true;
                    buildingDamageMultiplier = 0.5f;
                    hitShake = 5f;
                    hitEffect = MissileFx.smallBoom;
                    hitSound = Sounds.explosion;

                    zoneRadius = 2f * 8f;
                    gravity = 0.05f;
                    arcFragDrift = 2f;
                    targetDriftDrag = 0.02f;
                    trailLength = 15;
                    trailWidth = 1f;
                    trailColor = targetColor = PMPal.missileFrag;

                    absorbEffect = Pseudo3DFx.absorbedSmall;
                }};
            }};
        }};

        artemisBombing = new ArcMissileBulletType("prog-mats-bombing-missile"){{
            splashDamage = 200f;
            splashDamageRadius = 32f;
            buildingDamageMultiplier = 0.5f;
            hitShake = 5f;
            lifetime = 240;
            scaleLife = false;

            trailLength = 25;
            trailWidth = 1f;
            trailColor = targetColor = Pal.suppress;

            accel = 0.03f;
            gravity = 0.05f;
            bulletInterval = 20f;
            intervalDelay = 45f;
            intervalRandomSpread = 0f;
            intervalBullet = new ArcBombBulletType(170f, 3f * 8f, "prog-mats-bombing-missile-bomb"){{
                accel = 0f;
                buildingDamageMultiplier = 0.5f;
                homingPower = 0.5f;

                trailColor = targetColor = Pal.suppress;
                trailLength = 12;
                trailWidth = 1f;
            }};
        }};

        paragonBasic = new ArcMissileBulletType("prog-mats-basic-nuke"){{
            splashDamage = 27000f;
            splashDamageRadius = 240f;
            buildingDamageMultiplier = 0.5f;
            lifetime = 3f * 60f;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            despawnEffect = MissileFx.nuclearExplosion;
            absorbEffect = Pseudo3DFx.absorbedLarge;

            accel = 0.01f;
            gravity = 0.02f;
            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 2.5f;
            trailColor = targetColor = PMPal.missileBasic;
        }};

        paragonCluster = new ArcMissileBulletType("prog-mats-cluster-nuke"){{
            status = StatusEffects.none;

            lifetime = 5f * 60f;

            zoneRadius = 12f * 8f;
            trailLength = 35;
            trailWidth = 2.5f;
            trailColor = targetColor = PMPal.missileFrag;

            hitEffect = despawnEffect = Fx.none;
            hitSound = despawnSound = Sounds.none;

            accel = 0.008f;
            gravity = 0.025f;
            lifetimeScl = 0.5f;
            fragBullets = 20;
            fragBullet = new ArcMissileBulletType("prog-mats-cluster-nuke-split"){{
                splashDamage = 3500f;
                splashDamageRadius = 40f;
                buildingDamageMultiplier = 0.5f;

                homingPower = 1f;
                homingRange = 30f * 8f;

                hitShake = 5f;
                despawnEffect = MissileFx.missileExplosion;
                absorbEffect = Pseudo3DFx.absorbed;

                arcFragDrift = 4f;
                targetDriftDrag = 0.01f;
                gravity = 0.025f;
                trailLength = 35;
                trailWidth = 1f;
                trailColor = targetColor = PMPal.missileFrag;
            }};
        }};

        paragonEMP = new ArcMissileBulletType("prog-mats-emp-nuke"){{
            lifetime = 3f * 60f;
            status = StatusEffects.none;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            despawnEffect = MissileFx.nuclearExplosion;
            absorbEffect = Pseudo3DFx.absorbedLarge;

            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 2.5f;
            trailColor = targetColor = Pal.lancerLaser;

            accel = 0.01f;
            gravity = 0.02f;
            fragBullets = 60;
            fragRandomSpread = 0;
            fragSpread = 360f / fragBullets;
            fragVelocityMin = fragVelocityMax = fragLifeMin = fragLifeMax = 1f;
            fragBullet = new SparkingBulletType(2.5f, 0, "large-orb"){{
                //Distance to travel... ~40 tiles?
                lifetime = (40 * 8) / speed * 4f;
                drag = 0.01f;

                empRadius = 6f * 8f;
                empDamage = 150f;

                width = height = 15f;
                shrinkX = shrinkY = 0f;
                frontColor = hitColor = Pal.lancerLaser;
                backColor = trailColor = PMPal.lancerlaserDark;
                //trailWidth = 3.25f;
                //trailLength = 24;
                hitEffect = Fx.none;

                status = PMStatusEffects.empStun;
                suppressionRange = 10f * 8f;
                statusDuration = suppressionDuration = 7f * 60f;
                collides = false;
                hittable = absorbable = false;

                trailInterval = 2f;
                trailEffect = MissileFx.hitEmpSpark;
                //hitSound = Sounds.spark;

                despawnEffect = new WaveEffect(){{
                    lifetime = 10f;
                    sizeTo = 30f;
                    colorFrom = colorTo = Pal.lancerLaser;
                }};
            }};
        }};

        paragonBlackHole = new ArcMissileBulletType("prog-mats-blackhole-nuke"){{
            lifetime = 3f * 60f;
            status = StatusEffects.none;

            hitSound = PMSounds.nuclearExplosion;
            hitShake = 30f;
            despawnEffect = Fx.none;
            absorbEffect = Pseudo3DFx.absorbedLarge;

            zoneRadius = 8f * 8f;
            trailLength = 35;
            trailWidth = 2.5f;
            trailColor = Pal.sapBulletBack; //Black doesn't work because of bloom
            targetColor = Color.black;

            accel = 0.01f;
            gravity = 0.02f;
            fragBullets = 1;
            fragBullet = new BlackHoleBulletType(0f, 4400f / 30f){{
                lifetime = 4f * 60f;
                growTime = lifetime;
                shrinkTime = 0f;
                swirlEffect = MissileFx.bigBlackHoleSwirl;
                loopSoundVolume = 6f;

                damageRadius = 18f * tilesize;
                suctionRadius = 64f * tilesize;

                despawnEffect = MissileFx.slashTest;
            }};
        }};

        ohno = (ArcMissileBulletType)paragonCluster.copy();
        ohno.sprite = "prog-mats-sandbox-nuke";
        ohno.targetColor = ohno.trailColor = Color.red;
        ohno.rangeChange = 500 * tilesize;

        ArcMissileBulletType stop = (ArcMissileBulletType)artemisRecursive.copy();

        ArcMissileBulletType why = (ArcMissileBulletType)stop.fragBullet.copy();
        why.inheritVelDrift = true;
        ArcMissileBulletType tooFar = (ArcMissileBulletType)stop.fragBullet.fragBullet.copy();
        tooFar.buildingDamageMultiplier = 1f;
        tooFar.inheritVelDrift = true;
        stop.fragBullet = why;
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
                bullet = new ExplosionBulletType(526f, 8f * tilesize){
                    float fragMinRange = 20f * tilesize, fragMaxRange = 50f * tilesize;

                    {
                        hitColor = Pal.remove;
                        shootEffect = new MultiEffect(Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
                            lifetime = 10f;
                            strokeFrom = 4f;
                            sizeTo = 130f;
                        }});

                        makeFire = true;
                        status = PMStatusEffects.incendiaryBurn;
                        fragBullets = 1;
                        fragBullet = stop;
                    }

                    @Override
                    public void createFrags(Bullet b, float x, float y){
                        ArcMissileBulletType aType = (ArcMissileBulletType)fragBullet;
                        for(int i = 0; i < fragBullets; i++){
                            float dst = Mathf.randomSeed(b.id + (2 * i), fragMinRange, fragMaxRange);
                            float a = Mathf.randomSeed(b.id + (2 * i + 1), 360f);
                            float time = Mathf.sqrt((2 * dst) / aType.accel);
                            float zVel = -0.5f * -aType.gravity * time;
                            Tmp.v1.set(a, dst);
                            float tx = Tmp.v1.x + x,
                                ty = Tmp.v1.y + y;
                            aType.create3DVel(b.owner, b.team, b.x, b.y, 0f, a, zVel, aType.accel, tx, ty);
                        }
                    }
                };
            }});

            abilities.add(new MoveEffectAbility(){{
                effect = MissileFx.rocketTrailSmoke;
                rotateEffect = true;
                y = -9f;
                color = Color.grays(0.6f).lerp(Pal.redLight, 0.5f).a(0.4f);
                interval = 4f;
            }});
        }};

        ArcMissileBulletType enough = (ArcMissileBulletType)paragonBasic.copy();
        enough.buildingDamageMultiplier = 1f;
        enough.fragBullets = 10;
        enough.fragBullet = cease;
        enough.arcFragDrift = 5f;
        enough.targetDriftDrag = 0.01f;

        ohno.fragBullet = enough;
    }
}
