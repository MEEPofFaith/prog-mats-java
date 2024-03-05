package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.effect.*;
import progressed.type.unit.*;
import progressed.util.*;
import progressed.world.blocks.crafting.*;
import progressed.world.blocks.defence.*;
import progressed.world.blocks.defence.turret.*;
import progressed.world.blocks.defence.turret.nexus.*;
import progressed.world.blocks.defence.turret.payload.*;
import progressed.world.blocks.defence.turret.testing.*;
import progressed.world.blocks.distribution.*;
import progressed.world.blocks.payloads.*;
import progressed.world.blocks.production.*;
import progressed.world.blocks.storage.*;
import progressed.world.draw.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;
import static progressed.content.blocks.PMPayloads.*;

public class PMBlocks{
    public static Block

    // region Turrets

    //Miniguns
    minigun, miinigun, mivnigun,

    //Teslas
    shock, spark, storm,

    //Geomancy
    concretion, flame, blaze,

    //Pixel Turrets
    bit,

    //Magnets
    magnet,

    //Crit Sniper
    caliber,

    //Support
    allure, vaccinator,

    //Behold: a laser pointer

    pinpoint,

    //Swords
    dance, masquerade,

    //Misc
    kugelblitz, excalibur,

    //Payload
    javelin, artemis, paragon,

    //Nexus
    judgement,

    //Test turrets
    testTurret0, testTurret1, testTurret2, testTurret3,

    // endregion
    // region production

    smartDrill,

    unitMinerDepot,

    // endregion
    // region Distribution

    //Conveyor
    floatingConveyor,

    //Misc
    burstDriver, sandDriver,

    // endregion
    // region Crafting

    //Crafters
    mindronCollider, pyroclastForge,

    //Payloads
    shellPress, missileFactory,

    // endregion
    // region defence

    igneousPillar,

    // endregion
    // region Units

    healZone, speedZone, strengthZone, harmacist,

    // endregion
    // region Effect

    coreShatter, coreCovalence,

    fence, web,

    ballisticProjector;

    // endregion

    public static void load(){
        PMPayloads.load();
        PMModules.load();

        // region Turrets
        minigun = new MinigunTurret("minigun"){{
            requirements(Category.turret, with(
                Items.copper, 200,
                Items.graphite, 175,
                Items.titanium, 100,
                Items.thorium, 80
            ));
            ammo(
                Items.copper, MinigunBullets.standardCopperMini,
                Items.graphite, MinigunBullets.standardDenseMini,
                Items.silicon, MinigunBullets.standardHomingMini,
                Items.pyratite, MinigunBullets.standardIncendiaryMini,
                Items.thorium, MinigunBullets.standardThoriumMini
            );
            size = 4;
            range = 255f;
            scaledHealth = 140f;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoil = 3f;
            recoilTime = 90f;
            cooldownTime = 10f;
            inaccuracy = 3f;
            shootEffect = smokeEffect = ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 4f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            coolant = consumeCoolant(0.2f);
        }};

        miinigun = new MinigunTurret("miinigun"){{
            requirements(Category.turret, with(
                Items.copper, 350,
                Items.graphite, 300,
                Items.titanium, 150,
                Items.plastanium, 175,
                Items.thorium, 170,
                PMItems.tenelium, 120
            ));
            ammo(
                Items.copper, MinigunBullets.standardCopperMini,
                Items.graphite, MinigunBullets.standardDenseMini,
                Items.silicon, MinigunBullets.standardHomingMini,
                Items.pyratite, MinigunBullets.standardIncendiaryMini,
                Items.thorium, MinigunBullets.standardThoriumMini
            );
            size = 4;
            range = 255f;
            maxSpeed = 27f;
            scaledHealth = 150;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoil = 3f;
            recoilTime = 90f;
            cooldownTime = 10f;
            inaccuracy = 3f;
            shootEffect = smokeEffect = ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 4f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            shoot = new ShootBarrel(){{
                shots = 2;
                barrels = new float[]{
                    -4f, 0f, 0f,
                    4f, 0f, 0f
                };
            }};
            coolant = consumeCoolant(0.2f);
        }};

        mivnigun = new MinigunTurret("mivnigun"){{
            requirements(Category.turret, with(
                Items.copper, 650,
                Items.graphite, 600,
                Items.titanium, 370,
                Items.thorium, 340,
                Items.plastanium, 325,
                Items.surgeAlloy, 220,
                PMItems.tenelium, 270
            ));
            ammo(
                Items.copper, MinigunBullets.standardCopperMini,
                Items.graphite, MinigunBullets.standardDenseMini,
                Items.silicon, MinigunBullets.standardHomingMini,
                Items.pyratite, MinigunBullets.standardIncendiaryMini,
                Items.thorium, MinigunBullets.standardThoriumMini
            );
            size = 4;
            hideDetails = false;
            range = 255f;
            maxSpeed = 24f;
            scaledHealth = 160f;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoil = 3f;
            recoilTime = 90f;
            cooldownTime = 10f;
            inaccuracy = 3f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 5f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            shoot = new ShootBarrel(){{
                shots = 4;
                barrels = new float[]{
                    -9f, -3f / 4f, 0f,
                    -3f, 0f, 0f,
                    3f, 0f, 0f,
                    9f, -3f / 4f, 0f
                };
            }};
            coolant = consumeCoolant(0.2f);
        }};

        shock = new TeslaTurret("shock"){{
            requirements(Category.turret, with(
                Items.copper, 45,
                Items.lead, 60,
                Items.silicon, 25,
                Items.titanium, 25
            ));
            rings.add(
                new TeslaRing(0.75f),
                new TeslaRing(2.5f)
            );
            size = 1;
            health = 310;
            reload = 30f;
            range = 72f;
            maxTargets = 6;
            damage = 20f;
            status = StatusEffects.shocked;

            consumePower(3.6f);
            coolant = consumeCoolant(0.2f);
        }};

        spark = new TeslaTurret("spark"){{
            requirements(Category.turret, with(
                Items.copper, 60,
                Items.lead, 85,
                Items.graphite, 40,
                Items.silicon, 55,
                Items.titanium, 80
            ));
            rings.add(
                new TeslaRing(2f),
                new TeslaRing(6f)
            );
            size = 2;
            scaledHealth = 200;
            reload = 20f;
            range = 130f;
            maxTargets = 5;
            damage = 23f;
            status = StatusEffects.shocked;

            consumePower(4.8f);
            coolant = consumeCoolant(0.2f);
        }};

        storm = new TeslaTurret("storm"){{
            requirements(Category.turret, with(
                Items.copper, 120,
                Items.lead, 150,
                Items.graphite, 55,
                Items.silicon, 105,
                Items.titanium, 90,
                Items.surgeAlloy, 40,
                PMItems.tenelium, 50
            ));
            float spinSpeed = 12f;
            rings.addAll(
                //Center
                new TeslaRing(1f),
                new TeslaRing(3.25f),
                new TeslaRing(6.5f),
                //Spinner 1
                new TeslaRing(4.25f){{ //TL
                    hasSprite = true;
                    drawUnder = true;
                    xOffset = -8.625f;
                    yOffset = 8.625f;
                    rotationMul = spinSpeed;
                }},
                new TeslaRing(4.25f){{ //TR
                    drawUnder = true;
                    xOffset = yOffset = 8.625f;
                    rotationMul = spinSpeed;
                }},
                new TeslaRing(4.25f){{ //BL
                    drawUnder = true;
                    xOffset = yOffset = -8.625f;
                    rotationMul = spinSpeed;
                }},
                new TeslaRing(4.25f){{ //BR
                    drawUnder = true;
                    xOffset = 8.625f;
                    yOffset = -8.625f;
                    rotationMul = spinSpeed;
                }},
                //Spinner 2
                new TeslaRing(1f){{ //TL
                    hasSprite = true;
                    drawUnder = true;
                    xOffset = -7.625f;
                    yOffset = 7.625f;
                    rotationMul = -spinSpeed;
                }},
                new TeslaRing(1f){{ //TR
                    drawUnder = true;
                    xOffset = yOffset = 7.625f;
                    rotationMul = -spinSpeed;
                }},
                new TeslaRing(1f){{ //BL
                    drawUnder = true;
                    xOffset = yOffset = -7.625f;
                    rotationMul = -spinSpeed;
                }},
                new TeslaRing(1f){{ //BR
                    drawUnder = true;
                    xOffset = 7.625f;
                    yOffset = -7.625f;
                    rotationMul = -spinSpeed;
                }}
            );
            size = 3;
            scaledHealth = 180;
            reload = 10f;
            range = 315f;
            maxTargets = 16;
            coolantMultiplier = 1f;
            hasSpinners = true;
            damage = 27f;
            status = StatusEffects.shocked;

            consumePower(8.9f);
            coolant = consumeCoolant(0.2f);
        }};

        concretion = new GeomancyTurret("concretion"){{
            requirements(Category.turret, with(
                Items.copper, 100,
                Items.lead, 120,
                Items.silicon, 75,
                Items.titanium, 60
            ));
            size = 2;
            scaledHealth = 310;
            reload = 120f;
            shootSound = Sounds.rockBreak;
            range = 35f * tilesize;
            recoil = -25f / 4f;
            shootY = 8f + 15f / 4f;
            targetAir = false;
            cooldownTime = 300f;
            shootType = PMBullets.pillarField;
            shoot.firstShotDelay = 10f;

            armX = 15f / 4f;
            armY = -2f / 4f;

            consumePower(2f);
            coolant = consumeCoolant(0.2f);
        }};

        flame = new EruptorTurret("flame"){{
            requirements(Category.turret, with(
                Items.copper, 200,
                Items.lead, 300,
                Items.graphite, 300,
                Items.silicon, 325,
                Items.titanium, 200,
                Items.thorium, 200
            ));
            size = 3;
            scaledHealth = 210;
            shootDuration = 90f;
            range = 240f;
            reload = 90f;
            shootY = -0.25f;
            recoil = 3f;
            shootType = new MagmaBulletType(62f, 14f){{
                shake = 1f;
                crackEffects = 4;
            }};

            consumePower(14f);
            coolant = consumeCoolant(0.2f);
        }};

        blaze = new EruptorTurret("blaze"){{
            requirements(Category.turret, with(
                Items.copper, 350,
                Items.lead, 550,
                Items.graphite, 550,
                Items.silicon, 600,
                Items.titanium, 350,
                Items.surgeAlloy, 200,
                PMItems.tenelium, 200
            ));
            size = 4;
            scaledHealth = 190;
            shootDuration = 120f;
            range = 280f;
            reload = 150f;
            shootY = 0.25f;
            rotateSpeed = 3.5f;
            recoil = 4f;
            beamEffect = LightningFx.blazeBeam;
            shootType = new MagmaBulletType(76f, 24f){{
                shake = 2f;
                crackEffects = 6;
            }};

            consumePower(17f);
            coolant = consumeCoolant(0.2f);
        }};

        bit = new BitTurret("bit"){{
            requirements(Category.turret, with(
                Items.copper, 50,
                Items.lead, 60,
                Items.silicon, 40,
                Items.titanium, 30
            ));
            size = 2;
            scaledHealth = 300;
            reload = 70f;
            rotateSpeed = 10f;
            recoil = 4f;
            inaccuracy = 15f;
            range = 140f;
            shootType = PMBullets.pixel;

            consumePower(1.35f);
            coolant = consumeCoolant(0.2f);
        }};

        magnet = new ItemTurret("attraction"){
            {
                requirements(Category.turret, with(
                    Items.copper, 115,
                    Items.lead, 80,
                    Items.graphite, 30,
                    Items.titanium, 25
                ));
                ammo(
                    Items.copper, PMBullets.magnetCopper,
                    Items.titanium, PMBullets.magnetTitanium,
                    PMItems.tenelium, PMBullets.magenetTenelium
                );
                size = 3;
                scaledHealth = 90;
                range = 23f * 8f;
                reload = 200f;
                inaccuracy = 30f;

                velocityRnd = 0.2f;
                shoot.shots = 4;
                shoot.shotDelay = 5f;

                coolant = consumeCoolant(0.2f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
            }
        
            @Override
            public void setBars(){
                super.setBars();
                addBar("pm-reload", (ItemTurretBuild entity) -> new Bar(
                    () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
                    () -> entity.team.color,
                    () -> Mathf.clamp(entity.reloadCounter / reload)
                ));
            }
        };

        caliber = new ItemTurret("caliber"){
            {
                requirements(Category.turret, with(
                    Items.copper, 220,
                    Items.titanium, 200,
                    Items.thorium, 150,
                    Items.plastanium, 110,
                    PMItems.tenelium, 60
                ));
                ammo(
                    Items.titanium, SniperBullets.sniperBoltTitanium,
                    Items.thorium, SniperBullets.sniperBoltThorium,
                    Items.silicon, SniperBullets.sniperBoltSilicon,
                    PMItems.tenelium, SniperBullets.sniperBoltTenelium,
                    Items.surgeAlloy, SniperBullets.sniperBoltSurge
                );
                size = 3;
                hideDetails = false;
                scaledHealth = 120;
                reload = 150f;
                range = 544f;
                rotateSpeed = 2.5f;
                recoil = 5f;
                cooldownTime = 300f;
                shootSound = Sounds.cannon;

                coolant = consumeCoolant(0.2f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
            }
        };

        allure = new SignalFlareTurret("signal"){{
            requirements(Category.turret, with(
                Items.lead, 80,
                Items.silicon, 130,
                Items.plastanium, 110,
                PMItems.tenelium, 90
            ));
            ammo(
                Items.silicon, PMBullets.smallFlare,
                PMItems.tenelium, PMBullets.mediumFlare,
                Items.surgeAlloy, PMBullets.largeFlare
            );
            size = 2;
            scaledHealth = 250;
            minRange = 5f * tilesize;
            range = 60f * tilesize;
            shootY = 23f / 4f;
            reload = 900f;
            inaccuracy = 10f;
            velocityRnd = 0.2f;
            shootSound = Sounds.shootSnap;
            maxAmmo = 30;
            ammoPerShot = 10;

            coolant = consumeCoolant(0.2f);
        }};

        vaccinator = new ItemTurret("vaccinator"){
            {
                requirements(Category.turret, with(
                    Items.lead, 70,
                    Items.titanium, 50,
                    Items.silicon, 80,
                    Items.plastanium, 20
                ));
                ammo(
                    Items.plastanium, PMBullets.syringe
                );

                size = 2;
                hideDetails = false;
                scaledHealth = 80;
                range = 17f * 8f;
                shootY = 21f / 4f;
                reload = 120f;
                buildingFilter = b -> false; //Don't shoot buildings

                shoot = new ShootSpread(){{
                    shots = 4;
                    shotDelay = 3;
                    spread = 15;
                }};

                shootSound = Sounds.shootSnap;

                coolant = consumeCoolant(0.2f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
            }
        };

        pinpoint = new SwingContinuousTurret("pinpoint"){{
            requirements(Category.turret, with(
                Items.copper, 200,
                Items.titanium, 150,
                Items.plastanium, 125,
                Items.silicon, 180,
                PMItems.tenelium, 150
            ));

            shootType = new PointLaserBulletType(){{
                damage = 200f;
                sprite = "prog-mats-swing-laser";

                beamEffect = Fx.none;
                trailLength = 6;
                trailColor = hitColor = Color.valueOf("fd4f60");
                trailWidth = 3f;
                trailSinMag = oscMag;
                trailSinScl = oscScl;
            }};

            drawer = new DrawSwingTurret(){{
                parts.add(new RegionPart("-break"){{
                    x = 19f / 4f;
                    y = 17f / 4f;
                    moveRot = 90f;
                    progress = PartProgress.life;
                    mirror = true;
                    under = true;
                }});
            }};

            shootSound = Sounds.none;
            loopSoundVolume = 1f;
            loopSound = Sounds.laserbeam;

            rotateSpeed = 6f;
            rotateSpeedAccel = 0.2f;
            rotateSpeedDrag = 0.02f;
            aimChangeSpeed = 6f;
            aimChangeSpeedAccel = 0.5f;
            aimChangeSpeedDrag = 0.06f;

            size = 3;
            range = 16f * tilesize;
            scaledHealth = 140;
            shootY = 34f / 4f;
            shootCone = 360f;

            unitSort = UnitSorts.strongest;

            consumePower(1.5f);
        }};

        //"lets dance"
        dance = new SwordTurret("dance"){{
            requirements(Category.turret, with(
                Items.copper, 500,
                Items.graphite, 250,
                Items.silicon, 350,
                Items.titanium, 200,
                Items.phaseFabric, 50,
                PMItems.tenelium, 150
            ));
            size = 3;
            hideDetails = false;
            scaledHealth = 340;
            range = 180f;
            maxSwords = 3;
            unitSort = UnitSorts.weakest;
            targetY = 35f / 4f;

            buildPowerUse = 4f;
            attackPowerUse = 6.5f;
            buildY = 13f / 4f;
        }};

        masquerade = new SwordTurret("ball"){{
            requirements(Category.turret, with(
                Items.copper, 1400,
                Items.graphite, 350,
                Items.silicon, 400,
                Items.surgeAlloy, 400,
                Items.phaseFabric, 200,
                PMItems.tenelium, 450
            ));
            size = 5;
            hideDetails = false;
            scaledHealth = 230;
            range = 260f;
            swordType = (SwordUnitType)PMUnitTypes.masqueradeSword;
            maxSwords = 5;
            unitSort = UnitSorts.weakest;
            targetY = 67f / 4f;
            targetRad = 6f;

            buildPowerUse = 6f;
            attackPowerUse = 13.5f;
            buildY = 31f / 4f;
            buildWaveOffset = 0.05f;
        }};

        kugelblitz = new BlackHoleTurret("blackhole"){{
            requirements(Category.turret, with(
                Items.titanium, 100,
                Items.thorium, 150,
                Items.plastanium, 250,
                Items.surgeAlloy, 250,
                Items.silicon, 800,
                Items.phaseFabric, 500,
                PMItems.tenelium, 500
            ));
            size = 4;
            hideDetails = false;
            scaledHealth = 230;
            canOverdrive = false;
            reload = 520f;
            range = 256f;
            shootEffect = smokeEffect = Fx.none;
            shoot.firstShotDelay = EnergyFx.kugelblitzGrow.lifetime - 1f;
            rotateSpeed = 2f;
            recoil = 2f;
            recoilTime = 240f;
            cooldownTime = 300f;
            shootY = 0f;
            shootSound = Sounds.release;
            shootType = PMBullets.blackHole;

            consumePower(35f);
            coolant = consumeCoolant(0.2f);
        }};

        excalibur = new PowerTurret("excalibur"){{
            requirements(Category.turret, with(
                Items.copper, 1200,
                Items.lead, 1100,
                Items.graphite, 800,
                Items.silicon, 1500,
                Items.titanium, 800,
                Items.thorium, 700,
                Items.plastanium, 350,
                Items.surgeAlloy, 450,
                PMItems.tenelium, 800
            ));
            size = 6;
            hideDetails = false;
            scaledHealth = 140;
            reload = 450f;
            range = 740f;
            shootEffect = smokeEffect = Fx.none;
            shootY = 0f;
            cooldownTime = 300f;
            shootWarmupSpeed = 0.05f;
            minWarmup = 0.9f;
            heatColor = Pal.surge;
            shootSound = Sounds.malignShoot;
            rotateSpeed = 2f;
            recoil = 8f;
            recoilTime = 300f;
            shootType = PMBullets.excaliburLaser;

            consumePower(30f);
            coolant = consumeCoolant(0.2f);

            Color transSurge = Pal.surge.cpy().a(0);
            PartProgress[] p = {PartProgress.reload.inv().curve(0f, 0.3f),
                PartProgress.reload.inv().curve(0.3f, 0.3f),
                PartProgress.reload.inv().curve(0.7f, 0.1f),
                PartProgress.reload.inv().curve(0.8f, 0.1f),
                PartProgress.reload.inv().curve(0.9f, 0.1f)
            };
            drawer = new DrawTurret(){{
                for(int i = 0; i < 5; i++){
                    int ii = i;
                    parts.add(
                        new RegionPart("-cell-" + ii){{
                            outline = false;
                            progress = heatProgress = p[ii];
                            color = transSurge;
                            colorTo = heatColor = Pal.surge;
                        }}
                    );
                }

                parts.addAll(
                    new RegionPart("-side"){{
                        progress = PartProgress.warmup.curve(Interp.pow2Out);
                        moves.add(new PartMove(PartProgress.warmup.curve(Interp.pow5In), 0f, -3f, 0f));
                        moveX = -9f / 4f;
                        mirror = true;
                        for(int i = 2; i < 5; i++){
                            int ii = i;
                            children.add(
                                new RegionPart("-side-cell-" + (ii - 2)){{
                                    outline = false;
                                    progress = heatProgress = p[ii];
                                    color = transSurge;
                                    colorTo = heatColor = Pal.surge;
                                }}
                            );
                        }
                    }},
                    new RegionPart("-cross"){{
                        heatColor = Pal.surge;
                    }}
                );
            }};
        }};

        javelin = new SinglePayloadAmmoTurret("arbalest"){{
            requirements(Category.turret, with(
                Items.copper, 150,
                Items.graphite, 300,
                Items.silicon, 325,
                Items.titanium, 350,
                PMItems.tenelium, 160
            ));
            ammo(
                basicCruiseMissile, PayloadBullets.arbalestBasic,
                incendiaryCruiseMissile, PayloadBullets.arbalestIncend,
                splitterCruiseMissile, PayloadBullets.arbalestSplitter
            );

            PartProgress baseProgress = PartProgress.warmup.shorten(0.3f);
            drawer = new DrawMulti(
                new DrawPayloadTurret(true){{
                    parts.add(new RegionPart("-doors"){{
                        progress = baseProgress.shorten(0.3f).clamp().curve(Interp.smooth);
                        mirror = true;
                        moveX = 6f;
                    }});
                }},
                new DrawPayloadAmmo(){{
                    progress = matProgress = baseProgress.delay(0.7f);
                    layer = Layer.turret + 1f;
                    materialize = false;
                    rotation = 90f;
                    xScl = yScl = 0.8f;
                    growX = growY = 0.2f;
                }}
            );

            size = 5;
            hideDetails = false;
            scaledHealth = 180;
            reload = 2f * 60f;
            setWarmupTime(1.5f);
            shootCone = 1f;
            shootY = 0f;
            range = 800f;
            recoil = 0.5f;
            rotateSpeed = 0.9f;
            shootSound = Sounds.missileLaunch;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootSmokeMissile;

            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.2f);
            limitRange();
            setUsers();
        }};

        artemis = new ArcMissileTurret("artemis"){{
            requirements(Category.turret, with(
                Items.copper, 70,
                Items.lead, 350,
                Items.graphite, 300,
                Items.silicon, 300,
                Items.titanium, 250,
                PMItems.tenelium, 120
            ));
            ammo(
                basicBallisticMissile, PayloadBullets.artemisBasic,
                recursiveBallisticMissile, PayloadBullets.artemisRecursive,
                bombingBallisticMissile, PayloadBullets.artemisBombing
            );
            size = 5;
            scaledHealth = 160;
            reload = 90f;
            range = 85f * 8f;
            minRange = 25f * 8f;
            shootY = 0f;
            shootSound = Sounds.missileLaunch;
            cooldownTime = 3.5f * 60f;
            shake = 5f;
            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.2f);
            limitRange();
            setUsers();
        }};

        paragon = new ArcMissileTurret("paragon"){{
            requirements(Category.turret, with(
                Items.copper, 6000,
                Items.graphite, 5200,
                Items.silicon, 3500,
                Items.titanium, 2500,
                Items.thorium, 1250,
                Items.surgeAlloy, 1000,
                PMItems.tenelium, 1800
            ));
            ammo(
                basicNuke, PayloadBullets.paragonBasic,
                clusterNuke, PayloadBullets.paragonCluster,
                empNuke, PayloadBullets.paragonEMP,
                sandboxNuke, PayloadBullets.ohno
            );
            size = 7;
            scaledHealth = 170;
            reload = 12f * 60f;
            range = 240f * 8f;
            minRange = 50f * 8f;
            shootY = 0f;
            shootSound = Sounds.missileLaunch;
            cooldownTime = 6f * 60f;
            shake = 10f;
            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.2f);
            limitRange();
            setUsers();
        }};

        judgement = new NexusTurret("judgement"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with());
            size = 9;
        }};

        testTurret0 = new ArcBulletTestTurret("test-turret0");
        testTurret1 = new SkyLaserTestTurret("test-turret1");
        testTurret2 = new ArcBulletScatterTestTurret("test-turret2");
        testTurret3 = new FreeTurret("test-turret3"){{
            shootType = new UltraRailgunBulletType(0f, 40f, 15_000_000){{
                lifetime = 90f;
                width *= 4;
                glowWidth *= 4;
                hitSize = width * 2;
                strokeTo = 0.5f;

                colors = new Color[6];
                for(int i = 0; i < colors.length; i++){
                    colors[i] = Color.white.cpy().a((i + 1f) / colors.length);
                }
            }};
            shootEffect = new FlashEffect(new WrapDataEffect(UltraRailgunBulletType.defaultShockwave, 150f * 8f), 60f);
        }};
        // endregion

        // region Production
        smartDrill = new SmartDrill("smart-drill"){{
            requirements(Category.production, with(
                Items.copper, 45,
                Items.graphite, 40,
                Items.silicon, 45,
                PMItems.tenelium, 35
            ));
            size = 3;
            drillTime = 260;
            hasPower = true;
            tier = 5;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;
            rotateSpeed = -4f;

            consumePower(1.8f);
            consumeLiquid(Liquids.water, 0.09f).boost();
        }};

        unitMinerDepot = new UnitMinerDepot("unit-miner-depot"){{
            requirements(Category.production, BuildVisibility.sandboxOnly, with());

            size = 3;
            buildTime = 60f * 8f;

            consumePower(8f / 60f);

            //intentionally set absurdly high to make this block not overpowered
            consumeLiquid(Liquids.nitrogen, 10f / 60f);

            itemCapacity = 100;
        }};
        // endregion

        // region Distribution
        floatingConveyor = new CoveredConveyor("floating-conveyor"){{
            requirements(Category.distribution, with(
                Items.lead, 3,
                Items.metaglass, 3,
                Items.plastanium, 3,
                PMItems.tenelium, 3
            ));
            health = 15;
            floating = true;
            placeableLiquid = true;
            speed = 0.06f;
            displayedSpeed = 8.4f;
            buildCostMultiplier = 0.25f;
        }};

        burstDriver = new BurstDriver("burst-driver"){{
            requirements(Category.distribution, with(
                Items.titanium, 275,
                Items.silicon, 200,
                Items.lead, 350,
                Items.thorium, 125,
                PMItems.tenelium, 75
            ));
            size = 3;
            itemCapacity = 180;
            reload = 120f;
            shots = 90;
            delay = 0.75f;
            range = 560f;

            consumePower(2.75f);
        }};

        sandDriver = new SandDriver("sand-driver"){{
            size = 3;
            itemCapacity = 180;
            reload = 120f;
            shots = 90;
            delay = 0.75f;
            range = 560f;

            consumePower(0.1f);
        }};
        // endregion

        // region Crafting
        mindronCollider = new AccelerationCrafter("mindron-collider"){{
            requirements(Category.crafting, with(
                Items.silicon, 150,
                Items.metaglass, 50,
                Items.plastanium, 80,
                Items.thorium, 100
            ));
            size = 3;

            craftTime = 60f;
            hasPower = hasLiquids = true;
            liquidCapacity = 50f;
            accelerationSpeed = 0.0004f;
            decelerationSpeed = 0.003125f;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawSpeedPlasma(){{
                plasma1 = Items.titanium.color;
                plasma2 = Items.thorium.color;
            }}, new DrawDefault());
            onCraft = tile -> {
                Tmp.v1.setToRandomDirection().setLength(28f / 4f);
                CrafterFx.colliderFusion.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
            };

            consumePower(6f);
            consumeItems(with(
                Items.titanium, 2,
                Items.thorium, 2
            ));
            consumeLiquid(Liquids.cryofluid, 0.2f);
            outputItem = new ItemStack(PMItems.tenelium, 2);
        }};

        pyroclastForge = new FuelCrafter("forge"){{
            requirements(Category.crafting, with(
                Items.titanium, 600,
                Items.metaglass, 150,
                Items.plastanium, 175,
                Items.silicon, 370,
                Items.surgeAlloy, 150,
                PMItems.tenelium, 250
            ));
            size = 5;
            itemCapacity = 32;
            craftTime = 75f;
            hasPower = true;
            hasLiquids = false;
            craftEffect = CrafterFx.superSmeltsmoke;
            ambientSound = Sounds.smelter;
            ambientSoundVolume = 1f; //Big flame louder sound. LIKE REALLY LOUD.

            drawer = new DrawMulti(
                new DrawDefault(),
                new DrawFlame(Color.valueOf("ffef99")){{
                    flameRadiusIn = 4f;
                    flameRadius = 7.5f;
                    flameRadiusMag = 2.5f;
                    flameRadiusScl = 8f;
                }}
            );

            fuelItem = Items.pyratite;
            fuelPerItem = 3;
            fuelPerCraft = 12;
            fuelCapacity = 48;
            fuelUseReduction = 0.8f;

            consumePower(9f);
            consumeItems(with(
                Items.lead, 4,
                Items.sand, 10,
                Items.coal, 6
            ));
            outputItems = with(
                Items.silicon, 14,
                Items.metaglass, 6
            );
        }};

        shellPress = new PayloadCrafter("shell-press"){{
            requirements(Category.crafting, with(
                Items.copper, 75,
                Items.lead, 100,
                Items.titanium, 100,
                Items.silicon, 80
            ));

            size = 5;
            ambientSound = Sounds.machine;
            recipes(
                emptyCruiseMissile,
                emptyBallisticMissile,
                emptyNuke
            );
            recipes.each(r -> r.centerBuild = true);
            setRecipeProductionStats();
        }};

        missileFactory = new PayloadCrafter("missile-factory"){{
            requirements(Category.crafting, with(
                Items.copper, 350,
                Items.lead, 250,
                Items.silicon, 220,
                Items.plastanium, 160,
                Items.thorium, 110
            ));

            size = 5;
            hideDetails = false;
            ambientSound = Sounds.machine;
            liquidCapacity = 80f;
            recipes(
                basicCruiseMissile, incendiaryCruiseMissile, splitterCruiseMissile,
                basicBallisticMissile, recursiveBallisticMissile, bombingBallisticMissile,
                basicNuke, clusterNuke, empNuke
            );
            recipes.get(1).liquidRequirements = new LiquidStack(Liquids.slag, 40f);
            setRecipeProductionStats();
        }};
        // endregion

        // region Defense
        igneousPillar = new IgneousPillar("igneous-pillar"){{
            health = 30 * 4; //4 is wallHealthMultiplier in Blocks.java
            destroySound = PMSounds.rockExplode;
            glowVariants = 5;
            glowWeights = new int[]{1, 4, 4, 5, 5};
        }};

        ((PillarFieldBulletType)(PMBullets.pillarField)).pillar = (IgneousPillar)igneousPillar;
        // endregion

        // region Units
        healZone = new EffectZone("rejuvenation-beacon"){
            final float healing = 60f;

            {
                requirements(Category.units, with(
                    Items.silicon, 60,
                    Items.plastanium, 35,
                    PMItems.tenelium, 50
                ));
                size = 2;
                range = 16f * tilesize;
                height = 16f;
                baseColor = Pal.heal;
                reload = 40f;

                zoneEffect = tile -> {
                    if(!all.contains(Healthc::damaged)) return;
                    all.each(u -> {
                        if(!u.damaged()) return;
                        u.heal(healing * tile.heat);
                        Fx.heal.at(u);
                    });
                    Fx.healWaveDynamic.at(tile, range);
                };

                activate = () -> all.contains(Healthc::damaged);

                consumePower(7f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.add(Stat.repairSpeed, healing * (60f / reload), StatUnit.perSecond);
            }
        };

        speedZone = new EffectZone("speed-field"){
            {
                requirements(Category.units, with(
                    Items.silicon, 25,
                    Items.titanium, 30,
                    Items.lead, 30
                ));
                size = 2;
                range = 16f * tilesize;
                height = 16f;

                zoneEffect = tile -> all.each(u -> u.apply(PMStatusEffects.speedBoost, 22f * tile.heat));


                consumePower(3f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.add(Stat.output, PMStatValues.statusEffect(PMStatusEffects.speedBoost));
            }
        };

        strengthZone = new EffectZone("strength-emitter"){
            {
                requirements(Category.units, with(
                    Items.silicon, 40,
                    PMItems.tenelium, 35,
                    Items.lead, 35,
                    Items.phaseFabric, 20
                ));
                size = 2;
                range = 16f * tilesize;
                height = 16f;
                baseColor = Pal.redderDust;

                zoneEffect = tile -> all.each(u -> u.apply(PMStatusEffects.strengthBoost, 22f * tile.heat));

                consumePower(10f);
            }


            @Override
            public void setStats(){
                super.setStats();

                stats.add(Stat.output, PMStatValues.statusEffect(PMStatusEffects.strengthBoost));
            }
        };

        harmacist = new EffectZone("harmacist"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;

            size = 2;
            range = 32f * tilesize;
            height = 16f;
            baseColor = Color.red;
            reload = 5f;
            affectOwnTeam = false;
            affectEnemyTeam = true;

            zoneEffect = tile -> all.each(u -> PMBullets.harmanuke.create(tile, u.x, u.y, 0f));
        }};
        // endregion

        // region Effect
        coreShatter = new CrashCore("core-cripple"){{
            requirements(Category.effect, with(
                Items.copper, 400,
                Items.lead, 150
            ));
            isFirstTier = true;
            unitType = UnitTypes.alpha;
            health = 500;
            itemCapacity = 1500;
            size = 2;
            explosionSoundVolume = 4f;
            thrusterLength = 2f;

            unitCapModifier = 3;
        }};
        Blocks.coreShard.buildVisibility = BuildVisibility.shown;
        ((CoreBlock)Blocks.coreShard).isFirstTier = false;

        coreCovalence = new CoreLink("core-covalence"){{
            requirements(Category.effect, with(
                Items.copper, 6000,
                Items.lead, 6000,
                Items.silicon, 3000,
                Items.thorium, 2000,
                Items.phaseFabric, 1000,
                PMItems.tenelium, 3000
            ));
            size = 4;
            portalRad = 3f * tilesize / 2f * 0.625f;
            clouds = 15;
            minCloudSize = 0.5f;
            maxCloudSize = 1.25f;

            consumePower(42.5f);
        }};

        fence = new StaticNode("fence"){{
            requirements(Category.effect, with(
                Items.copper, 60,
                Items.lead, 50,
                Items.silicon, 20
            ));
            size = 1;
            health = 90;
            laserRange = 35;
            damage = 7f;
            powerPerLink = 1.2f;
        }};

        web = new StaticNode("web"){{
            requirements(Category.effect, with(
                Items.copper, 70,
                Items.lead, 35,
                Items.silicon, 25
            ));
            size = 1;
            health = 110;
            laserRange = 17;
            maxNodes = 6;
            damage = 4f;
            powerPerLink = 0.5f;
        }};

        ballisticProjector = new ShieldProjector("shield-projector"){{
            requirements(Category.effect, with(
                Items.lead, 325,
                Items.titanium, 225,
                Items.surgeAlloy, 75,
                PMItems.tenelium, 125
            ));
            size = 4;
            hideDetails = false;
            radius = 64.5f;
            phaseRadiusBoost = 27.5f;
            height = 15f * tilesize;
            shieldHealth = 3600f;
            phaseShieldBoost = 2000f;
            shieldCharge = 700f;
            phaseShieldCharge = 400f;
            chargeTime = 450f;
            cooldownBrokenBase *= 2f;

            consumePower(7f);
            itemConsumer = consumeItems(with(Items.phaseFabric, 1, PMItems.tenelium, 1)).boost();
        }};
        // endregion

        PMErekirBlocks.load();
    }
}

//Oops, someone spilled their semicolons
;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;
;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;
