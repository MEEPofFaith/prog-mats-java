package progressed.content;

import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import progressed.ai.*;
import progressed.content.effects.*;
import progressed.gen.entities.*;
import progressed.type.unit.*;
import progressed.type.weapons.*;

@SuppressWarnings("unchecked")
public class PMUnitTypes{
    public static UnitType

    //TODO A chain of air units with DriftTrail shenanigans. Serpulo. Steal crab unit cyan pallet.
    echo, presence, ghoul, phantom, apparition,

    //TODO A chain of sword-based units. Ground. Serpulo or Erekir? Red pallet.
    puncture, penetration, incision, laceration, amputation,

    //sentry
    barrage, strikedown,

    //legged ground miner.
    draug,

    //signal flare
    flareSmall, flareMedium, flareLarge,

    //swords
    danceSword, masqueradeSword;

    public static void load(){
        //Region Sentry Units
        barrage = EntityRegistry.content("barrage", SentryUnit.class, name -> new SentryUnitType(name){{
            lifetime = 39f * 60f;

            Weapon gunL = new Weapon(name + "-gun-r"){{
                top = false;
                rotate = false;
                mirror = false;
                alternate = true;
                otherSide = 1;

                x = 9.5f / 4f;
                y = 22f / 4f;
                shootX = -1f / 4f;
                shootY = 6f / 4f;

                reload = 6f;
                recoil = 3f / 4f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3f, 23f){{
                    lifetime = 80f;
                    buildingDamageMultiplier = 0.3f;
                    width = 7f;
                    height = 9f;
                    recoil = 0.1f;
                    homingPower = 0.03f;
                    homingRange = 120f;
                }};
            }};
            Weapon gunR = gunL.copy();
            gunR.name = name + "-gun-l";
            gunR.x *= -1;
            gunR.shootX *= -1;
            gunR.flipSprite = true;
            gunR.otherSide = 0;

            weapons.add(gunL, gunR);

            for(int i = 0; i < 4; i++){
                engines.add(new AnchorEngine(3f * Geometry.d8edge(i).x, 3f * Geometry.d8edge(i).y, 1.25f, i * 90f + 45f));
            }
        }});

        strikedown = EntityRegistry.content("downpour", SentryUnit.class, name -> new SentryUnitType(name){{
            lifetime = 32f * 60f;
            range = maxRange = 40f * 8f;

            weapons.add(new RocketWeapon(name + "-rocket"){{
                x = 0f;
                shootY = 0f;
                mirror = false;
                rotate = false;
                reload = 120f;
                shootCone = 5f;
                shootSound = Sounds.missileSmall;
                layerOffset = -0.005f;

                bullet = new BulletType(0, 0){{
                    shootEffect = Fx.none;
                    smokeEffect = MissileFx.shootSmokeDownpour;
                    hitColor = Pal.sap;
                    recoil = 1f;

                    spawnUnit = new MissileUnitType("downpour-rocket"){{
                        speed = 6.4f;
                        maxRange = 9f;
                        lifetime = 60f * 1.2f;
                        missileAccelTime = 0.5f * 60f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.sapBulletBack;
                        engineLayer = Layer.effect;
                        engineOffset = 10;
                        engineSize = 1.5f;
                        health = 45;
                        loopSoundVolume = 0.1f;

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            shootOnDeath = true;
                            bullet = new ExplosionBulletType(260f, 21f){{
                                shootEffect = Fx.massiveExplosion;
                                buildingDamageMultiplier = 0.3f;
                            }};
                        }});
                    }};
                }};
            }});

            for(int i = 0; i < 4; i++){
                engines.add(new AnchorEngine(3f * Geometry.d8edge(i).x, 3f * Geometry.d8edge(i).y, 1.25f, i * 90f + 45f));
            }
        }});

        draug = EntityRegistry.content("draug", NoCoreDepositBuildingTetherLegsUnit.class, name -> new ErekirUnitType(name){{
            controller = u -> new DepotMinerAI();
            isEnemy = false;
            allowedInPayloads = false;
            logicControllable = false;
            playerControllable = false;
            hidden = true;
            hideDetails = false;
            hitSize = 14f;
            speed = 1f;
            rotateSpeed = 2.5f;
            health = 1300;
            armor = 5f;
            omniMovement = false;
            rotateMoveFirst = true;
            itemOffsetY = 5f;

            itemCapacity = 50;
            mineTier = 5;
            mineSpeed = 6f;
            mineWalls = true;
            mineItems = Seq.with(Items.beryllium, Items.graphite, Items.tungsten);

            allowLegStep = true;
            legCount = 6;
            legGroupSize = 3;
            legLength = 12f;
            lockLegBase = true;
            legContinuousMove = true;
            legExtension = -3f;
            legBaseOffset = 5f;
            legMaxLength = 1.1f;
            legMinLength = 0.2f;
            legForwardScl = 1f;
            legMoveSpace = 2.5f;
            hovering = true;

            weapons.add(new Weapon("prog-mats-draug-you-have-incurred-my-wrath-prepare-to-die"){{
                x = 22f / 4f;
                y = -3f;
                shootX = -3f / 4f;
                shootY = 4.5f / 4f;
                rotate = true;
                rotateSpeed = 35f;
                reload = 35f;
                shootSound = Sounds.laser;

                bullet = new LaserBulletType(){{
                    damage = 45f;
                    sideAngle = 30f;
                    sideWidth = 1f;
                    sideLength = 5.25f * 8;
                    length = 13.75f * 8f;
                    colors = new Color[]{Pal.heal.cpy().a(0.4f), Pal.heal, Color.white};
                }};
            }});

            abilities.add(new RegenAbility(){{
                //fully regen in 90 seconds
                percentAmount = 1f / (90f * 60f) * 100f;
            }});
        }});

        flareSmall = EntityRegistry.content("small-flare", SignalFlareUnit.class, name -> new SignalFlareUnitType(name){{
            health = 300f;
            hideDetails = false;
            attraction = 800f;
            flareY = 29f / 4f;
        }});

        flareMedium = EntityRegistry.content("medium-flare", SignalFlareUnit.class, name -> new SignalFlareUnitType(name, 360f){{
            health = 900f;
            hideDetails = false;
            attraction = 11000f;
            flareY = 45f / 4f;
            flareEffectSize = 1.5f;
        }});

        flareLarge = EntityRegistry.content("large-flare", SignalFlareUnit.class, name -> new SignalFlareUnitType(name, 420f){{
            health = 2700f;
            hideDetails = false;
            attraction = 26000f;
            flareY = 61f / 4f;
            flareEffectSize = 2f;
        }});

        danceSword = EntityRegistry.content("dance-sword", SwordUnit.class, name -> new SwordUnitType(name){{
            baseY = -4f;
            tipY = 11f;
            damage = 65f;
            hitEffect = OtherFx.swordStab;
            trailLength = 5;
        }});

        masqueradeSword = EntityRegistry.content("ball-sword", SwordUnit.class, name -> new SwordUnitType(name){{
            baseY = -33f / 4f;
            tipY = 89f / 4f;
            damage = 90f;
            hitEffect = OtherFx.swordStab;
            trailLength = 8;
            trailScl = 4;
            trailVel = 8;
        }});
    }
}
