package progressed.world.blocks.defence.turret;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;
import progressed.world.meta.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class SwordTurret extends BaseTurret{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public static final float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    public int targetInterval = 20;

    public boolean targetAir = true;
    public boolean targetGround = true;

    public int swords = 3;
    public float minRadius = tilesize, radius = 3f * tilesize, expandedRadius = -1;
    public float expandTime = 9f, pauseTime = 15f, stabTime = 18f, totalTime = 30f;
    public float attackRadius = 2f * tilesize, speed = 2f;
    public Color heatColor = Pal.surge;
    public float cooldown = 0.05f;

    public float bladeCenter, trailWidth = 8f;
    public Color trailColor = Color.violet;
    public int trailLength = 8;

    public float baseLength = -1f;
    public float baseWidth = 2f, connectWidth = 4f;

    public float damage = 450f, buildingDamageMultiplier = 0.25f, damageRadius = tilesize;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 10 * 60;
    public Sound hitSound = PMSounds.swordStab;
    public float minVolume = 1f, maxVolume = 1f;
    public float minPitch = 0.9f, maxPitch = 1.1f;
    public Effect hitEffect = PMFx.swordStab;
    public Color hitColor = Pal.surge;
    public float hitShake;

    public float powerUse = 1f;

    public Sortf unitSort = Unit::dst2;

    public float elevation = -1f, swordElevation = -1f;

    public TextureRegion baseRegion, outlineRegion, swordRegion, heatRegion;

    public SwordTurret(String name){
        super(name);
        hasPower = true;
        rotateSpeed = 4f;
        acceptCoolant = canOverdrive = false;
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, SwordTurretBuild::isActive);
        if(elevation < 0) elevation = size / 2f;
        if(swordElevation < 0) swordElevation = elevation * 2f;
        if(expandedRadius < 0) expandedRadius = radius * 2.5f;
        if(baseLength < 0) baseLength = size * tilesize / 2f;

        super.init();
    }

    @Override
    public void load(){
        super.load();

        baseRegion = atlas.find(name + "-base", "block-" + size);
        swordRegion = atlas.find(name + "-sword");
        outlineRegion = atlas.find(name + "-sword-outline");
        heatRegion = atlas.find(name + "-sword-heat");

        clipSize = Math.max(clipSize, (range + expandedRadius + swordRegion.height) * 2f);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
            baseRegion,
            region
        };
    }

    @Override
    public void setStats(){
        super.setStats();

        if(acceptCoolant){
            stats.add(Stat.booster, StatValues.boosters(speed, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }

        stats.add(Stat.reload, PMUtls.stringsFixed(totalTime / 60f));
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);

        stats.add(Stat.ammo, PMStatValues.swordDamage(damage, damageRadius, buildingDamageMultiplier, speed, status));
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawRequestRegion(req, list);

        for(int i = 0; i < swords; i++){
            float rot = 90f + i * (360f / swords);
            Tmp.v1.trns(rot, -radius);
            Draw.rect(outlineRegion, req.drawx() + Tmp.v1.x, req.drawy() + Tmp.v1.y, rot);
        }
    }

    public class SwordTurretBuild extends BaseTurretBuild implements ControlBlock{
        public @Nullable Posc target;
        public Vec2 targetPos = new Vec2(), currentPos = new Vec2();
        protected float animationTime, coolantScl = 1f, logicControlTime = -1, lookAngle, heat;
        public BlockUnitc unit;
        protected boolean logicShooting, wasAttacking, ready, hit;
        protected PMTrail[] trails = new PMTrail[swords];

        @Override
        public void created(){
            unit = (BlockUnitc)UnitTypes.block.create(team);
            unit.tile(this);
            currentPos.set(x, y);
            for(int i = 0; i < swords; i++){
                trails[i] = new PMTrail(trailLength);
            }
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                targetPos.trns(angleTo(World.unconv((float)p1), World.unconv((float)p2)), dst(World.unconv((float)p1), World.unconv((float)p2))).limit(range).add(this);
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p3);
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.shootp && !unit.isPlayer()){
                logicControlTime = logicControlCooldown;
                logicShooting = !Mathf.zero(p2);

                if(p1 instanceof Posc p){
                    targetPosition(p);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor){
            //Ignore the "Java 14" error, it somehow works
            return switch(sensor){
                case ammo -> power.status;
                case shootX -> World.conv(targetPos.x);
                case shootY -> World.conv(targetPos.y);
                case shooting -> isAttacking() ? 1 : 0;
                default -> super.sense(sensor);
            };
        }

        /** @return whether this block is being controlled by a player. */
        public boolean isControlled(){
            return unit().isPlayer();
        }

        /** @return whether this block can be controlled at all. */
        public boolean canControl(){
            return true;
        }

        /** @return whether targets should automatically be selected (on mobile) */
        public boolean shouldAutoTarget(){
            return true;
        }

        public boolean isAttacking(){
            return (isControlled() ? unit.isShooting() : logicControlled() ? logicShooting : target != null);
        }

        @Override
        public Unit unit(){
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0f;
        }

        public boolean isActive(){
            return target != null || wasAttacking;
        }

        public void targetPosition(Posc pos){
            if(!consValid() || pos == null) return;
            Tmp.v1.trns(angleTo(pos), dst(pos)).limit(range).add(this);
            targetPos.set(Predict.intercept(currentPos, Tmp.v1, speed));
        }

        @Override
        public void draw(){
            //Base turret
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            Drawf.shadow(region, x - elevation, y - elevation, lookAngle);
            Draw.rect(region, x, y, lookAngle);

            //Swords
            float swordOpacity = settings.getInt("pm-swordopacity") / 100f;

            Tmp.c1.set(trailColor).lerp(heatColor, heat).mul(1f, 1f, 1f, 0.5f * swordOpacity);

            Vec2 v1 = Tmp.v1.trns(lookAngle + 90f, baseLength);

            Draw.z(Layer.flyingUnit + 0.002f);
            for(int i = 0; i < swords; i++){
                float rot = rotation + i * (360f / swords);

                Vec2 v2 = Tmp.v2.trns(rot, -getRadius()).add(currentPos).sub(this);

                Vec2 v3 = Tmp.v3.trns(v1.angleTo(v2) - 90f, -connectWidth / 2f, v1.dst(v2)).add(v1).add(this);
                Vec2 v4 = Tmp.v4.trns(v1.angleTo(v2) - 90f, connectWidth / 2f, v1.dst(v2)).add(v1).add(this);

                Vec2 v5 = Tmp.v5.trns(v1.angleTo(v2) - 90f, -baseWidth / 2f, 0f);
                Vec2 v6 = Tmp.v6.trns(v1.angleTo(v2) - 90f, baseWidth / 2f, 0f);

                Draw.color(Tmp.c1);
                Fill.quad(x + v1.x + v5.x, y + v1.y + v5.y, v3.x, v3.y, v4.x, v4.y, x + v1.x + v6.x, y + v1.y + v6.y);
            }
            Fill.circle(x + v1.x, y + v1.y, baseWidth / 2f);

            Tmp.c1.set(trailColor).lerp(heatColor, heat).mul(1f, 1f, 1f, swordOpacity);

            Draw.z(Layer.flyingUnit + 0.003f);
            for(PMTrail t : trails){
                t.draw(Tmp.c1, trailWidth);
            }

            for(int i = 0; i < swords; i++){
                float rot = rotation + i * (360f / swords);

                Tmp.v1.trns(rot, -getRadius());

                float sX = currentPos.x + Tmp.v1.x, sY = currentPos.y + Tmp.v1.y;

                Draw.z(Layer.flyingUnit + 0.001f);
                PMDrawf.shadowAlpha(outlineRegion, sX - swordElevation, sY - swordElevation, rot + getRotation(), swordOpacity);

                Draw.alpha(swordOpacity);
                Draw.z(Layer.flyingUnit + 0.004f);
                Draw.rect(outlineRegion, sX, sY, rot + getRotation());
            }

            for(int i = 0; i < swords; i++){
                float rot = rotation + i * (360f / swords);

                Tmp.v1.trns(rot, -getRadius());

                float sX = currentPos.x + Tmp.v1.x, sY = currentPos.y + Tmp.v1.y;

                Draw.alpha(swordOpacity);
                Draw.z(Layer.flyingUnit + 0.005f);
                Draw.rect(swordRegion, sX, sY, rot + getRotation());

                if(ready && heat > 0f){
                    Draw.color(heatColor, heat * swordOpacity);
                    Draw.blend(Blending.additive);
                    Draw.rect(heatRegion, sX, sY, rot + getRotation());
                    Draw.color();
                    Draw.blend();
                }
            }
        }

        protected float getRotation(){
            float expand = Mathf.curve(animationTime, 0f, expandTime);
            float attack = Mathf.curve(animationTime, pauseTime, stabTime);
            float endRot = Mathf.curve(animationTime, stabTime + (totalTime - stabTime) * 0.2f, totalTime);
            return -270 * expand + -180f * attack + -270f * endRot;
        }

        protected float getRadius(){
            float expand = Mathf.curve(animationTime, 0f, expandTime);
            float pause = Mathf.curve(animationTime, expandTime, pauseTime);
            float attack = Mathf.curve(animationTime, pauseTime, stabTime);
            float reset = Mathf.curve(animationTime, stabTime, totalTime);
            float progress = (expand + pause + attack + reset) / 4f;
            return PMUtls.multiLerp(new float[]{radius, expandedRadius, expandedRadius, minRadius, radius}, progress);
        }

        protected void turnTo(float target){
            lookAngle = Angles.moveToward(lookAngle, target - 90f, speed * 3f * cdelta() * efficiency());
        }

        @Override
        public void updateTile(){
            if(!validateTarget() || aiTargetDistCheck()) target = null;

            wasAttacking = false;

            unit.health(health);
            unit.rotation(rotation);
            unit.team(team);
            unit.set(x, y);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(consValid()){

                if(!ready && (!validateTarget() || aiTargetDistCheck()) && timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canAttack = true;

                    if(isControlled()){ //player behavior
                        targetPos.trns(unit.angleTo(unit.aimX(), unit.aimY()), unit.dst(unit.aimX(), unit.aimY())).limit(range).add(this);
                        canAttack = unit.isShooting();
                    }else if(logicControlled()){ //logic behavior
                        canAttack = logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);
                    }

                    if(canAttack){
                        wasAttacking = true;
                        moveTo(targetPos, true);
                        if(acceptCoolant){
                            updateCooling();
                        }
                    }else if(!ready){
                        reset();
                    }
                }else if(!ready){
                    reset();
                }
            }else if(!ready){
                reset();
            }

            if(ready){
                animationTime += cdelta() * efficiency();
                if(animationTime >= pauseTime && animationTime <= stabTime){
                    heat = Mathf.curve(animationTime, pauseTime, stabTime);
                }else{
                    heat = Mathf.lerpDelta(heat, 0f, cooldown);
                }
                if(animationTime >= stabTime && !hit){
                    hit = true;
                    hitEffect.at(currentPos.x, currentPos.y, hitColor);
                    for(int i = 0; i < swords; i++){
                        hitSound.at(currentPos.x, currentPos.y, Mathf.random(minPitch, maxPitch), Mathf.random(minVolume, maxVolume));
                    }
                    if(hitShake > 0f){
                        Effect.shake(hitShake, hitShake, this);
                    }
                    //Slow speed, weak hit -> * efficiency()
                    PMDamage.completeDamage(team, currentPos.x, currentPos.y, damageRadius, damage * efficiency(), buildingDamageMultiplier, targetAir, targetGround);
                    if(status != StatusEffects.none){
                        Damage.status(team, currentPos.x, currentPos.y, damageRadius, status, statusDuration * efficiency(), targetAir, targetGround);
                    }
                }
                if(animationTime > totalTime){
                    if(!validateTarget() || !isAttacking() || !consValid() || aiTargetDistCheck() || currentPos.dst(targetPos) > attackRadius){
                        ready = false; //do not stop until dead or unable to attack
                        target = null;
                    }
                    hit = false;
                    animationTime = 0f;
                }
            }else{
                heat = Mathf.lerpDelta(heat, 0f, cooldown);
                animationTime = 0f;
            }

            if(dst(currentPos) > size / 2f || isAttacking()) turnTo(angleTo(currentPos));
            rotation = (rotation - rotateSpeed * cdelta() * efficiency()) % 360f;

            for(int i = 0; i < swords; i++){
                float rot = rotation + i * (360f / swords);

                Tmp.v1.trns(rot, -getRadius());

                float sX = currentPos.x + Tmp.v1.x, sY = currentPos.y + Tmp.v1.y;

                Tmp.v2.trns(rot + getRotation() + 90f, bladeCenter);

                trails[i].update(sX + Tmp.v2.x, sY + Tmp.v2.y, rot + getRotation());
            }
        }

        protected void updateCooling(){
            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

            Liquid liquid = liquids.current();

            float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, (speed / coolantMultiplier) / liquid.heatCapacity));
            coolantScl = 1f + used;
            liquids.remove(liquid, used);

            if(Mathf.chance(0.06 * used)){
                coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
            }
        }

        protected void reset(){
            coolantScl = 1f;
            moveTo(x, y, false);
        }

        protected void moveTo(Vec2 pos, boolean readyUp){
            float angle = currentPos.angleTo(pos);
            float dist = currentPos.dst(pos);
            boolean checkDst = dist < attackRadius;
            ready = checkDst && readyUp;
            Tmp.v1.trns(angle, speed * cdelta() * efficiency()).limit(dist);
            currentPos.add(Tmp.v1);
        }

        protected void moveTo(float x, float y, boolean readyUp){
            moveTo(Tmp.v1.set(x, y), readyUp);
        }

        protected boolean validateTarget(){
            return (!Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled());
        }

        protected boolean aiTargetDistCheck(){ //Returns true if the turret is not controlled and the target it out of range.
            return (!isControlled() && !logicControlled()) && dst(target) > range;
        }

        protected void findTarget(){
            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, unitSort);
            }
        }

        protected float cdelta(){
            return delta() * coolantScl;
        }

        @Override
        public float efficiency(){
            //disabled -> 0.1f efficiency
            if(!enabled) return 0.1f;
            return power != null && (block.consumes.has(ConsumeType.power) && !block.consumes.getPower().buffered) ? (0.1f + power.status * 0.9f) : 1f;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(ready);
            write.bool(hit);
            write.f(lookAngle);
            write.f(animationTime);
            write.f(currentPos.x);
            write.f(currentPos.y);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                ready = read.bool();
                hit = read.bool();
                lookAngle = read.f();
                animationTime = read.f();
                currentPos.set(read.f(), read.f());
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }
}
