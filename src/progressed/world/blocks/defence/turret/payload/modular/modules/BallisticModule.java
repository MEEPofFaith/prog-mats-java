package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.entities.*;
import progressed.entities.bullet.explosive.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.draw.*;
import progressed.world.meta.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class BallisticModule extends ItemTurret{
    public ModuleSize moduleSize = ModuleSize.small;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    public BallisticModule(String name){
        super(name);

        consumeAmmoOnce = false;
        recoil = 0f;
        shootY = 0f;
        shootEffect = smokeEffect = Fx.none;

        outlinedIcon = 0;
        drawer = new DrawTurretModule();
    }

    @Override
    public void limitRange(float margin){
        for(var entry: ammoTypes.copy().entries()){
            limitRange(entry.value, margin);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
    }

    @Override
    public void limitRange(BulletType bullet, float margin){
        bullet.speed = range + bullet.rangeChange + margin;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
        addModuleBar("ammo", (BallisticModuleBuild entity) ->
            new Bar(
                "stat.ammo",
                Pal.ammo,
                () -> (float)entity.totalAmmo / maxAmmo
            )
        );
    }

    public <T extends Building> void addModuleBar(String name, Func<T, Bar> sup){
        moduleBarMap.put(name, (Func<Building, Bar>)sup);
    }

    public class BallisticModuleBuild extends ItemTurretBuild implements TurretModule{
        public ModuleModule module;
        protected Seq<Posc> targets = new Seq<>();

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        @Override
        public Unit unit(){
            if(parent() != null) return (Unit)(unit = parent().unit);

            return super.unit();
        }

        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            BulletType bullet = peekAmmo();

            var offset = Tmp.v1.setZero();

            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(shoot.firstShotDelay / Time.delta);
            }

            if(bullet instanceof BallisticMissileBulletType){
                targetPos.set(pos).add(offset);
                if(pos instanceof Hitboxc h){
                    offset.set(h.deltaX(), h.deltaY()).scl(bullet.lifetime);
                    targetPos.add(offset);
                }
            }else{
                targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
            }

            if(targetPos.isZero()){
                targetPos.set(pos);
            }
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;

            super.updateTile();
        }

        @Override
        protected void updateReload(){
            if(queuedBullets > 0) return;

            super.updateReload();
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;

            super.drawSelect();

            Drawf.dashCircle(x, y, minRange, Pal.accentBack);
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            if(!isControlled() && !logicControlled()){
                targets.clear();
                PMDamage.allNearbyEnemies(team, x, y, range, h -> {
                    if(h instanceof Unit u){
                        if(!u.dead() && unitFilter.get(u) && (u.isGrounded() || targetAir) && (!u.isGrounded() || targetGround) && dst(u) >= minRange) targets.add(u);
                    }else if(h instanceof Building b){
                        if(targetGround && buildingFilter.get(b) && dst(b) >= minRange) targets.add(b);
                    }
                });

                if(!targets.isEmpty()){
                    target = targets.random();
                    targetPosition(target);
                }
            }
            turnToTarget(angleTo(targetPos));

            super.bullet(type, xOffset, yOffset, angleOffset, mover);
        }

        @Override
        public float drawrot(){
            return 0;
        }

        @Override
        public ModuleModule module(){
            return module;
        }

        @Override
        public Building build(){
            return self();
        }

        @Override
        public ModuleSize size(){
            return moduleSize;
        }

        @Override
        public Iterable<Func<Building, Bar>> listModuleBars(){
            return moduleBarMap.values();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            module.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            (module == null ? new ModuleModule(self(), hasPower) : module).read(read);
        }

    }
}
