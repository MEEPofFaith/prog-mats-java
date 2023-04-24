package progressed.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.energy.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.*;
import progressed.world.draw.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static Block

    //Small
    augment, skeet,
    //TODO a mid-range shooty turret (a mini breach i guess?)

    //Medium
    abyss, halberd, gravity,
    //TODO a weak, long range tractor beam. purpose is drawing in those pesky disrupts and quells
    //TODO a neoplasm artillery cannon. does ??? to enemy units

    //Large
    firestorm, judgement;

    public static void load(){
        augment = new BoostModule("augment"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            hasPower = true;
            healPercent = 100f / 60f / 60f;

            consumePower(2f);
        }};

        skeet = new ItemTurretModule("skeet"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            float brange = 140f;
            ammo(
                Items.tungsten, new RailBulletType(){{
                    length = brange;
                    damage = 18f;
                    ammoMultiplier = 3;
                    collidesGround = false;
                    hitColor = Color.valueOf("feb380");
                    hitEffect = Fx.hitBulletColor;
                    pierceDamageFactor = 0.5f;

                    shootEffect = ModuleFx.skeetShoot;
                    smokeEffect = Fx.colorSpark;
                    lineEffect = ModuleFx.skeetLine;
                    endEffect = ModuleFx.skeetEnd;
                }}
            );

            range = brange;
            reload = 15f;
            targetInterval = 10f;
            rotateSpeed = 20f;
            shootCone = 1f;
            targetGround = false;
            //Prioritize missiles above regular units
            unitSort = (u, x, y) -> (u.type instanceof MissileUnitType ? -1000 : 0) + UnitSorts.closest.cost(u, x, y);
        }};

        abyss = new PowerTurretModule("abyss"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.medium;
            size = 2;

            float brange = 14.5f * 8f;
            range = brange;
            shootType = new AbyssBulletType(){{
                splashDamage = 280f;
                splashDamageRadius = 20f;
                pierceArmor = true;
                buildingDamageMultiplier = 0.3f;
                lifetime = ModuleFx.abyssGrow.lifetime;
                swirlEffects = 10;
                maxSwirlDelay = lifetime - ModuleFx.abyssSwirl.lifetime;
                length = brange;

                beamEffect = ModuleFx.abyssBeam;
                swirlEffect = ModuleFx.abyssSwirl;
                growEffect = ModuleFx.abyssGrow;

                //swirlRad = 4f * 8f;
                despawnEffect = ModuleFx.abyssBurst;
                displayAmmoMultiplier = false;
            }};

            drawer = new DrawTurretModule(){{
                parts.add(new RegionPart("-shell"){{
                    x = 3f/4f;
                    y = -10f/4f;

                    moveX = 0.9f;
                    moveY = -2f/4f;
                    moveRot = -6.5f;

                    mirror = true;
                }});
            }};

            reload = 2.5f * 60f;
            shootSound = Sounds.bolt;
            linearWarmup = true;
            minWarmup = 1f;
            shootWarmupSpeed = 1.5f / 60f;
            shootY = -2f/4f;

            consumePower(8f);
        }};

        halberd = new BeamModule("halberd"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.medium;
            size = 2;

            float brange = 12f * 8f;
            range = brange;
            shootType = new BeamBulletType(40f, "prog-mats-halberd-beam"){{
                length = brange + 2f;
                pierceCap = 4;

                growTime = 8f;
                fadeTime = 30f;
                lifetime = growTime + fadeTime;
                optimalLifeFract = growTime / lifetime;
            }};

            reload = 90f;
            shootDuration = 120f;
            shootSound = Sounds.laser;

            consumePower(4f);
        }};

        gravity = new TractorConeModule("gravity"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.medium;
            size = 2;

            range = 475f; //Just slightly longer range than disrupt :)
            tractorCone = 20f;
            force = 30;
            scaledForce = 20;

            consumePower(4f);
        }};

        firestorm = new BallisticModule("firestorm"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            ammo(
                Items.carbide, ModuleBullets.firestormMissile
            );
            limitRange(1f);

            reload = 5f * 60f;
            maxAmmo = 27;
            moduleSize = ModuleSize.large;
            size = 3;
            range = 27f * 8f;
            minRange = 7f * 8f;
            targetAir = false;
            shootSound = Sounds.missileSmall;
            hideDetails = false;

            shoot = new ShootBarrel(){{
                barrels = new float[]{
                    0f, 0f, 0f,
                    -3f, 3f, 0f,
                    3f, 3f, 0f,
                    -3f, -3f, 0f,
                    3f, -3f, 0f
                };

                shots = 9;
                shotDelay = 10f;
            }};
        }};

        judgement = new SweepLaserTurretModule("judgement"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());

            reload = 3f * 60f;
            moduleSize = ModuleSize.large;
            size = 3;
            range = 14f * 8f;

            shootType = new PointLaserBulletType(){{
                damageInterval = 2f;
                damage = 800f / 60f * damageInterval;
                beamEffect = Fx.none;
                color = trailColor = Pal.remove;
                shake = 0f;
                trailLength = 24;
            }};

            consumePower(12f);
        }};
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
