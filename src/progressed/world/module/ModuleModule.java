package progressed.world.module;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.modules.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.meta.*;

import java.util.*;

import static mindustry.Vars.*;

public class ModuleModule extends BlockModule{
    public TurretModule module;
    public ModularTurretBuild parent;
    public float progress = 0, hAlpha;
    public short mountNumber, swapNumber;
    public boolean highlight, hasPower;

    public ModuleModule(TurretModule module, boolean hasPower){
        this.module = module;
        this.hasPower = hasPower;
    }

    public void moduleAdded(ModularTurretBuild parent, short pos){
        this.parent = parent;
        mountNumber = swapNumber = pos;
        if(hasPower) parent.power.graph.add(module.build());
    }

    public void highlight(){
        if(hAlpha > 0.001f){
            Draw.mixcol(parent.team.color, Mathf.absin(7f, 1f) * hAlpha);
            Draw.rect(module.block().fullIcon, module.x(), module.y(), module.build().drawrot());
            Draw.mixcol();
        }
    }

    public void moduleUpdate(){
        if(hasPower && module.build().power.graph != parent.power.graph){
            module.build().power.graph.remove(module.build());
            parent.power.graph.add(module.build());
        }
        module.build().updateConsumption();
        hAlpha = Mathf.approachDelta(hAlpha, Mathf.num(highlight), 0.15f);

        if(module.deployTime() > 0){
            progress = Mathf.approachDelta(progress, 1f, 1f / module.deployTime());
        }else{
            progress = 1;
        }
    }

    public void moduleRemoved(){
        parent = null;
        highlight = false;
        hAlpha = 0f;
        if(hasPower) module.build().power.graph.remove(module.build());
    }

    public void moduleDisplay(Table table){
        table.table(t -> {
            t.left();
            t.image(module.icon()).scaling(Scaling.fit).size(5f * 8f);
            t.label(() -> module.name() + " (" + (mountNumber + 1) + ")").left().growX().padLeft(5);
            PMStatValues.infoButton(t, module.block(), 5f * 8f).left().padLeft(5f);
            t.button(new TextureRegionDrawable(Icon.upload),2.5f * 8f, () -> {
                tryPickUp(parent);
            }).size(5f * 8f).left().padLeft(5f).tooltip("@pm-pickup.label");
        }).growX().top();

        table.row();

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(4f);

            displayModuleBars(bars);
        }).growX().top();
    }

    public void displayModuleBars(Table table){
        if(module.listModuleBars() == null) return;
        for(Func<Building, Bar> bar : module.listModuleBars()){
            var result = bar.get(module.build());
            if(result == null) continue;
            table.add(result).growX();
            table.row();
        }
    }

    public void tryPickUp(ModularTurretBuild parent){
        if(canPickUp()){ //jeez this is a mess
            Payloadc p = (Payloadc)player.unit();
            BuildPayload modPay = new BuildPayload(module.build());
            p.addPayload(modPay);
            Fx.unitPickup.at(module.x(), module.y());
            Events.fire(new PickupEvent(player.unit(), modPay.build));
            boolean has = parent.modules.contains(m -> m.checkSize(module.size()));
            parent.removeMount(module);
            parent.setSelection(module.size());
            parent.rebuild(true, !has, has);
        }else{
            showPickupFail();
        }
    }

    public boolean canPickUp(){
        Unit u = player.unit();
        if(!(u instanceof Payloadc p)) return false;

        return p.payloadUsed() + module.block().size * module.block().size * tilePayload <= u.type.payloadCapacity + 0.01f
            && u.within(module.x(), module.y(), tilesize * module.block().size * 1.2f);
    }

    public void showPickupFail(){
        Unit u = player.unit();
        if(!(u instanceof Payloadc p)){
            ui.showInfoToast("@pm-pickup.invalid", 2f);
        }else{
            if(!player.unit().within(module.x(), module.y(), module.block().size * tilesize * 1.2f)){
                ui.showInfoToast("@pm-pickup.toofar", 2f);
            }else if(p.payloadUsed() + module.block().size * module.block().size * tilePayload > u.type.payloadCapacity + 0.01f){
                ui.showInfoToast("@pm-pickup.full", 2f);
            }
        }
    }

    public void updatePos(ModularTurretBuild parent){
        this.parent = parent;
        if(mountNumber != swapNumber){
            mountNumber = swapNumber;
            progress = 0;
        }

        Vec2 offset = parent.getMountPos(module.size())[mountNumber];
        module.build().x = parent.x + offset.x;
        module.build().y = parent.y + offset.y;
    }

    @Override
    public void write(Writes write){
        write.s(mountNumber);
        write.f(progress);
    }

    @Override
    public void read(Reads read){
        mountNumber = swapNumber = read.s();
        progress = read.f();
    }

    /** Modular Turrets have mounts of 3 sizes. */
    public enum ModuleSize{
        small, medium, large;

        public String title(){
            return Core.bundle.get("pm-size." + name().toLowerCase(Locale.ROOT) + ".short");
        }
        public String fullTitle(){
            return Core.bundle.get("pm-size." + name().toLowerCase(Locale.ROOT) + ".full");
        }
        public String amount(int amount){
            return Core.bundle.format("pm-size." + name().toLowerCase(Locale.ROOT) + ".amount", amount);
        }
    }
}
