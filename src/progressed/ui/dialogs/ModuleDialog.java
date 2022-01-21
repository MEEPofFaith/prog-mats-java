package progressed.ui.dialogs;

import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import progressed.ui.*;
import progressed.world.blocks.defence.turret.multi.ModularTurret.*;
import progressed.world.blocks.defence.turret.multi.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.multi.mounts.*;

public class ModuleDialog extends BaseDialog{
    public static Point3 output = new Point3();

    public int selFirst = -1;
    public ModuleSize selSize;
    protected ModularTurretBuild base;

    public ModuleDialog(){
        super("@pm-module-dialog");

        addCloseButton();

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
            cont.label(mSize::fullTitle).right();
            cont.table(b -> {
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

                    BaseMount mount = base.allMounts.find(m -> m.checkSize(mSize) && m.checkNumber(ii));

                    if(mount != null){
                        button.get().getStyle().imageUp = new TextureRegionDrawable(mount.module.region);
                        button.tooltip(mount.module.localizedName + " (" + (mount.mountNumber + 1) + ")");
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
            base.configure(output.set(selSize.ordinal(), selFirst, sel));
            setUp();
        }
    }

    private void deselect(){
        selFirst = -1;
    }
}