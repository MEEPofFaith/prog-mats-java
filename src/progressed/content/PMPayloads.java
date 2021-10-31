package progressed.content;

import mindustry.content.*;
import mindustry.ctype.*;
import progressed.content.bullets.*;
import progressed.entities.bullet.explosive.*;
import progressed.world.blocks.payloads.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class PMPayloads implements ContentList{
    public static Missile

    //Region Rockets

    emptyRocket,

    basicRocket, incendiaryRocket, bomberRocket,

    //Region Missiles

    emptyMissile,

    basicMissile, recursiveMissile, //TODO third missile

    //Region Nukes

    emptyNuke,

    basicNuke, clusterNuke, //TODO third nuke

    //Region Sentries

    basicSentry, strikeSentry, dashSentry;

    @Override
    public void load(){
        emptyRocket = new Missile("empty-rocket"){{
            requirements = with(Items.copper, 3, Items.lead, 3, Items.titanium, 4);

            size = 3;
            powerUse = 0.5f;
            constructTime = 60f * 2f;
            elevation = 2f / 3f;
        }};

        basicRocket = new Missile("basic-rocket"){{
            requirements = with(Items.titanium, 3, Items.blastCompound, 4);

            prev = emptyRocket;
            size = 3;
            powerUse = 1f;
            constructTime = 60f * 6f;
            elevation = 2f / 3f;

            explosion = PayloadBullets.arbalestBasic;
        }};

        incendiaryRocket = new Missile("incendiary-rocket"){{
            requirements = with(Items.titanium, 4, Items.pyratite, 6);

            prev = emptyRocket;
            size = 3;
            powerUse = 1.25f;
            constructTime = 60f * 6.5f;
            elevation = 2f / 3f;

            explosion = PayloadBullets.arbalestIncend;
        }};

        bomberRocket = new Missile("bomber-rocket"){{
            requirements = with(Items.titanium, 2, Items.silicon, 3, Items.pyratite, 10);

            prev = emptyRocket;
            size = 3;
            powerUse = 1.25f;
            constructTime = 60f * 6.5f;
            elevation = 2f / 3f;

            explosionArea = -1f;
            explosion = ((RocketBulletType)(PayloadBullets.arbalestBomber)).bombBullet;
            explosions = 50;
            maxDelay = 25f;
        }};

        emptyMissile = new Missile("empty-missile"){{
            requirements = with(Items.copper, 4, Items.lead, 4, Items.titanium, 6);

            size = 2;
            powerUse = 0.75f;
            constructTime = 60f * 2.5f;
        }};

        basicMissile = new Missile("basic-missile"){{
            requirements = with(Items.titanium, 4, Items.blastCompound, 6);

            prev = emptyMissile;
            size = 2;
            powerUse = 1.5f;
            constructTime = 60f * 6f;

            explosion = PayloadBullets.strikedownBasic;
        }};

        recursiveMissile = new Missile("recursive-missile"){{
            requirements = with(Items.titanium, 3, Items.plastanium, 4, Items.silicon, 4, Items.blastCompound, 7);

            prev = emptyMissile;
            size = 2;
            powerUse = 2f;
            constructTime = 60f * 8f;
            requiresUnlock = true;

            explosionArea = -1f;
            explosion = ((StrikeBulletType)(((StrikeBulletType)(PayloadBullets.strikedownRecursive)).splitBullet)).splitBullet;
            explosions = 13;
            maxDelay = 20f;
        }};

        emptyNuke = new Missile("empty-nuke"){{
            requirements = with(Items.titanium, 25, Items.surgeAlloy, 18, PMItems.valexitite, 20);

            size = 3;
            powerUse = 5f;
            constructTime = 60f * 8f;
            requiresUnlock = true;
        }};

        basicNuke = new Missile("basic-nuke"){{
            requirements = with(Items.lead, 40,Items.titanium, 30, Items.thorium, 35);

            prev = emptyNuke;
            size = 3;
            powerUse = 6f;
            constructTime = 60f * 55f;
            requiresUnlock = true;

            explosion = PayloadBullets.trinityBasic;
        }};

        clusterNuke = new Missile("cluster-nuke"){{
            requirements = with(Items.titanium, 35, Items.plastanium, 25, PMItems.valexitite, 15, Items.silicon, 30, Items.blastCompound, 25);

            prev = emptyNuke;
            size = 3;
            powerUse = 6.25f;
            constructTime = 60f * 60f;
            requiresUnlock = true;

            explosionArea = -1f;
            StrikeBulletType b = (StrikeBulletType)PayloadBullets.trinityCluster;
            explosion = b.splitBullet;
            explosions = b.splitBullets;
            maxDelay = 20f;
        }};

        basicSentry = new Sentry("basic-sentry"){{
            requirements = with(Items.copper, 20, Items.lead, 25, Items.titanium, 10, Items.silicon, 20);

            size = 2;
            powerUse = 4f;
            constructTime = 60f * 20f;
            unit = PMUnitTypes.barrage;
        }};

        strikeSentry = new Sentry("strike-sentry"){{
            requirements = with(Items.copper, 30, Items.lead, 30, Items.titanium, 15, Items.silicon, 25, Items.blastCompound, 15);

            size = 2;
            powerUse = 4.5f;
            constructTime = 60f * 25f;
            unit = PMUnitTypes.downpour;
        }};

        dashSentry = new Sentry("dash-sentry"){{
            requirements = with(Items.copper, 20, Items.lead, 20, Items.titanium, 25, Items.graphite, 20, Items.silicon, 25);

            size = 2;
            powerUse = 5.25f;
            constructTime = 60f * 23f;
            unit = PMUnitTypes.rapier;
        }};
    }
}