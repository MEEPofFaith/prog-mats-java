package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
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

import static mindustry.Vars.tilesize;

@SuppressWarnings("unchecked")
public class BeamModule extends PowerTurret{
    public float firingMoveFract = 0f;
    public float shootDuration = 100f;

    public ModuleSize moduleSize = ModuleSize.small;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();


    public BeamModule(String name){
        super(name);
        update = false;
        destructible = true;
        breakable = rebuildable = false;
        group = BlockGroup.turrets;

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

    public class BeamModuleBuild extends PowerTurretBuild implements TurretModule{
        public ModuleModule module;
        public Seq<BulletEntry> bullets = new Seq<>();

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            module = new ModuleModule(self(), hasPower);
            return self();
        }

        @Override
        public Unit unit(){
            if(parent() != null){
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

            bullets.removeAll(b -> !b.bullet.isAdded() || b.bullet.type == null || b.life <= 0f || b.bullet.owner != this);
            if(bullets.any()){
                for(var entry : bullets){
                    float
                        bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
                        bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
                        angle = rotation + entry.rotation;

                    entry.bullet.rotation(angle);
                    entry.bullet.set(bulletX, bulletY);
                    entry.bullet.time = Math.min(entry.bullet.time + Time.delta, entry.bullet.type.lifetime * entry.bullet.type.optimalLifeFract);
                    entry.bullet.keepAlive = true;
                    entry.life -= Time.delta / Math.max(efficiency, 0.00001f);
                }

                wasShooting = true;
                heat = 1f;
                curRecoil = 1f;
            }
        }

        @Override
        public void drawSelect(){
            if(!isModule()) return;
            super.drawSelect();
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
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta() * (bullets.any() ? firingMoveFract : 1f));
        }

        @Override
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                bullets.add(new BulletEntry(bullet, offsetX, offsetY, angleOffset, shootDuration));
            }
        }

        @Override
        public float activeSoundVolume(){
            return 1f;
        }

        @Override
        public boolean shouldActiveSound(){
            return bullets.any();
        }

        @Override
        public boolean shouldConsume(){
            //still consumes power when bullet is around
            return bullets.any() || isActive() || isShooting();
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
            reloadCounter = 0f;
            rotation = 90f;
        }

        @Override
        public boolean isValid(){
            return super.isValid() || (parent() != null && parent().isValid());
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
