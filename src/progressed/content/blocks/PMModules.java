package progressed.content.blocks;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;
import progressed.world.blocks.payloads.*;

public class PMModules implements ContentList{
    public static ModulePayload

    //Region Small

    miniOverdrive, shrapnel, froth, bifurcation,

    //Region Medium

    blunderbuss, airburst, vulcan,

    //Region Large

    sunrise;

    @Override
    public void load(){
        miniOverdrive = new ModulePayload("mini-overdrive"){{
            module = new SingleModule("mini-overdrive"){
                {
                    mountType = ChargeMount::new;

                    powerUse = 0.75f;
                }

                @Override
                public void update(ModularTurretBuild parent, BaseMount mount){
                    super.update(parent, mount);

                    if(isDeployed(mount) && mount instanceof ChargeMount m){
                        m.heat = Mathf.lerpDelta(m.heat, Mathf.num(parent.consValid()), 0.08f);
                        m.charge += Time.delta * m.heat;

                        if(m.charge >= 60f){
                            m.charge = 0f;
                            parent.applyBoost(1.25f * parent.efficiency(), 61f);
                        }
                    }
                }
            };
        }};

        shrapnel = new ModulePayload("shrapnel"){{
            module = new ItemTurretModule("shrapnel"){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopper,
                    Items.graphite, ModuleBullets.shotgunDense,
                    Items.titanium, ModuleBullets.shotgunTitanium,
                    Items.thorium, ModuleBullets.shotgunThorium
                );

                reloadTime = 90f;
                shootCone = 30;
                range = 120f;
                shots = 5;
                inaccuracy = 25;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 9f;

                limitRange(6f);
            }};
        }};

        froth = new ModulePayload("froth"){{
            module = new LiquidTurretModule("froth"){{
                ammo(
                    Liquids.water, Bullets.waterShot,
                    Liquids.slag, Bullets.slagShot,
                    Liquids.cryofluid, Bullets.cryoShot,
                    Liquids.oil, Bullets.oilShot
                );

                reloadTime = 5f;
                range = 92f;
                inaccuracy = 8;
                recoilAmount = 0f;
                shootCone = 50f;
                shootEffect = Fx.shootLiquid;
            }};
        }};

        bifurcation = new ModulePayload("bifurcation"){{
            module = new PowerTurretModule("bifurcation"){{
                shootType = new LightningBulletType(){{
                    damage = 12;
                    lightningLength = 20;
                    collidesAir = false;
                    displayAmmoMultiplier = false;
                }};

                reloadTime = 35f;
                shootCone = 40f;
                powerUse = 3.5f;
                targetAir = false;
                range = 100f;
                barrels = shots = 2;
                barrelSpacing = 3;
                shootEffect = Fx.lightningShoot;
                heatColor = Color.red;
                recoilAmount = 1f;
                shootSound = Sounds.spark;
            }};
        }};

        blunderbuss = new ModulePayload("blunderbuss"){{
            size = 2;

            module = new ItemTurretModule("blunderbuss"){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopperCrit,
                    Items.graphite, ModuleBullets.shotgunDenseCrit,
                    Items.titanium, ModuleBullets.shotgunTitaniumCrit,
                    Items.thorium, ModuleBullets.shotgunThoriumCrit
                );
                size = ModuleSize.medium;

                reloadTime = 75f;
                shootCone = 25;
                range = 200f;
                shots = 6;
                inaccuracy = 20;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 9f;

                limitRange(5f);
            }};
        }};

        airburst = new ModulePayload("airburst"){{
            size = 2;

            module = new ItemTurretModule("airburst"){{
                ammo(
                    Items.pyratite, ModuleBullets.swarmIncendiary,
                    Items.blastCompound, ModuleBullets.swarmBlast
                );
                size = ModuleSize.medium;

                reloadTime = 60f;
                range = 200f;
                shots = 7;
                xRand = 4f;
                burstSpacing = 2f;
                inaccuracy = 7f;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 3f;
                recoilAmount = 2f;
                shootSound = Sounds.missile;
            }};
        }};

        vulcan = new ModulePayload("vulcan"){{
            size = 2;

            module = new PowerTurretModule("vulcan"){{
                size = ModuleSize.medium;
                reloadTime = 43f;
                powerUse = 10f;
                range = 116;
                recoilAmount = 2f;
                rotateSpeed = 6.5f;
                shootSound = Sounds.shotgun;
                heatColor = Color.red;

                float brange = range + 10f;

                shootType = new ShrapnelBulletType(){{
                    damage = 53f;
                    length = brange;
                    width = 7f;
                    toColor = Pal.remove;
                    shootEffect = smokeEffect = PMFx.flameShoot;
                    makeFire = true;
                    status = StatusEffects.burning;
                }};
            }};
        }};
    }
}