package progressed.content.blocks;

import arc.math.*;
import arc.math.Interp.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import progressed.content.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.part.*;
import progressed.world.blocks.defence.turret.energy.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.ModuleGroup.*;
import progressed.world.blocks.defence.turret.payload.modular.modules.BaseModule.*;
import progressed.world.blocks.payloads.*;

import static mindustry.type.ItemStack.with;

public class PMErekirBlocks{
    public static Block
    // region Turrets

    //Modular
    matrix,

    //Missile
    paragon,

    //Nexus
    ravage, onslaught,

    // endregion
    // region Crafting
    moduleAssembler, moduleFoundry; //TODO paragon is gonna need it's own missile crafters


    public static void load(){
        // region Turrets
        matrix = new ModularTurret("matrix"){{
            requirements(Category.turret, with());
            size = 7;
            scaledHealth = 370;

            moduleGroups = new ModuleGroup[]{
                new ModuleGroup(ModuleSize.small, ModuleGroupType.oct, 73f / 4f, 12f),
                new ModuleGroup(ModuleSize.medium, ModuleGroupType.quad, 0f, 73f / 4f),
                new ModuleGroup(ModuleSize.large)
            };
        }};

        if(false){
            paragon = new PayloadAmmoTurret("paragon"){{
                requirements(Category.turret, with());
                size = 7;
            }};
        }

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
                }});

                for(float i = 0; i < 3; i++){
                    float scl = 2f - i * 0.5f;
                    float del = i * 0.1f;
                    float ii = i;
                    parts.add(new RingPart(){{
                        height = (ii + 1) * 0.25f;
                        inRad = 12f * scl;
                        outRad = 20f * scl;
                        //colorTo = colorFrom.cpy().a(0.5f);
                        radProg = p -> 2f - Interp.swingOut.apply(Mathf.curve(p.warmup, 0.25f + del, 0.8f + del));
                        alphaProg = p -> Mathf.curve(p.warmup, 0.5f + del, 0.8f + del);
                        //Uncomment this in the next build if my PR is merged.
                        //radProg = PartProgress.warmup.compress(0.25f + del, 0.7f + del).curve(Interp.swingOut).inv().add(1f);
                        //alphaProg = PartProgress.warmup.compress(0.5f + del, 0.7f + del);
                    }});
                }
            }};

            linearWarmup = true;
            minWarmup = 1f;
            shootWarmupSpeed = 1f / (1.5f * 60f);

            reload = 15f;

            heatRequirement = 150f;
            maxHeatEfficiency = 1f;
            consumePower(20f);
        }};

        onslaught = new NexusTurret("onslaught"){{
            requirements(Category.turret, with());
            size = 6;

            //TODO Make OrbitalStrikeRainBulletType
            float brange = range = 100f * 8f;
            ammo(Items.phaseFabric, new OrbitalStrikeBulletType(){{
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
                        radProg = p -> 2f - Interp.swingOut.apply(Mathf.curve(p.warmup, 0.25f + del, 0.8f + del));
                        alphaProg = p -> Mathf.curve(p.warmup, 0.5f + del, 0.8f + del);
                        //Uncomment this in the next build if my PR is merged.
                        //radProg = PartProgress.warmup.compress(0.25f + del, 0.7f + del).curve(Interp.swingOut).inv().add(1f);
                        //alphaProg = PartProgress.warmup.compress(0.5f + del, 0.7f + del);
                    }});
                }
            }};

            linearWarmup = true;
            minWarmup = 1f;
            shootWarmupSpeed = 1f / (1.5f * 60f);

            reload = 15f;

            heatRequirement = 150f;
            maxHeatEfficiency = 1f;
            consumePower(20f);
        }};

        if(false){
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

                recipes(
                    new Recipe(Blocks.duo, 4f, 5f * 60f) //Placeholder
                );
            }};
        }
    }
}
