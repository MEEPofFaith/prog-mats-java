package progressed.world.blocks.sandbox.units;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.gen.entities.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class TargetDummyBase extends Block{
    public final int DPSUpdateTime = timers++;

    public float resetTime = 120f;
    public UnitType unitType = PMUnitTypes.targetDummy;
    public float pullScale = 0.33f;
    public TextureRegion tether, tetherEnd;

    public TargetDummyBase(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        update = alwaysUpdateInUnits = true;
        configurable = logicConfigurable = saveConfig = true;
        underBullets = true;
        targetable = false;

        config(Boolean.class, (TargetDummyBaseBuild tile, Boolean b) -> tile.boosting = b);
        config(Integer.class, (TargetDummyBaseBuild tile, Integer i) -> tile.unitTeam = Team.get(i));
        config(Float.class, (TargetDummyBaseBuild tile, Float f) -> tile.unitArmor = f);
        config(int[].class, (TargetDummyBaseBuild tile, int[] config) -> {
            tile.unitTeam = tile.team; //set default to config off of
            if(config[0] == 1) tile.unitTeam = Team.get(tile.dummyTeam());
            tile.boosting = config[1] == 1;
            tile.unitArmor = Float.intBitsToFloat(config[2]);
        });
    }

    @Override
    public void load(){
        super.load();

        tether = Core.atlas.find(name + "-tether");
        tetherEnd = Core.atlas.find(name + "-tether-end");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{region, Core.atlas.find(unitType.name + "-full")};
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("health");

        addBar("pm-dps", (TargetDummyBaseBuild entity) -> new Bar(
            () -> entity.displayDPS(false),
            () -> Pal.ammo,
            () -> 1f - (entity.reset / resetTime)
        ));
    }

    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawDefaultPlanRegion(plan, list);

        Draw.rect(unitType.fullIcon, plan.drawx(), plan.drawy());
    }

    public class TargetDummyBaseBuild extends Building implements UnitTetherBlock{
        //needs to be "unboxed" after reading, since units are read after buildings.
        public int readUnitId = -1;
        public Unit unit;
        public float total, reset = resetTime, time, DPS;
        public boolean boosting;
        public float unitArmor;
        public Team unitTeam;

        @Override
        public void updateTile(){
            //unit was lost/destroyed somehow
            if(unit != null && (unit.dead || !unit.isAdded())){
                unit = null;
            }

            if(readUnitId != -1){
                unit = Groups.unit.getByID(readUnitId);
                if(unit != null || !net.client()){
                    readUnitId = -1;
                }
            }

            if(unitTeam == null) unitTeam = team;

            if(unit == null){
                if(!net.client()){
                    unit = unitType.create(team);
                    if(unit instanceof TargetDummyUnit td){
                        td.building(this);
                    }
                    unit.set(x, y);
                    unit.rotation = 90f;
                    unit.add();
                    Call.unitTetherBlockSpawned(tile, unit.id);
                }
            }

            if(unit != null){
                unit.updateBoosting(boosting);
                unit.armor(unitArmor);
                unit.team(unitTeam);

                //similar to impulseNet but does not factor in mass
                Tmp.v1.set(this).sub(unit).limit(dst(unit) * pullScale * Time.delta);
                unit.vel.add(Tmp.v1);

                //manually move units to simulate velocity for remote players
                if(unit.isRemote()) unit.move(Tmp.v1);

                if(unit.moving()) unit.lookAt(unit.vel().angle());
            }

            time += Time.delta;
            reset += Time.delta;

            if(timer(DPSUpdateTime, 20)) DPS = total / time * 60f;

            if(reset >= resetTime){
                total = 0f;
                time = 0f;
                DPS = 0f;
            }
        }

        public void spawned(int id){
            Fx.spawn.at(x, y);
            if(net.client()){
                readUnitId = id;
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(tether.found() && unit != null){
                float z = unit.elevation > 0.5f ? (unitType.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : unitType.groundLayer + Mathf.clamp(unitType.hitSize / 4000f, 0, 0.01f);
                Draw.z(z - 0.01f);
                Draw.color(team.color);
                Drawf.laser(tether, tetherEnd, x, y, unit.x, unit.y);
                Draw.color();
            }

            Draw.z(Layer.overlayUI);
            String text = displayDPS(true);
            PMDrawf.text(x, y, false, size * tilesize, team.color, text);
        }

        public String displayDPS(boolean round){
            if(time > 0){
                return (round ? (DPS > 0 ? PMUtls.round(DPS) : "---") : Strings.autoFixed(total / time * 60f, 2)) + " DPS";
            }else{
                return "--- DPS";
            }
        }

        @Override
        public boolean collide(Bullet other){ //Hit the unit, not the building
            return false;
        }

        @Override
        public boolean collision(Bullet other){ //Hit the unit, not the building
            return false;
        }

        public void dummyHit(float damage){
            reset = 0f;
            total += damage ;
        }

        @Override
        public void damage(float damage){
            //just in case
        }

        @Override
        public void kill(){
            //just in case
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                t.background(Styles.black6);
                t.defaults().left();

                t.check("@pm-target-dummy.enemy", unitTeam != team, b -> configure(dummyTeam()));
                t.row();
                t.check("@stat.flying", boosting, this::configure);
                t.row();
                t.table(a -> {
                    a.add(Core.bundle.get("stat.armor") + ":");
                    a.field("" + unitArmor, TextFieldFilter.floatsOnly, s -> configure(Strings.parseFloat(s))).width(200f).padLeft(8f);
                });
            }).top().grow().margin(8f);
        }

        public int dummyTeam(){
            if(unitTeam != team) return team.id; //Return to own team

            if(team == state.rules.defaultTeam) return state.rules.waveTeam.id; //Set to wave team if player team
            if(team == state.rules.waveTeam) return state.rules.defaultTeam.id; //Set to player team if wave team
            if(team != Team.crux) return Team.crux.id; //Set to crux if not crux
            return Team.sharded.id; //Set to sharded if crux
        }

        @Override
        public Object config(){
            return new int[]{Mathf.num(unitTeam != team), Mathf.num(boosting), Float.floatToIntBits(unitArmor)};
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(unit == null ? -1 : unit.id);
            write.bool(boosting);
            write.f(unitArmor);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            readUnitId = read.i();
            boosting = read.bool();
            unitArmor = read.f();
        }
    }
}
