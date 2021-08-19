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
import static progressed.content.PMBlocks.*;
import static progressed.content.PMItems.*;
import static progressed.content.PMPayloads.*;
import static progressed.content.PMUnitTypes.*;

public class PMTechTree implements ContentList{
    //Dont mind me I'ma just yoink some stuff from BetaMindy
    static TechTree.TechNode context = null;

    @Override
    public void load(){
        vanillaNode(meltdown, () ->{
            // Eruptors
            node(flame, () -> {
                node(blaze, Seq.with(new SectorComplete(SectorPresets.overgrowth)), () -> {
                    node(inferno, Seq.with(new SectorComplete(SectorPresets.nuclearComplex)));
                });

                //Sword
                node(masquerade, Seq.with(new SectorComplete(SectorPresets.overgrowth)), () -> {
                    node(violet, Seq.with(new SectorComplete(SectorPresets.nuclearComplex)));
                });
            });
        });

        vanillaNode(salvo, () -> {
            //Miniguns
            node(minigun, Seq.with(new SectorComplete(SectorPresets.fungalPass)), () -> {
                node(miinigun, Seq.with(new SectorComplete(SectorPresets.overgrowth)), () -> {
                    node(mivnigun);
                });
            });

            //Kugelblitz
            node(blackhole, Seq.with(new SectorComplete(SectorPresets.nuclearComplex), new Research(meltdown)));
        });

        vanillaNode(ripple, () -> {
            //Missile (also painful to look at)
            node(firestorm, Seq.with(new Research(launchPad), new SectorComplete(SectorPresets.impact0078)), () -> {
                node(strikedown, PMUtls.addItemStacks(new ItemStack[][]{
                    brq(strikedown),
                    brq(shellPress),
                    brq(emptyMissile),
                    brq(basicMissile),
                    brq(missileFactory),
                }), Seq.with(new Research(launchPad), new SectorComplete(SectorPresets.nuclearComplex)), () -> {
                    node(shellPress, ItemStack.empty, Seq.with(new Research(strikedown)), () -> {
                        node(emptyMissile, ItemStack.empty, Seq.with(new Research(strikedown)));
                        node(emptyNuke, ItemStack.empty, Seq.with(new Research(trinity)));
                        node(missileFactory, ItemStack.empty, Seq.with(new Research(strikedown)), () -> {
                            //Missile
                            node(basicMissile, ItemStack.empty, Seq.with(new Research(strikedown)), () -> {
                                node(empMissile);
                                node(recursiveMissile);
                            });
                            //Nuke
                            node(basicNuke, ItemStack.empty, Seq.with(new Research(trinity)), () -> {
                                node(clusterNuke);
                            });
                        });
                    });
                    node(trinity, PMUtls.addItemStacks(new ItemStack[][]{
                        brq(trinity),
                        brq(emptyNuke),
                        brq(basicNuke)
                    }), Seq.with(new Research(interplanetaryAccelerator), new SectorComplete(SectorPresets.planetaryTerminal)));
                });
            });

            //Tinker
            node(tinker, PMUtls.addItemStacks(new ItemStack[][]{
                brq(tinker),
                brq(sentryBuilder),
                brq(basicSentry)
            }), Seq.with(new SectorComplete(SectorPresets.windsweptIslands)), () -> {
                node(sentryBuilder, ItemStack.empty, Seq.with(new Research(tinker)), () -> {
                    node(basicSentry, ItemStack.empty, Seq.with(new Research(sentryBuilder)), () -> {
                        node(barrage, ItemStack.empty, Seq.with(new Research(basicSentry)));
                    });
                    node(strikeSentry, Seq.with(new Research(firestorm)), () -> {
                        node(downpour, ItemStack.empty, Seq.with(new Research(strikeSentry)));
                    });
                    node(dashSentry, Seq.with(new Research(lancer), new Research(quasar)), () -> {
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
                node(spark, Seq.with(new Research(differentialGenerator)), () -> {
                    node(storm, Seq.with(new Research(thoriumReactor)));
                });
            });
        });

        vanillaNode(lancer, () -> {
            node(sentinel, Seq.with(new SectorComplete(SectorPresets.impact0078)));
        });

        vanillaNode(cyclone, () -> {
            //Sniper
            node(caliber);
        });

        vanillaNode(foreshadow, () -> {
            //P o p e s h a d o w
            node(excalibur, Seq.with(new SectorComplete(SectorPresets.nuclearComplex)));
        });

        vanillaNode(parallax, () -> {
            //Nanomachines
            node(vaccinator);
        });

        vanillaNode(segment, () -> {
            //Signal flare
            node(signal);
        });

        // Distribution
        vanillaNode(armoredConveyor, () -> {
            //Floating Conveyor
            node(floatingConveyor, Seq.with(new SectorComplete(SectorPresets.windsweptIslands)));
        });

        vanillaNode(massDriver, () -> {
            //Burst Driver
            node(burstDriver, Seq.with(new Research(plastaniumConveyor)));
        });

        // Crating
        vanillaNode(surgeSmelter, () -> {
            //Mindron Collider
            node(mindronCollider);
        });

        // Effect
        vanillaNode(shockMine, () -> {
            //Static link
            node(fence);
            node(web);
        });

        vanillaNode(forceProjector, () -> {
            //Shield Projector
            node(shieldProjector, Seq.with(new Research(strikedown)));
        });

        // Items
        vanillaNode(surgeAlloy, () -> {
            nodeProduce(fusium);
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