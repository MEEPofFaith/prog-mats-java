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
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

public class TeslaTurret extends Block{
    private final Seq<Healthc> targets = new Seq<>();
    public final int timerCheck = timers++;
    public int checkInterval = 20;

    public Seq<TeslaRing> rings = new Seq<>();
    public boolean hasSpinners;
    public Color lightningColor = Pal.surge;

    public float reload;
    public float coolantMultiplier = 5f;
    /** If not null, this consumer will be used for coolant. */
    public ConsumeLiquidBase coolant;

    public int maxTargets;
    public float range, damage;
    public float placeOverlapMargin = 8 * 7f;
    public StatusEffect status;
    public float statusDuration = 10f * 60f;

    public float spinUp = 0.01f, spinDown = 0.0125f;
    public float sectionRad = 0.14f, blinkScl = 20f;
    public int sections = 5;

    public Sound shootSound = Sounds.spark;
    public LightningEffect lightningEffect = LightningFx.teslaLightning;
    public Effect shootEffect = Fx.sparkShoot;
    public Effect hitEffect = Fx.hitLaserBlast;
    public Effect coolEffect = Fx.fuelburn;
    public Color heatColor = Pal.turretHeat;
    public float shake;

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
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
        stats.add(Stat.ammo, PMStatValues.teslaZapping(damage, maxTargets, status));

        if(coolant != null){
            stats.add(Stat.booster, StatValues.boosters(reload, coolant.amount, coolantMultiplier, true, l -> l.coolant && consumesLiquid(l)));
        }
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
    public void createIcons(MultiPacker packer){
        if(hasSpinners) Outliner.outlineRegion(packer, bottomRegion, outlineColor, name + "-bottom");
        Outliner.outlineRegions(packer, ringRegions, outlineColor, name + "-outline");
        super.createIcons(packer);
    }

    @Override
    public void init(){
        if(rings.size <= 0){
            PMUtls.uhOhSpeghettiOh(name + " does not have any rings!");
        }
        if(maxTargets <= 0) maxTargets = 1;

        if(elevation < 0) elevation = size / 2f;
        clipSize = Math.max(clipSize, (range + 3f) * 2f);

        if(coolant != null){
            coolant.update = false;
            coolant.booster = true;
            coolant.optional = true;
        }

        placeOverlapRange = Math.max(placeOverlapRange, range + placeOverlapMargin);
        fogRadius = Math.max(Mathf.round(range / tilesize), fogRadius);

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
        protected float rotation = 90f, speedScl, curStroke, reloadCounter;
        protected boolean nearby;

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

            Draw.rect(topRegion.found() ? topRegion : region, x, y);

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
                Lines.stroke((0.7f + Mathf.absin(blinkScl, 0.7f)) * curStroke, lightningColor);
                for(int i = 0; i < sections; i++){
                    float rot = i * 360f / sections + Time.time * rotateSpeed;
                    Lines.arc(x, y, range, sectionRad, rot);
                }
            }
        }

        @Override
        public void drawLight(){
            Drawf.light(x, y, range * 1.5f, lightningColor, curStroke * 0.8f);
            super.drawLight();
        }

        @Override
        public void updateTile(){
            for(int i = 0; i < heats.length; i++){
                heats[i] = Mathf.lerpDelta(heats[i], 0f, cooldown);
            }

            if(!nearby || !canConsume()){
                speedScl = Mathf.lerpDelta(speedScl, 0, spinDown);
            }
            if(nearby && canConsume()){
                Liquid liquid = liquids.current();
                speedScl = Mathf.lerpDelta(speedScl, 1, spinUp * liquid.heatCapacity * coolantMultiplier * edelta());
            }
                
            rotation -= speedScl * edelta();
            curStroke = Mathf.lerpDelta(curStroke, nearby ? 1 : 0, 0.09f);

            if(canConsume()){
                if(timer(timerCheck, checkInterval)){
                    nearby = PMDamage.checkForTargets(team, x, y, range);
                }
            }else{
                nearby = false;
            }

            if(nearby){
                updateCooling();

                if((reloadCounter += edelta()) >= reload){
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
                            if(other instanceof Building b){
                                b.damage(team, damage);
                            }else{
                                other.damage(damage);
                            }
                            if(other instanceof Statusc s){
                                s.apply(status, statusDuration);
                            }

                            //Lightning effect
                            TeslaRing ring = rings.random();
                            heats[rings.indexOf(ring)] = 1f;
                            Tmp.v1.trns(rotation * ring.rotationMul, ring.xOffset, ring.yOffset); //ring location
                            Tmp.v2.setToRandomDirection().setLength(ring.radius); //ring

                            float shootX = x + Tmp.v1.x + Tmp.v2.x, shootY = y + Tmp.v1.y + Tmp.v2.y;
                            float shootAngle = Angles.angle(shootX, shootY, other.x(), other.y());

                            shootSound.at(shootX, shootY, Mathf.random(0.9f, 1.1f));
                            shootEffect.at(shootX, shootY, shootAngle, lightningColor);
                            hitEffect.at(other.x(), other.y(), lightningColor);
                            lightningEffect.at(shootX, shootY, other.x(), other.y(), lightningColor);
                        }

                        Effect.shake(shake, shake, this);

                        reloadCounter %= reload;
                    }
                }
            }
        }

        protected void updateCooling(){
            if(reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : 1f;
                coolant.update(this);
                reloadCounter += coolant.amount * edelta() * capacity * coolantMultiplier;

                if(Mathf.chance(0.06 * coolant.amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume() && nearby;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(reloadCounter);
            write.bool(nearby);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                reloadCounter = read.f();
                nearby = read.bool();
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}
