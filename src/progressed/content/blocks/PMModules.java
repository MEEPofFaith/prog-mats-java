package progressed.content.blocks;

import mindustry.content.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.type.*;
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
    augment,

    //Medium
    abyss,

    //Large
    firestorm, judgement;

    public static void load(){
        augment = new BoostModule("augment"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            hasPower = true;
            healPercent = 100f / 60f / 60f;

            consumePower(2f);
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
            shootSound = Sounds.missile;
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

            shootType = new MagmaBulletType(62f, 4f){{ //Temp bullet for testing. Copied from Flame.
                shake = 0f;
                crackEffects = 0;
                puddleLiquid = null;
                makeFire = false;
            }};

            consumePower(12f);
        }};
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
