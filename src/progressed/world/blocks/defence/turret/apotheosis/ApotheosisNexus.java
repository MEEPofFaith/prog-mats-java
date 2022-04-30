package progressed.world.blocks.defence.turret.apotheosis;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.audio.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.content.effects.EnergyFx.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.apotheosis.ApotheosisChargeTower.*;

import java.util.*;

import static mindustry.Vars.*;

public class ApotheosisNexus extends ReloadTurret{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public ApotheosisChargeTower chargeTower; //Literally just for copying the laser for the spinner chargers

    public final int timerTarget = timers++, damageTimer = timers++, pulseTimer = timers++, bigPulseTimer = timers++;
    public int targetInterval = 20, damageInterval = 5, pulseInterval = 45, bigPulseInterval = 135;

    public float powerUse = 1f;
    public float speed, duration = 60f;
    public float damage, damageRadius = tilesize;
    public float buildingDamageMultiplier = 1f;
    public float boostFalloff = 0.1f;
    public float chargeTime = 5f * 60f;
    public float arcTime = 30f;
    public float fadeTime = 120f;
    public StatusEffect status;
    public float statusDuration = 6f * 10f;
    public float cooldown = 0.02f;
    public float baseRotateSpeed = 0.2f, rotateSpeed = 20f, spinUpSpeed = 0.01f, spinDownSpeed = 0.006f;
    public float[] ringExpand = {20f, 44f}, baseDst = {0f, 0f}, spinnerWidth = {0f, 0f};

    public int lights = 9;
    public Color lightsBase = Color.valueOf("252835"), lightsDark = PMPal.apotheosisLaserDark, lightsLight = PMPal.apotheosisLaser;
    public float lightInterval = 300f;
    public float laserRadius;
    public float height = 150f * tilesize;
    public Color[] colors = PMPal.apotheosisLaserColors;
    public float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
    public float[] strokes = {2f, 1.7f, 1.2f, 0.6f};
    public float[] lenscales = {0.90f, 0.95f, 0.98f, 1f}, blankscales;
    public float width = -1f, oscScl = 3f, oscMag = 0.2f, spaceMag = 35f;
    public float lightStroke = -1f;
    public float vibration = 1f;
    public float shake, laserShake;
    public float pissChance = 0.01f;

    public Effect
        chargeEffect = EnergyFx.apotheosisCharge,
        fireEffect = Fx.none,
        touchdownEffect = EnergyFx.apotheosisTouchdown,
        damageEffect = new MultiEffect(EnergyFx.apotheosisPuddle, EnergyFx.apotheosisDamage),
        pulseEffect = EnergyFx.apotheosisPulse;
    public float bigPulseScl = 2f;
    public Color[] flashStart = {PMPal.apotheosisLaser, PMPal.pissbeam}, flashEnd = {PMPal.apotheosisLaserDark, PMPal.pissbeamDark};
    public float flashSeconds = 1f;
    public Interp flashInterp = Interp.pow3In;
    public Sound fireSound = Sounds.laserblast;
    public Sound chargeSound = Sounds.techloop, beamSound = PMSounds.pulseBeam;
    public float chargeVolume = 1f, beamVolume = 1f;
    public float fireSoundPitch = 1f, fireSoundVolume = 1f;

    public Sortf unitSort = Unit::dst2;

    protected float sort;
    protected Vec2 tr = new Vec2();
    protected Vec2 tr2 = new Vec2();
    protected Color tc = new Color();

    public TextureRegion spinners;
    public TextureRegion[] lightRegions, spinnerRegionsLight = new TextureRegion[2], spinnerRegionsDark = new TextureRegion[2];

    public ApotheosisNexus(String name){
        super(name);

        hasPower = true;
        canOverdrive = false;
        outlineIcon = false;
        acceptCoolant = true;
        lightColor = PMPal.apotheosisLaser;

        consumes.add(new ConsumeCoolant(0.01f)).update(false);
        coolantMultiplier = 1f;
        liquidCapacity = 20f;
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisNexusBuild::isActive);
        super.init();

        if(width < 0) width = size * tilesize / 3f;
        if(lightStroke < 0) lightStroke = width * 2;
        blankscales = new float[lenscales.length];
        Arrays.fill(blankscales, 1f);
    }

    @Override
    public void load(){
        super.load();

        lightRegions = new TextureRegion[lights];
        for(int i = 0; i < lights; i++){
            lightRegions[i] = Core.atlas.find(name + "-lights" + i);
        }
        spinners = Core.atlas.find(name + "-spinners");
        for(int i = 0; i < 2; i++){
            spinnerRegionsLight[i] = Core.atlas.find(name + "-spinner-light-" + i);
            spinnerRegionsDark[i] = Core.atlas.find(name + "-spinner-dark-" + i);
        }

        clipSize = Math.max(clipSize, (range + height + 4f) * 2f);
    }

    @Override
    public void createIcons(MultiPacker packer){
        Outliner.outlineRegion(packer, spinners, outlineColor, name + "-spinners");
        Outliner.outlineRegions(packer, spinnerRegionsLight, outlineColor, name + "-spinner-light");
        Outliner.outlineRegions(packer, spinnerRegionsDark, outlineColor, name + "-spinner-dark");
        super.createIcons(packer);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.ammo, s -> {
            s.row();
            s.table(st -> {
                st.left().defaults().padRight(3).left();
                st.add(Core.bundle.format("bullet.pm-continuous-splash-damage", damage * 12, Strings.fixed(damageRadius / tilesize, 1)));
                if(buildingDamageMultiplier != 1){
                    st.row();
                    st.add(Core.bundle.format("bullet.buildingdamage", (int)(buildingDamageMultiplier * 100)));
                }
                st.row();
                st.add(Core.bundle.format("bullet.pm-flare-lifetime", Strings.fixed(duration / 60f, 2)));
            }).padTop(-9).left().get().background(Tex.underline);
        });

        stats.remove(Stat.booster);
        stats.add(Stat.input, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes.liquidfilters.get(l.id)));
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            region,
            spinners
        };
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-reload", (ApotheosisNexusBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reload / reloadTime)
        ));
        bars.add("pm-shoot-duration", (ApotheosisNexusBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-shoot-duration", PMUtls.stringsFixed(Mathf.clamp(1f - entity.activeTime / entity.realDuration) * 100f)),
            () -> Tmp.c1.set(PMPal.apotheosisLaser).lerp(PMPal.apotheosisLaserDark, entity.activeTime / entity.realDuration),
            () -> 1f - Mathf.clamp(entity.activeTime / entity.realDuration)
        ));
    }

    public class ApotheosisNexusBuild extends ReloadTurretBuild implements ControlBlock{
        public IntSeq chargers = new IntSeq(), connectedChargers = new IntSeq();
        public float heat, warmup, spinUp, rotation, logicControlTime = -1;
        public float charge, arc, fade, activeTime = 80000f;
        public float falloff, realDamage, realRadius, realSpeed, realDuration;
        public int shotCounter;
        public boolean logicShooting = false;
        public Posc target;
        public Vec2 targetPos = new Vec2(), curPos = new Vec2();
        public boolean wasShooting, damaging, charging, arcing, fading, piss;
        protected PitchedSoundLoop chargeSoundLoop = new PitchedSoundLoop(chargeSound, chargeVolume);
        protected PitchedSoundLoop beamSoundLoop = new PitchedSoundLoop(beamSound, beamVolume);
        public BlockUnitc unit;

        @Override
        public void created(){
            unit = (BlockUnitc)UnitTypes.block.create(team);
            unit.tile(this);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && (unit == null || !unit.isPlayer())){
                targetPos.set(World.unconv((float)p1), World.unconv((float)p2));
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && (unit == null || !unit.isPlayer())){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc){
                    targetPosition((Posc)p1);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor){
            return switch(sensor){
                case shootX -> World.conv(targetPos.x);
                case shootY -> World.conv(targetPos.y);
                case shooting -> isShooting() ? 1 : 0;
                case progress -> Mathf.clamp(reload / reloadTime);
                default -> super.sense(sensor);
            };
        }

        public boolean isShooting(){
            return (isControlled() ? (unit != null && unit.isShooting()) : logicControlled() ? logicShooting : target != null);
        }

        @Override
        public void draw(){
            super.draw();

            for(int i = 0; i < lights; i++){
                float lerp = (0.5f + Mathf.absin(Time.time - i * (lightInterval / lights), lightInterval / 8, 0.5f)) * warmup;
                tc.set(lightsBase).lerp(lightsDark, lerp);
                Draw.color(tc);
                Draw.rect(lightRegions[i], x, y);
                tc.set(lightsBase).lerp(lightsLight, lerp);
                Draw.color(tc);
                Draw.rect(lightRegions[i], x, y, 180f);
            }
            Draw.color();

            if(arc > 0){
                Color[] cols = getColors();
                float fin = arcf();
                float fout = fadef();
                float rScl = radscl();
                float rand = vibration * fin * fout;

                float uFin = Mathf.curve(fin, 0f, 0.5f);
                if(uFin > 0.01f){
                    Draw.z(Layer.effect + 0.002f);
                    for(int s = 0; s < cols.length; s++){
                        float c1 = Tmp.c1.set(cols[s]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)).toFloatBits();
                        float c2 = Tmp.c2.set(Tmp.c1).a(1 - uFin).toFloatBits();
                        float rx = Mathf.range(rand);
                        float ry = Mathf.range(rand);
                        for(int i = 0; i < tscales.length; i++){
                            float w = (width + Mathf.absin(Time.time, oscScl, oscMag)) * fout * strokes[s] * tscales[i] / 2f * fadef();
                            float b = (lenscales[i] - 1f) * spaceMag;
                            float e = height * lenscales[i] * uFin;
                            float w2 = Mathf.lerp(w, w * rScl, uFin / 2f);
                            Fill.quad(
                                x - w + rx, y - b + ry, c1,
                                x + w + rx, y - b + ry, c1,
                                x + w2 + rx, y + e + ry, c2,
                                x - w2 + rx, y + e + ry, c2
                            );
                        }
                    }

                    Drawf.light(team, x, y, x, y + height * uFin, lightStroke * fout, lightColor, 0.7f);
                }

                float dFin = Mathf.curve(fin, 0.5f, 1f);
                if(dFin > 0.01f){
                    Draw.z(Layer.effect + (curPos.y < y ? 0.0021f : 0.0019f));
                    for(int s = 0; s < cols.length; s++){
                        float c1 = Tmp.c1.set(cols[s]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)).toFloatBits();
                        float c2 = Tmp.c2.set(Tmp.c1).a(0).toFloatBits();
                        float rx = Mathf.range(rand);
                        float ry = Mathf.range(rand);
                        for(int i = 0; i < tscales.length; i++){
                            float w = (width + Mathf.absin(Time.time, oscScl, oscMag)) * fout * strokes[s] * tscales[i] / 2f * fadef();
                            float b = (lenscales[i] - 1f) * spaceMag;
                            float e = height * lenscales[i];
                            float top = curPos.y + e;
                            float h = (e + b) * dFin;
                            float w1 = Mathf.lerp(w, w * rScl, 0.5f);
                            float w2 = Mathf.lerp(w, w * rScl, 0.5f + dFin / 2f);
                            Fill.quad(
                                curPos.x - w2 + rx, top - h + ry, c1,
                                curPos.x + w2 + rx, top - h + ry, c1,
                                curPos.x + w1 + rx, top + ry, c2,
                                curPos.x - w1 + rx, top + ry, c2
                            );
                        }
                    }

                    Drawf.light(team, curPos.x, curPos.y + height, curPos.x, curPos.y + height - (height * dFin), lightStroke * fout, lightColor, 0.7f);
                }
            }

            Draw.z(Layer.turret + 1);
            for(int i = 0; i < 2; i++){
                float s = Mathf.signs[i];
                tr.trns(rotation * s, ringExpand[i] * spinUp);
                PMDrawf.spinSprite(spinnerRegionsLight[i], spinnerRegionsDark[i], x + tr.x, y + tr.y, rotation * s);
                tr.rotate(180f);
                PMDrawf.spinSprite(spinnerRegionsLight[i], spinnerRegionsDark[i], x + tr.x, y + tr.y, rotation * s + 180f);
            }

            Draw.z(Layer.effect); //S e n d   h e l p
            for(int i = 0; i < 2; i++){
                float s = Mathf.signs[i];
                ApotheosisChargeTower ct = chargeTower;
                tr.trns(rotation * s, baseDst[i] + ringExpand[i] * spinUp);
                for(int j = 0; j < 2; j++){
                    tr2.trns(rotation * s + 90f * Mathf.signs[j], spinnerWidth[i]);
                    tr2.add(tr).add(this);
                    PMDrawf.laser(team, tr2.x, tr2.y,
                        (dst(tr2) - laserRadius) * Interp.pow3Out.apply(Mathf.clamp(chargef() * 3f)),
                        ct.width, tr2.angleTo(this),
                        (1f + (ct.activeScl - 1f) * Mathf.clamp((chargef() - (1f / 3f)) * 1.5f)) * fadef() * efficiency(),
                        ct.tscales, ct.strokes, ct.lenscales, ct.oscScl, ct.oscMag, ct.spaceMag, ct.colors, ct.laserLightColor
                    );
                }
                tr.rotate(180f);
                for(int j = 0; j < 2; j++){
                    tr2.trns(rotation * s + 180f + 90f * Mathf.signs[j], spinnerWidth[i]);
                    tr2.add(tr).add(this);
                    PMDrawf.laser(team, tr2.x, tr2.y,
                        (dst(tr2) - laserRadius) * Interp.pow3Out.apply(Mathf.clamp(chargef() * 3f)),
                        ct.width, tr2.angleTo(this),
                        (1f + (ct.activeScl - 1f) * Mathf.clamp((chargef() - (1f / 3f)) * 1.5f)) * fadef() * efficiency(),
                        ct.tscales, ct.strokes, ct.lenscales, ct.oscScl, ct.oscMag, ct.spaceMag, ct.colors, ct.laserLightColor
                    );
                }
            }
        }

        public Color[] getColors(){
            return isPiss() ? PMPal.pissbeamColors : colors;
        }

        @Override
        public Unit unit(){
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        public boolean isActive(){
            return (target != null || wasShooting || damaging) && enabled;
        }

        public void targetPosition(Posc pos){ //Entity targets, leads targets
            if(!consValid() || pos == null) return;

            if(!damaging && !arcing && !fading){
                Tmp.v1.set(pos.getX() - x, pos.getY() - y).setLength(Math.min(Tmp.v1.len(), range)).add(x, y);
                curPos.set(Tmp.v1);
            }else{
                Tmp.v1.set(Predict.intercept(curPos, pos, realSpeed));
                Tmp.v2.set(Tmp.v1.getX() - x, Tmp.v1.getY() - y).setLength(Math.min(Tmp.v2.len(), range)).add(x, y);
                Tmp.v3.trns(curPos.angleTo(Tmp.v2), Math.min(realSpeed * edelta(), curPos.dst(Tmp.v2)));
                curPos.add(Tmp.v3);
            }
        }

        public void targetPosition(Vec2 pos){ //Logic/manual aim. Doesn't lead because it can't lead
            if(!consValid() || pos == null) return;

            Tmp.v1.set(pos.getX() - x, pos.getY() - y).setLength(Math.min(Tmp.v1.len(), range)).add(x, y);

            if(!damaging && !arcing && !fading){
                curPos.set(Tmp.v1);
            }else{
                Tmp.v1.trns(curPos.angleTo(Tmp.v1), Math.min(realSpeed * edelta(), curPos.dst(Tmp.v1)));
                curPos.add(Tmp.v1);
            }
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;
            checkConnections();

            wasShooting = false;

            heat = Mathf.lerpDelta(heat, 0f, cooldown);
            warmup = Mathf.lerpDelta(warmup, power.status, 0.01f);
            rotation += Mathf.lerp(baseRotateSpeed, rotateSpeed, spinUp) * delta();
            if(!charging && !arcing && (!damaging || fading)){
                spinUp = Mathf.lerp(spinUp, 0f, spinDownSpeed);
            }else{
                spinUp = Mathf.lerp(spinUp, 1f, spinUpSpeed);
            }
            chargeSoundLoop.update(x, y, charging ? chargef() : 0f, 1f);
            beamSoundLoop.update(x, y, arcing || damaging ? fadef() : 0f, 1f);

            if(unit != null){
                unit.health(health);
                unit.rotation(rotation);
                unit.team(team);
                unit.set(x, y);
            }

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(consValid()){
                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot = true;

                    if(isControlled()){ //player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                        targetPosition(targetPos);
                    }else if(logicControlled()){ //logic behavior
                        canShoot = logicShooting;
                        targetPosition(targetPos);
                    }else{ //default AI behavior
                        targetPos.set(target);
                        targetPosition(target);

                        if(Float.isNaN(rotation)){
                            rotation = 0;
                        }
                    }

                    if(canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }else{
                reload = 0;
            }

            updateFiring();

            if(notFiring() && reload < reloadTime){
                updateCooling();
            }

            connectedChargers.each(i -> {
                ApotheosisChargeTowerBuild charger = getChargerPos(i);
                if(charger != null) charger.scl = fadef();
            });
        }

        protected void updateShooting(){
            if(notFiring()){
                if(reload >= reloadTime){
                    connectChargers();
                    activeTime = 0f;
                    charging = true;
                    fade = fadeTime;
                    piss = Mathf.chance(pissChance);
                    chargeEffect.at(x, y);
                    for(int i = 0; i < connectedChargers.size; i++){
                        int pos = connectedChargers.get(i);
                        Time.run(i * (chargeTime / 2f / connectedChargers.size) + chargeTime / 3f, () -> {
                            ApotheosisChargeTowerBuild charger = getChargerPos(pos);
                            if(isValid() && charger != null){
                                charger.activate();
                            }
                        });
                    }
                }else{
                    activeTime = realDuration;
                }
            }
        }

        @Override
        protected void updateCooling(){
            Liquid liquid = liquids.current();
            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

            float used = (cheating() ? maxUsed : Math.min(liquids.get(liquid), maxUsed)) * Time.delta;
            reload += used * liquid.heatCapacity * coolantMultiplier;
            liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
            }
        }

        protected void updateFiring(){
            if(charging){
                if(consValid()){
                    charge += delta();
                    if(charge >= chargeTime){
                        charge = chargeTime;
                        charging = false;
                        arcing = true;
                        fireSound.at(x, y, fireSoundPitch, fireSoundVolume);
                        if(isPiss()){
                            (ProgMats.farting() && piss ? PMSounds.loudMoonPiss : PMSounds.moonPiss).at(x, y, 1f, 2f);
                            Sounds.wind3.at(x, y, 1f, 5f);
                        }
                        fireEffect.at(x, y);
                    }
                }else{
                    reset();
                }
            }

            if(arcing){
                arc += delta();
                Effect.shake(shake * (arc / arcTime), shake * (arc / arcTime), this);
                if(arc >= arcTime){
                    arc = arcTime;
                    arcing = false;
                    damaging = true;
                    PMDrawf.flash(flashStart[Mathf.num(isPiss())], flashEnd[Mathf.num(isPiss())], flashSeconds, flashInterp);
                    effect(touchdownEffect);
                }
            }

            if(fading){
                fade -= delta();
                if(fade <= 0f){
                    reset();
                    shotCounter++;
                }
            }

            if(damaging){
                activeTime += Time.delta / Math.max(efficiency(), 0.00001f);
                calc();
                Effect.shake(shake * fadef(), shake * fadef(), this);
                Effect.shake(laserShake * fadef() * radscl(), laserShake * fadef() * radscl(), curPos);
                if(timer.get(damageTimer, damageInterval)){
                    PMDamage.allNearbyEnemies(team, curPos.x, curPos.y, realRadius * fadef(), other -> {
                        if(other instanceof Building b){
                            b.damage(team, realDamage * buildingDamageMultiplier * fadef());
                        }else{
                            other.damage(realDamage * fadef());
                        }
                        if(other instanceof Statusc s){
                            s.apply(status, statusDuration);
                        }
                    });
                    effect(damageEffect);
                }

                if(timer.get(pulseTimer, pulseInterval)){
                    effect(pulseEffect);
                }

                if(timer.get(bigPulseTimer, bigPulseInterval)){
                    effect(pulseEffect, bigPulseScl);
                }

                if(activeTime >= realDuration){
                    fading = true;
                }
            }
        }

        public boolean notFiring(){
            return !(charging || arcing || damaging || fading);
        }

        public boolean isPiss(){
            return ProgMats.farting() || piss;
        }

        public float chargef(){
            return charge / chargeTime;
        }

        public float arcf(){
            return arc / arcTime;
        }

        public float fadef(){
            return fade / fadeTime;
        }

        public float radscl(){
            return realRadius / damageRadius;
        }

        public void reset(){
            fade = 0f;
            charging = false;
            arcing = false;
            damaging = false;
            fading = false;
            reload = 0;
            charge = 0f;
            arc = 0f;
            chargeSoundLoop.update(x, y, 0f, 1f);
            beamSoundLoop.update(x, y, 0f, 1f);
            chargers.each(i -> ((ApotheosisChargeTowerBuild)(world.build(i))).fullLaser = false);
        }


        public void effect(Effect eff, float scl){
            eff.at(curPos.x, curPos.y, 0f, new ApotheosisEffectData(
                curPos.y < y ? 0.0029f : 0.0009f,
                radscl() * fadef() * scl,
                isPiss() ? PMPal.pissbeam : PMPal.apotheosisLaser,
                isPiss() ? PMPal.pissbeamDark : PMPal.apotheosisLaserDark
            ));
        }

        public void effect(Effect eff){
            effect(eff, 1f);
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        protected void findTarget(){
            if(arcing || damaging || fading){
                //When firing, use distance in relation to the laser instead of the nexus itself.
                target = PMDamage.bestTarget(team, x, y, curPos.getX(), curPos.getY(), range, e -> !e.dead(), b -> true, unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead(), b -> true, unitSort);
            }
        }

        @Override
        public void display(Table table){
            super.display(table);

            table.row();
            table.table(t -> {
                Runnable rebuild = () -> {
                    t.clearChildren();

                    if(chargers.size > 0){
                        t.left();
                        ApotheosisChargeTowerBuild towerBuild = getCharger(0);
                        if(towerBuild != null){
                            ApotheosisChargeTower tower = (ApotheosisChargeTower)towerBuild.block;
                            t.image(tower.uiIcon).size(32).padBottom(-4).padRight(2);
                            t.label(() -> Core.bundle.format("pm-apotheosis-chargers", chargers.size)).padLeft(6).fillX().labelAlign(Align.left).color(Color.lightGray);
                        }else{
                            t.label(() -> Core.bundle.get("pm-apotheosis-none")).fillX().labelAlign(Align.left).color(Color.lightGray);
                        }
                    }else{
                        t.label(() -> Core.bundle.get("pm-apotheosis-none")).fillX().labelAlign(Align.left).color(Color.lightGray);
                    }
                };

                t.update(rebuild);
            });
        }

        protected void checkConnections(){
            int index = 0;
            while(index < chargers.size){
                if(getCharger(index) == null){
                    chargers.removeIndex(index);
                }else{
                    index++;
                }
            }
        }

        protected ApotheosisChargeTowerBuild getCharger(int index){
            if(index >= chargers.size) return null;
            return world.build(chargers.get(index)) instanceof ApotheosisChargeTowerBuild b ? b : null;
        }

        protected ApotheosisChargeTowerBuild getChargerPos(int pos){
            return world.build(pos) instanceof ApotheosisChargeTowerBuild b ? b : null;
        }

        protected void connectChargers(){
            connectedChargers.clear();
            chargers.each(i -> {
                ApotheosisChargeTowerBuild other = (ApotheosisChargeTowerBuild)world.build(i);
                other.connected = other.consValid();
                if(other.consValid()){
                    connectedChargers.add(i);
                }
            });
            connectedChargers.shuffle();
        }
        
        protected void calc(){
            falloff = 1f;
            realDamage = realRadius = realSpeed = realDuration = 0;
            chargers.each(i -> {
                ApotheosisChargeTowerBuild other = (ApotheosisChargeTowerBuild)world.build(i);
                if(other.consValid() && other.connected){
                    ApotheosisChargeTower b = ((ApotheosisChargeTower)(other.block()));
                    realDamage += b.damageBoost * falloff * other.efficiency();
                    realRadius += b.radiusBoost * falloff * other.efficiency();
                    realSpeed += b.speedBoost * falloff * other.efficiency();
                    realDuration += b.durationBoost * falloff * other.efficiency();
                    falloff *= 1f - boostFalloff;
                }else{
                    other.connected = false;
                }
            });
            realDamage += damage;
            realRadius += damageRadius;
            realSpeed += speed;
            realDuration += duration;
        }

        @Override
        public void remove(){
            chargeSoundLoop.stop();
            beamSoundLoop.stop();
            super.remove();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(realDamage);
            write.f(realRadius);
            write.f(realSpeed);
            write.f(realDuration);

            write.i(chargers.size);
            chargers.each(write::i);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            realDamage = read.f();
            realRadius = read.f();
            realSpeed = read.f();
            realDuration = read.f();

            for(int i = 0, n = read.i(); i < n; i++){
                chargers.add(read.i());
            }
        }
    }
}
