package progressed.world.blocks.defence.turret.energy;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
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
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.entities.units.*;
import progressed.graphics.*;
import progressed.type.unit.*;

import java.util.*;

public class SwordTurret extends BaseTurret{
    //after being logic-controlled and this amount of time passes, the turret will resume normal AI
    public final static float logicControlCooldown = 60 * 2;

    public final int timerTarget = timers++;
    /** Ticks between attempt at finding a target. */
    public float targetInterval = 20;

    public SwordUnitType swordType = (SwordUnitType)PMUnitTypes.danceSword;
    public int maxSwords = 1;
    public float buildTime = 60f * 5f;
    public float buildPowerUse, attackPowerUse;
    public float buildY = Float.NEGATIVE_INFINITY, buildWaveOffset = 0.1f;
    /** Visual elevation of turret shadow, -1 to use defaults. */
    public float elevation = -1f;
    public Color swordColor;
    /** Function for choosing which unit to target. */
    public Sortf unitSort = UnitSorts.closest;
    /** Filter for types of units to attack. */
    public Boolf<Unit> unitFilter = u -> true;
    /** Filter for types of buildings to attack. */
    public Boolf<Building> buildingFilter = b -> !b.block.underBullets;

    public TextureRegion baseRegion;

    public SwordTurret(String name){
        super(name);

        outlinedIcon = 1;
        consumePowerDynamic(SwordTurretBuild::powerUse);
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base", "block-" + size);

        if(!baseRegion.found() && minfo.mod != null) baseRegion = Core.atlas.find(minfo.mod.name + "-block-" + size);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.weapons, table -> {
            table.row();
            table.image(swordType.fullIcon).padRight(4).right().top();
            table.add("[lightgray]x" + maxSwords).padRight(10).left().bottom();
            table.table(s -> {
                if(swordType.damage > 0){
                    s.add(Core.bundle.format("bullet.damage", swordType.damage));
                }
                s.row();
                s.add("@bullet.infinitepierce");
                if(swordType.status != StatusEffects.none){
                    s.row();
                    s.add((swordType.status.minfo.mod == null ? swordType.status.emoji() : "") + "[stat]" + swordType.status.localizedName);
                }
            }).left().top().get().background(Tex.underline);
        });
    }

    @Override
    public void init(){
        super.init();

        if(swordType.orbitRadius == -1){
            swordType.orbitRadius = size * Vars.tilesize;
            if(buildY == Float.NEGATIVE_INFINITY) buildY = swordType.orbitRadius;
        }

        if(buildY == Float.NEGATIVE_INFINITY) buildY = size * Vars.tilesize / 2f;
        if(elevation < 0) elevation = size / 2f;
    }

    public class SwordTurretBuild extends BaseTurretBuild implements ControlBlock{
        public IntSeq readUnitIds = new IntSeq(maxSwords);
        public Seq<Unit> swords = new Seq<>(maxSwords);
        public float buildProgress, totalProgress;
        public float logicControlTime = -1;
        public boolean logicShooting;
        public Posc target;
        public Vec2 targetPos = new Vec2();
        public BlockUnitc unit = (BlockUnitc)UnitTypes.block.create(team);
        public boolean wasShooting;

        @Override
        public Unit unit(){
            //make sure stats are correct
            unit.tile(this);
            unit.team(team);
            return (Unit)unit;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.shoot && !unit.isPlayer()){
                targetPos.set(World.unconv((float)p1), World.unconv((float)p2)).sub(this).limit(range).add(this);
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

                if(p1 instanceof Posc pos){
                    targetPosition(pos);
                }
            }

            super.control(type, p1, p2, p3, p4);
        }

        public boolean isAttacking(){
            return (isControlled() ? unit.isShooting() : logicControlled() ? logicShooting : target != null);
        }

        public boolean logicControlled(){
            return logicControlTime > 0;
        }

        public boolean isActive(){
            return isAttacking() && enabled && canConsume();
        }

        public void targetPosition(Posc pos){
            if(pos == null) return;

            targetPos.set(pos).sub(this).limit(range).add(this);

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        public float drawrot(){
            return rotation - 90f;
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret - 0.02f);
            Drawf.shadow(region, x - elevation, y - elevation, drawrot());

            Draw.z(Layer.turret);
            Draw.rect(region, x, y, drawrot());

            if(swordCount() < maxSwords && buildProgress > 0.001f){
                float
                    swordX = x + Angles.trnsx(rotation, buildY),
                    swordY = y + Angles.trnsy(rotation, buildY),
                    z = Layer.flyingUnitLow - 1f;

                Draw.draw(Math.min(Layer.darkness, z - 1f), () -> PMDrawf.materialize(
                    swordX + UnitType.shadowTX, swordY + UnitType.shadowTY,
                    swordType.fullIcon, swordColor == null ? team.color : swordColor,
                    drawrot(), buildWaveOffset, buildProgress, true
                ));

                Draw.draw(z, () -> PMDrawf.materialize(
                    swordX, swordY,
                    swordType.fullIcon, swordColor == null ? team.color : swordColor,
                    drawrot(), buildWaveOffset, buildProgress
                ));
            }
        }

        @Override
        public void updateTile(){
            //Sword construction stuff
            for(int i = 0; i < Math.min(readUnitIds.size, maxSwords); i++){
                int id = readUnitIds.get(i);
                if(id != -1){
                    Unit u = Groups.unit.getByID(id);
                    if(u instanceof SwordUnit st){
                        st.orbitPos(i);
                    }
                    swords.add(u);
                    readUnitIds.set(i, -1);
                }
            }

            if(shouldConsume() && swordCount() < maxSwords){
                buildProgress += edelta() / buildTime;
                totalProgress += edelta();

                if(buildProgress >= 1f){
                    Unit u = swordType.create(team);
                    if(u instanceof BuildingTetherc bt){
                        bt.building(this);
                    }
                    if(u instanceof SwordUnit st){
                        st.orbitPos(swordCount());
                    }
                    float
                        spawnX = x + Angles.trnsx(rotation, buildY),
                        spawnY = y + Angles.trnsy(rotation, buildY);
                    u.set(spawnX, spawnY);
                    u.rotation = rotation;
                    u.add();

                    swords.add(u);

                    Fx.spawn.at(spawnX, spawnY);
                    buildProgress = 0f;
                }
            }

            //Turret stuff
            if(!validateTarget()) target = null;

            unit.tile(this);
            unit.rotation(rotation);
            unit.team(team);

            if(logicControlTime > 0f){
                logicControlTime -= Time.delta;
            }

            wasShooting = false;

            if(swordCount() > 0){
                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    wasShooting = true;

                    if(isControlled()){ //player control
                        targetPos.set(unit.aimX(), unit.aimY()).sub(this).limit(range).add(this);
                        wasShooting = unit.isShooting();
                    }else if(logicControlled()){ //logic control
                        wasShooting = logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);

                        if(Float.isNaN(rotation)) rotation = 0;
                    }

                    turnToTarget(angleTo(targetPos));
                }
            }
        }

        public int swordCount(){
            return swords.size;
        }

        protected boolean validateTarget(){
            return !Units.invalidateTarget(target, team, x, y) || isControlled() || logicControlled();
        }

        public void findTarget(){
            UnitType u = swordType;
            if(u.targetAir && !u.targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded() && unitFilter.get(e), unitSort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && unitFilter.get(e) && (e.isGrounded() || u.targetAir) && (!e.isGrounded() || u.targetGround), b -> u.targetGround && buildingFilter.get(b), unitSort);
            }
        }

        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * potentialEfficiency);
        }

        @Override
        public boolean shouldConsume(){
            return swordCount() < maxSwords || isAttacking();
        }

        @Override
        public float progress(){
            return buildProgress;
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        public float powerUse(){
            return (swordCount() < maxSwords ? buildPowerUse : 0) + (isAttacking() ? attackPowerUse : 0);
        }

        @Override
        public void created(){
            super.created();

            Arrays.fill(readUnitIds.items, -1);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(rotation);
            write.f(buildProgress);
            write.i(swords.size);
            swords.each(u -> write.i(u == null ? -1 : u.id));
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                rotation = read.f();
                buildProgress = read.f();
                int size = read.i();
                for(int i = 0; i < size; i++){
                    readUnitIds.add(read.i());
                }
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}
