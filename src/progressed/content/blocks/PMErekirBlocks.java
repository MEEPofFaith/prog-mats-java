package progressed.content.blocks;

import arc.graphics.*;
import arc.math.*;
import arc.math.Interp.*;
import mindustry.content.*;
import mindustry.entities.*;
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
import progressed.entities.bullet.energy.*;
import progressed.entities.part.*;
import progressed.world.blocks.defence.turret.energy.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.ModuleGroup.*;
import progressed.world.blocks.payloads.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.type.ItemStack.*;

public class PMErekirBlocks{
    public static Block
    // region Turrets

    //Why do I hear anxiety piano
    sentinel,

    //Modular
    matrix,

    //Nexus
    ravage, onslaught,

    // endregion
    // region Crafting

    teneliumFuser,
    moduleAssembler, moduleFoundry; //TODO paragon is gonna need it's own missile crafters


    public static void load(){
        // region Turrets

        sentinel = new PowerTurret("sentinel"){{
            requirements(Category.turret, with(
                Items.copper, 900,
                Items.lead, 375,
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
            envEnabled |= Env.space;
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

        matrix = new ModularTurret("matrix"){{
            requirements(Category.turret, with());
            size = 7;
            scaledHealth = 370;
            envEnabled |= Env.space;

            moduleGroups = new ModuleGroup[]{
                new ModuleGroup(ModuleSize.small, ModuleGroupType.oct, 73f / 4f, 12f),
                new ModuleGroup(ModuleSize.medium, ModuleGroupType.quad, 0f, 73f / 4f),
                new ModuleGroup(ModuleSize.large)
            };
        }};

        ravage = new NexusTurret("ravage"){{
            requirements(Category.turret, with());
            size = 6;

            float brange = range = 100f * 8f;
            ammo(Items.phaseFabric, new OrbitalStrikeBulletType(){{
                speed = brange;
                splashDamage = 2000f;
                splashDamageRadius = 32f;
            }});

            drawer = new DrawTurret(/*"reinforced-"*/){{
                Interp swing = new SwingOut(4f);
                parts.add(new PillarPart(){{
                    radProg = PartProgress.warmup.curve(swing).inv().add(1f);
                    alphaProg = PartProgress.warmup;
                    heightProg = PartProgress.warmup.curve(Interp.pow2In).mul(0.5f).add(0.5f);
                    blending = Blending.additive;
                }});

                for(float i = 0; i < 3; i++){
                    float scl = 2f - i * 0.5f;
                    float del = i * 0.1f;
                    float ii = i;
                    parts.add(new RingPart(){{
                        height = (ii + 1) * 0.25f;
                        inRad = 12f * scl;
                        outRad = 20f * scl;
                        radProg = PartProgress.warmup.compress(0.25f + del, 0.8f + del).curve(Interp.swingOut).inv().add(1f);
                        alphaProg = PartProgress.warmup.compress(0.5f + del, 0.8f + del);
                        blending = Blending.additive;
                    }});
                }
            }};

            linearWarmup = true;
            minWarmup = 1f;
            shootWarmupSpeed = 1f / (1.5f * 60f);
            warmupMaintainTime = 30f;

            reload = 15f;
            envEnabled |= Env.space;

            heatRequirement = 150f;
            maxHeatEfficiency = 1f;
            consumePower(20f);
        }};

        onslaught = new NexusTurret("onslaught"){{
            requirements(Category.turret, with());
            size = 6;

            float brange = range = 100f * 8f;
            ammo(Items.phaseFabric, new OrbitalRainBulletType(){{
                speed = brange;
                bottomColor = Pal.surge;
                topColor = null;
                splashDamage = 2000f;
                splashDamageRadius = 32f;
            }});

            drawer = new DrawTurret(/*"reinforced-"*/){{
                Interp swing = new SwingOut(4f);
                parts.add(new PillarPart(){{
                    colorFrom = Pal.surge;
                    colorTo = null;
                    radProg = PartProgress.warmup.curve(swing).inv().add(1f);
                    alphaProg = PartProgress.warmup;
                    heightProg = PartProgress.warmup.curve(Interp.pow2In).mul(0.5f).add(0.5f);
                    blending = Blending.additive;
                }});

                for(float i = 0; i < 3; i++){
                    float scl = 2f - i * 0.5f;
                    float del = i * 0.1f;
                    float ii = i;
                    parts.add(new RingPart(){{
                        colorFrom = Pal.surge;
                        colorTo = null;
                        height = (ii + 1) * 0.25f;
                        inRad = 12f * scl;
                        outRad = 20f * scl;
                        radProg = PartProgress.warmup.compress(0.25f + del, 0.8f + del).curve(Interp.swingOut).inv().add(1f);
                        alphaProg = PartProgress.warmup.compress(0.5f + del, 0.8f + del);
                        blending = Blending.additive;
                    }});
                }
            }};

            linearWarmup = true;
            minWarmup = 1f;
            shootWarmupSpeed = 1f / (1.5f * 60f);
            warmupMaintainTime = 30f;

            reload = 140f;
            envEnabled |= Env.space;

            heatRequirement = 150f;
            maxHeatEfficiency = 1f;
            consumePower(20f);
        }};

        // endregion
        // region Crafting

        teneliumFuser = new HeatCrafter("tenelium-fuser"){{
            requirements(Category.crafting, with());

            size = 3;
            envEnabled |= Env.space;

            itemCapacity = 20;
            heatRequirement = 15f;
            craftTime = 60f * 2f;
            liquidCapacity = 60f * 5;
            hasLiquids = true;

            outputItem = new ItemStack(PMItems.tenelium, 1);

            craftEffect = CrafterFx.teneliumFuse;

            drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag),
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

        if(false){
            moduleAssembler = new PayloadCrafter("module-assembler"){{
                requirements(Category.crafting, with(
                    Items.copper, 220,
                    Items.lead, 250,
                    Items.silicon, 100
                ));
                size = 3;
                envEnabled |= Env.space;

                recipes(
                    new Recipe(Blocks.duo, 3f, 1.5f * 60f) //Placeholder
                );
            }};
            // endregion

            // region Crafting
            moduleFoundry = new PayloadCrafter("module-foundry"){{
                requirements(Category.crafting, with(
                    Items.lead, 540,
                    Items.silicon, 430,
                    PMItems.tenelium, 300,
                    Items.plastanium, 240
                ));
                size = 5;
                envEnabled |= Env.space;

                recipes(
                    new Recipe(Blocks.duo, 4f, 5f * 60f) //Placeholder
                );
            }};
        }
    }
}
