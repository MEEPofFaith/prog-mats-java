package progressed.world.blocks.sandbox.power;

import arc.*;
import arc.flabel.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;
import progressed.util.*;

public class StrobeNode extends PowerNode{
    public float lerpSpeed = 0.005f;
    public Color laserColor3 = Color.red;

    public TextureRegion colorRegion;

    public StrobeNode(String name){
        super(name);
        requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        laserRange = 200;
        maxNodes = 65535;
        laserColor1 = Color.valueOf("ffcccc");
        laserColor2 = Color.valueOf("fb6767");
    }

    @Override
    public void load(){
        super.load();

        colorRegion = Core.atlas.find(name + "-strobe");
        laser = Core.atlas.find("prog-mats-rainbow-laser");
        laserEnd = Core.atlas.find("prog-mats-rainbow-laser-end");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.powerRange);
        stats.remove(Stat.powerConnections);

        stats.add(Stat.powerRange, t -> t.add(new FLabel("{wave}{rainbow}" + PMUtls.stringsFixed(laserRange) + " " + StatUnit.blocks.localized())));
        stats.add(Stat.powerConnections, t -> t.add(new FLabel("{wave}{rainbow}" + PMUtls.stringsFixed(maxNodes))));
    }

    @Override
    protected void setupColor(float satisfaction){
        float speed = Core.settings.getInt("pm-strobespeed") / 2f;

        Color c1 = Tmp.c1.set(laserColor1).lerp(laserColor3, Mathf.absin(Time.time * lerpSpeed, 1, 1));
        Draw.color(c1.shiftHue(Time.time * speed), Tmp.c1.set(laserColor2).shiftHue(Time.time * speed), 1 - satisfaction);
        Draw.alpha(Renderer.laserOpacity);
    }

    public class StrobeNodeBuild extends PowerNodeBuild{
        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.block);
            float speed = Core.settings.getInt("pm-strobespeed") / 2f;
            Color c1 = Tmp.c1.set(laserColor1).lerp(laserColor3, Mathf.absin(Time.time * lerpSpeed, 1, 1));
            Draw.color(c1.shiftHue(Time.time * speed), Tmp.c1.set(laserColor2).shiftHue(Time.time * speed), 1 - this.power.graph.getSatisfaction());
            Draw.alpha(1);
            Draw.rect(colorRegion, this.x, this.y);
            Draw.reset();
        }
    }
}
