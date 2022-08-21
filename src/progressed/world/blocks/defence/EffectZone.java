package progressed.world.blocks.defence;

import arc.*;
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
import progressed.entities.units.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class EffectZone extends Block{
    protected static final Seq<Unit> all = new Seq<>();

    public float reload = 20f;
    public float range = 10f * 8f;
    public boolean affectEnemyTeam, affectOwnTeam = true;

    public Color
        baseColor = Pal.lancerLaser,
        topColor;
    public float height = 0.25f;
    public float zoneLayer = -1f, ringLayer = Layer.flyingUnit + 0.5f;

    public Cons<EffectZoneBuild> zoneEffect = tile -> {};

    public Boolp activate = all::any;

    public TextureRegion topRegion;

    public EffectZone(String name){
        super(name);

        solid = true;
        update = true;
        group = BlockGroup.projectors;
        canOverdrive = false;
        emitLight = true;
        lightRadius = -1f;
        envEnabled |= Env.space;
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void init(){
        if(lightRadius < 0) lightRadius = range * 2f;

        super.init();

        if(topColor == null) topColor = baseColor.cpy().a(0f);
        if(zoneLayer < 0) zoneLayer = ringLayer - 0.1f;

        clipSize = Math.max(clipSize, (range + 4f) * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);
    }

    public class EffectZoneBuild extends Building{
        public float heat, activeHeat, activeHeight;
        public float charge = Mathf.random(reload);
        public float smoothEfficiency;
        public boolean active;

        @Override
        public void updateTile(){
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, Mathf.num(canConsume()), 0.08f);
            activeHeat = Mathf.lerpDelta(activeHeat, Mathf.num(canConsume() && active), 0.08f);
            activeHeight = Mathf.lerpDelta(activeHeight, Mathf.num(canConsume() && active) * smoothEfficiency, 0.08f);
            charge += heat * Time.delta;

            if(charge >= reload){
                charge = 0f;

                all.clear();
                Units.nearby(affectEnemyTeam ? null : team, x, y, range, other -> {
                    if(
                        !other.dead &&
                        !(other instanceof SwordUnit) &&
                        (affectOwnTeam && other.team == team || affectEnemyTeam && team != other.team)
                    ) all.add(other);
                });
                active = activate.get();

                zoneEffect.get(this);
            }
        }

        @Override
        public boolean shouldConsume(){
            return active;
        }

        @Override
        public void draw(){
            super.draw();

            float scl = Mathf.absin(Time.time, 50f / Mathf.PI2, 0.125f);
            float opacity = Core.settings.getInt("pm-zone-opacity", 100) / 100f;

            Draw.color(baseColor);
            Draw.alpha(heat * scl * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.color();
            Draw.alpha(1f);

            if(activeHeat > 0.01f){
                Draw.z(zoneLayer);
                float a = activeHeat * smoothEfficiency * opacity;
                Tmp.c1.set(baseColor).a(baseColor.a * scl * a);
                Tmp.c2.set(baseColor).a(baseColor.a * (0.25f + scl) * a);
                Fill.light(
                    x, y,
                    Lines.circleVertices(range),
                    range, Tmp.c1, Tmp.c2
                );
            }

            if(smoothEfficiency > 0.01f){
                Draw.z(ringLayer);
                float a = smoothEfficiency * opacity;
                Tmp.c1.set(baseColor).a(baseColor.a * a);
                Tmp.c2.set(topColor).a(topColor.a * a);

                Lines.stroke(1f, Tmp.c1);
                Lines.circle(x, y, range);
                DrawPseudo3D.tube(x, y, range, realHeight(), Tmp.c1, Tmp.c2);
            }

            Draw.color();
            Draw.alpha(1f);
        }

        public float realHeight(){
            return height * activeHeight;
        }

        @Override
        public void drawLight(){
            super.drawLight();

            if(activeHeat < 0.01f) return;
            Drawf.light(x, y, lightRadius, baseColor,  0.8f * activeHeat);
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
