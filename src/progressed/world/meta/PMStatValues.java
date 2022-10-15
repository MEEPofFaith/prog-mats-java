package progressed.world.meta;

import arc.*;
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
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.bullet.physical.InjectorBulletType.*;
import progressed.entities.bullet.unit.*;
import progressed.type.unit.*;
import progressed.ui.*;
import progressed.util.*;
import progressed.world.blocks.crafting.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;
import progressed.world.blocks.payloads.*;
import progressed.world.module.ModuleModule.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMStatValues{
    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map){
        return ammo(map, 0, false);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean showUnit){
        return ammo(map, 0, showUnit);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent, boolean showUnit){
        return table -> {
            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t : orderedKeys){
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;
                boolean payload = t instanceof Block || (t instanceof UnitType && !showUnit);

                if(payload && t instanceof Missile m && !m.displayCampaign && state.isCampaign()) continue;

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
                    table.table(n -> {
                        n.add(payload && !t.unlockedNow() ? "@pm-missing-research" : t.localizedName);
                        if(payload && t.unlockedNow()){
                            n.row();
                            infoButton(n, t, 4f * 8f);
                        }
                    }).padRight(10).left().top();
                }

                if(!payload || t.unlockedNow()){
                    table.table(bt -> {
                        bt.left().defaults().padRight(3).left();

                        if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                            if(type instanceof BlackHoleBulletType stype){
                                bt.add(bundle.format("bullet.pm-continuous-splash-damage", stype.continuousDamage(), stype.damageRadius / tilesize));
                                sep(bt, bundle.format("bullet.pm-suction-radius", stype.suctionRadius / tilesize));
                            }else if(type instanceof PillarFieldBulletType stype){
                                bt.add(bundle.format("bullet.pm-multi-splash", stype.amount, stype.pillar.damage, stype.pillar.radius / tilesize));
                            }else if(type instanceof MagmaBulletType stype){
                                bt.add(bundle.format("bullet.pm-continuous-splash-damage", stype.continuousDamage(), stype.radius / tilesize));
                            }else if(type.continuousDamage() > 0){
                                bt.add(bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                            }else{
                                if(Float.isInfinite(type.damage)){
                                    bt.add(PMElements.infiniteDamage());
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

                        if(type.rangeChange != 0 && !compact){
                            sep(bt, Core.bundle.format("bullet.range", (type.rangeChange > 0 ? "+" : "-") + Strings.autoFixed(type.rangeChange / tilesize, 1)));
                        }

                        if(type.displayAmmoMultiplier && !compact && !Mathf.equal(type.ammoMultiplier, 1f) && !(type instanceof LiquidBulletType) && !(t instanceof PowerTurret)){
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
                            sep(bt, (type.status.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" + ((int)(type.statusDuration / 60f)) + "[lightgray] " + Core.bundle.get("unit.seconds")));
                        }

                        if(type instanceof PillarFieldBulletType stype && stype.pillar.status != StatusEffects.none){
                            sep(bt, (stype.pillar.status.minfo.mod == null ? stype.pillar.status.emoji() : "") + "[stat]" + stype.pillar.status.localizedName);
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

                        if(type instanceof SentryBulletType){
                            bt.row();
                            bt.table(ut -> {
                                ut.add("@bullet.pm-sentry-spawn");
                                ut.image(icon(type.despawnUnit)).size(3 * 8);
                                ut.add("[lightgray]" + type.despawnUnit.localizedName).padLeft(6);
                                infoButton(ut, type.despawnUnit, 4 * 8).padLeft(6);
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

                        if(type instanceof TargetBulletType stype){
                            sep(bt, "@bullet.pm-target");
                            if(stype.tDamage > 0){
                                sep(bt, bundle.format("bullet.damage", stype.tDamage)).padLeft(8f);
                            }
                            if(stype.tStatus != StatusEffects.none){
                                sep(bt, (stype.tStatus.minfo.mod == null ? stype.tStatus.emoji() : "") + "[stat]" + stype.tStatus.localizedName).padLeft(8f);
                            }
                        }

                        if(type instanceof RocketBulletType stype && stype.bombBullet != null){
                            float amount = type.speed * stype.bombInterval / tilesize;
                            if(amount == 1){
                                sep(bt, "@bullet.pm-bombs-single");
                            }else{
                                sep(bt, bundle.format("bullet.pm-bombs", amount));
                            }
                            bt.row();

                            ammo(ObjectMap.of(t, stype.bombBullet), indent + 1, false).display(bt);
                        }

                        if(type.fragBullet != null){
                            sep(bt, bundle.format("bullet.frags", type.fragBullets));
                            bt.row();

                            ammo(ObjectMap.of(t, type.fragBullet), indent + 1, false).display(bt);
                        }
                    }).padTop(compact ? 0 : -9).padLeft(indent * 8).left().fillY().get().background(compact ? null : Tex.underline);
                }

                table.row();
            }
        };
    }

    public static StatValue statusEffect(StatusEffect effect){
        return table -> table.add((effect.minfo.mod == null ? effect.emoji() : "") + "[white]" + effect.localizedName);
    }

    public static StatValue fuelEfficiency(Floor floor, float multiplier){
        return table -> table.stack(
            new Image(floor.uiIcon).setScaling(Scaling.fit),
            new Table(t -> t.top().right().add((multiplier < 0 ? "[accent]" : "[scarlet]+") + PMUtls.stringsFixed(multiplier * 100)).style(Styles.outlineLabel))
        );
    }

    public static StatValue fuel(FuelCrafter crafter){
        return table -> table.table(t -> {
            t.table(ct -> {
                for(ItemStack stack : ((ConsumeItems)(crafter.findConsumer(c -> c instanceof ConsumeItems))).items){
                    ct.add(new ItemDisplay(stack.item, stack.amount, crafter.craftTime, true)).padRight(5);
                }
            }).left().get().background(Tex.underline);

            t.row();

            t.table(tt -> {
                tt.add("@stat.pm-fuel").top();
                tt.table(ft -> {
                    ft.image(icon(crafter.fuelItem)).size(3 * 8).padRight(4).right().top();
                    ft.add(crafter.fuelItem.localizedName).padRight(10).left().top();

                    ft.table(st -> {
                        st.clearChildren();
                        st.left().defaults().padRight(3).left();

                        st.add(bundle.format("stat.pm-fuel.input", crafter.fuelPerItem));

                        sep(st, bundle.format("stat.pm-fuel.use", crafter.fuelPerCraft));

                        sep(st, bundle.format("stat.pm-fuel.capacity", crafter.fuelCapacity));

                        if(crafter.attribute != null){
                            st.row();
                            st.table(at -> {
                                Runnable[] rebuild = {null};
                                Map[] lastMap = {null};

                                rebuild[0] = () -> {
                                    at.clearChildren();
                                    at.left();

                                    at.add("@stat.pm-fuel.affinity");

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
    }

    public static StatValue payloadProducts(Seq<Recipe> products){
        return table -> {
            table.row();
            products.each(r -> {
                Block b = r.outputBlock;
                boolean unlocked = b.unlockedNow() || !r.requiresUnlock;
                if(unlocked){
                    table.image(icon(b)).padRight(4).right().top();
                }else{
                    Image out = new Image(icon(b));
                    Image res = new Image(Icon.tree.getRegion());
                    res.setColor(Color.red);
                    res.setSize(out.getWidth() / 2f);
                    ShiftedStack s = new ShiftedStack(out, res);
                    s.setStackPos(out.getWidth() - res.getWidth(), -out.getHeight() + res.getHeight());
                    table.add(s).padRight(4).right().top();
                }
                table.table(n -> {
                    n.add(unlocked ? b.localizedName : "@pm-missing-research");
                    if(unlocked){
                        n.row();
                        infoButton(n, b, 4f * 8f);
                    }
                }).padRight(10).left().top();

                if(unlocked){
                    table.table(ct -> {
                        ct.left().defaults().padRight(3).left();

                        if(r.buildCost != null || r.liquidCost != null){
                            ct.table(it -> {
                                it.add("[lightgray]" + Stat.input.localized() + ": []");
                                if(r.buildCost != null)
                                    for(ItemStack stack: r.buildCost){
                                        it.add(new ItemDisplay(stack.item, stack.amount, r.craftTime, false));
                                    }
                                if(r.liquidCost != null){
                                    //Copy over from ItemDisplay and LiquidDisplay
                                    it.add(new Stack(){{
                                        add(new Image(r.liquidCost.liquid.uiIcon));

                                        if(r.liquidCost.amount != 0){
                                            Table t = new Table().left().bottom();
                                            t.add(Strings.autoFixed(r.liquidCost.amount, 2)).style(Styles.outlineLabel);
                                            add(t);
                                        }
                                    }}).size(iconMed).padRight(3  + (r.liquidCost.amount != 0 && Strings.autoFixed(r.liquidCost.amount, 2).length() > 2 ? 8 : 0));
                                    it.add(Strings.autoFixed(r.liquidCost.amount / (r.craftTime / 60f), 2) + StatUnit.perSecond.localized()).padLeft(2).padRight(5).color(Color.lightGray).style(Styles.outlineLabel);
                                }
                            });
                        }

                        if(r.inputBlock != null){
                            ct.row();
                            ct.table(pt -> {
                                if(r.inputBlock.unlockedNow()){
                                    pt.image(icon(r.inputBlock)).padLeft(60f).padRight(4).right().top();
                                }else{
                                    pt.add(PMElements.imageStack(icon(r.inputBlock), Icon.tree.getRegion(), Color.red)).padLeft(60f).padRight(4).right().top();
                                }
                                pt.table(n -> {
                                    n.add(r.inputBlock.unlockedNow() ? r.inputBlock.localizedName : "@pm-missing-research");
                                    if(r.inputBlock.unlockedNow()){
                                        n.row();
                                        infoButton(n, r.inputBlock, 4f * 8f);
                                    }
                                }).padRight(10).left().top();
                            });
                        }
                        if(r.craftTime > 0){
                            ct.row();
                            ct.add("[lightgray]" + Stat.buildTime.localized() + ": []" + PMUtls.stringsFixed(r.craftTime / 60f) + " " + StatUnit.seconds.localized());
                        }
                        if(r.powerUse > 0){
                            ct.row();
                            ct.add("[lightgray]" + Stat.powerUse.localized() + ": []" + PMUtls.stringsFixed(r.powerUse * 60f) + " " + StatUnit.powerSecond.localized());
                        }
                    }).padTop(-9).left().get().background(Tex.underline);
                }

                table.row();
            });
        };
    }

    public static StatValue signalFlareHealth(float health, float attraction, float duration){
        return table -> table.table(ht -> {
            ht.left().defaults().padRight(3).left();

            ht.add(bundle.format("bullet.pm-flare-health", health));
            ht.row();
            ht.add(bundle.format("bullet.pm-flare-attraction", attraction));
            ht.row();
            ht.add(bundle.format("bullet.pm-flare-lifetime", (int)(duration / 60f)));
        }).padTop(-9f).left().get().background(Tex.underline);
    }

    public static StatValue staticDamage(float damage, float reload, StatusEffect status){
        return table -> table.table(t -> {
            t.left().defaults().padRight(3).left();

            t.add(bundle.format("bullet.damage", damage * 60f / reload) + StatUnit.perSecond.localized());
            t.row();

            if(status != StatusEffects.none){
                t.add((status.minfo.mod == null ? status.emoji() : "") + "[stat]" + status.localizedName);
                t.row();
            }
        }).padTop(-9).left().get().background(Tex.underline);
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

    public static StatValue moduleCounts(ModuleGroup[] groups){
        return table -> {
            int[] mounts = {0, 0, 0};
            for(ModuleGroup group : groups){
                mounts[group.size.ordinal()] += group.amount();
            }

            table.row();
            table.table(t -> {
                for(int i = 0; i < 3; i++){
                    if(mounts[i] > 0){
                        t.add(ModuleSize.values()[i].amount(mounts[i])).left();
                        t.row();
                    }
                }
            }).padLeft(24f);
        };
    }

    public static Cell<TextButton> infoButton(Table table, UnlockableContent content, float size){
        return table.button("?", Styles.flatBordert, () -> ui.content.show(content)).size(size).left().name("contentinfo");
    }

    private static Cell<Label> sep(Table table, String text){
        table.row();
        return table.add(text);
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.fullIcon;
    }
}
