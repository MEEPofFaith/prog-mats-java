package progressed.world.blocks.sandbox;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.world.blocks.distribution.*;

public class SandDriver extends BurstDriver{
    public TextureRegion baseRainbow, rainbow;

    public SandDriver(String name){
        super(name);
        requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.empty);
    }

    @Override
    public void load(){
        super.load();

        baseRainbow = Core.atlas.find(name + "-base-rainbow");
        rainbow = Core.atlas.find(name + "-rainbow");
    }

    public class SandDriverBuild extends BurstDriverBuild{
        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            float speed = Core.settings.getInt("pm-strobespeed") / 2f;

            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed));
            Draw.rect(baseRainbow, x, y);
            Draw.color();

            Draw.z(Layer.turret);

            tr2.trns(rotation, -recoil);

            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90);
            Draw.rect(region, x + tr2.x, y + tr2.y, rotation - 90);


            Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed));
            Draw.rect(rainbow, x + tr2.x, y + tr2.y, rotation - 90);
            Draw.color();
        }

        @Override
        public boolean sandy(){
            return true;
        }
    }
}