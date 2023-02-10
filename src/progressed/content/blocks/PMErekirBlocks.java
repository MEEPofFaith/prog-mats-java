package progressed.content.blocks;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.part.*;
import progressed.world.blocks.defence.turret.payload.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.ModuleGroup.*;
import progressed.world.blocks.payloads.*;
import progressed.world.draw.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;
import static progressed.content.blocks.PMPayloads.*;

public class PMErekirBlocks{
    public static Block
        // region Turrets

    //Why do I hear anxiety piano
    sentinel,

    //Sentries
    sergeant,

    //Modular
    matrix,

    // endregion
    // region Crafting

    teneliumFuser,
    moduleAssembler, moduleFoundry,
    sentryBuilder;

    // endregion

    public static void load(){
        // region Turrets

        sentinel = new PowerTurret("sentinel"){{
            requirements(Category.turret, with(
                Items.beryllium, 900,
                Items.tungsten, 375,
                Items.graphite, 350,
                Items.surgeAlloy, 450,
                Items.silicon, 450,
                PMItems.tenelium, 250
            ));

            float aimLength = 48f;
            clipSize = aimLength * 2f;

            drawer = new DrawTurret("reinforced-"){{
                parts.add(new RegionPart("-blade"){{
                    progress = PartProgress.warmup;
                    heatColor = Pal.techBlue;
                    mirror = true;
                    under = true;
                    moveX = 4.75f;
                    children.add(new RegionPart("-blade-glow"){{
                        heatProgress = PartProgress.warmup.add(-0.2f).add(p -> Mathf.sin(9f, 0.2f) * p.warmup);
                        heatColor = Pal.techBlue;
                        drawRegion = false;
                    }});
                }}, new RegionPart("-glow"){{
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.techBlue;
                    drawRegion = false;
                    mirror = false;
                }}, new AimLaserPart(){{
                    alpha = PartProgress.warmup.mul(0.5f).add(0.5f);
                    blending = Blending.additive;
                    length = aimLength;
                    y = -4f;
                }}, new RegionPart("-top"){{
                    progress = PartProgress.warmup;
                    moves.add(new PartMove(PartProgress.warmup.curve(Interp.smooth), 0f, -4f, 0f));
                    heatColor = Pal.techBlue;
                    mirror = true;
                    moveX = 3.25f;
                    children.add(new RegionPart("-top-glow"){{
                        heatProgress = PartProgress.warmup.add(-0.2f).add(p -> Mathf.sin(9f, 0.2f) * p.warmup);
                        heatColor = Pal.techBlue;
                        drawRegion = false;
                    }});
                }});
            }};
            heatColor = Pal.techBlue;
            cooldownTime = 75f;

            size = 4;
            scaledHealth = 210;

            shootY = 6f / 4f;
            range = 328f;
            reload = 120f;

            recoil = 3f;
            outlineColor = Pal.darkOutline;
            linearWarmup = true;
            shootWarmupSpeed = 0.03f;
            minWarmup = 1f;
            warmupMaintainTime = 30f;
            shootSound = Sounds.laserblast;
            rotateSpeed = 1.5f;

            shootType = PMBullets.sentinelLaser;
            unitSort = UnitSorts.strongest;

            consumePower(29f);
            coolant = consumeLiquid(Liquids.water, 1f);
            coolantMultiplier = 0.5f;
        }};

        sergeant = new SinglePayloadAmmoTurret("sergeant"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 125,
                Items.lead, 75,
                Items.silicon, 30,
                Items.titanium, 50
            ));
            ammo(
                basicSentry, PayloadBullets.barrageLaunch,
                missileSentry, PayloadBullets.downpourLaunch
            );

            size = 3;
            reload = 5f * 60f;
            hideDetails = false;
            scaledHealth = 140;
            minRange = 5f * tilesize;
            range = 40 * tilesize;
            velocityRnd = 0.2f;
            cooldownTime = 30f;
            recoil = 6f;
            recoilTime = 60f;
            shake = 2f;
            shootY = -1f;
            setWarmupTime(0.75f);

            drawer = new DrawMulti(
                new DrawPayloadTurret(true, "reinforced-"){{
                    parts.add(new LaunchPart(){{
                        progress = PartProgress.warmup.shorten(0.125f);
                        start = size * tilesize / 2f + 1.5f;
                        length = size * tilesize / 2f * 1.5f;
                        spacing = size * tilesize / 4f;

                        //Set block clipSize
                        clipSize = Math.max(clipSize, size * tilesize + (length + start) * 2f);
                    }});
                }},
                new DrawPayloadAmmo(){{
                    matProgress = PartProgress.reload.inv().shorten(0.125f);
                    y = shootY;
                }}
            );
            outlineColor = Pal.darkOutline;

            coolant = consumeLiquid(Liquids.water, 0.4f);
        }};

        matrix = new ModularTurret("matrix"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with());
            size = 7;
            scaledHealth = 370;
            regionSuffix = "-dark";
            squareSprite = false;
            hideDetails = false;

            moduleGroups = new ModuleGroup[]{
                new ModuleGroup(ModuleSize.small, ModuleGroupType.oct, 73f / 4f, 12f),
                new ModuleGroup(ModuleSize.medium, ModuleGroupType.quad, 0f, 73f / 4f),
                new ModuleGroup(ModuleSize.large)
            };
        }};

        // endregion
        // region Crafting

        teneliumFuser = new HeatCrafter("tenelium-fuser"){{
            requirements(Category.crafting, with());

            size = 3;

            itemCapacity = 20;
            heatRequirement = 15f;
            craftTime = 60f * 2f;
            liquidCapacity = 60f * 5;
            hasLiquids = true;

            outputItem = new ItemStack(PMItems.tenelium, 1);

            craftEffect = CrafterFx.teneliumFuse;

            drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag, 3f),
                new DrawDefault(),
                new DrawHeatInput(),
                new DrawGlowRegion("-glow"){{
                    color = Color.valueOf("70170b");
                }},
                new DrawBlurSpin("-rotator", 12f)
            );

            consumeItem(Items.thorium, 1);
            consumeLiquid(Liquids.slag, 30f / 60f);
            //consumePower(4f);
        }};

        if(false){ //TODO Re-set these up once I create modules for Matrix.
            moduleAssembler = new PayloadCrafter("module-assembler"){{
                requirements(Category.crafting, with(
                    Items.copper, 220,
                    Items.lead, 250,
                    Items.silicon, 100
                ));
                size = 3;

                recipes(
                    new Recipe(Blocks.duo, 3f, 1.5f * 60f) //Placeholder
                );
            }};

            moduleFoundry = new PayloadCrafter("module-foundry"){{
                requirements(Category.crafting, with(
                    Items.lead, 540,
                    Items.silicon, 430,
                    PMItems.tenelium, 300,
                    Items.plastanium, 240
                ));
                size = 5;

                recipes(
                    new Recipe(Blocks.duo, 4f, 5f * 60f) //Placeholder
                );
            }};
        }

        sentryBuilder = new PayloadCrafter("sentry-builder"){{
            requirements(Category.crafting, with(
                Items.copper, 90,
                Items.lead, 80,
                Items.titanium, 60,
                Items.silicon, 150
            ));

            size = 3;
            recipes(
                basicSentry,
                missileSentry
            );

            recipes.each(r -> r.blockBuild = false);
        }};

        // endregion
    }
}
