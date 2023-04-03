package progressed.content.blocks;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.*;
import progressed.world.blocks.defence.*;
import progressed.world.blocks.defence.turret.sandbox.*;
import progressed.world.blocks.sandbox.defence.*;
import progressed.world.blocks.sandbox.distribution.*;
import progressed.world.blocks.sandbox.heat.*;
import progressed.world.blocks.sandbox.items.*;
import progressed.world.blocks.sandbox.power.*;
import progressed.world.blocks.sandbox.units.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class PMSandboxBlocks{
    public static Block

    //Turret
    eviscerator, everythingGun, testTurret,

    //Distribution
    everythingItemSource, sandDriver,

    //liquid
    everythingLiquidSource,

    //Power
    strobeNode, strobeInf, strobeBoost,

    //Defense
    sandboxWall, sandboxWallLarge, targetDummyBase,

    //Heat
    infiniHeatSource,

    //Unit
    godFactory, capBlock, harmacist,

    //Items
    multiSource, multiVoid, multiSourceVoid, multiEverythingSourceVoid,

    //Effect
    infiniMender, infiniOverdrive;
    
    public static void load(){
        // Turret
        eviscerator = new ChaosTurret("harbinger"){
            {
                size = 8;
                shake = 150f;
                range = ((LaserBulletType)PMBullets.harbingerLaser).length;
                recoil = 8f;
                shootY = 16f;
                rotateSpeed = 0.3f;
                shootCone = 20f;
                cooldownTime = 600f;
                recoilTime = 600f;
                reload = 450f;
                moveWhileCharging = false;
                chargeSound = PMSounds.harbingerCharge;
                shootSound = PMSounds.harbingerBlast;
                shootType = PMBullets.harbingerLaser;

                shoot = new ShootSpread(){{
                    shots = 100;
                    spread = 55f / shots;
                    firstShotDelay = EnergyFx.harbingerCharge.lifetime;
                }};
                inaccuracy = 15f;

                consumePower(300f);
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
            }
        };

        everythingGun = new EverythingTurret("everything-gun"){{
            size = 6;
            rotateSpeed = 20f;
            reload = 1f;
            range = 4400f;
            shootCone = 360f;
        }};

        testTurret = new FreeTurret("test-turret"){{
            requirements(Category.turret, OS.username.equals("MEEP") ? BuildVisibility.sandboxOnly : BuildVisibility.hidden, with());
            size = 2;
            range = 69 * tilesize;
            reload = 60f;
            shootType = new SnakeBulletType(3f, 50f, "aflare"){{
                length = 5;
                lifetime = 300f;
                weaveScale = 8f;
                weaveMag = 2f;
                homingPower = 0.3f;
            }};
        }};

        // Distribution
        everythingItemSource = new EverythingItemSource("everything-item-source");

        sandDriver = new SandDriver("sand-driver"){{
            size = 3;
            itemCapacity = 180;
            reload = 120f;
            shots = 90;
            delay = 0.75f;
            range = 560f;

            consumePower(0.1f);
        }};

        // Liquid
        everythingLiquidSource = new EverythingLiquidSource("everything-liquid-source");

        // Power
        strobeNode = new StrobeNode("rainbow-power-node");

        strobeInf = new StrobeSource("rainbow-power-source");

        strobeBoost = new StrobeSource("rainbow-power-boost"){{
            size = 2;
            speedBoost = 100f;
        }};

        // Defense
        sandboxWall = new SandboxWall("sandbox-wall");

        sandboxWallLarge = new SandboxWall("sandbox-wall-large"){{
            size = 2;
        }};

        targetDummyBase = new TargetDummyBase("target-dummy-base"){{
            size = 2;
            pullScale = 0.1f;
        }};

        // Heat
        infiniHeatSource = new InfiniHeatSource("infini-heater");

        // Unit
        godFactory = new UnitFactory("god-factory"){{
            requirements(Category.units, ProgMats.everything() ? BuildVisibility.sandboxOnly : BuildVisibility.hidden, with());
            alwaysUnlocked = true;
            hasItems = false;
            configurable = false;

            size = 3;
            plans = Seq.with(
                new UnitPlan(PMUnitTypes.everythingUnit, 60f * 10f, with())
            );

            consumePower(69f / 60f);
        }};

        capBlock = new CapBlock("cap-block"){{
            unitCapModifier = 25;
        }};

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

        // Items
        multiSource = new MultiSource("multi-source");
        multiVoid = new MultiVoid("multi-void");
        multiSourceVoid = new MultiSourceVoid("multi-source-void");
        multiEverythingSourceVoid = new MaterialSourceVoid("material-source-void");

        // Effect
        infiniMender = new InfiniMendProjector("infini-mender");
        infiniOverdrive = new InfiniOverdriveProjector("infini-overdrive");
    }
}
