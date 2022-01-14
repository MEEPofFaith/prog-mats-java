package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.multi.modules.*;
import progressed.world.blocks.defence.turret.multi.modules.turret.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;
import progressed.world.blocks.payloads.*;

import static mindustry.Vars.*;

public class PMModules implements ContentList{
    public static ModulePayload

    //Region Small

    miniOverdrive, shrapnel, froth, bifurcation,

    //Region Medium

    blunderbuss, airburst, vulcan, gravity,

    //Region Large

    trident;

    @Override
    public void load(){
        miniOverdrive = new ModulePayload("mini-overdrive"){{
            module = new SingleModule("mini-overdrive"){
                TextureRegion topRegion;
                final float speedBoost = 1.25f;

                {
                    mountType = ChargeMount::new;

                    powerUse = 0.75f;
                }

                @Override
                public void load(){
                    super.load();
                    topRegion = Core.atlas.find(name + "-top");
                }

                @Override
                public void setStats(Stats stats){
                    super.setStats(stats);

                    stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
                }

                @Override
                public void draw(ModularTurretBuild parent, BaseMount mount){
                    super.draw(parent, mount);

                    ChargeMount m = (ChargeMount)mount;

                    Draw.color(PMPal.overdrive);
                    Draw.alpha(m.heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
                    Draw.rect(topRegion, m.x, m.y);
                    Draw.color();
                }

                @Override
                public void update(ModularTurretBuild parent, BaseMount mount){
                    super.update(parent, mount);

                    if(isDeployed(mount) && mount instanceof ChargeMount m){
                        m.heat = Mathf.lerpDelta(m.heat, Mathf.num(parent.consValid()), 0.08f);
                        m.charge += Time.delta * m.heat;

                        if(m.charge >= 60f){
                            m.charge = 0f;
                            parent.applyBoost(speedBoost * parent.efficiency(), 61f);
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
                    Liquids.water, ModuleBullets.waterShotMini,
                    Liquids.slag, ModuleBullets.slagShotMini,
                    Liquids.cryofluid, ModuleBullets.cryoShotMini,
                    Liquids.oil, ModuleBullets.oilShotMini
                );

                reloadTime = 5f;
                range = 96f;
                targetAir = false;
                inaccuracy = 8;
                recoilAmount = 0f;
                shootCone = 20f;
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
                shootSound = Sounds.spark;
            }};
        }};

        blunderbuss = new ModulePayload("blunderbuss"){{
            size = 2;

            module = new ItemTurretModule("blunderbuss", ModuleSize.medium){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopperCrit,
                    Items.graphite, ModuleBullets.shotgunDenseCrit,
                    Items.titanium, ModuleBullets.shotgunTitaniumCrit,
                    Items.thorium, ModuleBullets.shotgunThoriumCrit
                );

                reloadTime = 75f;
                shootCone = 25;
                range = 200f;
                shots = 6;
                inaccuracy = 20;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 9f;
                shootSound = Sounds.shootBig;

                limitRange(5f);
            }};
        }};

        airburst = new ModulePayload("airburst"){{
            size = 2;

            module = new ItemTurretModule("airburst", ModuleSize.medium){{
                ammo(
                    Items.pyratite, ModuleBullets.swarmIncendiary,
                    Items.blastCompound, ModuleBullets.swarmBlast
                );

                reloadTime = 60f;
                range = 200f;
                shootCone = 45;
                shots = 7;
                xRand = 4f;
                burstSpacing = 2f;
                inaccuracy = 7f;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 3f;
                shootSound = Sounds.missile;
            }};
        }};

        vulcan = new ModulePayload("vulcan"){{
            size = 2;

            module = new PowerTurretModule("vulcan", ModuleSize.medium){{
                reloadTime = 43f;
                powerUse = 10f;
                range = 116;
                rotateSpeed = 6.5f;
                shootSound = Sounds.shotgun;
                heatColor = Color.red;

                float brange = range + 10f;

                shootType = new ShrapnelBulletType(){{
                    damage = 67f;
                    length = brange;
                    width = 12f;
                    toColor = Pal.remove;
                    shootEffect = smokeEffect = PMFx.flameShoot;
                    makeFire = true;
                    status = StatusEffects.burning;
                }};
            }};
        }};

        gravity = new ModulePayload("gravity"){{
            size = 2;

            module = new ForceModule("gravity", ModuleSize.medium){{
                range = 180f;
                damage = 0.2f;
                powerUse = 2f;
                radius = 5f;
                laserWidth = 0.3f;
                maxTargets = 6;
                force = 8f;
                scaledForce = 5f;
            }};
        }};

        trident = new ModulePayload("trident"){{
            size = 3;

            module = new ItemTurretModule("trident", ModuleSize.large){
                {
                    ammo(
                        Items.blastCompound, ModuleBullets.tridentMissile
                    );

                    range = 34f * tilesize;
                    reloadTime = 120f;
                    shots = 3;
                    barrels = 3;
                    barrelSpacing = 6f;
                    burstSpacing = 15f;
                    shootLength -= 6f;
                    topLayerOffset = 0.30f;
                    shootSound = Sounds.artillery;
                }

                @Override
                public void createIcons(MultiPacker packer){
                    Outliner.outlineRegion(
                        packer,
                        Core.atlas.find("prog-mats-trident-missile"),
                        outlineColor,
                        "prog-mats-trident-missile-outline"
                    );
                    super.createIcons(packer);
                }
            };
        }};
    }
}