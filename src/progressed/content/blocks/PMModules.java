package progressed.content.blocks;

import arc.graphics.*;
import arc.math.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.pattern.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.*;
import progressed.world.draw.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static Block

    //Small
    coil, skeet, burst, augment,

    //Medium
    abyss, halberd, gravity,
    //TODO crit sniper
    //TODO? a neoplasm artillery cannon. does ??? to enemy units

    //Large
    firestorm, judgement;

    public static void load(){
        coil = new ItemTurretModule("coil"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            outlineColor = Pal.darkOutline;

            Effect sfe = new MultiEffect(Fx.shootSmallColor, Fx.colorSpark);
            ammo(
                Items.beryllium, new BasicBulletType(2f, 16){{
                    lifetime = 80f;
                    width = 3f;
                    hitSize = 3f;
                    height = 5f;
                    shootEffect = sfe;
                    smokeEffect = Fx.shootSmallSmoke;
                    ammoMultiplier = 1;
                    pierceCap = 2;
                    pierce = true;
                    pierceBuilding = true;
                    hitColor = backColor = trailColor = Pal.berylShot;
                    frontColor = Color.white;
                    trailWidth = 0.75f;
                    trailLength = 10;
                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    buildingDamageMultiplier = 0.3f;
                }}
            );

            shootSound = Sounds.shootAlt;
            targetUnderBlocks = false;
            reload = 40f;
            range = 18f * 8f;

            shoot = new EnhancedShootHelix(5f, 0.4f){{
                shots = 4;
                offset *= Mathf.pi / 4;
            }};

            coolantMultiplier = 5f;
            coolant = consume(new ConsumeLiquid(Liquids.water, 5f / 60f));
            limitRange();
        }};

        skeet = new ItemTurretModule("skeet"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            outlineColor = Pal.darkOutline;

            float brange = 140f;
            ammo(
                Items.tungsten, new AntiMissileRailBulletType(){{
                    length = brange;
                    damage = 45f; //One-shots quell missiles, but not quite anthicus missiles
                    ammoMultiplier = 3;
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
            reload = 45f;
            targetInterval = 10f;
            rotateSpeed = 20f;
            shootCone = 1f;
            playerControllable = false;
            targetGround = false;
            //Only target missile units
            unitFilter = u -> u.controller() instanceof MissileAI;

            coolantMultiplier = 5f;
            coolant = consume(new ConsumeLiquid(Liquids.water, 5f / 60f));
        }};

        burst = new PowerTurretModule("burst"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            outlineColor = Pal.darkOutline;

            shootType = new BulletType(4.5f, 4f){{
                hitSize = 7f;
                lifetime = 24f;
                pierce = true;
                impact = true;
                knockback = 5f;
                hitColor = Color.white;
                hitEffect = ModuleFx.hitSteam;
                despawnEffect = Fx.none;
                hittable = false;
            }};

            reload = 1.25f * 60f;
            range = 13.5f * tilesize;
            shootEffect = ModuleFx.steamBurst;
            shootSound = Sounds.flame;
            shootCone = 60f;

            shoot = new ShootSpread(12, 3f);

            consumePower(2f);
            consumeLiquid(Liquids.water, 12f / 60f);
            limitRange(2f);
        }};

        augment = new BoostModule("augment"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            outlineColor = Pal.darkOutline;

            hasPower = true;
            healPercent = 100f / 60f / 60f;

            consumePower(1f);
            consumeLiquid(Liquids.hydrogen, 1f / 60f);
        }};

        abyss = new PowerTurretModule("abyss"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.medium;
            size = 2;
            outlineColor = Pal.darkOutline;

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
            outlineColor = Pal.darkOutline;
            shootY = 4f;

            float brange = 12f * 8f;
            range = brange;
            ammo(Liquids.nitrogen, new BeamBulletType(40f, "prog-mats-halberd-beam"){{
                length = brange - shootY * 2 + 1f;
                pierceCap = 4;
                knockback = 1;

                growTime = 16f;
                fadeTime = 28f;
                lifetime = growTime + fadeTime;
                optimalLifeFract = growTime / lifetime;
            }});
        }};

        gravity = new TractorConeModule("gravity"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.medium;
            size = 2;
            outlineColor = Pal.darkOutline;

            range = 66f * tilesize; //Just slightly longer range than disrupt :)
            tractorCone = 20f;
            ignoreMass = true;
            force = 0.02f;
            scaledForce = 0.06f;

            consumePower(4f);
        }};

        firestorm = new BallisticModule("firestorm"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            moduleSize = ModuleSize.large;
            size = 3;
            outlineColor = Pal.darkOutline;

            ammo(
                Items.carbide, ModuleBullets.firestormMissile
            );
            limitRange(1f);

            reload = 5f * 60f;
            maxAmmo = 27;
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
            moduleSize = ModuleSize.large;
            size = 3;
            outlineColor = Pal.darkOutline;

            shootType = new PointLaserBulletType(){{
                damageInterval = 2f;
                damage = 800f / 60f * damageInterval;
                beamEffect = Fx.none;
                color = trailColor = Pal.remove;
                shake = 0f;
                trailLength = 24;
            }};

            reload = 3f * 60f;
            range = 14f * 8f;

            consumePower(12f);
        }};
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
