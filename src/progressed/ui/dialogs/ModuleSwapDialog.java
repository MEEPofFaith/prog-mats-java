package progressed.ui.dialogs;

import arc.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;
import progressed.ui.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class ModuleSwapDialog extends BaseDialog{
    public int selFirst = -1;
    public ModuleSize selSize;
    protected ModularTurretBuild base;

    public ModuleSwapDialog(){
        super("@pm-swap.title");

        buttons.defaults().size(210f, 64f);
        buttons.button("@cancel", Icon.cancel, () -> {
            hide();
            base.resetSwap();
        });
        buttons.button("@confirm", Icon.flipX, () -> {
            hide();
            
            for(int i = 0; i < base.allMounts.size; i++){
                base.configure(Point2.pack(i, base.allMounts.get(i).mountNumber));
            }
            base.configure(true);
        });

        closeOnBack(() -> {
            base.resetSwap();
        });

        shown(this::setUp);
    }

    public Dialog show(ModularTurretBuild base){
        this.base = base;
        return show();
    }

    private void setUp(){
        cont.clear();
        selSize = ModuleSize.small;
        deselect();

        for(ModuleSize mSize : ModuleSize.values()){
            if(base.getMountPos(mSize) == null) continue;
            cont.label(mSize::fullTitle).top().right();
            cont.table(b -> {
                int c = 0;
                for(int i = 0; i < base.getMaxMounts(mSize); i++){
                    int ii = i;
                    Cell<ImageButton> button = b.button(Tex.clear, PMStyles.squarei, 48f, () -> {
                        if(selSize != mSize) deselect();
                        selSize = mSize;
                        if(selFirst != ii){
                            select(ii);
                        }else{
                            deselect();
                        }
                    }).update(ib -> {
                        ib.setChecked(selSize == mSize && selFirst == ii);
                    }).size(64f).left();

                    BaseMount mount = base.allMounts.find(m -> m.checkSize(mSize) && m.checkSwap(ii));
                    String num = " (" + (i + 1) + ")";

                    if(mount != null){
                        button.get().getStyle().imageUp = new TextureRegionDrawable(mount.module.region);
                        button.tooltip(mount.module.localizedName + num);
                    }else{
                        button.get().clearChildren();
                        button.get().label(() -> String.valueOf(ii + 1));
                        button.tooltip(Core.bundle.get("empty") + num);
                    }

                    if(c++ % 4 == 3){
                        b.row();
                    }
                }
            }).left().padLeft(16f);
            cont.row();
        }
    }

    private void select(int sel){
        if(selFirst < 0){
            selFirst = sel;
        }else{
            BaseMount m1 = base.allMounts.find(m -> m.checkSize(selSize) && m.checkSwap(selFirst)),
                m2 = base.allMounts.find(m -> m.checkSize(selSize) && m.checkSwap(sel));

            if(m1 != null) m1.swap(sel);
            if(m2 != null) m2.swap(selFirst);

            setUp();
        }
    }

    private void deselect(){
        selFirst = -1;
    }
}