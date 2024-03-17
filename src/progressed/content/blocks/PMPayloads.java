package progressed.content.blocks;

import mindustry.content.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.world.blocks.payloads.*;

import static mindustry.type.ItemStack.*;

public class PMPayloads{
    public static Missile

    //Region Rockets

    emptyCruiseMissile,

    basicCruiseMissile, incendiaryCruiseMissile, splitterCruiseMissile,

    //Region Missiles

    emptyBallisticMissile,

    basicBallisticMissile, recursiveBallisticMissile, bombingBallisticMissile,

    //Region Nukes

    emptyNuke,

    basicNuke, clusterNuke, empNuke, blackHoleNuke,

    sandboxNuke,

    //Region Sentries

    basicSentry, missileSentry;

    public static void load(){
        emptyCruiseMissile = new Missile("empty-rocket"){{
            buildCost(
                Items.copper, 1,
                Items.lead, 1,
                Items.titanium, 2
            );

            size = 3;
            powerUse = 0.5f;
            constructTime = 60f * 2f;
            elevation = 2f / 3f;
            outlineIcon = true;
            outlinedIcon = 0;
        }};

        basicCruiseMissile = new Missile("basic-rocket"){{
            buildCost(
                Items.titanium, 1,
                Items.blastCompound, 3
            );

            prev = emptyCruiseMissile;
            size = 3;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionBullet = PayloadBullets.arbalestBasic;
        }};

        incendiaryCruiseMissile = new Missile("incendiary-rocket"){{
            buildCost(
                Items.titanium, 2,
                Items.pyratite, 4
            );

            prev = emptyCruiseMissile;
            size = 3;
            powerUse = 1.2f;
            constructTime = 60f * 4.5f;

            elevation = 2f / 3f;
            outlined = true;

            explosionBullet = PayloadBullets.arbalestIncend;
        }};

        splitterCruiseMissile = new Missile("splitter-rocket"){{
            buildCost(
                Items.titanium, 2,
                Items.silicon, 2,
                Items.blastCompound, 5
            );

            prev = emptyCruiseMissile;
            size = 3;
            powerUse = 1.1f;
            constructTime = 60f * 5f;

            elevation = 2f / 3f;
            outlined = true;

            //madness
            explosionBullet = PayloadBullets.arbalestSplitter
                .spawnUnit.weapons.first().bullet
                .fragBullet
                .spawnUnit.weapons.first().bullet
                .fragBullet;
            explosions = 9 + 6 + 3;
            explosionArea = -1f;
            maxDelay = 20f;
        }};

        emptyBallisticMissile = new Missile("empty-missile"){{
            buildCost(
                Items.copper, 1,
                Items.lead, 1,
                Items.titanium, 2
            );

            size = 2;
            powerUse = 0.6f;
            constructTime = 60f * 2f;
        }};

        basicBallisticMissile = new Missile("basic-missile"){{
            buildCost(
                Items.titanium, 1,
                Items.blastCompound, 3
            );

            prev = emptyBallisticMissile;
            size = 2;
            powerUse = 1.1f;
            constructTime = 60f * 4f;

            explosionBullet = PayloadBullets.artemisBasic;
        }};

        recursiveBallisticMissile = new Missile("recursive-missile"){{
            buildCost(
                Items.plastanium, 2,
                Items.silicon, 3,
                Items.blastCompound, 5
            );

            prev = emptyBallisticMissile;
            size = 2;
            powerUse = 1.25f;
            constructTime = 60f * 7.5f;

            explosionBullet = PayloadBullets.artemisRecursive.fragBullet.fragBullet;
            explosions = PayloadBullets.artemisRecursive.fragBullets * PayloadBullets.artemisRecursive.fragBullet.fragBullets;
            explosionArea = -1f;
            maxDelay = 20f;
        }};

        bombingBallisticMissile = new Missile("bombing-missile"){{
            buildCost(
                Items.silicon, 5,
                Items.blastCompound, 6
            );

            prev = emptyBallisticMissile;
            size = 2;
            powerUse = 1.2f;
            constructTime = 60f * 7f;

            explosionBullet = PayloadBullets.artemisBombing.intervalBullet;
            explosions = 13; //Some value
            explosionArea = -1f;
            maxDelay = 20f;
        }};

        emptyNuke = new Missile("empty-nuke"){{
            buildCost(
                Items.titanium, 30,
                Items.surgeAlloy, 20,
                PMItems.tenelium, 25
            );

            size = 3;
            powerUse = 5f;
            constructTime = 60f * 12f;
        }};

        basicNuke = new Missile("basic-nuke"){{
            buildCost(
                Items.lead, 100,
                Items.titanium, 80,
                Items.thorium, 120
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 9.5f;
            constructTime = 60f * 45f;

            explosionBullet = PayloadBullets.paragonBasic;
        }};

        clusterNuke = new Missile("cluster-nuke"){{
            buildCost(
                Items.plastanium, 80,
                PMItems.tenelium, 90,
                Items.blastCompound, 110
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 10f;
            constructTime = 60f * 53f;

            explosionArea = -1f;
            explosionBullet = PayloadBullets.paragonCluster.fragBullet;
            explosions = PayloadBullets.paragonCluster.fragBullets;
            maxDelay = 20f;
        }};

        empNuke = new Missile("emp-nuke"){{
            buildCost(
                PMItems.tenelium, 110,
                Items.surgeAlloy, 65,
                Items.silicon, 80
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 15f;
            constructTime = 60f * 49f;

            explosionBullet = PayloadBullets.paragonEMP;
        }};

        blackHoleNuke = new Missile("black-hole-nuke"){{
            buildCost(
                PMItems.tenelium, 110,
                Items.surgeAlloy, 65,
                Items.silicon, 80
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 15f;
            constructTime = 60f * 49f;

            explosionBullet = PayloadBullets.paragonBlackHole;
        }};

        sandboxNuke = new Missile("sandbox-nuke"){{
            requirements = empty;
            displayCampaign = false;

            size = 3;

            explosionArea = -1f;
            explosionBullet = PayloadBullets.ohno.fragBullet;
            explosions = PayloadBullets.ohno.fragBullets;
            maxDelay = 20f;
        }};

        basicSentry = new Sentry("basic-sentry"){{
            buildCost(
                Items.graphite, 12,
                Items.tungsten, 7,
                Items.silicon, 7
            );

            size = 2;
            powerUse = 1.75f;
            constructTime = 60f * 12f;
            unit = PMUnitTypes.barrage;
        }};

        missileSentry = new Sentry("missile-sentry"){{
            buildCost(
                Items.graphite, 15,
                Items.tungsten, 10,
                Items.oxide, 5,
                Items.silicon, 10
            );

            size = 2;
            baseExplosiveness = 100f;
            powerUse = 2f;
            constructTime = 60f * 16f;
            unit = PMUnitTypes.strikedown;
        }};
    }
}
