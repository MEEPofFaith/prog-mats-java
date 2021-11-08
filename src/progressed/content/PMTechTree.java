package progressed.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.content.Blocks.*;
import static mindustry.content.Items.*;
import static mindustry.content.TechTree.*;
import static mindustry.content.UnitTypes.*;
import static progressed.content.PMItems.*;
import static progressed.content.PMUnitTypes.*;
import static progressed.content.blocks.PMBlocks.*;
import static progressed.content.blocks.PMPayloads.*;

public class PMTechTree implements ContentList{
    //Dont mind me I'ma just yoink some stuff from BetaMindy
    static TechTree.TechNode context = null;

    @Override
    public void load(){
        vanillaNode(lancer, () -> {
            // Geomancy
            node(concretion, () -> {
                // Eruptors
                node(flame, Seq.with(
                    new SectorComplete(SectorPresets.impact0078),
                    new Research(meltdown)
                ), () -> {
                    node(blaze, Seq.with(
                        new SectorComplete(SectorPresets.overgrowth)
                    ), () -> {
                        node(inferno, Seq.with(
                            new SectorComplete(SectorPresets.nuclearComplex)
                        ));
                    });
                });
            });
        });

        vanillaNode(salvo, () -> {
            //Miniguns
            node(minigun, Seq.with(
                new SectorComplete(SectorPresets.fungalPass)
            ), () -> {
                node(miinigun, Seq.with(
                    new SectorComplete(SectorPresets.overgrowth)
                ), () -> {
                    node(mivnigun);
                });
            });
        });

        vanillaNode(fuse, () -> {
            //Kugelblitz
            node(blackhole, Seq.with(
                new SectorComplete(SectorPresets.nuclearComplex),
                new Research(meltdown)
            ));
        });

        vanillaNode(ripple, () -> {
            //Missile Launchers (also painful to look at)
            node(firestorm, Seq.with(
                new SectorComplete(SectorPresets.impact0078),
                new Research(launchPad)
            ), () -> {
                node(strikedown, Seq.with(
                    new SectorComplete(SectorPresets.nuclearComplex),
                    new Research(arbalest)
                ), () -> {
                    node(trinity, Seq.with(
                        new Research(interplanetaryAccelerator)
                    ), () -> {
                        //Apotheosis
                        node(apotheosisNexus, Seq.with(
                            new Research(impactReactor) //I should probably think of power generation block ideas for PM
                        ), () -> {
                            node(apotheosisCharger);
                        });
                    });
                });
            });

            //Tinker
            node(sergeant, Seq.with(
                new SectorComplete(SectorPresets.windsweptIslands)
            ), () -> {
                node(arbalest, Seq.with(
                    new SectorComplete(SectorPresets.nuclearComplex)
                ), () -> {
                    node(shellPress, () -> {
                        node(emptyRocket);
                        node(emptyMissile, Seq.with(
                            new Research(strikedown)
                        ));
                        node(emptyNuke, Seq.with(
                            new Research(trinity)
                        ));
                        node(missileFactory, () -> {
                            //Rockets
                            node(basicRocket, () -> {
                                node(incendiaryRocket);
                                node(bomberRocket);
                            });
                            //Missiles
                            node(basicMissile, Seq.with(
                                new Research(strikedown)
                            ), () -> {
                                node(recursiveMissile);
                            });
                            //Nukes
                            node(basicNuke, Seq.with(
                                new Research(trinity)
                            ), () -> {
                                node(clusterNuke);
                            });
                        });
                    });
                });

                node(sentryBuilder, () -> {
                    node(basicSentry, () -> {
                        node(barrage, ItemStack.empty, Seq.with(
                            new Research(basicSentry)
                        ));
                    });
                    node(missileSentry, Seq.with(new Research(firestorm)), () -> {
                        node(downpour, ItemStack.empty, Seq.with(
                            new Research(missileSentry)
                        ));
                    });
                    node(dashSentry, Seq.with(
                        new Research(lancer),
                        new Research(quasar)
                    ), () -> {
                        node(rapier, ItemStack.empty, Seq.with(new Research(dashSentry)));
                    });
                });
            });
        });

        vanillaNode(arc, () -> {
            //Pixel
            node(bit);
            
            //Coil
            node(shock, () -> {
                node(spark, Seq.with(
                    new Research(differentialGenerator)
                ), () -> {
                    node(storm, Seq.with(
                        new Research(thoriumReactor)
                    ));
                });
            });
        });

        vanillaNode(lancer, () -> {
            node(sentinel, Seq.with(
                new SectorComplete(SectorPresets.impact0078)
            ));
        });

        vanillaNode(cyclone, () -> {
            //Sniper
            node(caliber);

            //Sword
            node(ball, Seq.with(
                new SectorComplete(SectorPresets.overgrowth)
            ), () -> {
                node(masquerade, Seq.with(
                    new SectorComplete(SectorPresets.nuclearComplex)
                ));
            });
        });

        vanillaNode(foreshadow, () -> {
            //P o p e s h a d o w
            node(excalibur, Seq.with(
                new SectorComplete(SectorPresets.nuclearComplex)
            ));
        });

        vanillaNode(parallax, () -> {
            //Nanomachines
            node(vaccinator);
        });

        vanillaNode(segment, () -> {
            //Signal flare
            node(allure);
        });

        // Distribution
        vanillaNode(armoredConveyor, () -> {
            //Floating Conveyor
            node(floatingConveyor, Seq.with(
                new SectorComplete(SectorPresets.windsweptIslands)
            ));
        });

        vanillaNode(massDriver, () -> {
            //Burst Driver
            node(burstDriver, Seq.with(
                new Research(plastaniumConveyor)
            ));

            //Drones
            node(dronePad, Seq.with(new Research(launchPad), new Research(mega)), () -> {
                node(itemDroneStation, Seq.with(
                    new Research(vault)
                ));
                node(liquidDroneStation, Seq.with(
                    new Research(liquidTank)
                ));
                node(payloadDroneStation, Seq.with(
                    new Research(payloadConveyor)
                ));
            });
        });

        // Crating
        vanillaNode(surgeSmelter, () -> {
            //Mindron Collider
            node(mindronCollider);
        });

        vanillaNode(siliconCrucible, () -> {
           //Forge
           node(pyroclastForge);
        });

        // Effect
        vanillaNode(shockMine, () -> {
            //Static link
            node(fence);
            node(web);
        });

        vanillaNode(forceProjector, () -> {
            //Shield Projector
            node(shieldProjector, Seq.with(
                new Research(strikedown)
            ));
        });

        // Items
        vanillaNode(surgeAlloy, () -> {
            nodeProduce(tenelium);
        });
    }

    private static ItemStack[] brq(Block content){
        return content.researchRequirements();
    }

    private static void vanillaNode(UnlockableContent parent, Runnable children){
        context = TechTree.get(parent);
        children.run();
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Seq<Objective> objectives, Runnable children){
        TechNode node = new TechNode(context, content, requirements);
        if(objectives != null) node.objectives = objectives;

        TechNode prev = context;
        context = node;
        children.run();
        context = prev;
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Seq<Objective> objectives){
        node(content, requirements, objectives, () -> {});
    }

    private static void node(UnlockableContent content, Seq<Objective> objectives){
        node(content, content.researchRequirements(), objectives, () -> {});
    }

    private static void node(UnlockableContent content, ItemStack[] requirements){
        node(content, requirements, Seq.with(), () -> {});
    }

    private static void node(UnlockableContent content, ItemStack[] requirements, Runnable children){
        node(content, requirements, null, children);
    }

    private static void node(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives, children);
    }

    private static void node(UnlockableContent content, Runnable children){
        node(content, content.researchRequirements(), children);
    }

    private static void node(UnlockableContent block){
        node(block, () -> {});
    }

    private static void nodeProduce(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives.and(new Produce(content)), children);
    }

    private static void nodeProduce(UnlockableContent content, Seq<Objective> objectives){
        nodeProduce(content, objectives, () -> {});
    }

    private static void nodeProduce(UnlockableContent content, Runnable children){
        nodeProduce(content, Seq.with(), children);
    }

    private static void nodeProduce(UnlockableContent content){
        nodeProduce(content, Seq.with(), () -> {});
    }
}