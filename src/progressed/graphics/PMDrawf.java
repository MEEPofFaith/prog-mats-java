package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;
import static progressed.graphics.PMShaders.*;

public class PMDrawf{
    private static final Vec2 vec1 = new Vec2(), vec2 = new Vec2(), vec3 = new Vec2(), vec4 = new Vec2();

    public static void light(float x, float y, TextureRegion region, float rotation, Color color, float opacity, boolean flip){
        float res = color.toFloatBits();
        renderer.lights.add(() -> {
            Draw.color(res);
            Draw.alpha(opacity);
            Draw.rect(region, x, y, region.width / 4f * Mathf.sign(flip), region.height / 4f, rotation);
        });
    }

    public static void plus(float x, float y, float diameter, float angle){
        plus(x, y, diameter / 3f, diameter, angle);
    }

    public static void plus(float x, float y, float stroke, float diameter, float angle){
        for(int i = 0; i < 2; i++){
            Fill.rect(x, y, stroke, diameter, angle + i * 90f);
        }
    }

    public static void cross(float x, float y, float width, float length, float angle){
        for(int i = 0; i < 4; i++){
            Drawf.tri(x, y, width, length, i * 90f + angle);
        }
    }

    public static void cross(float x, float y, float size, float angle){
        cross(x, y, size, size, angle);
    }

    public static void shadow(TextureRegion texture, float x, float y, float rotation, float alpha){
        Draw.color(Tmp.c1.set(Pal.shadow).mulA(alpha));
        Draw.rect(texture, x, y, rotation);
        Draw.color();
    }

    public static void vecLine(float x, float y, Vec2 v1, Vec2 v2, boolean cap){
        line(v1.x + x, v1.y + y, v2.x + x, v2.y + y, cap);
    }

    public static void lineAngleCenter(float x, float y, float angle, float length, boolean cap){
        vec1.trns(angle, length);

        line(x - vec1.x / 2, y - vec1.y / 2, x + vec1.x / 2, y + vec1.y / 2, cap);
    }

    public static void pill(float x, float y, float angle, float length, float width){
        stroke(width);
        lineAngleCenter(x, y, angle, length - width, false);
        
        for(int i = 0; i < 2; i++){
            vec1.trns(angle + 180f * i, length / 2f - width / 2f);
            Fill.circle(x + vec1.x, y + vec1.y, width / 2f);
        }
    }

    public static void baseTri(float x, float y, float b, float h, float rot){
        vec1.trns(rot, h).add(x, y);
        vec2.trns(rot - 90f, b / 2f).add(x, y);
        vec3.trns(rot + 90f, b / 2f).add(x, y);

        Fill.tri(
            vec1.x, vec1.y,
            vec2.x, vec2.y,
            vec3.x, vec3.y
        );
    }

    public static void targetLine(float x1, float y1, float x2, float y2, float r1, float r2, Color color){
        float ang = Angles.angle(x1, y1, x2, y2);
        float calc = 1f + (1f - Mathf.sinDeg(Mathf.mod(ang, 90f) * 2)) * (Mathf.sqrt2 - 1f);

        Tmp.v1.trns(ang, (r1 / Mathf.sqrt2) * calc).add(x1, y1);
        Tmp.v2.trns(ang + 180, (r2 / Mathf.sqrt2) * calc).add(x2, y2);

        Lines.stroke(3f, Pal.gray);
        Lines.square(x1, y1, r1, 45f);
        Lines.square(x2, y2, r2, 45f);
        Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

        Lines.stroke(1f, color);
        Lines.square(x1, y1, r1, 45f);
        Lines.square(x2, y2, r2, 45f);
        Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
    }

    public static void target(float x, float y, float angle, float radius, Color ringColor, Color spikeColor, float alpha){
        color(Pal.gray, alpha);
        stroke(3);
        poly(x, y, 4, 7f * radius, angle);
        spikes(x, y, 3f * radius, 6f * radius, 4, angle);
        color(ringColor, alpha);
        stroke(1);
        poly(x, y, 4, 7f * radius, angle);
        color(spikeColor);
        spikes(x, y, 3f * radius, 6f * radius, 4, angle);
        color();
    }

    public static void target(float x, float y, float angle, float radius, Color color, float alpha){
        target(x, y, angle, radius, color, color, alpha);
    }

    public static void ring(float x, float y, float rad1, float rad2){
        if(Math.abs(rad1 - rad2) > 0.01f){
            int sides = circleVertices(Math.max(rad1, rad2));
            float space = 360f / sides;

            for(int i = 0; i < sides; i++){
                float a = space * i, cos = Mathf.cosDeg(a), sin = Mathf.sinDeg(a), cos2 = Mathf.cosDeg(a + space), sin2 = Mathf.sinDeg(a + space);
                Fill.quad(
                    x + rad1 * cos, y + rad1 * sin,
                    x + rad1 * cos2, y + rad1 * sin2,
                    x + rad2 * cos2, y + rad2 * sin2,
                    x + rad2 * cos, y + rad2 *  sin
                );
            }
        }
    }

    /** Similar to {@link Drawf#laser} but doesn't draw light. */
    public static void laser(TextureRegion line, TextureRegion start, TextureRegion end, float x, float y, float x2, float y2, float scale){
        float scl = 8f * scale * Draw.scl, rot = Mathf.angle(x2 - x, y2 - y);
        float vx = Mathf.cosDeg(rot) * scl, vy = Mathf.sinDeg(rot) * scl;

        Draw.rect(start, x, y, start.width * scale * start.scl(), start.height * scale * start.scl(), rot + 180);
        Draw.rect(end, x2, y2, end.width * scale * end.scl(), end.height * scale * end.scl(), rot);

        Lines.stroke(12f * scale);
        Lines.line(line, x + vx, y + vy, x2 - vx, y2 - vy, false);
        Lines.stroke(1f);
    }

    public static void blockBuild(float x, float y, TextureRegion region, float rotation, float progress){
        blockBuild(x, y, region, Pal.accent, rotation, progress);
    }

    public static void blockBuild(float x, float y, TextureRegion region, Color color, float rotation, float progress){
        Shaders.blockbuild.region = region;
        Shaders.blockbuild.progress = progress;

        Draw.color(color);
        Draw.shader(Shaders.blockbuild);
        Draw.rect(region, x, y, rotation);
        Draw.shader();
        Draw.color();
    }

    public static void blockBuildCenter(float x, float y, TextureRegion region, float rotation, float progress){
        blockBuildCenter(x, y, region, Pal.accent, rotation, progress);
    }

    public static void blockBuildCenter(float x, float y, TextureRegion region, Color color, float rotation, float progress){
        blockBuildCenter.region = region;
        blockBuildCenter.progress = progress;

        Draw.color(color);
        Draw.shader(blockBuildCenter);
        Draw.rect(region, x, y, rotation);
        Draw.shader();
        Draw.color();
    }

    public static void vertConstruct(float x, float y, TextureRegion region, float rotation, float progress, float alpha, float time){
        vertConstruct(x, y, region, Pal.accent, rotation, progress, alpha, time);
    }

    public static void vertConstruct(float x, float y, TextureRegion region, Color color, float rotation, float progress, float alpha, float time){
        vertBuild.region = region;
        vertBuild.progress = progress;
        vertBuild.color.set(color);
        vertBuild.color.a = alpha;
        vertBuild.time = -time / 20f;

        shader(vertBuild);
        rect(region, x, y, rotation);
        shader();

        reset();
    }

    public static void materialize(float x, float y, TextureRegion region, Color color, float rotation, float offset, float progress){
        materialize(x, y, region, color, rotation, offset, progress, Time.time, false);
    }

    public static void materialize(float x, float y, TextureRegion region, Color color, float rotation, float offset, float progress, boolean shadow){
        materialize(x, y, region, color, rotation, offset, progress, Time.time, shadow);
    }

    public static void materialize(float x, float y, TextureRegion region, Color color, float rotation, float offset, float progress, float time){
        materialize(x, y, region, color, rotation, offset, progress, time, false);
    }

    public static void materialize(float x, float y, TextureRegion region, Color color, float rotation, float offset, float progress, float time, boolean shadow){
        materialize.region = region;
        materialize.progress = Mathf.clamp(progress);
        materialize.color.set(color);
        materialize.time = time;
        materialize.offset = offset;
        materialize.shadow = Mathf.num(shadow);

        shader(materialize);
        rect(region, x, y, rotation);
        shader();

        reset();
    }

    /** Draws a sprite that should be light-wise correct. Provided sprites must be similar in shape and face towards the right. */
    public static void spinSprite(TextureRegion base, TextureRegion bottomLeft, TextureRegion topRight, float x, float y, float r, float alpha){
        if(alpha < 0.001f) return;
        if(alpha < 0.999f){
            FrameBuffer buffer = renderer.effectBuffer;
            float z = Draw.z();
            float xScl = xscl, yScl = yscl;
            Draw.draw(z, () -> {
                buffer.begin(Color.clear);
                Draw.scl(xScl, yScl);
                spinSprite(base, bottomLeft, topRight, x, y, r);
                buffer.end();

                alphaShader.alpha = alpha;
                buffer.blit(alphaShader);
            });
            return;
        }

        float ar = mod(r - 135f, 360f);
        float a = mod(ar, 90f) / 90f;
        alpha(1f);
        if(ar >= 270){ //Bottom Right
            rect(bottomLeft, x, y, r);
            alpha(a);
            yscl *= -1;
            rect(base, x, y, r);
            yscl *= -1;
        }else if(ar >= 180){ //Bottom Left
            rect(base, x, y, r);
            alpha(a);
            rect(bottomLeft, x, y, r);
        }else if(ar >= 90){ //Top Left
            rect(topRight, x, y, r);
            alpha(a);
            rect(base, x, y, r);
        }else{ //Top Right
            yscl *= -1;
            rect(base, x, y, r);
            yscl *= -1;
            alpha(a);
            rect(topRight, x, y, r);
        }
        alpha(1f);
    }

    public static void spinSprite(TextureRegion base, TextureRegion bottomLeft, TextureRegion topRight, float x, float y, float r){
        spinSprite(base, bottomLeft, topRight, x, y, r, 1f);
    }

    public static void ellipse(float x, float y, float rad, float wScl, float hScl, float rot){
        float sides = circleVertices(rad);
        float space = 360 / sides;
        float r1 = rad - getStroke() / 2f, r2 = rad + getStroke() / 2f;

        for(int i = 0; i < sides; i++){
            float a = space * i;
            vec1.trns(rot,
                r1 * wScl * cosDeg(a),
                r1 * hScl * sinDeg(a)
            );
            vec2.trns(rot,
                r1 * wScl * cosDeg(a + space),
                r1 * hScl * sinDeg(a + space)
            );
            vec3.trns(rot,
                r2 * wScl * cosDeg(a + space),
                r2 * hScl * sinDeg(a + space)
            );
            vec4.trns(rot,
                r2 * wScl * cosDeg(a),
                r2 * hScl * sinDeg(a)
            );
            Fill.quad(
                x + vec1.x, y + vec1.y,
                x + vec2.x, y + vec2.y,
                x + vec3.x, y + vec3.y,
                x + vec4.x, y + vec4.y
            );
        }
    }

    /**
     * Color flash over the entire screen
     * @author sunny, customization by MEEP
     * */
    public static void flash(Color fromColor, Color toColor, float seconds, Interp fade){
        if(!headless){
            Image flash = new Image();
            flash.touchable = Touchable.disabled;
            flash.setColor(fromColor);
            flash.setFillParent(true);
            flash.actions(Actions.color(toColor, seconds));
            flash.actions(Actions.fadeOut(seconds, fade), Actions.remove());
            flash.update(() -> {
                if(!Vars.state.isGame()){
                    flash.remove();
                }
            });
            Core.scene.add(flash);
        }
    }

    public static void spinningCircle(int seed, float time, float x, float y, float radius, int spikes, float spikeDuration, float durationRnd, float spikeWidth, float spikeHeight, float pointOffset){
        spinningCircle(seed, time, time, x, y, radius, spikes, spikeDuration, durationRnd, spikeWidth, spikeHeight, pointOffset);
    }

    public static void spinningCircle(int seed, float angle, float time, float x, float y, float radius, int spikes, float spikeDuration, float durationRnd, float spikeWidth, float spikeHeight, float pointOffset){
        Fill.circle(x, y, radius);

        for(int i = 0; i < spikes; i++){
            float d = spikeDuration + Mathf.randomSeedRange(seed + i + spikes, durationRnd);
            float timeOffset = Mathf.randomSeed((seed + i) * 314L, 0f, d);
            int timeSeed = Mathf.floor((time + timeOffset) / d);
            float a = angle + Mathf.randomSeed(Math.max(timeSeed, 1) + ((i + seed) * 245L), 360f);
            float fin = ((time + timeOffset) % d) / d;
            float fslope = (0.5f - Math.abs(fin - 0.5f)) * 2f;
            vec1.trns(a + spikeWidth / 2f, radius).add(x, y);
            vec2.trns(a - spikeWidth / 2f, radius).add(x, y);
            vec3.trns(a + pointOffset, radius + spikeHeight * fslope).add(x, y);
            Fill.tri(
                vec1.x, vec1.y,
                vec2.x, vec2.y,
                vec3.x, vec3.y
            );
        }
    }

    public static void arcLine(float x, float y, float radius, float fraction, float rotation){
        arc(x, y, radius, fraction, rotation, 50);
    }

    public static void arcLine(float x, float y, float radius, float fraction, float rotation, int sides){
        int max = Mathf.ceil(sides * fraction);
        Lines.beginLine();

        for(int i = 0; i <= max; i++){
            vec1.trns((float)i / max * fraction * 360f + rotation, radius);
            float x1 = vec1.x;
            float y1 = vec1.y;

            vec1.trns((float)(i + 1) / max * fraction * 360f + rotation, radius);

            Lines.linePoint(x + x1, y + y1);
        }

        Lines.endLine();
    }

    public static void arcFill(float x, float y, float radius, float fraction, float rotation){
        arcFill(x, y, radius, fraction, rotation, 50);
    }

    public static void arcFill(float x, float y, float radius, float fraction, float rotation, int sides){
        int max = Mathf.ceil(sides * fraction);
        Fill.polyBegin();
        Fill.polyPoint(x, y);

        for(int i = 0; i <= max; i++){
            float a = fraction * 360f * ((float)i / max) + rotation;
            float x1 = Angles.trnsx(a, radius);
            float y1 = Angles.trnsy(a, radius);

            Fill.polyPoint(x + x1, y + y1);
        }
        Fill.polyPoint(x, y);

        Fill.polyEnd();
    }

    public static float text(float x, float y, Color color, CharSequence text){
        return text(x, y, true, -1, color, text);
    }

    public static float text(float x, float y, boolean underline, float maxWidth, Color color, CharSequence text){
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        if(maxWidth <= 0){
            font.getData().setScale(1f / 3f);
            layout.setText(font, text);
        }else{
            font.getData().setScale(1f);
            layout.setText(font, text);
            font.getData().setScale(Math.min(1f / 3f, maxWidth / layout.width));
            layout.setText(font, text);
        }

        font.setColor(color);
        font.draw(text, x, y + (underline ? layout.height + 1 : layout.height / 2f), Align.center);
        if(underline){
            y -= 1f;
            Lines.stroke(2f, Color.darkGray);
            Lines.line(x - layout.width / 2f - 2f, y, x + layout.width / 2f + 1.5f, y);
            Lines.stroke(1f, color);
            Lines.line(x - layout.width / 2f - 2f, y, x + layout.width / 2f + 1.5f, y);
        }

        float width = layout.width;

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);

        return width;
    }

    public static void tractorCone(float cx, float cy, float time, float spacing, float thickness, Runnable draw){
        FrameBuffer buffer = renderer.effectBuffer;
        float z = Draw.z();
        Draw.draw(z, () -> {
            buffer.begin(Color.clear);
            draw.run();
            buffer.end();

            tractorCone.setCenter(cx, cy);
            tractorCone.time = time;
            tractorCone.spacing = spacing;
            tractorCone.thickness = thickness;
            buffer.blit(tractorCone);
        });
    }
}
