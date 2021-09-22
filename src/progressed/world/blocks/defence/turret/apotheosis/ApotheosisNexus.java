package progressed.world.blocks.defence.turret.apotheosis;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.*;
import progressed.content.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.apotheosis.ApotheosisChargeTower.*;

import java.util.*;

import static mindustry.Vars.*;

public class ApotheosisNexus extends ReloadTurret{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public ApotheosisChargeTower chargeTower;

    public final int timerTarget = timers++, damageTimer = timers++, pulseTimer = timers++;
    public int targetInterval = 20, damageInterval = 5, pulseInterval = 45;

    public float powerUse = 1f;
    public float speed, duration = 60f;
    public float damage, damageRadius = tilesize;
    public float chargeTime = 5f * 60f;
    public float arcTime = 30f;
    public float fadeTime = 60f;
    public StatusEffect status;
    public float statusDuration = 6f * 10f;
    public float cooldown = 0.02f;
    public float baseRotateSpeed = 1f, rotateSpeed = 12f, spinUpSpeed = 0.005f, spinDownSpeed = 0.005f;
    public float ringExpand1 = 12f, ringExpand2 = 32f;

    public int lights = 9;
    public Color lightsBase = Color.valueOf("252835"), lightsDark = PMPal.apotheosisLaserDark, lightsLight = PMPal.apotheosisLaser;
    public float lightInterval = 300f;
    public float laserRadius;
    public float hight = 150f * tilesize;
    public Color[] colors = PMPal.apotheosisLaserColors;
    public Color laserLightColor = PMPal.apotheosisLaser;
    public float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
    public float[] strokes = {2f, 1.5f, 1f, 0.3f};
    public float[] pullLengths = {0f, 2.25f, 3.5f, 4f};
    public float[] lenscales = {0.90f, 0.95f, 0.98f, 1f}, blankscales;
    public float width = -1f, oscScl = 3f, oscMag = 0.2f;
    public float pissChance = 0.01f;

    public Effect fireEffect = Fx.none, touchDownEffect = Fx.none, damageEffect = PMFx.apotheosisDamage, pulseEffect = PMFx.apotheosisPulse;
    public Sound fireSound = Sounds.none;
    public float fireSoundPitch = 1f, fireSoundVolume = 1f;

    public Sortf unitSort = Unit::dst2;

    protected Vec2 tr = new Vec2();
    protected Color tc = new Color();

    public TextureRegion spinners;
    public TextureRegion[] lightRegions, lightSpinRegions = new TextureRegion[2], darkSpinRegions = new TextureRegion[2];

    public ApotheosisNexus(String name){
        super(name);

        hasPower = true;
        canOverdrive = false;
        outlineIcon = false;
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisNexusBuild::isActive);
        super.init();

        if(width < 0) width = size * tilesize / 3f;
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
            lightSpinRegions[i] = Core.atlas.find(name + "-spinner-light-" + i);
            darkSpinRegions[i] = Core.atlas.find(name + "-spinner-dark-" + i);
        }

        clipSize = Math.max(clipSize, (range + hight + 4f) * 2f);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            region,
            spinners
        };
    }

    public class ApotheosisNexusBuild extends ReloadTurretBuild implements ControlBlock{
        public IntSeq chargers = new IntSeq(), connectedChargers = new IntSeq();
        public float heat, warmup, spinUp, rotation = 90f, logicControlTime = -1;
        public float charge, arc, fade, activeTime;
        public float realDamage, realRadius, realSpeed, realDuration;
        public int shotCounter;
        public boolean logicShooting = false;
        public Posc target;
        public Vec2 targetPos = new Vec2(), curPos = new Vec2();
        public boolean wasShooting, shooting, charging, arcing, fading, piss;
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
                case rotation -> rotation;
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
                float lerp = (0.25f + Mathf.absin(Time.time - i * (lightInterval / lights), lightInterval / 8, 0.75f)) * warmup;
                tc.set(lightsBase).lerp(lightsDark, lerp);
                Draw.color(tc);
                Draw.rect(lightRegions[i], x, y);
                tc.set(lightsBase).lerp(lightsLight, lerp);
                Draw.color(tc);
                Draw.rect(lightRegions[i], x, y, 180f);
            }
            Draw.color();

            if(arc > 0){
                Color[] c = Core.settings.getBool("pm-farting") || piss ? PMPal.piss : colors;
                //Very messy, don't know how to clean this
                Draw.z(Layer.effect + 0.002f);
                float u1 = Mathf.curve(arc * 2f, 0f, arcTime / 2f),
                    u2 = Mathf.curve(arc * 2f, arcTime / 2f, arcTime / 2f + arcTime / 3f),
                    u3 = Mathf.curve(arc * 2f, arcTime / 2f + arcTime / 3f, arcTime);
                if(u1 > 0.01){
                    PMDrawf.laser3(team, x, y, u1 * hight / 2f, width, 90f, fadef(), tscales, strokes, pullLengths, oscScl, oscMag, c, laserLightColor, fadef());
                }
                if(u2 > 0.01){
                    PMDrawf.laser(team, x, y + hight / 2f, u2 * hight / 3f, width, 90f, fadef() * (1f + (radscl() - 1f) / 2f), tscales, strokes, blankscales, oscScl, oscMag, 0f, c, laserLightColor, 2f / 3f * fadef());
                }
                if(u2 > 0.01){
                    PMDrawf.laser(team, x, y + hight / 2f + hight / 3f, u3 * hight / 6f, width, 90f, fadef() * radscl(), tscales, strokes, lenscales, oscScl, oscMag, 0f, c, laserLightColor, 1f / 3f * fadef());
                }

                Draw.z(Layer.effect + (curPos.y < y ? 0.003f : 0.001f));
                float d1 = Mathf.curve((arc - arcTime / 2f) * 2f, 0f, arcTime / 6f),
                    d2 = Mathf.curve((arc - arcTime / 2f) * 2f, arcTime / 6f, arcTime / 2f),
                    d3 = Mathf.curve((arc - arcTime / 2f) * 2f, arcTime / 2f, arcTime);
                if(d1 > 0.01){
                    PMDrawf.laser2(team, curPos.x, curPos.y + hight, d1 * hight / 6f, width, -90f, fadef() * radscl(), tscales, strokes, lenscales, oscScl, oscMag, c, laserLightColor, 1f / 3f * fadef());
                }
                if(d2 > 0.01){
                    PMDrawf.laser2(team, curPos.x, curPos.y + hight / 2f + hight / 3f, d2 * hight / 3f, width, -90f, fadef() * radscl(), tscales, strokes, blankscales, oscScl, oscMag, c, laserLightColor, 2f / 3f * fadef());
                }
                if(d2 > 0.01){
                    PMDrawf.laser2(team, curPos.x, curPos.y + hight / 2f, d3 * hight / 2f, width, -90f, fadef() * radscl(), tscales, strokes, blankscales, oscScl, oscMag, c, laserLightColor, fadef());
                }
            }

            Draw.z(Layer.blockOver); //Can this by simplified? Probably.
            tr.trns(rotation, ringExpand1 * spinUp);
            PMDrawf.spinSprite(lightSpinRegions[0], darkSpinRegions[0], x + tr.x, y + tr.y, rotation);
            tr.rotate(180f);
            PMDrawf.spinSprite(lightSpinRegions[0], darkSpinRegions[0], x + tr.x, y + tr.y, rotation + 180f);
            tr.trns(-rotation - 90f, ringExpand2 * spinUp);
            PMDrawf.spinSprite(lightSpinRegions[1], darkSpinRegions[1], x + tr.x, y + tr.y, -rotation - 90f);
            tr.rotate(180f);
            PMDrawf.spinSprite(lightSpinRegions[1], darkSpinRegions[1], x + tr.x, y + tr.y, -rotation + 90f);
        }

        @Override
        public Unit unit(){
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        public boolean isActive(){
            return (target != null || wasShooting || shooting) && enabled;
        }

        public void targetPosition(Posc pos){ //Entity targets, leads targets
            if(!consValid() || pos == null) return;

            if(!shooting && !arcing && !fading){
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

            if(!shooting && !arcing && !fading){
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
            calc();

            wasShooting = false;

            heat = Mathf.lerpDelta(heat, 0f, cooldown);
            warmup = Mathf.lerpDelta(warmup, consValid() ? 1 : 0, 0.01f);
            rotation += Mathf.lerp(baseRotateSpeed, rotateSpeed, spinUp) * delta();
            if(!charging && !arcing && !shooting || fading) spinUp = Mathf.lerp(spinUp, 0f, spinDownSpeed);

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

            if(acceptCoolant){
                updateCooling();
            }

            connectedChargers.each(i -> {
                ApotheosisChargeTowerBuild charger = (ApotheosisChargeTowerBuild)world.build(i);
                if(charger != null) charger.scl = fadef();
            });
        }

        protected void updateShooting(){
            reload += delta() * baseReloadSpeed();

            if(reload >= reloadTime && !(charging || shooting || arcing || fading)){
                connectChargers();
                charging = true;
                fade = fadeTime;
                piss = Mathf.chance(pissChance);
                for(int i = 0; i < connectedChargers.size; i++){
                    int ii = i;
                    Time.run(i * (chargeTime / 2f / connectedChargers.size) + chargeTime / 3f, () -> {
                        ApotheosisChargeTowerBuild charger = (ApotheosisChargeTowerBuild)world.build(connectedChargers.get(ii));
                        if(isValid() && charger != null){
                            charger.activate();
                        }
                    });
                }
            }
        }

        protected void updateFiring(){
            if(charging){
                if(consValid()){
                    charge += delta();
                    spinUp = Mathf.lerp(spinUp, 1f, spinUpSpeed);
                    if(charge >= chargeTime){
                        charge = chargeTime;
                        charging = false;
                        arcing = true;
                        getSound().at(x, y, fireSoundPitch, fireSoundVolume);
                        fireEffect.at(x, y);
                    }
                }else{
                    reset();
                }
            }

            if(arcing){
                arc += delta();
                spinUp = Mathf.lerp(spinUp, 1f, spinUpSpeed);
                if(arc >= arcTime){
                    arc = arcTime;
                    arcing = false;
                    shooting = true;
                    activeTime = 0f;
                    effect(touchDownEffect);
                }
            }

            if(fading){
                fade -= delta();
                if(fade <= 0f){
                    reset();
                    shotCounter++;
                }
            }

            if(shooting){
                activeTime += Time.delta / Math.max(efficiency(), 0.00001f);
                if(timer.get(damageTimer, damageInterval)){
                    PMDamage.allNearbyEnemies(team, curPos.x, curPos.y, realRadius * fadef(), other -> {
                        if(other instanceof Building b){
                            b.damage(team, realDamage * fadef());
                        }else{
                            other.damage(realDamage * fadef());
                        }
                        if(other instanceof Statusc s){
                            s.apply(status, statusDuration);
                        }
                    });
                    effect(damageEffect);
                }
                if(!fading) spinUp = Mathf.lerp(spinUp, 1f, spinUpSpeed);

                if(timer.get(pulseTimer, pulseInterval)){
                    effect(pulseEffect);
                }

                if(activeTime >= realDuration){
                    fading = true;
                }
            }
        }

        public float chargef(){
            return charge / chargeTime;
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
            shooting = false;
            fading = false;
            reload %= reloadTime;
            charge = 0f;
            arc = 0f;
            chargers.each(i -> ((ApotheosisChargeTowerBuild)(world.build(i))).fullLaser = false);
        }

        public void effect(Effect eff){
            eff.at(curPos.x, curPos.y, 0f, new float[]{Layer.effect + (curPos.y < y ? 0.0029f : 0.0009f), radscl() * fadef()}); //Layer data to draw properly close to the beam [layer, scl])
        }

        public Sound getSound(){
            return Core.settings.getBool("pm-farting") ? (piss ? PMSounds.loudMoonPiss : PMSounds.moonPiss) : (piss ? PMSounds.moonPiss : fireSound);
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        protected void findTarget(){
            target = Units.bestTarget(team, x, y, range, e -> !e.dead(), b -> true, unitSort);
        }

        @Override
        public void display(Table table){
            super.display(table);

            TextureRegionDrawable reg = new TextureRegionDrawable();

            table.row();
            table.table(t -> {
                t.left();
                t.image().update(i -> {
                    i.setDrawable(chargeTower.unlockedNow() ? reg.set(chargeTower.uiIcon) : Icon.lock);
                    i.setScaling(Scaling.fit);
                    i.setColor(chargeTower.unlockedNow() ? Color.white : Color.lightGray);
                }).size(32).padBottom(-4).padRight(2);
                t.label(() -> Core.bundle.format("pm-apotheosis-chargers", chargeTower.unlockedNow() ? chargers.size : Core.bundle.get("pm-missing-research"))).wrap().width(230f).color(Color.lightGray);
            });
        }

        protected void checkConnections(){
            int index = 0;
            while(index < chargers.size){
                if(world.build(chargers.get(index)) == null){
                    chargers.removeIndex(index);
                }else{
                    index++;
                }
            }
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
            realDamage = realRadius = realSpeed = realDuration = 0;
            chargers.each(i -> {
                ApotheosisChargeTowerBuild other = (ApotheosisChargeTowerBuild)world.build(i);
                if(other.consValid() && other.connected){
                    ApotheosisChargeTower b = ((ApotheosisChargeTower)(other.block()));
                    realDamage += b.damageBoost * other.efficiency();
                    realRadius += b.radiusBoost * other.efficiency();
                    realSpeed += b.speedBoost * other.efficiency();
                    realDuration += b.durationBoost * other.efficiency();
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
                int build = read.i();
                chargers.add(build);
                ((ApotheosisChargeTowerBuild)(world.build(build))).nexus = pos();
                ProgMats.print(self(), i);
            }
        }
    }
}