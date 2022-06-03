package progressed.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;
import mindustry.world.*;
import progressed.util.*;

import static mindustry.content.Blocks.*;
import static mindustry.content.Items.*;
import static mindustry.content.TechTree.*;
import static mindustry.content.UnitTypes.*;
import static progressed.content.PMItems.*;
import static progressed.content.blocks.PMBlocks.*;
import static progressed.content.blocks.PMModules.*;
import static progressed.content.blocks.PMPayloads.*;

@SuppressWarnings("CodeBlock2Expr")
public class PMTechTree{
    //Dont mind me I'ma just yoink some stuff from BetaMindy
    static TechTree.TechNode context = null;

    static Seq<ItemStack[]> priceAddition = new Seq<>();

    public static void load(){
        vanillaNode(lancer, () -> {
            // Anime Sweep Laser
            node(incision, () -> {
                node(fissure);
            });

            // Geomancy
            node(concretion, () -> {
                // Eruptors
                node(flame, Seq.with(
                    new SectorComplete(SectorPresets.impact0078),
                    new Research(meltdown)
                ), () -> {
                    node(blaze, Seq.with(new SectorComplete(SectorPresets.overgrowth)));
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

            //Module Turret
            node(council, combineCosts(council, moduleAssembler, shrapnel, froth, bifurcation), Seq.with(
                new Research(wave),
                new Research(arc)
            ), () -> {
                node(congress, () -> {
                    node(pantheon);
                });

                nodeFree(moduleAssembler, council, () -> {
                    node(moduleFoundry);
                });

                nodeFree(shrapnel, council, () -> {
                    node(blunderbuss, () -> {
                        node(airburst, Seq.with(
                            new Research(moduleFoundry),
                            new Research(swarmer)
                        ), () -> {
                            node(trifecta, Seq.with(new Research(congress)));
                        });
                        node(rebound, Seq.with(
                            new Research(moduleFoundry),
                            new Research(congress)
                        ));
                    });
                });
                nodeFree(froth, council);
                nodeFree(bifurcation, council, () -> {
                    node(vulcan, Seq.with(new Research(lancer)), () -> {
                        node(ares, Seq.with(
                            new Research(moduleFoundry),
                            new Research(congress)
                        ));
                    });
                    node(bandage, Seq.with(new Research(mender)), () -> {
                        node(ambrosia, Seq.with(new Research(repairTurret)), () -> {
                            node(vigilance, Seq.with(new Research(moduleFoundry)));
                            node(gravity, Seq.with(new Research(moduleFoundry)));
                        });
                        node(overclocker, Seq.with(new Research(overdriveProjector)));
                        node(pinpoint, Seq.with(new Research(moduleFoundry)));
                    });
                });
                node(iris, () -> {
                    node(lotus, () -> {
                        node(jupiter, Seq.with(
                            new Research(moduleFoundry),
                            new Research(congress)
                        ));
                    });
                });
            });
        });

        vanillaNode(fuse, () -> {
            //Kugelblitz
            node(blackhole, Seq.with(
                new SectorComplete(SectorPresets.nuclearComplex),
                new Research(meltdown),
                new Research(fissure)
            ));
        });

        vanillaNode(ripple, () -> {
            //Missile Launchers
            node(strikedown, combineCosts(strikedown, emptyMissile, basicMissile), Seq.with(
                new SectorComplete(SectorPresets.impact0078),
                new Research(launchPad),
                new Research(arbalest)
            ), () -> {
                node(trinity, combineCosts(trinity, emptyNuke, basicNuke), Seq.with(
                    new Research(interplanetaryAccelerator)
                ), () -> {
                    //Apotheosis
                    node(apotheosisNexus, Seq.with(
                        new Research(impactReactor) //I should probably think of power generation ideas for PM
                    ), () -> {
                        node(apotheosisCharger);
                    });
                });
            });

            //Tinker
            node(sergeant, combineCosts(sergeant, sentryBuilder, basicSentry), Seq.with(
                new Research(payloadPropulsionTower),
                new SectorComplete(SectorPresets.windsweptIslands)
            ), () -> {
                node(arbalest, combineCosts(arbalest, shellPress, emptyRocket, basicRocket), Seq.with(
                    new SectorComplete(SectorPresets.nuclearComplex)
                ), () -> {
                    nodeFree(shellPress, arbalest, () -> {
                        nodeFree(emptyRocket, arbalest);
                        nodeFree(emptyMissile, strikedown);
                        nodeFree(emptyNuke, trinity);
                        nodeFree(missileFactory, arbalest, () -> {
                            //Rockets
                            nodeFree(basicRocket, arbalest, () -> {
                                node(incendiaryRocket);
                            });
                            //Missiles
                            nodeFree(basicMissile, strikedown, () -> {
                                node(recursiveMissile);
                            });
                            //Nukes
                            nodeFree(basicNuke, trinity, () -> {
                                node(clusterNuke);
                            });
                        });
                    });
                });

                nodeFree(sentryBuilder, sergeant, () -> {
                    nodeFree(basicSentry, sergeant);
                    node(missileSentry, Seq.with(new Research(strikedown)));
                    node(dashSentry, Seq.with(
                        new Research(lancer),
                        new Research(quasar)
                    ));
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
            node(dance, Seq.with(
                new SectorComplete(SectorPresets.overgrowth),
                new Research(incision)
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

        // Drills
        vanillaNode(laserDrill, () -> {
            //Smart Drill
            node(smartDrill);
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

        // Units
        vanillaNode(repairPoint, () -> {
            //Zones
            node(healZone);
            node(speedZone, () -> {
                node(strengthZone);
            });
        });

        // Effect
        vanillaNode(coreNucleus, () -> {
            //Core link
            node(coreCovalence, Seq.with(
                new SectorComplete(SectorPresets.impact0078),
                new Research(phaseConveyor)
            ));
        });

        vanillaNode(shockMine, () -> {
            //Static link
            node(fence);
            node(web);
        });

        vanillaNode(overdriveDome, () -> {
            //Overdrive Diffuser
            node(systemBooster, Seq.with(
                new Research(surgeTower)
            ));
        });

        vanillaNode(forceProjector, () -> {
            //Shield Projector
            node(ballisticProjector, Seq.with(
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

    //"TODO: replace this with the standard TechTree API, it's public now -Anuke" -Betamindy
    private static void vanillaNode( UnlockableContent parent, Runnable children){
        context = TechTree.all.find(t -> t.content == parent);
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

    private static void nodeFree(UnlockableContent content, UnlockableContent source, Runnable children){
        node(content, ItemStack.empty, Seq.with(new Research(source)), children);
    }

    private static void nodeFree(UnlockableContent content, UnlockableContent source){
        node(content, ItemStack.empty, Seq.with(new Research(source)));
    }

    private static void nodeProduce(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives.add(new Produce(content)), children);
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

    private static ItemStack[] combineCosts(UnlockableContent... content){
        priceAddition.clear();
        for(UnlockableContent c : content){
            priceAddition.add(c.researchRequirements());
        }
        return PMUtls.addItemStacks(priceAddition);
    }
}
