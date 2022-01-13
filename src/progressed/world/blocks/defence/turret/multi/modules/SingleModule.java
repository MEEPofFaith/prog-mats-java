package progressed.world.blocks.defence.turret.multi.modules;

public class SingleModule extends BaseModule{
    public SingleModule(String name, ModuleSize size){
        super(name, size);
    }

    public SingleModule(String name){
        super(name);
    }

    @Override
    public boolean acceptModule(BaseModule module){
        return module != this;
    }
}