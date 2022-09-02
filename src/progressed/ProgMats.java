package progressed;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import progressed.content.*;
import progressed.content.blocks.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.ui.*;
import progressed.ui.dialogs.*;
import progressed.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ProgMats extends Mod{
    public static ModuleSwapDialog swapDialog;
    public static Seq<BulletData> allBullets = new Seq<>();
    public static int sandboxBlockHealth = 1000000;
    boolean hasProc;

    public ProgMats(){
        super();
        Events.on(ClientLoadEvent.class, e -> {
            loadSettings();
            PMPal.init();
        });

        // Load all assets once they're added into Vars.tree
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            if(!headless) PMShaders.init();
            PMSounds.load();
        }));

        if(!headless){
            Events.on(ContentInitEvent.class, e -> {
                float clip = PMModules.maxClip;
            });
        }
    }

    @Override
    public void init(){
        if(!headless){
            if(OS.username.equals("MEEP")){
                experimental = true;
            }

            LoadedMod progM = mods.locateMod("prog-mats");
            Func<String, String> getModBundle = value -> bundle.get("mod." + value);

            progM.meta.displayName = "[#FCC21B]" + progM.meta.displayName + "[]";
            progM.meta.description = getModBundle.get(progM.meta.name + ".description");

            StringBuilder contributors = new StringBuilder(getModBundle.get(progM.meta.name + ".author"));
            contributors.append("\n\n").append("[#FCC21B]Contributors:[]");
            int i = 0;
            while(bundle.has("mod." + progM.meta.name + "-contributor." + i)){
                contributors.append("\n        ").append(getModBundle.get(progM.meta.name + "-contributor." + i));
                i++;
            }
            progM.meta.author = contributors.toString();

            Events.on(ClientLoadEvent.class, e -> {
                if(everything()){
                    godHood(PMUnitTypes.everythingUnit);
                    setupEveryBullets((Turret)PMBlocks.everythingGun);
                }
                PMStyles.load();
                swapDialog = new ModuleSwapDialog();

                if(farting()){
                    content.blocks().each(b -> b.destroySound = Sounds.wind3);

                    content.units().each(u -> u.deathSound = Sounds.wind3);

                    Events.run(Trigger.newGame, () -> {
                        if(settings.getBool("skipcoreanimation")) return;
                        Time.run(coreLandDuration, () -> {
                            CoreBuild core = player.bestCore();
                            if(core != null){
                                OtherFx.fard.at(core);
                                PMSounds.gigaFard.at(core.x, core.y, 1f, 10f);
                            }
                        });
                    });
                }
            });


            if(!TUEnabled()){ //TU already does this, don't double up
                renderer.minZoom = Math.min(renderer.minZoom, 0.667f); //Zoom out farther
                renderer.maxZoom = Math.max(renderer.maxZoom, 24f); //Get a closer look at yourself

                Events.on(WorldLoadEvent.class, e -> {
                    //reset
                    hasProc = Groups.build.contains(b -> b.block.privileged);
                    renderer.minZoom = 0.667f;
                    renderer.maxZoom = 24f;
                });

                Events.run(Trigger.update, () -> {
                    if(state.isGame()){ //Zoom range
                        if(hasProc){
                            if(control.input.logicCutscene){ //Dynamically change zoom range to not break cutscene zoom
                                renderer.minZoom = 1.5f;
                                renderer.maxZoom = 6f;
                            }else{
                                renderer.minZoom = 0.667f;
                                renderer.maxZoom = 24f;
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void loadContent(){
        PMStatusEffects.load();
        PMLiquids.load();
        PMUnitTypes.load();
        PMItems.load();
        PMBullets.load();
        PMPlanets.load();
        PMBlocks.load();
        PMTechTree.load();
    }

    void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.pm-title"), "prog-mats-settings-icon", t -> {
            t.sliderPref("pm-sword-opacity", 100, 20, 100, 5, s -> s + "%");
            t.sliderPref("pm-zone-opacity", 100, 0, 100, 5, s -> s + "%");
            t.sliderPref("pm-strobespeed", 3, 1, 20, 1, s -> PMUtls.stringsFixed(s / 2f));
            t.checkPref("pm-tesla-range", true);
            t.checkPref("pm-sandbox-everything", false);
            t.checkPref("pm-farting", false, b -> Sounds.wind3.play(Interp.pow2In.apply(Core.settings.getInt("sfxvol") / 100f) * 5f));
        });
    }

    public static boolean farting(){
        return settings.getBool("pm-farting", false);
    }

    public static boolean everything(){
        return settings.getBool("pm-sandbox-everything", false);
    }

    static boolean TUEnabled(){
        LoadedMod testUtils = mods.getMod("test-utils");
        return testUtils != null && testUtils.isSupported() && testUtils.enabled();
    }

    public static void godHood(UnitType ascending){
        try{
            ascending.hitSize = 0;
            content.units().each(u -> {
                if(u != ascending){
                    u.weapons.each(w -> {
                        if(!w.bullet.killShooter){
                            Weapon copy = w.copy();
                            ascending.weapons.add(copy);
                            if(w.otherSide != -1){
                                int diff = u.weapons.get(w.otherSide).otherSide - w.otherSide;
                                copy.otherSide = ascending.weapons.indexOf(copy) + diff;
                            }

                            copy.rotateSpeed = 360f;
                            copy.shootCone = 360f;

                            if(copy.shootStatus == StatusEffects.unmoving || copy.shootStatus == StatusEffects.slow){
                                copy.shootStatus = StatusEffects.none;
                            }
                        }
                    });

                    u.abilities.each(a -> ascending.abilities.add(a));
                }
            });
        }catch(Throwable ignored){}
    }

    public static void setupEveryBullets(Turret base){
        content.units().each(u -> u.weapons.each(w -> {
            if(w.bullet != null && !w.bullet.killShooter){
                BulletType bul = w.bullet;
                BulletData data = new BulletData(bul, w.shootSound, bul.shootEffect, bul.smokeEffect, w.shake, bul.lifetime);
                if(!allBullets.contains(data)){
                    allBullets.add(data);
                }
            }
        }));
        content.blocks().each(b -> {
            if(b != base && b instanceof Turret){
                if(b instanceof LaserTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : block.shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : block.smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime + block.shootDuration, true);
                    if(!allBullets.contains(data)){
                        allBullets.add(data);
                    }
                }else if(b instanceof PowerTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : base.shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : base.smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                    if(!allBullets.contains(data)){
                        allBullets.add(data);
                    }
                }else if(b instanceof ItemTurret block){
                    content.items().each(i -> {
                        if(block.ammoTypes.get(i) != null){
                            BulletType bul = block.ammoTypes.get(i);
                            Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : base.shootEffect;
                            Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : base.smokeEffect;
                            BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                            if(!allBullets.contains(data)){
                                allBullets.add(data);
                            }
                        }
                    });
                }
            }
        });

        allBullets.sort(b -> PMUtls.bulletDamage(b.bulletType, b.lifetime));
    }

    public static class BulletData{
        public BulletType bulletType;
        public Sound shootSound;
        public Effect shootEffect, smokeEffect;
        public float shake, lifetime;
        public boolean continuousBlock;

        public BulletData(BulletType bulletType, Sound shootSound, Effect shakeEffect, Effect smokeEffect, float shake, float lifetime, boolean continuous){
            this.bulletType = bulletType;
            this.shootSound = shootSound;
            this.shootEffect = shakeEffect;
            this.smokeEffect = smokeEffect;
            this.shake = shake;
            this.lifetime = lifetime;
            this.continuousBlock = continuous;
        }

        public BulletData(BulletType bulletType, Sound shootSound, Effect shootEffect, Effect smokeEffect, float shake, float lifetime){
            this(bulletType, shootSound, shootEffect, smokeEffect, shake, lifetime, false);
        }
    }
}
