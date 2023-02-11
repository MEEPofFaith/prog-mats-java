package progressed.entities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class LightningEffect extends Effect{
    private static final Rand rand = new Rand();
    private static final Vec2 v = new Vec2();

    public Color colorFrom = null, colorTo = null;
    public float stroke = 4f;
    public float width = 6f;

    public boolean extend;
    public boolean shrink;

    private boolean initialized;

    public LightningEffect(float life, float clipsize, float stroke){
        this();
        this.lifetime = life;
        this.clip = clipsize;
        this.stroke = stroke;
    }

    public LightningEffect(){
        super();
        followParent = false;
    }

    public LightningEffect shrink(boolean s){
        shrink = s;
        return this;
    }

    public LightningEffect extend(boolean e){
        extend = e;
        return this;
    }

    public LightningEffect startDelay(float d){
        startDelay = d;
        return this;
    }

    public LightningEffect followParent(boolean follow){
        followParent = follow;
        return this;
    }

    public LightningEffect rotWithParent(boolean follow){
        rotWithParent = follow;
        return this;
    }

    public LightningEffect layer(float l){
        layer = l;
        return this;
    }

    public LightningEffect layer(float l, float duration){
        layer = l;
        this.layerDuration = duration;
        return this;
    }

    public LightningEffect width(float w){
        width = w;
        return this;
    }

    public void at(float x, float y, float ex, float ey, Color color){
        at(x, y, ex, ey, color, null);
    }

    public void at(float x, float y, float ex, float ey, Color color, Object data){
        create(x, y, ex, ey, color, data);
    }

    public void create(float x, float y, float ex, float ey, Color color, Object data){
        if(!shouldCreate()) return;

        if(Core.camera.bounds(Tmp.r1).overlaps(Tmp.r2.setCentered(x, y, clip))){
            if(!initialized){
                initialized = true;
                init();
            }

            if(startDelay <= 0f){
                add(ex, ey, x, y, color, data);
            }else{
                Time.run(startDelay, () -> add(x, y, ex, ey, color, data));
            }
        }
    }

    protected void add(float x, float y, float ex, float ey, Color color, Object data){
        LightningEffectState entity = LightningEffectState.create();
        entity.effect = this;
        entity.data = data;
        entity.lifetime = lifetime;
        entity.ex = ex;
        entity.ey = ey;
        entity.set(x, y);
        entity.color.set(color);
        if(followParent && data instanceof Posc p){
            entity.parent = p;
            entity.rotWithParent = rotWithParent;
        }
        entity.add();
    }

    public float render(int id, Color color, float life, float lifetime, float x, float y, float ex, float ey, Object data){
        Draw.z(layer);
        Draw.reset();

        float dst = Mathf.dst(x, y, ex, ey);

        v.set(ex, ey).sub(x, y).nor();

        float fin = life / lifetime, fout = 1 - fin;
        float normx = v.x, normy = v.y;
        int links = Mathf.ceil(dst / width);
        float spacing = dst / links;

        Lines.stroke(stroke * fout);
        Draw.color(colorFrom == null ? color : colorFrom, colorTo == null ? color : colorTo, fin);

        Lines.beginLine();
        Lines.linePoint(x, y);

        rand.setSeed(id);

        float l = links;
        if(extend) l *= Mathf.curve(fin, 0f, 0.5f);

        for(int i = 0; i < l; i ++){
            float nx, ny;
            if(i == links - 1){
                nx = ex;
                ny = ey;
            }else{
                float len = (i + 1) * spacing, r = width / 2f;
                if(shrink) r *= (float)i / links;
                v.setToRandomDirection(rand).scl(r);
                nx = x + normx * len + v.x;
                ny = y + normy * len + v.y;
            }

            Lines.linePoint(nx, ny);
        }

        Lines.endLine();
        Draw.reset();

        return lifetime;
    }

    public static class LightningEffectState extends EffectState{
        public float ex, ey;

        @Override
        public void draw(){
            lifetime = ((LightningEffect)effect).render(id, color, time, lifetime, x, y, ex, ey, data);
        }

        public static LightningEffectState create() {
            return Pools.obtain(LightningEffectState.class, LightningEffectState::new);
        }
    }
}
