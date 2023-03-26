package progressed.world.meta;

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
import mindustry.graphics.*;
import mindustry.maps.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
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

            for(T t: orderedKeys){
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;
                boolean payload = t instanceof Block || (t instanceof UnitType && !showUnit);

                if(payload && t instanceof Missile m && !m.displayCampaign && state.isCampaign()) continue;

                BulletType type = map.get(t);

                if(type.spawnUnit != null && type.spawnUnit.weapons.size > 0){
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false).display(table);
                    continue;
                }

                table.table(compact ? null : Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();

                    //no point in displaying unit icon twice
                    if(!compact && !(t instanceof PowerTurret)){
                        bt.table(title -> {
                            if(payload){
                                if(t.unlockedNow()){
                                    title.image(icon(t)).size(96f).padRight(4).right().top();
                                    title.table(n -> {
                                        n.add(t.localizedName);
                                        n.row();
                                        infoButton(n, t, 4f * 8f).padTop(4f);
                                    }).padRight(10).left().top();
                                }else{
                                    title.image(Icon.lock).color(Pal.darkerGray).size(40).padRight(4).right().top();
                                    title.add("@pm-missing-research").left().colspan(2);
                                }
                            }else{
                                title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top();
                                title.add(t.localizedName).padRight(10).left().top();
                            }
                        });
                        if(payload && !t.unlockedNow()) return;
                        bt.row();
                    }

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

                    if(type instanceof SignalFlareBulletType stype && stype.spawn instanceof SignalFlareUnitType u){
                        sep(bt, bundle.format("bullet.pm-flare-health", u.health));
                        sep(bt, bundle.format("bullet.pm-flare-attraction", u.attraction));
                        sep(bt, bundle.format("bullet.pm-flare-lifetime", (int)(u.lifetime / 60f)));
                    }

                    if(type.buildingDamageMultiplier != 1){
                        int val = (int)(type.buildingDamageMultiplier * 100 - 100);
                        sep(bt, bundle.format("bullet.buildingdamage", ammoStat(val)));
                    }

                    if(type.rangeChange != 0 && !compact){
                        sep(bt, bundle.format("bullet.range", ammoStat(type.rangeChange / tilesize)));
                    }

                    if(type.splashDamage > 0){
                        sep(bt, bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                    }

                    if(!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)){
                        sep(bt, bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
                    }

                    if(!compact && !Mathf.equal(type.reloadMultiplier, 1f)){
                        int val = (int)(type.reloadMultiplier * 100 - 100);
                        sep(bt, bundle.format("bullet.reload", ammoStat(val)));
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

                    if(type.homingPower > 0.01f){
                        sep(bt, "@bullet.homing");
                    }

                    if(type.lightning > 0){
                        sep(bt, bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                    }

                    if(type.pierceArmor){
                        sep(bt, "@bullet.armorpierce");
                    }

                    if(type.suppressionRange > 0){
                        sep(bt, bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
                    }

                    if(type.status != StatusEffects.none){
                        sep(bt, (type.status.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" + ((int)(type.statusDuration / 60f)) + "[lightgray] " + bundle.get("unit.seconds")));
                    }

                    if(type instanceof PillarFieldBulletType stype && stype.pillar.status != StatusEffects.none){
                        sep(bt, (stype.pillar.status.minfo.mod == null ? stype.pillar.status.emoji() : "") + "[stat]" + stype.pillar.status.localizedName);
                    }

                    if(type instanceof InjectorBulletType stype){ //This could probably be optimized, but whatever
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

                    if(type instanceof CritBulletType stype && stype.bouncing){
                        sep(bt, "@bullet.pm-bouncing");
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

                    if(type instanceof SparkingBulletType stype){
                        sep(bt, bundle.format("bullet.pm-sparking", Strings.autoFixed(60f / stype.empInterval, 2)));
                    }

                    if(type.intervalBullet != null){
                        bt.row();

                        Table ic = new Table();
                        ammo(ObjectMap.of(t, type.intervalBullet), indent + 1, false).display(ic);
                        Collapser coll = new Collapser(ic, true);
                        coll.setDuration(0.1f);

                        bt.table(it -> {
                            it.left().defaults().left();

                            it.add(bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                            it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.fragBullet != null){
                        bt.row();

                        Table fc = new Table();
                        ammo(ObjectMap.of(t, type.fragBullet), indent + 1, false).display(fc);
                        Collapser coll = new Collapser(fc, true);
                        coll.setDuration(0.1f);

                        bt.table(ft -> {
                            ft.left().defaults().left();

                            ft.add(bundle.format("bullet.frags", type.fragBullets));
                            ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }
                }).padLeft(indent * 5).padTop(5).growX().margin(compact ? 0 : 10);

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
            t.image(icon(crafter.fuelItem)).size(3 * 8).padRight(4).right().top();
            t.add(crafter.fuelItem.localizedName).padRight(10).left().top();

            t.table(ft -> {
                ft.clearChildren();
                ft.left().defaults().padRight(3).left();

                ft.add(bundle.format("stat.pm-fuel.input", crafter.fuelPerItem));

                sep(ft, bundle.format("stat.pm-fuel.use", crafter.fuelPerCraft));

                sep(ft, bundle.format("stat.pm-fuel.capacity", crafter.fuelCapacity));

                if(crafter.attribute != null){
                    ft.row();
                    ft.table(at -> {
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
            }).left().get().background(Tex.underline);
        });
    }

    public static StatValue payloadProducts(Seq<Recipe> products){
        return table -> {
            table.row();

            for(Recipe recipe: products){
                table.table(Styles.grayPanel, t -> {
                    Block out = recipe.outputBlock;

                    if(state.rules.bannedBlocks.contains(out)){
                        t.image(Icon.cancel).color(Pal.remove).size(40);
                        return;
                    }

                    if(recipe.unlocked()){
                        if(recipe.hasInputBlock()){
                            t.table(i -> {
                                i.left();

                                i.image(recipe.inputBlock.fullIcon).size(40).left().scaling(Scaling.fit);
                                i.add(recipe.inputBlock.localizedName).padLeft(8f).left();
                                infoButton(i, recipe.inputBlock, 32).padLeft(8f).left();

                                i.image(Icon.right).color(Pal.darkishGray).size(40).pad(8f).center();

                                i.image(out.fullIcon).size(40).right().scaling(Scaling.fit);
                                i.add(out.localizedName).padLeft(8f).right();
                                infoButton(i, out, 32).padLeft(8f).right();
                            }).left().padTop(5).padBottom(5);
                            t.row();
                            t.add(Strings.autoFixed(recipe.craftTime / 60f, 1) + " " + StatUnit.seconds.localized()).color(Color.lightGray).padLeft(10f).left();
                            if(recipe.powerUse > 0){
                                t.row();
                                t.add(Strings.autoFixed(recipe.powerUse * 60f, 1) + " " + StatUnit.powerSecond.localized()).color(Color.lightGray).padLeft(10f).left();
                            }
                            t.row();
                        }else{
                            t.image(out.uiIcon).size(40).pad(10f).left().top();
                            t.table(info -> {
                                info.top().defaults().left();

                                info.add(out.localizedName);
                                infoButton(info, out, 32).padLeft(8f).expandX();

                                info.row();
                                info.add(Strings.autoFixed(recipe.craftTime / 60f, 1) + " " + StatUnit.seconds.localized()).color(Color.lightGray).colspan(2);
                                if(recipe.powerUse > 0){
                                    info.row();
                                    info.add(Strings.autoFixed(recipe.powerUse * 60f, 1) + " " + StatUnit.powerSecond.localized()).color(Color.lightGray).colspan(2);
                                }
                            }).top();
                        }

                        if(recipe.showReqList()){
                            t.table(req -> {
                                if(recipe.hasInputBlock()){
                                    req.left().defaults().left();
                                }else{
                                    req.right().defaults().right();
                                }

                                int i = 0;
                                int col = recipe.hasInputBlock() ? 12 : recipe.powerUse > 0 ? 4 : 6;
                                if(recipe.itemRequirements.length > 0){
                                    while(i < recipe.itemRequirements.length){
                                        if(i % col == 0) req.row();

                                        ItemStack stack = recipe.itemRequirements[i];
                                        req.add(new ItemDisplay(stack.item, stack.amount, false)).pad(5);

                                        i++;
                                    }
                                }
                                if(recipe.liquidRequirements != null){
                                    if(i % col == 0) req.row();
                                    req.add(new NamelessLiquidDisplay(recipe.liquidRequirements.liquid, recipe.liquidRequirements.amount, false)).pad(5);
                                }
                            }).right().top().grow().pad(10f);
                        }
                    }else{
                        t.image(Icon.lock).color(Pal.darkerGray).size(40);
                        t.add("@pm-missing-research");
                    }
                }).growX().pad(5);
                table.row();
            }
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
            for(ModuleGroup group: groups){
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

    public static StatValue content(UnlockableContent content){
        return table -> {
            table.row();
            table.table(t -> {
                t.image(icon(content)).size(3 * 8);
                t.add("[lightgray]" + content.localizedName).padLeft(6);
                infoButton(t, content, 4 * 8).padLeft(6);
            });
        };
    }

    public static Cell<TextButton> infoButton(Table table, UnlockableContent content, float size){
        return table.button("?", Styles.flatBordert, () -> ui.content.show(content)).size(size).left().name("contentinfo");
    }

    private static Cell<Label> sep(Table table, String text){
        table.row();
        return table.add(text);
    }

    private static String ammoStat(float val){
        return (val > 0 ? "[stat]+" : "[negstat]") + Strings.autoFixed(val, 1);
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.fullIcon;
    }
}
