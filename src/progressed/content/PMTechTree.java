package progressed.content;

import arc.func.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;
import progressed.util.*;

import static mindustry.content.Blocks.*;
import static mindustry.content.Items.*;
import static mindustry.content.TechTree.*;
import static mindustry.content.UnitTypes.*;
import static progressed.content.PMItems.*;
import static progressed.content.blocks.PMBlocks.*;
import static progressed.content.blocks.PMErekirBlocks.*;
import static progressed.content.blocks.PMPayloads.*;

@SuppressWarnings("CodeBlock2Expr")
public class PMTechTree{
    //Dont mind me I'ma just yoink some stuff from BetaMindy
    static TechTree.TechNode context = null;

    static Seq<ItemStack[]> priceAddition = new Seq<>();

    public static void load(){
        /// Serpulo
        // Turrets
        vanillaNode(lancer, () -> {
            // Behold, a laser pointer
            node(pinpoint);

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
        });

        vanillaNode(fuse, () -> {
            //Kugelblitz
            node(kugelblitz, Seq.with(
                new SectorComplete(SectorPresets.nuclearComplex),
                new Research(meltdown)
            ));
        });

        vanillaNode(ripple, () -> {
            //Rockets
            node(arbalest, combineCosts(arbalest, shellPress, emptyRocket, basicRocket), Seq.with(
                new SectorComplete(SectorPresets.impact0078)
            ), () -> {
                nodeFree(shellPress, arbalest, () -> {
                    //Rockets
                    nodeFree(emptyRocket, arbalest, () -> {
                        nodeFree(basicRocket, arbalest, () -> {
                            node(incendiaryRocket);
                        });
                    });
                    //Missiles
                    nodeFree(emptyMissile, artemis, () -> {
                        nodeFree(basicMissile, artemis, () -> {
                            node(recursiveMissile);
                        });
                    });
                    //Nukes
                    nodeFree(emptyNuke, paragon, () -> {
                        nodeFree(basicNuke, paragon, () -> {
                            node(clusterNuke);
                        });
                    });
                    nodeFree(missileFactory, arbalest);
                });
            });

            //Missiles
            node(artemis, combineCosts(artemis, emptyMissile, basicMissile), Seq.with(
                new SectorComplete(SectorPresets.impact0078),
                new Research(launchPad),
                new Research(arbalest)
            ), () -> {
                //Nukes
                node(paragon, combineCosts(paragon, emptyNuke, basicNuke), Seq.with(
                    new SectorComplete(SectorPresets.planetaryTerminal)
                ), () -> {});
            });
        });

        vanillaNode(arc, () -> {
            //Pixel
            node(bit);
            
            //Tesla
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

        vanillaNode(cyclone, () -> {
            //Sniper
            node(caliber);

            //Sword
            node(dance, Seq.with(
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
        /*vanillaNode(coreShard, () -> { //check the block for why hidden
            node(coreCripple);
        });*/

        vanillaNode(coreFoundation, () -> {
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

        vanillaNode(forceProjector, () -> {
            //Shield Projector
            node(ballisticProjector, Seq.with(
                new Research(artemis)
            ));
        });

        // Items
        vanillaNode(surgeAlloy, () -> {
            nodeProduce(tenelium);
        });

        /// Erekir
        // Turrets
        String erekir = "erekir";
        vanillaNode(erekir, lustre, () -> {
            //Anxiety-inducing aiming laser go brrr
            node(sentinel);
        });

        vanillaNode(erekir, disperse, () -> {
            //Sentries
            node(sergeant, combineCosts(sergeant, sentryBuilder, basicSentry), Seq.with(
                new Research(avert), new Research(unitCargoLoader)
            ), () -> {
                nodeFree(sentryBuilder, sergeant, () -> {
                    nodeFree(basicSentry, sergeant, () -> {
                        node(missileSentry);
                    });
                });
            });
        });

        // Crafting
        vanillaNode(erekir, surgeCrucible, () -> {
            node(teneliumFuser);
        });

        // Items
        vanillaNode(erekir, surgeAlloy, () -> {
            node(tenelium);
        });
    }

    private static void vanillaNode(UnlockableContent parent, Runnable children){
        vanillaNode("serpulo", parent, children);
    }

    private static void vanillaNode(String tree, UnlockableContent parent, Runnable children){
        context = findNode(TechTree.roots.find(r -> r.name.equals(tree)), n -> n.content == parent);
        children.run();
    }

    private static TechNode findNode(TechNode root, Boolf<TechNode> filter){
        if(filter.get(root)) return root;
        for(TechNode node : root.children){
            TechNode search = findNode(node, filter);
            if(search != null) return search;
        }
        return null;
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
