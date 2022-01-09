package progressed.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import progressed.content.bullets.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.payloads.*;

public class PMModules implements ContentList{
    public static TurretModulePayload

    //Region Small

    shrapnel, liquidtest, bifurcation,

    //Region Medium

    airburst;

    //Region Large

    @Override
    public void load(){
        shrapnel = new TurretModulePayload("shrapnel"){{
            module = new ItemTurretModule("shrapnel"){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopper,
                    Items.graphite, ModuleBullets.shotgunDense,
                    Items.titanium, ModuleBullets.shotgunTitanium,
                    Items.thorium, ModuleBullets.shotunThorium
                );
                limitRange();

                reloadTime = 90f;
                shootCone = 15;
                range = 120f;
                shots = 5;
                inaccuracy = 12;
            }};
        }};

        liquidtest = new TurretModulePayload("liquid-test"){{
            module = new LiquidTurretModule("item-test"){{
                ammo(
                    Liquids.water, Bullets.standardThorium,
                    Liquids.slag, Bullets.fragSurge
                );

                reloadTime = 3f;
                inaccuracy = 8;
            }};
        }};

        bifurcation = new TurretModulePayload("bifurcation"){{
            module = new PowerTurretModule("bifurcation"){{
                shootType = new LightningBulletType(){{
                    damage = 12;
                    lightningLength = 25;
                    lightningColor = Color.valueOf("ff9c5a");
                    collidesAir = false;
                    displayAmmoMultiplier = false;
                }};

                reloadTime = 35f;
                shootCone = 40f;
                powerUse = 3.5f;
                targetAir = false;
                range = 100f;
                barrels = shots = 2;
                spread = 3;
                shootEffect = Fx.lightningShoot;
                recoilAmount = 1f;
            }};
        }};

        airburst = new TurretModulePayload("airburst"){{
            size = 2;

            module = new ItemTurretModule("airburst"){{
                ammo(
                    Items.graphite, ModuleBullets.swarmDense
                );
                size = ModuleSize.medium;

                reloadTime = 60f;
                range = 200f;
                shots = 7;
                xRand = 4f;
                burstSpacing = 1f;
                inaccuracy = 7f;
                velocityInaccuracy = 0.4f;
                recoilAmount = 4f;
            }};
        }};
    }
}