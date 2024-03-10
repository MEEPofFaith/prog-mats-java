package progressed.entities.effect;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.util.Tmp.*;

public class SwirlEffect extends Effect{
    public static TextureRegion hCircle;

    public int length;
    public float width;
    public float minRot, maxRot;
    public float minDst, maxDst;
    public boolean light;
    public boolean lerp;
    public Color centerColor;
    public Interp fallterp = Interp.pow2Out;
    public Interp spinterp = Interp.pow3Out;

    public SwirlEffect(float lifetime, float clipsize, Color centerColor, int length, float width, float minRot, float maxRot, float minDst, float maxDst, boolean light, boolean lerp){
        super();
        this.lifetime = lifetime;
        this.clip = clipsize;
        this.centerColor = centerColor;
        this.length = length;
        this.width = width;
        this.minRot = minRot;
        this.maxRot = maxRot;
        this.minDst = minDst;
        this.maxDst = maxDst;
        this.light = light;
        this.lerp = lerp;
    }

    public SwirlEffect(float lifetime, Color centerColor, int length, float width, float minRot, float maxRot, float minDst, float maxDst, boolean light, boolean lerp){
        this(lifetime, 400f, centerColor, length, width, minRot, maxRot, minDst, maxDst, light, lerp);
    }

    public SwirlEffect(float lifetime, int length, float width, float minRot, float maxRot, boolean light, boolean lerp){
        this(lifetime, Color.black, length, width, minRot, maxRot, -1, -1, light, lerp);
    }

    public SwirlEffect(float lifetime, int length, float width, float minRot, float maxRot, boolean lerp){
        this(lifetime, Color.black, length, width, minRot, maxRot, -1, -1, false, lerp);
    }

    public SwirlEffect setInterps(Interp fallterp, Interp spinterp){
        this.fallterp = fallterp;
        this.spinterp = spinterp;
        return this;
    }

    public SwirlEffect setInterps(Interp interp){
        return setInterps(interp, interp);
    }

    @Override
    public void render(EffectContainer e){
        float lifetime = e.lifetime - length;
        float dst;
        if(minDst < 0 || maxDst < 0){
            dst = Math.abs(e.rotation);
        }else{
            dst = Mathf.randomSeed(e.id, minDst, maxDst);
        }
        float l = Mathf.clamp(e.time / lifetime);
        if(lerp){
            color(centerColor, e.color, l);
        }else{
            color(centerColor);
        }

        int points = (int)Math.min(e.time, length);
        float width = Mathf.clamp(e.time / (e.lifetime - length)) * this.width;
        float size = width / points;
        float baseRot = Mathf.randomSeed(e.id + 1, 360f), addRot = Mathf.randomSeed(e.id + 2, minRot, maxRot) * Mathf.sign(e.rotation);

        float fout, lastAng = 0f;
        for(int i = 0; i < points; i++){
            fout = 1f - Mathf.clamp((e.time - points + i) / lifetime);
            v1.trns(baseRot + addRot * spinterp.apply(fout), Mathf.maxZero(dst * fallterp.apply(fout)));
            fout = 1f - Mathf.clamp((e.time - points + i + 1) / lifetime);
            v2.trns(baseRot + addRot * spinterp.apply(fout), Mathf.maxZero(dst * fallterp.apply(fout)));

            float a2 = -v1.angleTo(v2) * Mathf.degRad;
            float a1 = i == 0 ? a2 : lastAng;

            float
                cx = Mathf.sin(a1) * i * size,
                cy = Mathf.cos(a1) * i * size,
                nx = Mathf.sin(a2) * (i + 1) * size,
                ny = Mathf.cos(a2) * (i + 1) * size;

            Fill.quad(
                e.x + v1.x - cx, e.y + v1.y - cy,
                e.x + v1.x + cx, e.y + v1.y + cy,
                e.x + v2.x + nx, e.y + v2.y + ny,
                e.x + v2.x - nx, e.y + v2.y - ny
            );
            if(light){
                Drawf.light(
                    e.x + v1.x, e.y + v1.y,
                    e.x + v2.x, e.y + v2.y,
                    i * size * 6f, Draw.getColor().cpy(), l
                );
            }

            lastAng = a2;
        }

        if(hCircle == null) hCircle = Core.atlas.find("hcircle");
        Draw.rect(hCircle, e.x + v2.x, e.y + v2.y, width * 2f, width * 2f, -Mathf.radDeg * lastAng);
    }
}
