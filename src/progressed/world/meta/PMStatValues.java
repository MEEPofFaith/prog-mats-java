package progressed.world.meta;

import arc.flabel.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.maps.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.InjectorBulletType.*;
import progressed.entities.units.*;
import progressed.ui.*;
import progressed.util.*;
import progressed.world.blocks.crafting.*;
import progressed.world.blocks.payloads.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMStatValues{
    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map){
        return ammo(map, 0);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent){
        return table -> {
            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t : orderedKeys){
                boolean compact = t instanceof UnitType || indent > 0;
                boolean payload = t instanceof Missile;

                BulletType type = map.get(t);
                //no point in displaying unit icon twice
                if(!compact && !(t instanceof PowerTurret)){
                    if(payload){
                        if(t.unlockedNow()){
                            table.image(icon(t)).padRight(4).right().top();
                        }else{
                            table.add(PMElements.imageStack(icon(t), Icon.tree.getRegion(), Color.red)).padRight(4).right().top();
                        }
                    }else{
                        table.image(icon(t)).size(3 * 8).padRight(4).right().top();
                    }
                    table.add(payload && !t.unlockedNow() ? "@pm-missing-research" : t.localizedName).padRight(10).left().top();
                }

                if(!payload || t.unlockedNow()){
                    table.table(bt -> {
                        bt.left().defaults().padRight(3).left();

                        if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                            if(type instanceof BlackHoleBulletType stype){
                                bt.add(bundle.format("bullet.pm-blackhole-damage", stype.continuousDamage(), Strings.fixed(stype.damageRadius / tilesize, 1)));
                                sep(bt, bundle.format("bullet.pm-suction-radius", stype.suctionRadius / tilesize));
                            }else if(type.continuousDamage() > 0){
                                bt.add(bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                            }else{
                                if(Float.isInfinite(type.damage)){
                                    bt.add(new FLabel("{wave}{rainbow}" + bundle.format("bullet.damage", bundle.get("pm-infinity"))));
                                }else{
                                    bt.add(bundle.format("bullet.damage", type.damage));
                                }
                                if(type instanceof MagnetBulletType stype){
                                    sep(bt, bundle.format("bullet.pm-attraction-radius", stype.force, stype.attractRange / tilesize));
                                }
                            }
                        }

                        if(type instanceof CritBulletType stype){
                            sep(bt, bundle.format("bullet.pm-crit-chance", (int)(stype.critChance * 100f)));
                            sep(bt, bundle.format("bullet.pm-crit-multiplier", (int)stype.critMultiplier));
                        }

                        if(type instanceof SignalFlareBulletType stype && stype.spawn instanceof FlareUnitType u){
                            sep(bt, bundle.format("bullet.pm-flare-health", u.health));
                            sep(bt, bundle.format("bullet.pm-flare-attraction", u.attraction));
                            sep(bt, bundle.format("bullet.pm-flare-lifetime", (int)(u.duration / 60f)));
                        }

                        if(type.buildingDamageMultiplier != 1){
                            sep(bt, bundle.format("bullet.buildingdamage", (int)(type.buildingDamageMultiplier * 100)));
                        }

                        if(type.splashDamage > 0){
                            sep(bt, bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                        }

                        if(!compact && !Mathf.equal(type.ammoMultiplier, 1f) && !(type instanceof LiquidBulletType) && !(t instanceof PowerTurret)){
                            sep(bt, bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
                        }

                        if(!Mathf.equal(type.reloadMultiplier, 1f)){
                            sep(bt, bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)));
                        }

                        if(type.knockback > 0){
                            sep(bt, bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
                        }

                        if(type.healPercent > 0f){
                            sep(bt, bundle.format("bullet.healpercent", (int)type.healPercent));
                        }

                        if(type.pierce || type.pierceCap != -1){
                            sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : bundle.format("bullet.pierce", type.pierceCap));
                        }

                        if(type.incendAmount > 0){
                            sep(bt, "@bullet.incendiary");
                        }

                        if(type.status != StatusEffects.none){
                            sep(bt, (type.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName);
                        }

                        if(type instanceof InjectorBulletType stype){ //This could probably be optimized, but stat display is only run once so whatever
                            Vaccine[] v = stype.vaccines;
                            StringBuilder str = new StringBuilder();
                            str.append("[lightgray]");

                            if(v.length == 1){ //Single
                                StatusEffect s = v[0].status;
                                str.append(s.minfo.mod == null ? s.emoji() : "")
                                    .append("[stat]")
                                    .append(s.localizedName);
                            }else if(v.length == 2){ //Double
                                StatusEffect s = v[0].status;
                                str.append(s.minfo.mod == null ? s.emoji() : "")
                                    .append("[stat]")
                                    .append(s.localizedName)
                                    .append("[] or ");

                                s = v[1].status;
                                str.append(s.minfo.mod == null ? s.emoji() : "")
                                    .append("[stat]")
                                    .append(s.localizedName);
                            }else if(v.length > 2){ //3 or more
                                for(int i = 0; i < v.length - 1; i++){
                                    StatusEffect s = v[i].status;
                                    str.append(s.minfo.mod == null ? s.emoji() : "")
                                        .append("[stat]")
                                        .append(s.localizedName)
                                        .append("[], ");
                                }

                                StatusEffect s = v[v.length - 1].status;
                                str.append("or ")
                                    .append(s.minfo.mod == null ? s.emoji() : "")
                                    .append("[stat]")
                                    .append(s.localizedName);
                            }

                            sep(bt, str.toString());
                            if(stype.nanomachines){
                                bt.row();
                                bt.image(atlas.find("prog-mats-nanomachines")).padTop(8f).scaling(Scaling.fit);
                            }
                        }

                        if(type instanceof SentryBulletType stype){
                            bt.row();
                            bt.table(ut -> {
                                ut.add("@bullet.pm-sentry-spawn");
                                ut.image(icon(stype.unit)).size(3 * 8);
                                ut.add("[lightgray]" + stype.unit.localizedName).padLeft(6);
                                infoButton(ut, stype.unit, 4 * 8).padLeft(6);
                            });
                        }

                        if(type.homingPower > 0.01f){
                            sep(bt, "@bullet.homing");
                        }

                        if(type instanceof CritBulletType stype && stype.bouncing){
                            sep(bt, "@bullet.pm-bouncing");
                        }

                        if(type.lightning > 0){
                            sep(bt, bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                        }

                        if(type instanceof StrikeBulletType stype && stype.splitBullet != null){
                            sep(bt, bundle.format("bullet.pm-splits", stype.splitBullets));
                            bt.row();

                            ammo(ObjectMap.of(t, stype.splitBullet), indent + 1).display(bt);
                        }

                        if(type.fragBullet != null){
                            sep(bt, bundle.format("bullet.frags", type.fragBullets));
                            bt.row();

                            ammo(ObjectMap.of(t, type.fragBullet), indent + 1).display(bt);
                        }
                    }).padTop(compact ? 0 : -9).padLeft(indent * 8).left().fillY().get().background(compact ? null : Tex.underline);
                }

                table.row();
            }
        };
    }

    public static StatValue fuelEfficiency(Floor floor, float multiplier){
        return table -> table.stack(
            new Image(floor.uiIcon).setScaling(Scaling.fit),
            new Table(t -> t.top().right().add((multiplier < 0 ? "[accent]" : "[scarlet]+") + PMUtls.stringsFixed(multiplier * 100)).style(Styles.outlineLabel))
        );
    }

    public static StatValue fuel(FuelCrafter crafter){
        return table -> {
            table.table(t -> {
                t.table(ct -> {
                    for(ItemStack stack : crafter.consumes.getItem().items){
                        ct.add(new ItemDisplay(stack.item, stack.amount, crafter.craftTime, true)).padRight(5);
                    }
                }).left().get().background(Tex.underline);

                t.row();

                t.table(tt -> {
                    tt.add("@pm-fuel").top();
                    tt.table(ft -> {
                        ft.image(icon(crafter.fuelItem)).size(3 * 8).padRight(4).right().top();
                        ft.add(crafter.fuelItem.localizedName).padRight(10).left().top();

                        ft.table(st -> {
                            st.clearChildren();
                            st.left().defaults().padRight(3).left();

                            st.add(bundle.format("pm-fuel.input", crafter.fuelPerItem));

                            sep(st, bundle.format("pm-fuel.use", crafter.fuelPerCraft));

                            sep(st, bundle.format("pm-fuel.capacity", crafter.fuelCapacity));

                            if(crafter.attribute != null){
                                st.row();
                                st.table(at -> {
                                    Runnable[] rebuild = {null};
                                    Map[] lastMap = {null};

                                    rebuild[0] = () -> {
                                        at.clearChildren();
                                        at.left();

                                        at.add("@pm-fuel.affinity");

                                        if(state.isGame()){
                                            var blocks = Vars.content.blocks()
                                                .select(block -> block instanceof Floor f && indexer.isBlockPresent(block) && f.attributes.get(crafter.attribute) != 0 && !(f.isLiquid && !crafter.floating))
                                                .<Floor>as().with(s -> s.sort(f -> f.attributes.get(crafter.attribute)));

                                            if(blocks.any()){
                                                int i = 0;
                                                for(var block: blocks){
                                                    fuelEfficiency(block, block.attributes.get(crafter.attribute) * crafter.fuelUseReduction / -100f).display(at);
                                                    if(++i % 5 == 0){
                                                        at.row();
                                                    }
                                                }
                                            }else{
                                                at.add("@none.inmap");
                                            }
                                        }else{
                                            at.add("@stat.showinmap");
                                        }
                                    };

                                    rebuild[0].run();

                                    //rebuild when map changes.
                                    at.update(() -> {
                                        Map current = state.isGame() ? state.map : null;

                                        if(current != lastMap[0]){
                                            rebuild[0].run();
                                            lastMap[0] = current;
                                        }
                                    });
                                });
                            }
                        }).padTop(-9).left().get().background(Tex.underline);
                    }).left();
                }).left();
            });
        };
    }

    public static StatValue payloadProducts(Seq<Missile> products){
        return table -> {
            table.row();
            products.each(p -> {
                if(p.unlockedNow()){
                    table.image(icon(p)).padRight(4).right().top();
                }else{
                    table.add(PMElements.imageStack(icon(p), Icon.tree.getRegion(), Color.red)).padRight(4).right().top();
                }
                table.add(p.unlockedNow() ? p.localizedName : "@pm-missing-research").padRight(10).left().top();

                if(p.unlockedNow()){
                    table.table(ct -> {
                        ct.left().defaults().padRight(3).left();

                        ct.table(it -> {
                            it.add("[lightgray]" + Stat.input.localized() + ": []");
                            for(ItemStack stack: p.requirements){
                                it.add(PMElements.itemImage(stack.item.uiIcon, () -> stack.amount == 0 ? "" : stack.amount + ""));
                            }
                        });

                        if(p.prev != null){
                            ct.row();
                            ct.table(pt -> {
                                if(p.prev.unlockedNow()){
                                    pt.image(icon(p.prev)).padLeft(60f).padRight(4).right().top();
                                }else{
                                    pt.add(PMElements.imageStack(icon(p.prev), Icon.tree.getRegion(), Color.red)).padLeft(60f).padRight(4).right().top();
                                }
                                pt.add(p.prev.unlockedNow() ? p.prev.localizedName : "@pm-missing-research").padRight(10).left().top();
                            });
                        }
                        if(p.constructTime > 0){
                            ct.row();
                            ct.add("[lightgray]" + Stat.buildTime.localized() + ": []" + PMUtls.stringsFixed(p.constructTime / 60f) + " " + StatUnit.seconds.localized());
                        }
                        if(p.powerUse > 0){
                            ct.row();
                            ct.add("[lightgray]" + Stat.powerUse.localized() + ": []" + PMUtls.stringsFixed(p.powerUse * 60f) + " " + StatUnit.powerSecond.localized());
                        }
                    }).padTop(-9).left().get().background(Tex.underline);
                }

                table.row();
            });
        };
    }

    public static StatValue signalFlareHealth(float health, float attraction, float duration){
        return table -> {
            table.table(ht -> {
                ht.left().defaults().padRight(3).left();

                ht.add(bundle.format("bullet.pm-flare-health", health));
                ht.row();
                ht.add(bundle.format("bullet.pm-flare-attraction", attraction));
                ht.row();
                ht.add(bundle.format("bullet.pm-flare-lifetime", (int)(duration / 60f)));
            }).padTop(-9f).left().get().background(Tex.underline);
        };
    }

    public static StatValue staticDamage(float damage, float reload, StatusEffect status){
        return table -> {
            table.table(t -> {
                t.left().defaults().padRight(3).left();

                t.add(bundle.format("bullet.damage", damage * 60f / reload) + StatUnit.perSecond.localized());
                t.row();

                if(status != StatusEffects.none){
                    t.add((status.minfo.mod == null ? status.emoji() : "") + "[stat]" + status.localizedName);
                    t.row();
                }
            }).padTop(-9).left().get().background(Tex.underline);
        };
    }

    public static StatValue swordDamage(float damage, float damageRadius, float buildingDamageMultiplier, float speed, StatusEffect status){
        return table -> {
            table.table(t -> {
                t.left().defaults().padRight(3).left();

                t.add(bundle.format("bullet.splashdamage", damage, Strings.fixed(damageRadius / tilesize, 1)));
                t.row();

                if(buildingDamageMultiplier != 1f){
                    t.add(bundle.format("bullet.buildingdamage", PMUtls.stringsFixed(buildingDamageMultiplier * 100f)));
                    t.row();
                }

                if(status != StatusEffects.none){
                    t.add((status.minfo.mod == null ? status.emoji() : "") + "[stat]" + status.localizedName);
                    t.row();
                }

                t.add(bundle.format("bullet.pm-sword-speed", speed));
            }).padTop(-9).left().get().background(Tex.underline);
        };
    }

    public static StatValue teslaZapping(float damage, float maxTargets, StatusEffect status){
        return table -> {
            table.row();
            table.table(t -> {
                t.left().defaults().padRight(3).left();

                t.add(bundle.format("bullet.lightning", maxTargets, damage));
                t.row();

                if(status != StatusEffects.none){
                    t.add((status.minfo.mod == null ? status.emoji() : "") + "[stat]" + status.localizedName);
                }
            }).padTop(-9).left().get().background(Tex.underline);
        };
    }

    public static StatValue dronePower(float buildUse, float chargeUse){
        return table -> {
            table.row();
            table.table(t -> {
                t.add("@pm-drone-use-construct").growX().left().padRight(10);
                t.add((buildUse * 60f) + " " + StatUnit.powerSecond.localized()).growX().left();
                t.row();
                t.add("@pm-drone-use-recharge").growX().left().padRight(10);
                t.add((chargeUse * 60f) + " " + StatUnit.powerSecond.localized()).growX().left();
            }).padTop(-9).left().get().background(Tex.underline);
        };
    }

    public static StatValue unitOutput(UnitType u){
        return table -> {
            table.table(t -> {
                t.image(icon(u)).size(3 * 8);
                t.add(u.localizedName).padLeft(8);
                infoButton(t, u, 5 * 8).padLeft(6);
            });
        };
    }

    private static Cell<TextButton> infoButton(Table table, UnlockableContent content, float size){
        return table.button("?", Styles.clearPartialt, () -> {
            ui.content.show(content);
        }).size(size).left().grow().name("contentinfo");
    }

    private static void sep(Table table, String text){
        table.row();
        table.add(text);
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.fullIcon;
    }
}
