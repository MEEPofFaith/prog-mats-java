package progressed.world.blocks.defence.turret;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.graphics.PMFx.*;
import progressed.ui.*;

import static mindustry.Vars.*;

public class TeslaTurret extends Block{
    private final Seq<Healthc> targets = new Seq<>();
    public final int timerCheck = timers++;
    public int checkInterval = 20;

    public Seq<TeslaRing> rings = new Seq<>();
    public boolean hasSpinners;
    public Color lightningColor = Pal.surge;

    public float reloadTime;
    public float powerUse = 1f;
    public boolean acceptCoolant = true;
    public float coolantMultiplier = 5f;

    public int maxTargets;
    public float range, damage;
    public StatusEffect status;
    public float statusDuration = 10f * 60f;

    public float spinUp, spinDown, lightningStroke = 3.5f;
    public float sectionRad = 0.14f, blinkScl = 20f;
    public int sections = 5;

    public Sound shootSound = Sounds.spark;
    public Effect shootEffect = Fx.sparkShoot;
    public Effect coolEffect = Fx.fuelburn;
    public Color heatColor = Pal.turretHeat;
    public float shootShake;

    public float elevation = -1f;
    public float cooldown = 0.04f;
    public float rotateSpeed = 0.5f;

    public TextureRegion[] ringRegions, heatRegions, outlineRegions;
    public TextureRegion baseRegion, bottomRegion, topRegion;

    public TeslaTurret(String name){
        super(name);

        update = true;
        solid = true;
        outlineIcon = true;
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        stats.add(Stat.ammo, PMStatValues.teslaZapping(damage, maxTargets, status));
    }

    @Override
    public void load(){
        super.load();

        ringRegions = new TextureRegion[rings.size];
        heatRegions = new TextureRegion[rings.size];
        outlineRegions = new TextureRegion[rings.size];
        
        for(int i = 0; i < ringRegions.length; i++){
            if(rings.get(i).hasSprite){
                ringRegions[i] = Core.atlas.find(name + "-ring-" + i);
                outlineRegions[i] = Core.atlas.find(name + "-outline-" + i);
            }
            heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
        }

        if(hasSpinners) bottomRegion = Core.atlas.find(name + "-bottom");
        topRegion = Core.atlas.find(name + "-top");
        baseRegion = Core.atlas.find(name + "-base", "block-" + size);
    }

    @Override
    public void init(){
        if(rings.size <= 0){
            throw new RuntimeException(name + " does not have any rings!");
        }
        if(maxTargets <= 0){
            throw new RuntimeException("The 'maxTargets' of " + name + " is 0!");
        }

        if(acceptCoolant && !consumes.has(ConsumeType.liquid)){
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(0.2f)).update(false).boost();
        }

        consumes.powerCond(powerUse, TeslaTurretBuild::active);

        if(elevation < 0) elevation = size / 2f;
        clipSize = Math.max(clipSize, (range + 3f) * 2f);

        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    public static class TeslaRing{ //Create different rings out of this
        public boolean drawUnder, hasSprite;
        public float rotationMul, radius, xOffset, yOffset;

        public TeslaRing(float radius){
            this.radius = radius;
        }
    }
    
    public class TeslaTurretBuild extends Building implements Ranged{
        protected float[] heats = new float[rings.size];
        protected float rotation = 90f, speedScl, curStroke, reload;
        protected boolean nearby;

        public boolean active(){
            return nearby;
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, team.color);
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            for(int i = 0; i < rings.size; i++){
                TeslaRing ring = rings.get(i);
                if(ring.hasSprite){
                    Drawf.shadow(ringRegions[i], x - elevation, y - elevation, rotation * ring.rotationMul - 90f);
                }
            }

            TextureRegion r = hasSpinners ? bottomRegion : region;

            Drawf.shadow(r, x - elevation, y - elevation);

            Draw.rect(r, x, y);

            for(int i = 0; i < rings.size; i++){
                TeslaRing ring = rings.get(i);
                if(ring.hasSprite){
                    Draw.rect(outlineRegions[i], x, y, rotation * ring.rotationMul - 90f);
                }
            }

            for(int i = 0; i < rings.size; i++){
                TeslaRing ring = rings.get(i);
                if(ring.drawUnder){
                    if(ring.hasSprite){
                        if(ring.rotationMul != 0){
                            Drawf.spinSprite(ringRegions[i], x, y, rotation * ring.rotationMul - 90f);
                        }else{
                            Draw.rect(ringRegions[i], x, y);
                        }
                    }

                    if(heats[i] > 0.00001f){
                        Draw.color(heatColor, heats[i]);
                        Draw.blend(Blending.additive);
                        Draw.rect(heatRegions[i], x, y, rotation * ring.rotationMul - 90f);
                        Draw.blend();
                        Draw.color();
                    }
                }
            }

            Draw.rect(topRegion, x, y);

            for(int i = 0; i < rings.size; i++){
                TeslaRing ring = rings.get(i);
                if(!ring.drawUnder){
                    if(ring.hasSprite){
                        if(ring.rotationMul != 0){
                            Drawf.spinSprite(ringRegions[i], x, y, rotation * ring.rotationMul - 90f);
                        }else{
                            Draw.rect(ringRegions[i], x, y);
                        }
                    }

                    if(heats[i] > 0.00001f){
                        Draw.color(heatColor, heats[i]);
                        Draw.blend(Blending.additive);
                        Draw.rect(heatRegions[i], x, y, rotation * ring.rotationMul - 90f);
                        Draw.blend();
                        Draw.color();
                    }
                }
            }

            if(Core.settings.getBool("pm-tesla-range") && curStroke > 0.001f){
                Draw.z(Layer.bullet - 0.001f);
                Lines.stroke((0.7f +  + Mathf.absin(blinkScl, 0.7f)) * curStroke, lightningColor);
                for(int i = 0; i < sections; i++){
                    float rot = i * 360f / sections + Time.time * rotateSpeed;
                    Lines.swirl(x, y, range, sectionRad, rot);
                }
            }
        }

        @Override
        public void updateTile(){
            for(int i = 0; i < heats.length; i++){
                heats[i] = Mathf.lerpDelta(heats[i], 0f, cooldown);
            }

            if(!nearby || !cons.valid()){
                speedScl = Mathf.lerpDelta(speedScl, 0, spinDown);
            }
            if(nearby && cons.valid()){
                Liquid liquid = liquids.current();
                speedScl = Mathf.lerpDelta(speedScl, 1, spinUp * liquid.heatCapacity * coolantMultiplier * edelta());
            }
                
            rotation -= speedScl * delta();
            curStroke = Mathf.lerpDelta(curStroke, nearby ? 1 : 0, 0.09f);

            if(consValid()){
                if(timer(timerCheck, checkInterval)){
                    nearby = PMDamage.checkForTargets(team, x, y, range);
                }
            }else{
                nearby = false;
            }

            if(nearby){
                updateCooling();

                if((reload += delta()) >= reloadTime){
                    targets.clear();
                    PMDamage.allNearbyEnemies(team, x, y, range, targets::add);

                    if(targets.size > 0){
                        targets.shuffle();
                        int max = Math.min(maxTargets, targets.size);

                        for(int i = 0; i < max; i++){
                            Healthc other = targets.get(i);

                            //lightning gets absorbed by plastanium
                            var absorber = Damage.findAbsorber(team, x, y, other.getX(), other.getY());
                            if(absorber != null){
                                other = absorber;
                            }

                            //Deal damage
                            other.damage(damage);
                            if(other instanceof Statusc s){
                                s.apply(status, statusDuration);
                            }

                            //Lightning effect
                            TeslaRing ring = rings.random();
                            heats[rings.indexOf(ring)] = 1f;
                            Tmp.v1.trns(rotation * ring.rotationMul, ring.xOffset, ring.yOffset); //ring location
                            Tmp.v2.setToRandomDirection().setLength(ring.radius); //ring

                            float shootX = x + Tmp.v1.x, shootY = y + Tmp.v1.y;
                            float shootAngle = Angles.angle(shootX, shootY, other.x(), other.y());

                            shootSound.at(shootX, shootY, Mathf.random(0.9f, 1.1f));
                            shootEffect.at(shootX, shootY, shootAngle, lightningColor);
                            PMFx.fakeLightning.at(shootX, shootY, shootAngle, lightningColor, new LightningData(other, lightningStroke));
                        }

                        Effect.shake(shootShake, shootShake, this);

                        reload %= reloadTime;
                    }
                }
            }
        }

        protected void updateCooling(){
            if(reload < reloadTime){
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
                Liquid liquid = liquids.current();

                float used = Math.min(liquids.get(liquid), maxUsed * Time.delta) * efficiency();
                reload += used * liquid.heatCapacity * coolantMultiplier;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(reload);
            write.bool(nearby);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                reload = read.f();
                nearby = read.bool();
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}