package progressed.world.blocks.defence.turret.payload.modular.modules;

import arc.func.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.draw.*;
import progressed.world.module.*;
import progressed.world.module.ModuleModule.*;

@SuppressWarnings("unchecked")
public class PowerTurretModule extends PowerTurret{
    public ModuleSize moduleSize = ModuleSize.small;

    OrderedMap<String, Func<Building, Bar>> moduleBarMap = new OrderedMap<>();

    public PowerTurretModule(String name){
        super(name);
        update = false;
        destructible = true;
        breakable = rebuildable = false;
        group = BlockGroup.turrets;

        drawer = new DrawTurretModule();
    }

    @Override
    public void setBars(){
        super.setBars();

        moduleBarMap.putAll(barMap);
        moduleBarMap.remove("health");
        addModuleBar("ammo", (PowerTurretModuleBuild entity) ->
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

    public class PowerTurretModuleBuild extends PowerTurretBuild implements TurretModule{
        public ModuleModule module;

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
