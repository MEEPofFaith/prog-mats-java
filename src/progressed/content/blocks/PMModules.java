package progressed.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.payloads.*;

public class PMModules implements ContentList{
    public static TurretModulePayload

    //small
    itemtest, liquidtest, bifurcation;

    @Override
    public void load(){
        itemtest = new TurretModulePayload("item-test"){{
            module = new ItemTurretModule("item-test"){{
                ammo(
                    Items.copper, Bullets.standardCopper,
                    Items.titanium, Bullets.artilleryExplosive
                );

                reloadTime = 60f;
                shots = 3;
                inaccuracy = 15;
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
                range = 90f;
                barrels = shots = 2;
                spread = 3;
                shootEffect = Fx.lightningShoot;
                recoilAmount = 1f;
            }};
        }};
    }
}