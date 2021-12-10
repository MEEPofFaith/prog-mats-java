package progressed.world.blocks.defence;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class EffectZone extends Block{
    public float reload = 60f;
    public float range = 10f * 8f;

    public Color ringColor = Pal.lancerLaser,
        baseColor,
        topColor;
    public float height = 0.25f;
    public float zoneLayer = Layer.blockOver + 1f, ringLayer = Layer.flyingUnit + 1f;

    public Cons<EffectZoneBuild> zoneEffect = tile -> {};
    public Cons<EffectZoneBuild> zoneDraw = tile -> {};

    protected Seq<Unit> all = new Seq<>();

    public EffectZone(String name){
        super(name);

        solid = true;
        update = true;
        group = BlockGroup.projectors;
        emitLight = true;
        lightRadius = 50f;
        envEnabled |= Env.space;
    }

    @Override
    public void init(){
        super.init();

        if(baseColor == null) baseColor = ringColor;
        if(topColor == null) topColor = baseColor.cpy().a(0f);

        clipSize = Math.max(clipSize, (range + 4f) * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, ringColor);
    }

    public class EffectZoneBuild extends Building{
        public float heat, activeHeat;
        public float charge = Mathf.random(reload);
        public float smoothEfficiency;
        public boolean active;

        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency(), 0.08f);
            heat = Mathf.lerpDelta(heat, Mathf.num(consValid()), 0.08f);
            activeHeat = Mathf.lerpDelta(activeHeat, Mathf.num(active), 0.08f);
            charge += heat * Time.delta;

            if(charge >= reload){
                charge = 0f;

                all.clear();
                Units.nearby(null, x, y, range, other -> {
                    all.add(other);
                });
                active = all.any();

                zoneEffect.get(this);
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(smoothEfficiency < 0.01f) return;

            Draw.z(zoneLayer);
            zoneDraw.get(this);

            Draw.z(ringLayer);
            Tmp.c1.set(baseColor).a(baseColor.a * smoothEfficiency);
            Tmp.c2.set(topColor).a(topColor.a * smoothEfficiency);

            Draw3D.cylinder(x, y, range, height * smoothEfficiency, Tmp.c1, Tmp.c2);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            charge = read.f();
            heat = read.f();
        }
    }
}