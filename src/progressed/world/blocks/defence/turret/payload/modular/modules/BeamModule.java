package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.content.blocks.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.draw.*;
import progressed.world.meta.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class BeamModule extends ContinuousLiquidTurret{
    public ModuleSize moduleSize = ModuleSize.small;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();


    public BeamModule(String name){
        super(name);
        update = false;
        destructible = true;
        breakable = rebuildable = false;
        group = BlockGroup.turrets;
        connectedPower = false;
        shootCone = 1f;

        drawer = new DrawTurretModule();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
    }

    @Override
    public void init(){
        super.init();
        PMModules.setClip(clipSize);
        fogRadius = -1;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
        removeBar("power");
        removeBar("heat");
    }

    public <T extends Building> void addModuleBar(String name, Func<T, Bar> sup){
        moduleBarMap.put(name, (Func<Building, Bar>)sup);
    }

    @Override
    public boolean canBreak(Tile tile){
        return state.isEditor() || state.rules.infiniteResources;
    }

    public class BeamModuleBuild extends ContinuousLiquidTurretBuild implements TurretModule{
        public ModuleModule module;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        @Override
        public Unit unit(){
            if(parent() != null && canControl()){
                unit = (BlockUnitc)parent().unit();
                return (Unit)unit;
            }

            return super.unit();
        }

        @Override
        public boolean canControl(){
            return super.canControl() && isDeployed();
        }

        @Override
        public void updateTile(){
            if(!isDeployed()) return;
            super.updateTile();
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;
            super.drawSelect();
        }

        @Override
        public boolean isShooting(){
            return (isControlled() ? unit.isShooting() : logicControlled() ? logicShooting : target != null && wasShooting);
        }

        @Override
        protected void updateCooling(){
            if(!bullets.any()) super.updateCooling();
        }

        @Override
        protected void updateReload(){
            if(!bullets.any()) super.updateReload();
        }

        @Override
        protected void updateShooting(){
            if(!bullets.any()) super.updateShooting();
        }

        @Override
        protected void updateBullet(BulletEntry entry){
            float
                bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
                angle = rotation + entry.rotation;

            entry.bullet.rotation(angle);
            entry.bullet.set(bulletX, bulletY);

            //target length of laser
            float shootLength = Math.min(dst(targetPos), range);
            //current length of laser
            float curLength = dst(entry.bullet.aimX, entry.bullet.aimY);
            //resulting length of the bullet (smoothed)
            float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
            //actual aim end point based on length
            Tmp.v1.trns(rotation, lastLength = resultLength).add(x, y);

            entry.bullet.aimX = Tmp.v1.x;
            entry.bullet.aimY = Tmp.v1.y;

            if(isShooting() && hasAmmo()){
                entry.bullet.time = Mathf.approachDelta(entry.bullet.time, entry.bullet.lifetime * entry.bullet.type.optimalLifeFract, 1f);
                entry.bullet.keepAlive = true;
            }
        }

        protected void turnToTarget(float targetRot){
            if(bullets.any()) return;
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * Mathf.num(hasAmmo()));
        }

        @Override
        public void moduleRemoved(){
            unit = (BlockUnitc)UnitTypes.block.create(team);
        }

        @Override
        public ModuleModule module(){
            return module;
        }

        @Override
        public ModuleSize size(){
            return moduleSize;
        }

        @Override
        public Building build(){
            return self();
        }

        @Override
        public Iterable<Func<Building, Bar>> listModuleBars(){
            return moduleBarMap.values();
        }

        @Override
        public void pickedUp(){
            module.progress = 0f;
            rotation = 90f;
        }

        @Override
        public boolean isValid(){
            return super.isValid() || (parent() != null && parent().isValid());
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return super.acceptLiquid(source, liquid) && (liquids.current() == null || liquids.currentAmount() < liquidCapacity);
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
