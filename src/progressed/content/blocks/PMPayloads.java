package progressed.content.blocks;

import mindustry.content.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.world.blocks.payloads.*;

import static mindustry.type.ItemStack.*;

public class PMPayloads{
    public static Missile

    //Region Rockets

    emptyRocket,

    basicRocket, incendiaryRocket, //TODO third rocket

    //Region Missiles

    emptyMissile,

    basicMissile, recursiveMissile, //TODO third missile

    //Region Nukes

    emptyNuke,

    basicNuke, clusterNuke, //TODO third nuke

    sandboxNuke,

    //Region Sentries

    basicSentry, missileSentry;

    public static void load(){
        emptyRocket = new Missile("empty-rocket"){{
            requirements = with(Items.copper, 3, Items.lead, 3, Items.titanium, 4);

            size = 3;
            powerUse = 0.5f;
            constructTime = 60f * 2f;
            elevation = 2f / 3f;
            outlineIcon = true;
            outlinedIcon = 0;
        }};

        basicRocket = new Missile("basic-rocket"){{
            requirements = with(Items.titanium, 3, Items.blastCompound, 4);

            prev = emptyRocket;
            size = 3;
            powerUse = 1f;
            constructTime = 60f * 6f;
            elevation = 2f / 3f;
            outlined = true;

            explosionBullet = PayloadBullets.arbalestBasic;
        }};

        incendiaryRocket = new Missile("incendiary-rocket"){{
            requirements = with(Items.titanium, 4, Items.pyratite, 6);

            prev = emptyRocket;
            size = 3;
            powerUse = 1.25f;
            constructTime = 60f * 6.5f;
            elevation = 2f / 3f;
            outlined = true;

            explosionBullet = PayloadBullets.arbalestIncend;
        }};

        emptyMissile = new Missile("empty-missile"){{
            requirements = with(
                Items.copper, 4,
                Items.lead, 4,
                Items.titanium, 6
            );

            size = 2;
            powerUse = 0.75f;
            constructTime = 60f * 2.5f;
        }};

        basicMissile = new Missile("basic-missile"){{
            requirements = with(
                Items.titanium, 4,
                Items.blastCompound, 6
            );

            prev = emptyMissile;
            size = 2;
            powerUse = 1.5f;
            constructTime = 60f * 6f;

            explosionBullet = PayloadBullets.artemisBasic;
        }};

        recursiveMissile = new Missile("recursive-missile"){{
            requirements = with(
                Items.plastanium, 4,
                Items.silicon, 5,
                Items.blastCompound, 7
            );

            prev = emptyMissile;
            size = 2;
            powerUse = 2f;
            constructTime = 60f * 8f;

            explosionBullet = PayloadBullets.artemisRecursive.fragBullet.fragBullet;
            explosions = PayloadBullets.artemisRecursive.fragBullets * PayloadBullets.artemisRecursive.fragBullet.fragBullets;
            explosionArea = -1f;
            maxDelay = 20f;
        }};

        emptyNuke = new Missile("empty-nuke"){{
            requirements = with(
                Items.titanium, 25,
                Items.surgeAlloy, 18,
                PMItems.tenelium, 20
            );

            size = 3;
            powerUse = 5f;
            constructTime = 60f * 8f;
        }};

        basicNuke = new Missile("basic-nuke"){{
            requirements = with(
                Items.lead, 40,
                Items.titanium, 30,
                Items.thorium, 35
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 6f;
            constructTime = 60f * 40f;

            explosionBullet = PayloadBullets.paragonBasic;
        }};

        clusterNuke = new Missile("cluster-nuke"){{
            requirements = with(
                Items.plastanium, 35,
                PMItems.tenelium, 40,
                Items.blastCompound, 40
            );

            prev = emptyNuke;
            size = 3;
            powerUse = 6.25f;
            constructTime = 60f * 45f;

            explosionArea = -1f;
            explosionBullet = PayloadBullets.paragonCluster.fragBullet;
            explosions = PayloadBullets.paragonCluster.fragBullets;
            maxDelay = 20f;
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
            requirements = with(Items.beryllium, 20, Items.graphite, 25, Items.tungsten, 20,  Items.silicon, 20);

            size = 2;
            powerUse = 3f;
            constructTime = 60f * 20f;
            unit = PMUnitTypes.barrage;
        }};

        missileSentry = new Sentry("missile-sentry"){{
            requirements = with(Items.beryllium, 25, Items.graphite, 30, Items.tungsten, 25, Items.oxide, 15, Items.silicon, 25);

            size = 2;
            baseExplosiveness = 100f;
            powerUse = 3.5f;
            constructTime = 60f * 25f;
            unit = PMUnitTypes.downpour;
        }};
    }

    public static void afterLoad(){
        basicRocket.user = PMBlocks.arbalest;
        basicRocket.bullet = basicRocket.explosionBullet;
        incendiaryRocket.user = PMBlocks.arbalest;
        incendiaryRocket.bullet = incendiaryRocket.explosionBullet;

        basicMissile.user = PMBlocks.artemis;
        basicMissile.bullet = basicMissile.explosionBullet;
        recursiveMissile.user = PMBlocks.artemis;
        recursiveMissile.bullet = PayloadBullets.artemisRecursive;

        basicNuke.user = PMBlocks.paragon;
        basicNuke.bullet = basicNuke.explosionBullet;
        clusterNuke.user = PMBlocks.paragon;
        clusterNuke.bullet = PayloadBullets.paragonCluster;
        sandboxNuke.user = PMBlocks.paragon;
        sandboxNuke.bullet = PayloadBullets.ohno;
    }
}
