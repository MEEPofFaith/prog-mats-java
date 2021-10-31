package progressed.world.blocks.defence.turret.payload;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

//Payload + Turret = A lot of copy paste aaaaa
public class PayloadMissileTurret extends PayloadBlock{
    public float range = 80f;

    public boolean acceptCoolant = true;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;

    public float reloadTime = 2f * 60f;

    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    public int targetInterval = 20;

    public Color heatColor = Pal.turretHeat;
    public Sound shootSound = Sounds.shoot;

    //general info
    public float inaccuracy = 0f;
    public float velocityInaccuracy = 0f;
    public float cooldown = 0.02f;
    public float coolantUsage = 0.2f;
    public float shootShake = 0f;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public boolean targetAir = true;
    public boolean targetGround = true;

    public Sortf unitSort = Unit::dst2;

    public ObjectMap<Block, BulletType> ammoTypes = new ObjectMap<>();

    public TextureRegion heatRegion;

    public PayloadMissileTurret(String name){
        super(name);

        solid = true;
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
        liquidCapacity = 20f;
        outputsPayload = false;
        acceptsPayload = true;
        rotate = false;
    }

    /** Initializes accepted ammo map. Format: [block1, bullet1, block2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = ObjectMap.of(objects);
    }

    @Override
    public void init(){
        if(acceptCoolant && !consumes.has(ConsumeType.liquid)){
            hasLiquids = true;
            consumes.add(new ConsumeCoolant(coolantUsage)).update(false).boost();
        }

        super.init();
    }

    @Override
    public void load(){
        super.load();

        inRegion = Core.atlas.find(name + "-in", Core.atlas.find("factory-in-" + size, "prog-mats-factory-in-" + size));
        heatRegion = Core.atlas.find(name + "-heat");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, inRegion, topRegion};
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(region, req.drawx(), req.drawy());
        Draw.rect(topRegion, req.drawx(), req.drawy());
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        stats.add(Stat.inaccuracy, (int)inaccuracy, StatUnit.degrees);
        stats.add(Stat.reload, 60f / reloadTime, StatUnit.perSecond);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));

        if(acceptCoolant){
            stats.add(Stat.booster, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, true, l -> consumes.liquidfilters.get(l.id)));
        }
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    public class PayloadMissileTurretBuild extends PayloadBlockBuild<BuildPayload> implements Ranged, ControlBlock{
        public float reload;
        public float heat, logicControlTime = -1;
        public int shotCounter;
        public boolean logicShooting = false;
        public @Nullable Posc target;
        public Vec2 targetPos = new Vec2();
        public @Nullable BlockUnitc unit;
        public boolean wasShooting;

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
        public Unit unit(){
            if(unit == null){
                unit = (BlockUnitc)UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit)unit;
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                }
            }

            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            if(heat >= 0.001f && heatRegion.found()){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, x, y);
                Draw.blend();
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;

            wasShooting = false;

            heat = Mathf.lerpDelta(heat, 0f, cooldown);

            if(unit != null){
                unit.health(health);
                unit.rotation(angleTo(targetPos));
                unit.team(team);
                unit.set(x, y);
            }

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(hasAmmo()){
                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot = true;

                    if(isControlled()){ //player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                    }else if(logicControlled()){ //logic behavior
                        canShoot = logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);
                    }

                    if(canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }
            }else{
                moveInPayload(false);
            }

            if(acceptCoolant){
                updateCooling();
            }
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            if(acceptCoolant && liquids.currentAmount() <= 0.001f){
                Events.fire(Trigger.turretCool);
            }

            super.handleLiquid(source, liquid, amount);
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        protected void findTarget(){
            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, unitSort);
            }
        }

        /** @return the ammo type that will be returned if useAmmo is called. */
        public BulletType peekAmmo(){
            return ammoTypes.get(payload.block());
        }

        /** @return  whether the turret has ammo. */
        public boolean hasAmmo(){
            return payload != null && hasArrived();
        }

        protected void updateShooting(){
            reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();

            if(reload >= reloadTime){
                BulletType type = peekAmmo();

                shoot(type);

                reload %= reloadTime;
            }
        }

        protected void shoot(BulletType type){
            bullet(type);
            shotCounter++;
            heat = 1f;
            effects();
            if(!cheating()) payload = null;
        }

        protected void bullet(BulletType type){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x, y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            float angle = angleTo(targetPos) + Mathf.range(inaccuracy + type.inaccuracy);
            type.create(this, team, x, y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        protected void effects(){
            shootSound.at(x, y, Mathf.random(0.9f, 1.1f));

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }
        }

        protected void updateCooling(){
            if(reload < reloadTime){
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
                Liquid liquid = liquids.current();

                float used = Math.min(liquids.get(liquid), maxUsed * Time.delta) * baseReloadSpeed();
                reload += used * liquid.heatCapacity * coolantMultiplier;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        protected float baseReloadSpeed(){
            return efficiency();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.payload == null && payload instanceof BuildPayload b && ammoTypes.containsKey(b.block());
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public boolean isControlled(){
            return unit().isPlayer();
        }

        @Override
        public boolean canControl(){
            return true;
        }

        @Override
        public boolean shouldAutoTarget(){
            return true;
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, team.color);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(reload);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            reload = read.f();
        }
    }
}