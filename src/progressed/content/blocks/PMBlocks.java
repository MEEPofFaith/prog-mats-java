package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.units.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.crafting.*;
import progressed.world.blocks.defence.*;
import progressed.world.blocks.defence.turret.*;
import progressed.world.blocks.defence.turret.apotheosis.*;
import progressed.world.blocks.defence.turret.energy.*;
import progressed.world.blocks.defence.turret.multi.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.ModuleGroup.*;
import progressed.world.blocks.defence.turret.multi.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.payload.*;
import progressed.world.blocks.defence.turret.sandbox.*;
import progressed.world.blocks.distribution.*;
import progressed.world.blocks.distribution.drones.*;
import progressed.world.blocks.distribution.drones.stations.*;
import progressed.world.blocks.payloads.*;
import progressed.world.blocks.production.*;
import progressed.world.blocks.sandbox.*;
import progressed.world.blocks.storage.*;
import progressed.world.draw.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;
import static progressed.content.blocks.PMModules.*;
import static progressed.content.blocks.PMPayloads.*;

public class PMBlocks implements ContentList{
    public static Block

    // region Turrets

    //Miniguns
    minigun, miinigun, mivnigun,

    //Teslas
    shock, spark, storm,

    //Geomancy
    concretion,

    //Eruptors
    flame, blaze, inferno,

    //Pixel Turrets
    bit,

    //Magnets
    magnet,

    //Crit Sniper
    caliber,

    //Support
    allure, vaccinator,

    //Anime sweep laser
    incision, fissure,

    //Swords
    dance, ball,

    //Why do I hear anxiety piano
    sentinel,

    //Misc
    blackhole, excalibur,

    //Modular
    council, congress, pantheon,

    //Payload
    sergeant, arbalest,

    //Missiles
    firestorm, strikedown, trinity,

    //Apotheosis
    apotheosisNexus, apotheosisCharger,

    // endregion
    // region production

    smartDrill,

    // endregion
    // region Distribution

    //Conveyor
    floatingConveyor,

    //Drone
    dronePad, itemDroneStation, liquidDroneStation, payloadDroneStation,

    //Misc
    burstDriver,

    // endregion
    // region Crafting

    //Crafters
    mindronCollider, pyroclastForge,

    //Payloads
    moduleAssembler, moduleFoundry, shellPress, missileFactory, sentryBuilder,

    // endregion
    // region defence

    igneousPillar,

    // endregion
    // region Units

    healZone, speedZone, strengthZone,

    // endregion
    // region Effect

    coreCovalence,

    fence, web,

    systemBooster, shieldProjector,

    // endregion
    // region Sandbox

    //Turret
    harbinger, everythingGun, omegaCharger,

    //Distribution
    everythingItemSource, sandDriver,

    //liquid
    everythingLiquidSource,
    
    //Power
    strobeNode, strobeInf, strobeBoost, 

    //Defense
    sandboxWall, sandboxWallLarge,

    //Unit
    godFactory, capBlock,

    //Effect
    multiSource, multiVoid, multiSourceVoid;

    // endregion

    private final ContentList[] otherBlocks = {
        new PMPayloads(),
        new PMModules()
    };

    @Override
    public void load(){
        for(ContentList blockList : otherBlocks){
            blockList.load();
        }

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
            maxSpeed = 0.75f;
            health = 140 * size * size;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoilAmount = 3f;
            restitution = 0.02f;
            cooldown = 0.11f;
            inaccuracy = 3f;
            shootEffect = smokeEffect = ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 4f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            shootLocs = new float[]{0f};
            windupSpeed = 0.0001875f;
            windDownSpeed = 0.003125f;
            minFiringSpeed = 1f/12f;
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
            maxSpeed = 0.73f;
            health = 150 * size * size;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoilAmount = 3f;
            restitution = 0.02f;
            cooldown = 0.11f;
            inaccuracy = 3f;
            shootEffect = smokeEffect = ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 4f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            shootLocs = new float[]{-4f, 4f};
            windupSpeed = 0.0001875f;
            windDownSpeed = 0.003125f;
            minFiringSpeed = 1f/12f;
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
            maxSpeed = 0.71f;
            health = 160 * size * size;
            shootCone = 35f;
            shootSound = Sounds.shootBig;
            targetAir = targetGround = true;
            recoilAmount = 3f;
            restitution = 0.02f;
            cooldown = 0.11f;
            inaccuracy = 3f;
            shootEffect = smokeEffect = ammoUseEffect = Fx.none;
            heatColor = Pal.turretHeat;

            barX = 5f;
            barY = -10f;
            barStroke = 1f;
            barLength = 9f;

            shootLocs = new float[]{-9f, -3f, 3f, 9f};
            windupSpeed = 0.0001875f;
            windDownSpeed = 0.003125f;
            minFiringSpeed = 1f/12f;
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
            reloadTime = 30f;
            powerUse = 3.6f;
            range = 72f;
            maxTargets = 6;
            damage = 20f;
            status = StatusEffects.shocked;
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
            health = 200 * size * size;
            reloadTime = 20f;
            powerUse = 4.8f;
            range = 130f;
            maxTargets = 5;
            damage = 23f;
            status = StatusEffects.shocked;
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
            health = 180 * size * size;
            reloadTime = 10f;
            powerUse = 8.9f;
            range = 210f;
            maxTargets = 16;
            coolantMultiplier = 1f;
            hasSpinners = true;
            damage = 27f;
            status = StatusEffects.shocked;
        }};

        concretion = new GeomancyTurret("concretion"){{
            requirements(Category.turret, with(
                Items.copper, 100,
                Items.lead, 120,
                Items.silicon, 75,
                Items.titanium, 60
            ));
            size = 2;
            health = 310 * size * size;
            reloadTime = 120f;
            shootSound = Sounds.rockBreak;
            range = 23f * tilesize;
            recoilAmount = -25f / 4f;
            shootLength = 8f + 15f / 4f;
            targetAir = false;
            cooldown = 0.005f;
            shootType = PMBullets.pillarField;
            chargeTime = PMFx.groundCrack.lifetime / 2f;

            armX = 15f / 4f;
            armY = -2f / 4f;
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
            health = 210 * size * size;
            powerUse = 14f;
            shootCone = 10f;
            range = 240f;
            rangeExtention = 40f;
            reloadTime = 90f;
            shootLength = 5f / 4f;
            recoilAmount = 3f;
            shootDuration = 180f;
            shootType = PMBullets.flameMagma;

            consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 1f/3f)).update(false);
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
            health = 190 * size * size;
            powerUse = 17f;
            shootDuration = 240f;
            range = 280f;
            rangeExtention = 60f;
            reloadTime = 150f;
            shootLength = 11f / 4f;
            rotateSpeed = 3.5f;
            recoilAmount = 4f;
            lightningStroke = 6f;
            shootType = PMBullets.blazeMagma;

            consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.75f)).update(false);
        }};

        inferno = new InfernoTurret("inferno"){{
            requirements(Category.turret, with(
                Items.copper, 700,
                Items.lead, 950,
                Items.graphite, 750,
                Items.silicon, 800,
                Items.titanium, 600,
                Items.thorium, 800,
                Items.surgeAlloy, 650,
                PMItems.tenelium, 600
            ));
            size = 4;
            health = 200 * size * size;
            powerUse = 23f;
            recoilAmount = 8f;
            range = 200f;
            rangeExtention = 60f;
            reloadTime = 240f;
            shootLength = 42f / 4f;
            shootDuration = 60f;
            rotateSpeed = 8f;
            shootType = PMBullets.infernoMagma;

            consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 1f)).update(false);
        }};

        bit = new BitTurret("bit"){{
            requirements(Category.turret, with(
                Items.copper, 50,
                Items.lead, 60,
                Items.silicon, 40,
                Items.titanium, 30
            ));
            size = 2;
            health = 300 * size * size;
            reloadTime = 70f;
            rotateSpeed = 10f;
            recoilAmount = 4f;
            inaccuracy = 15f;
            range = 140f;
            powerUse = 1.35f;
            shootType = PMBullets.pixel;
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
                    PMItems.tenelium, PMBullets.magnetTechtanite
                );
                size = 3;
                health = 90 * size * size;
                range = 23f * 8f;
                reloadTime = 200f;
                inaccuracy = 30f;
                velocityInaccuracy = 0.2f;
                burstSpacing = 5f;
                shots = 4;
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
                bars.add("pm-reload", (ItemTurretBuild entity) -> new Bar(
                    () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
                    () -> entity.team.color,
                    () -> Mathf.clamp(entity.reload / reloadTime)
                ));
            }
        };

        caliber = new SniperTurret("caliber"){{
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
                PMItems.tenelium, SniperBullets.sniperBoltValexitite,
                Items.surgeAlloy, SniperBullets.sniperBoltSurge
            );
            size = 3;
            hideDetails = false;
            health = 120 * size * size;
            reloadTime = 450f;
            inaccuracy = 0f;
            range = 544f;
            rotateSpeed = 2.5f;
            recoilAmount = 5f;
            split = 3f;
            chargeTime = 150f;
            shootSound = Sounds.railgun;
        }};

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
            health = 250 * size * size;
            minRange = 5f * tilesize;
            range = 60f * tilesize;
            shootLength = 23f / 4f;
            reloadTime = 900f;
            inaccuracy = 10f;
            velocityInaccuracy = 0.2f;
            shootSound = Sounds.shootSnap;
            maxAmmo = 30;
            ammoPerShot = 10;
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
                health = 80 * size * size;
                range = 17f * 8f;
                shootLength = 21f / 4f;

                reloadTime = 120f;

                shots = 4;
                spread = 15f; //h
                burstSpacing = 3f;

                shootSound = Sounds.shootSnap;
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
            }
        };

        incision = new SweepLaserTurret("incision"){
            final float brange = range = 22f * tilesize;

            final SweepLaserBulletType sweepLaser = new SweepLaserBulletType(){{
                speed = brange;
                drawSize = brange + 10f * tilesize;
                length = 8f * tilesize;
                startDelay = 0.125f;
                extendTime += startDelay;
                sweepTime += startDelay;
                angleRnd = 25f;
                blasts = 12;
                blastBullet = new BombBulletType(20, 32f, "clear"){{
                    lifetime = 0f;
                    hitEffect = Fx.explosion;
                    status = StatusEffects.blasted;
                }};
            }};

            {
                requirements(Category.turret, with(
                    Items.copper, 60,
                    Items.lead, 50,
                    Items.silicon, 60,
                    Items.titanium, 50
                ));
                health = 260 * size * size;
                size = 2;
                powerUse = 5f;
                reloadTime = 1.5f * 60f;
                shootLength = 23f / 4f;
                shootSound = Sounds.plasmadrop;
                retractDelay = 0.125f;
                hideDetails = false;
                shootType = sweepLaser;

                pointDrawer = t -> {
                    if(t.bullet == null) return;

                    Draw.z(Layer.effect + 1f);
                    Draw.color(Color.red);
                    tr.trns(t.rotation, shootLength);

                    float x = t.x + tr.x + tr2.x,
                        y = t.y + tr.y + tr2.y,
                        fin = Mathf.curve(t.bullet.fin(), 0f, sweepLaser.startDelay),
                        fout = 1f - Mathf.curve(t.bullet.fin(), sweepLaser.retractTime, sweepLaser.retractTime + 0.125f),
                        scl = fin * fout;

                    Fill.circle(x, y, (1.25f + Mathf.absin(Time.time, 1f, 0.25f)) * scl);
                };
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, s -> {
                    s.row();
                    s.table(bt -> {
                        bt.left().defaults().padRight(3).left();

                        BulletType blast = sweepLaser.blastBullet;
                        bt.add(
                            Core.bundle.format("bullet.pm-multi-splash",
                                sweepLaser.blasts,
                                blast.splashDamage,
                                Strings.fixed(blast.splashDamageRadius / tilesize, 1)
                            )
                        );
                        bt.row();
                        bt.add(
                            (blast.status.minfo.mod == null ? blast.status.emoji() : "") + "[stat]" + blast.status.localizedName
                        );
                    }).padTop(-9).left().fillY().get().background(Tex.underline);
                });
            }
        };

        fissure = new SweepLaserTurret("fissure"){{
            float brange = range = 25f * tilesize;

            requirements(Category.turret, with(
                Items.copper, 210,
                Items.titanium, 200,
                Items.silicon, 180,
                PMItems.tenelium, 150
            ));
            health = 230 * size * size;
            size = 3;
            powerUse = 8.5f;
            reloadTime = 2f * 60f;
            shootLength = 46f / 4f;
            shootSound = Sounds.plasmadrop;
            retractDelay = 0.125f;

            RiftBulletType rift = new RiftBulletType(550f){{
                speed = brange;
                drawSize = brange + 10f * tilesize;
                length = 12f * tilesize;
                startDelay = 0.125f;
                extendTime += startDelay;
                sweepTime += startDelay;
                angleRnd = 25f;
                hitSound = PMSounds.riftSplit;
                hitSoundVolume = 0.2f; //IT'S REALLY LOUD
                layer = Layer.effect + 1f;
            }};
            shootType = rift;

            pointDrawer = t -> {
                if(t.bullet == null) return;

                Draw.z(Layer.bullet - 1f);
                Draw.color(Color.black);
                tr.trns(t.rotation, shootLength);

                float x = t.x + tr.x + tr2.x,
                    y = t.y + tr.y + tr2.y,
                    fin = Mathf.curve(t.bullet.fin(), 0f, rift.startDelay),
                    fout = 1f - Mathf.curve(t.bullet.fin(), rift.retractTime, rift.retractTime + 0.125f),
                    scl = fin * fout,
                    s = Mathf.absin(Time.time, 1f, 0.25f),
                    w = 1.5f + s,
                    l = 4.5f + s;

                for(int i = 0; i < 4; i++){
                    float a = t.rotation + 45 + 90 * i;

                    Tmp.v1.trns(a, w * scl, 0f);
                    Tmp.v2.trns(a, -w * scl, 0f);
                    Tmp.v3.trns(a, 0f, l * scl);

                    Fill.tri(
                        x + Tmp.v1.x, y + Tmp.v1.y,
                        x + Tmp.v2.x, y + Tmp.v2.y,
                        x + Tmp.v3.x, y + Tmp.v3.y
                    );
                }
            };
        }};

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
            health = 340 * size * size;
            range = 180f;
            powerUse = 6.5f;
            minRadius = 16.5f;
            bladeCenter = 9f;
            trailWidth = 30f / 4f;
        }};

        ball = new SwordTurret("ball"){
            {
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
                health = 230 * size * size;
                range = 260f;
                powerUse = 13.5f;
                damage = 1000f;
                bladeCenter = 122f / 8f;
                trailWidth = 18f;
                trailLength = 6;
                float attackScl = 1.25f;
                damageRadius *= attackScl;
                attackRadius *= attackScl;
                swords = 5;
                minRadius = 33.25f;
                radius = 6.25f * tilesize;
                float timeScl = 0.9f;
                expandTime *= timeScl;
                pauseTime *= timeScl;
                stabTime *= timeScl;
                totalTime *= timeScl;
                cooldown *= timeScl;
                speed = 3f;
                rotateSpeed = 4.5f;
                float pitchDecrease = 0.25f;
                minPitch -= pitchDecrease;
                maxPitch -= pitchDecrease;
            }

            @Override
            public void load(){
                super.load();

                baseRegion = Core.atlas.find("prog-mats-block-" + size);
            }
        };

        sentinel = new AimLaserTurret("sentinel"){{
            requirements(Category.turret, with(
                Items.copper, 900,
                Items.lead, 375,
                Items.graphite, 350,
                Items.surgeAlloy, 450,
                Items.silicon, 450,
                PMItems.tenelium, 250
            ));

            size = 4;
            hideDetails = false;
            health = 120 * size * size;
            
            shootLength = 34f / 4f;
            range = 328f;
            reloadTime = 600f;

            powerUse = 29f;

            float mul = 3.5f;
            coolantUsage *= mul;
            coolantMultiplier /= mul;

            chargeTime = PMFx.aimChargeBegin.lifetime;
            chargeBeginEffect = PMFx.aimChargeBegin;
            chargeEffect = PMFx.aimCharge;
            chargeEffects = 30;
            chargeMaxDelay = PMFx.aimChargeBegin.lifetime - PMFx.aimCharge.lifetime;

            heatColor = Pal.lancerLaser;
            chargeSound = Sounds.techloop;
            shootSound = Sounds.laserblast;
            chargeVolume = 2f;
            minPitch = 0.75f;
            maxPitch = 1.5f;
            shootSoundVolume = 1f;
            warningDelay = 33f;
            warningVolume = 3f;
            warningSound = PMSounds.sentinelWarning;

            recoilAmount = 3f;
            restitution = 0.02f;
            cooldown = 0.005f;

            aimRnd = 16f;

            shootType = PMBullets.sentinelLaser;
            unitSort = UnitSorts.strongest;
        }};

        blackhole = new BlackHoleTurret("blackhole"){{
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
            health = 230 * size * size;
            canOverdrive = false;
            reloadTime = 520f;
            range = 256f;
            shootEffect = smokeEffect = Fx.none;
            chargeBeginEffect = PMFx.kugelblitzChargeBegin;
            chargeEffect = PMFx.kugelblitzCharge;
            chargeMaxDelay = 30f;
            chargeEffects = 16;
            chargeTime = PMFx.kugelblitzChargeBegin.lifetime;
            rotateSpeed = 2f;
            recoilAmount = 2f;
            restitution = 0.015f;
            cooldown = 0.005f;
            shootLength = 0f;
            shootSound = Sounds.release;
            shootType = PMBullets.blackHole;
        }};

        excalibur = new PopeshadowTurret("excalibur"){
            {
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
                health = 140 * size * size;
                reloadTime = 450f;
                range = 740f;
                shootEffect = smokeEffect = Fx.none;
                shootLength = 0f;
                cooldown = 0.0075f;
                heatColor = Pal.surge;
                chargeTime = 180f;
                chargeSound = PMSounds.popeshadowCharge;
                shootSound = Sounds.laserblast;
                rotateSpeed = 2f;
                recoilAmount = 8f;
                restitution = 0.05f;
                shootType = PMBullets.excaliburLaser;
            }

            @Override
            public void load(){
                super.load();

                baseRegion = Core.atlas.find("prog-mats-block-" + size);
            }
        };

        council = new ModularTurret("council"){{
            requirements(Category.turret, empty);
            size = 5;

            moduleGroups = new ModuleGroup[]{
                new ModuleGroup(ModuleSize.small, ModuleGroupType.circle, 39f/ 4f, 49f/ 4f),
                new ModuleGroup(ModuleSize.medium)
            };
        }};

        congress = new ModularTurret("congress"){{
            requirements(Category.turret, empty);
            size = 7;

            moduleGroups = new ModuleGroup[]{
                new ModuleGroup(ModuleSize.small, ModuleGroupType.oct, 76f / 4f, 49f / 4f),
                new ModuleGroup(ModuleSize.medium, ModuleGroupType.quad, 0f, 71f / 4f),
                new ModuleGroup(ModuleSize.large)
            };
        }};

        pantheon = new ModularTurret("pantheon"){{
            requirements(Category.turret, empty);
            size = 9;
            //TODO
        }};

        sergeant = new PayloadLaunchTurret("tinker"){{
            requirements(Category.turret, with(
                Items.copper, 125,
                Items.lead, 75,
                Items.silicon, 30,
                Items.titanium, 50
            ));
            ammo(
                PMPayloads.basicSentry, PayloadBullets.barrageLaunch,
                PMPayloads.missileSentry, PayloadBullets.downpourLaunch,
                PMPayloads.dashSentry, PayloadBullets.rapierLaunch
            );

            size = 3;
            hideDetails = false;
            health = 140 * size * size;
            minRange = 5f * tilesize;
            range = 40 * tilesize;
            velocityInaccuracy = 0.2f;
            cooldown = 0.03f;
            recoilAmount = 6f;
            restitution = 0.02f;
            shootShake = 2f;

            loadTime = 4f; //In LaunchTurret, this determines how far back the payload is shifted
            chargeTime = 60f;
            shootLength = 11f;
            lineSpacing = 3.5f;
        }};

        arbalest = new PayloadRocketTurret("arbalest"){{
            requirements(Category.turret, with(
                Items.copper, 150,
                Items.graphite, 300,
                Items.silicon, 325,
                Items.titanium, 350,
                PMItems.tenelium, 160
            ));
            ammo(
                PMPayloads.basicRocket, PayloadBullets.arbalestBasic,
                PMPayloads.incendiaryRocket, PayloadBullets.arbalestIncend,
                PMPayloads.bomberRocket, PayloadBullets.arbalestBomber
            );
            size = 5;
            hideDetails = false;
            health = 180 * size * size;
            reloadTime = 1.5f * 60f;
            range = 800f;
            recoilAmount = 4f;
            leadTargets = false;

            shootLength = doorOffset = 6f / 4f;
            doorWidth = 32f / 4f;
            doorLength = 116f / 4f;
            rotOffset = 90f;

            unitSort = UnitSorts.strongest;
        }};

        firestorm = new MissileTurret("firestorm"){{
            requirements(Category.turret, with(
                Items.copper, 180,
                Items.graphite, 140,
                Items.silicon, 65,
                Items.titanium, 70
            ));
            ammo(
                Items.blastCompound, PayloadBullets.firestormMissile
            );
            size = 3;
            hideDetails = false;
            health = 120 * size * size;
            range = 160f;
            reloadTime = 75f;
            shootSound = Sounds.missile;
            cooldown = 0.01f;
            shootShake = 1f;
            targetAir = false;
            burstSpacing = 7f;
            inaccuracy = 15f;
            maxAmmo = 36;
            shootLocs = new float[][]{
                {-31f/4f, 31f/4f}, //TL
                {31f/4f, 31f/4f}, //TR
                {-31f/4f, -31f/4f}, //BL
                {31f/4f, -31f/4f}, //BR
                {0f, 29f/4f}, //T
                {-29f/4f, 0f}, //L
                {0f, -29f/4f}, //B
                {29f/4f, 0f}, //R
                {0f, 0f} //C
            };
        }};

        strikedown = new PayloadMissileTurret("strikedown"){{
            requirements(Category.turret, with(
                Items.copper, 70,
                Items.lead, 350,
                Items.graphite, 300,
                Items.silicon, 300,
                Items.titanium, 250,
                PMItems.tenelium, 120
            ));
            ammo(
                PMPayloads.basicMissile, PayloadBullets.strikedownBasic,
                PMPayloads.recursiveMissile, PayloadBullets.strikedownRecursive
            );
            size = 4;
            hideDetails = false;
            health = 160 * size * size;
            reloadTime = 60f;
            range = 656f;
            shootSound = Sounds.artillery;
            cooldown = 0.01f;
            shootShake = 5f;
            inaccuracy = 5f;
            unitSort = UnitSorts.strongest;
        }};

        trinity = new PayloadMissileTurret("arbiter"){{
            requirements(Category.turret, with(
                Items.copper, 4000,
                Items.graphite, 2200,
                Items.silicon, 2000,
                Items.titanium, 1300,
                Items.thorium, 650,
                Items.surgeAlloy, 200,
                PMItems.tenelium, 800
            ));
            ammo(
                PMPayloads.basicNuke, PayloadBullets.trinityBasic,
                PMPayloads.clusterNuke, PayloadBullets.trinityCluster
            );
            size = 7;
            hideDetails = false;
            health = 170 * size * size;
            range = 2800f;
            shootSound = Sounds.explosionbig;
            cooldown = 0.005f;
            shootShake = 10f;
            unitSort = UnitSorts.strongest;
        }};

        apotheosisNexus = new ApotheosisNexus("apotheosis-nexus"){{
            requirements(Category.turret, with(
                Items.copper, 10200,
                Items.lead, 11600,
                Items.silicon, 7200,
                Items.titanium, 6300,
                Items.thorium, 3100,
                Items.surgeAlloy, 3600,
                PMItems.tenelium, 5400
            ));
            size = 9;
            health = 480 * size * size;
            reloadTime = 60f * 15f;
            range = 200f * tilesize;
            powerUse = 655f;
            damage = 12000f / 12f;
            damageRadius = 6f * tilesize;
            buildingDamageMultiplier = 0.25f;
            speed = 4f;
            duration = 4f * 60f;
            shake = laserShake = 5f;
            outlineColor = PMPal.darkOutline;

            unitSort = UnitSorts.strongest;

            baseDst = new float[]{11f, 19f};
            spinnerWidth = new float[]{49f / 4f, 82f / 4f};
            fireEffect = new MultiEffect(PMFx.apotheosisClouds, PMFx.apotheosisBlast);

            float cooleantUse = 8f;
            coolantMultiplier = 1f / (cooleantUse * Liquids.water.heatCapacity);
            consumes.add(new ConsumeCoolant(cooleantUse)).update(false);
        }};

        apotheosisCharger = new ApotheosisChargeTower("apotheosis-charger"){{
            requirements(Category.turret, with(
                Items.copper, 3200,
                Items.lead, 4100,
                Items.silicon, 4600,
                Items.titanium, 2400,
                Items.thorium, 2300,
                Items.surgeAlloy, 1000,
                PMItems.tenelium, 2500
            ));
            size = 7;
            health = 360 * size * size;
            range = 30f;
            powerUse = 163f;
            damageBoost = 6000f / 12f;
            boostFalloff = ((ApotheosisNexus)apotheosisNexus).boostFalloff;
            radiusBoost = 1f;
            speedBoost = 1f / 8f;
            durationBoost = 5f;
            outlineColor = PMPal.darkOutline;

            startLength = size * tilesize / -4f - 5f;
            endLength = size * tilesize / 2f - 2f;
            effectLength = endLength - 4f;
        }};

        ((ApotheosisNexus)apotheosisNexus).chargeTower = (ApotheosisChargeTower)apotheosisCharger;
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

            consumes.powerCond(1.8f, SmartDrillBuild::isDrilling);
            consumes.liquid(Liquids.water, 0.09f).boost();
        }};
        // endregion

        // region Distribution
        floatingConveyor = new FloatingConveyor("floating-conveyor"){{
            requirements(Category.distribution, with(
                Items.lead, 3,
                Items.metaglass, 3,
                Items.plastanium, 3,
                PMItems.tenelium, 3
            ));
            health = 15;
            speed = 0.06f;
            displayedSpeed = 8.4f;
            buildCostMultiplier = 0.25f;
            researchCostMultiplier = 300f;
        }};

        itemDroneStation = new ItemDroneStation("drone-station-items"){{
            requirements(Category.distribution, with(
                Items.titanium, 300,
                Items.plastanium, 150,
                Items.silicon, 125,
                Items.lead, 175,
                Items.thorium, 125,
                PMItems.tenelium, 75
            ));
            size = 3;
            itemCapacity = 500;
            squareSprite = false;
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
            reloadTime = 120f;
            shots = 90;
            delay = 0.75f;
            range = 560f;
            consumes.power(2.75f);
        }};
        // endregion

        // region Liquids
        liquidDroneStation = new LiquidDroneStation("drone-station-liquids"){{
            requirements(Category.liquid, with(
                Items.titanium, 250,
                Items.plastanium, 125,
                Items.silicon, 125,
                Items.lead, 300,
                Items.metaglass, 175,
                PMItems.tenelium, 75
            ));
            size = 3;
            liquidCapacity = 1000f;
            squareSprite = false;
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

            drawer = new DrawImpact(){{
                plasma1 = Items.titanium.color;
                plasma2 = Items.thorium.color;
            }};
            onCraft = tile -> {
                Tmp.v1.setToRandomDirection().setLength(28f / 4f);
                PMFx.colliderFusion.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
            };

            consumes.power(6f);
            consumes.items(with(
                Items.titanium, 2,
                Items.thorium, 2
            ));
            consumes.liquid(Liquids.cryofluid, 0.2f);
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
            craftEffect = PMFx.superSmeltsmoke;
            ambientSound = Sounds.smelter;
            ambientSoundVolume = 1f; //Big flame louder sound. LIKE REALLY LOUD.

            drawer = new DrawSmelter(Color.valueOf("ffef99")){{
                flameRadiusIn = 4f;
                flameRadius = 7.5f;
                flameRadiusMag = 2.5f;
                flameRadiusScl = 8f;
            }};

            fuelItem = Items.pyratite;
            fuelPerItem = 3;
            fuelPerCraft = 12;
            fuelCapacity = 48;
            fuelUseReduction = 0.8f;
            consumes.items(with(
                Items.lead, 4,
                Items.sand, 10,
                Items.coal, 6
            ));
            consumes.power(9f);
            outputItems = with(
                Items.silicon, 14,
                Items.metaglass, 6
            );
        }};

        moduleAssembler = new PayloadCrafter("module-assembler"){{
            requirements(Category.crafting, empty);
            size = 3;

            recipes(
                new Recipe(shrapnel, 1f, 60f),
                new Recipe(froth, 1f, 60f),
                new Recipe(bifurcation, 1f, 60f),
                new Recipe(overclocker, 1f, 60f)
            );
        }};

        moduleFoundry = new PayloadCrafter("module-foundry"){{
            requirements(Category.crafting, empty);
            size = 5;

            recipes(
                new Recipe(blunderbuss, shrapnel, 1f, 60f),
                new Recipe(airburst, 1f, 60f),
                new Recipe(vulcan, 1f, 60f),
                new Recipe(trifecta, airburst, 1f, 60f),
                new Recipe(jupiter, 1f, 60f)
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
                emptyRocket,
                emptyMissile,
                emptyNuke
            );

            recipes.each(r -> r.centerBuild = true);
        }};

        missileFactory = new PayloadCrafter("missile-factory"){{
            requirements(Category.crafting, with(
                Items.copper, 300,
                Items.lead, 200,
                Items.silicon, 200,
                Items.plastanium, 150,
                Items.thorium, 100,
                Items.surgeAlloy, 110
            ));

            size = 5;
            hideDetails = false;
            ambientSound = Sounds.machine;
            liquidCapacity = 80f;
            recipes(
                basicRocket, incendiaryRocket, bomberRocket,
                basicMissile, recursiveMissile,
                basicNuke, clusterNuke
            );
            recipes.get(1).liquidCost = new LiquidStack(Liquids.slag, 40f);
        }};

        sentryBuilder = new PayloadCrafter("sentry-builder"){{
            requirements(Category.crafting, with(
                Items.copper, 90,
                Items.lead, 80,
                Items.titanium, 60,
                Items.silicon, 150
            ));

            size = 3;
            recipes(
                basicSentry,
                missileSentry,
                dashSentry
            );

            recipes.each(r -> r.blockBuild = false);
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
            final float healing = 100f;

            {
                requirements(Category.units, with(
                    Items.silicon, 60,
                    Items.plastanium, 35,
                    PMItems.tenelium, 50
                ));
                size = 2;
                range = 16f * tilesize;
                height = 0.125f;
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
                height = 0.125f;

                zoneEffect = tile -> all.each(u -> u.apply(PMStatusEffects.speedBoost, 25f * tile.heat));
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
                height = 0.125f;
                baseColor = Pal.redderDust;

                zoneEffect = tile -> all.each(u -> u.apply(PMStatusEffects.strengthBoost, 25f * tile.heat));
            }


            @Override
            public void setStats(){
                super.setStats();

                stats.add(Stat.output, PMStatValues.statusEffect(PMStatusEffects.strengthBoost));
            }
        };

        dronePad = new DronePad("drone-pad"){{
            requirements(Category.units, with(
                Items.titanium, 600,
                Items.thorium, 300,
                Items.plastanium, 300,
                Items.silicon, 550,
                Items.lead, 500,
                Items.surgeAlloy, 90,
                PMItems.tenelium, 150
            ));
            size = 4;
            chargeX = chargeY = 41f / 4f;
            beamWidth = 0.5f;
            droneType = (DroneUnitType)PMUnitTypes.transportDrone;
            chargeRate = 12f; //5 second charge time
            constructTime = 10f * 60f;
            constructPowerUse = chargeRate / (constructTime / (droneType.powerCapacity / chargeRate)) + 4.5f;
            hideDetails = false;
            squareSprite = false;
        }};

        payloadDroneStation = new PayloadDroneStation("drone-station-payloads"){{
            requirements(Category.units, with(
                Items.titanium, 300,
                Items.plastanium, 175,
                Items.silicon, 100,
                Items.lead, 250,
                Items.thorium, 125,
                PMItems.tenelium, 100
            ));
            size = 5;
            maxPayloadSize = 4f;
            squareSprite = false;
        }};
        // endregion

        // region Effect
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
            consumes.power(42.5f);
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

        systemBooster = new SystemBooster("system-booster"){{
            requirements(Category.effect, with(
                Items.lead, 250,
                Items.titanium, 200,
                Items.silicon, 230,
                Items.plastanium, 100,
                Items.surgeAlloy, 130,
                PMItems.tenelium, 170
            ));
            size = 3;
            speedBoost = 1.2f;
            basePowerUse = 2.4f;
            powerPerBlock = 0.08f;
        }};

        shieldProjector = new ShieldProjector("shield-projector"){{
            requirements(Category.effect, with(
                Items.lead, 325,
                Items.titanium, 225,
                Items.surgeAlloy, 75,
                PMItems.tenelium, 125
            ));
            size = 4;
            hideDetails = false;
            radius = 88f;
            shieldHealth = 2600f;
            phaseShieldBoost = 1800f;
            shieldCharge = 600f;
            phaseShieldCharge = 350f;
            cooldownBrokenBase *= 2f;

            consumes.items(with(Items.phaseFabric, 1, PMItems.tenelium, 1)).boost();
            consumes.power(7f);
        }};
        // endregion

        // region Sandbox
        /// Turret
        harbinger = new ChaosTurret("harbinger"){
            {
                size = 8;
                health = 999999999;
                shots = 100;
                inaccuracy = 45f;
                shootShake = 150f;
                powerUse = 300f;
                range = 560f;
                recoilAmount = 8f;
                rotateSpeed = 0.3f;
                shootCone = 20f;
                cooldown = 0.0015f;
                restitution = 0.008f;
                reloadTime = 450f;
                chargeTime = PMFx.harbingerCharge.lifetime;
                chargeBeginEffect = PMFx.harbingerCharge;
                chargeSound = PMSounds.harbingerCharge;
                shootSound = PMSounds.harbingerBlast;
                shootType = PMBullets.harbingerLaser;
            }

            @Override
            public void init(){
                super.init();
                shootLength -= 16f;
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
            }

            @Override
            public void load(){
                super.load();
                baseRegion = Core.atlas.find("prog-mats-block-" + size);
            }
        };

        everythingGun = new EverythingTurret("everything-gun"){
            {
                size = 6;
                health = 999999999;
                reloadTime = 1f;
                range = 4400f;
                shootCone = 360f;
            }

            @Override
            public void load(){
                super.load();
                baseRegion = Core.atlas.find("prog-mats-block-" + size);
            }
        };

        omegaCharger = new ApotheosisChargeTower("omega-charger"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, empty);
            size = 1;
            health = 999999999;
            range = 60f;
            damageBoost = 500000f / 12f;
            boostFalloff = ((ApotheosisNexus)apotheosisNexus).boostFalloff;
            radiusBoost = 5f * tilesize;
            speedBoost = 3f;
            durationBoost = 18f;
            outlineColor = PMPal.darkOutline;

            width = 0.25f;
            startLength = -6f / 4f;
            endLength = 4f;
            effectLength = endLength - 1f;
        }};

        /// Distribution
        everythingItemSource = new EverythingItemSource("everything-item-source");

        sandDriver = new SandDriver("sand-driver"){{
            size = 3;
            itemCapacity = 180;
            reloadTime = 120f;
            shots = 90;
            delay = 0.75f;
            range = 560f;
            consumes.power(0.1f);
        }};

        /// Liquid
        everythingLiquidSource = new EverythingLiquidSource("everything-liquid-source");

        /// Power
        strobeNode = new StrobeNode("rainbow-power-node");

        strobeInf = new StrobeSource("rainbow-power-source");

        strobeBoost = new StrobeSource("rainbow-power-boost"){{
            size = 2;
            boost = true;
            speedBoost = 100f;
        }};

        /// Defense
        sandboxWall = new SandboxWall("sandbox-wall");

        sandboxWallLarge = new SandboxWall("sandbox-wall-large"){{
            size = 2;
            iconSize = 6f;
            rotateRadius = 5f;
        }};

        /// Unit
        godFactory = new UnitFactory("god-factory"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, empty);
            alwaysUnlocked = true;

            size = 3;
            health = 999999999;
            plans = Seq.with(
                new UnitPlan(PMUnitTypes.everythingUnit, 60f * 10f, empty)
            );

            consumes.power(1f);
        }};

        capBlock = new CapBlock("cap-block"){{
            health = 10000;
            unitCapModifier = 25;
        }};

        /// Effect
        multiSource = new MultiSource("multi-source");
        multiVoid = new MultiVoid("multi-void");
        multiSourceVoid = new MultiSourceVoid("multi-source-void");
        // endregion
    }
}