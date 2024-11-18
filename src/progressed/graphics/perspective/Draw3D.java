package progressed.graphics.perspective;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import progressed.graphics.*;
import progressed.util.*;

import static arc.Core.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public class Draw3D{
    public static final float shadowFadeEnd = 300f, zToShadowScl = 3f / shadowFadeEnd;
    public static final float shadowLayer = Layer.flyingUnit + 1;
    private static final Color tmpCol = new Color();
    private static final Seq<QueuedBloom> bloomQueue = new Seq<>();
    private static final Seq<Runnable> shadowQueue = new Seq<>();

    public static void init(){
        Events.run(Trigger.drawOver, () -> {
            if(shadowQueue.any()){
                Draw.draw(shadowLayer, () -> {
                    FrameBuffer buffer = renderer.effectBuffer;
                    buffer.begin(Color.clear);
                    Draw.sort(false);
                    Gl.blendEquationSeparate(Gl.funcAdd, Gl.max);

                    for(Runnable s : shadowQueue){
                        s.run();
                    }

                    Draw.sort(true);
                    buffer.end();
                    Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);

                    buffer.blit(PMShaders.passThrough);
                });
                shadowQueue.clear();
            }

            if(bloomQueue.any()){
                bloomQueue.sort(q -> q.layer);
                Bloom bloom = renderer.bloom;
                if(bloom != null){
                    Draw.draw(PMLayer.skyBloom, () -> {
                        bloom.capture();
                        for(QueuedBloom b : bloomQueue){
                            b.draw.run();
                        }
                        bloom.render();
                    });
                }else{
                    for(QueuedBloom b : bloomQueue){
                        b.draw.run();
                    }
                }
                bloomQueue.clear();
            }
        });
    }

    public static float shadowAlpha(float z){
        return Mathf.clamp(1f - z / shadowFadeEnd);
    }

    public static float shadowScale(float z){
        return 1f + zToShadowScl * z;
    }

    public static void tube(float x, float y, float rad, float z2, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        Vec2 pos = Perspective.drawPos(x, y, z2);
        float angle = Math3D.tubeStartAngle(x, y, pos.x, pos.y, rad, rad * Perspective.scale(x, y, z2));

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;

            pos = Perspective.drawPos(x1, y1, z2);
            float x3 = pos.x,
                y3 = pos.y;
            pos = Perspective.drawPos(x2, y2, z2);
            float x4 = pos.x,
                y4 = pos.y;
            
            float cLerp1 = 1f - Angles.angleDist(a, 45f) / 180f,
                cLerp2 = 1f - Angles.angleDist(a + space, 45f) / 180f;
            float bc1f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp1).toFloatBits(),
                tc1f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp1).toFloatBits(),
                bc2f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp2).toFloatBits(),
                tc2f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp2).toFloatBits();

            Fill.quad(x1, y1, bc1f, x2, y2, bc2f, x4, y4, tc2f, x3, y3, tc1f);
        }
    }

    public static void tube(float x, float y, float rad, float z2, Color baseColor, Color topColor){
        tube(x, y, rad, z2, baseColor, baseColor, topColor, topColor);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z2, float rad, Color baseColor, Color topColor, float offset){
        //Draw
        float scl = Perspective.scale(x2, y2, z2);
        int verts = Lines.circleVertices(rad * scl);
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);
        Vec2 pos = Perspective.drawPos(x2, y2, z2);
        float startAngle = Math3D.tubeStartAngle(pos.x, pos.y, x1, y1, rad * scl, rad);
        float[] castVerts = Math3D.castVertices(x1, y1, rotation, startAngle, tilt, rad, verts);
        float[] diskVerts = Math3D.diskVertices(x2, y2, z2, rotation, startAngle, tilt, rad, verts);
        float hAlpha = Perspective.alpha(x2, y2, z2 * offset);
        float baseCol = Tmp.c1.set(baseColor).mulA(hAlpha).toFloatBits();
        float topCol = Tmp.c1.set(topColor).mulA(hAlpha).toFloatBits();
        for(int i = 0; i < verts - 1; i++){
            int i2 = i + 1;
            float bx1 = castVerts[i * 2],
                by1 = castVerts[i * 2 + 1],
                bx2 = castVerts[i2 * 2],
                by2 = castVerts[i2 * 2 + 1];
            Tmp.v1.set(Perspective.drawPos(diskVerts[i * 3], diskVerts[i * 3 + 1], diskVerts[i * 3 + 2]));
            Tmp.v2.set(Perspective.drawPos(diskVerts[i2 * 3], diskVerts[i2 * 3 + 1], diskVerts[i2 * 3 + 2]));
            if(offset > 0f){
                Tmp.v1.lerp(bx1, by1, offset);
                Tmp.v2.lerp(bx2, by2, offset);
            }

            Fill.quad(
                bx1, by1, baseCol,
                bx2, by2, baseCol,
                Tmp.v2.x, Tmp.v2.y, topCol,
                Tmp.v1.x, Tmp.v1.y, topCol
            );
        }
        //Debug
        Draw.z(PMLayer.skyBloom + 10);
        Lines.stroke(4);
        Draw.color(Color.black);
        Lines.line(x1, y1, castVerts[0], castVerts[1]);
        line(x2, y2, z2, diskVerts[0], diskVerts[1], diskVerts[2]);
        line(castVerts[0], castVerts[1], 0, diskVerts[0], diskVerts[1], diskVerts[2]);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z, float rad, Color baseColor, Color topColor){
        slantTube(x1, y1, x2, y2, z, rad, baseColor, topColor, 0f);
    }

    public static void line(float x1, float y1, float z1, float x2, float y2, float z2){
        Tmp.v1.set(Perspective.drawPos(x1, y1, z1));
        Tmp.v2.set(Perspective.drawPos(x2, y2, z2));

        Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
    }

    public static void drawLineSegments(float x1, float y1, float z1, float x2, float y2, float z2){
        drawLineSegments(x1, y1, z1, x2, y2, z2, Math3D.linePointCounts(x1, y1, z1, x2, y2, z2));
    }

    public static void drawLineSegments(float x1, float y1, float z1, float x2, float y2, float z2, int pointCount){
        float[] points = Math3D.linePoints(x1, y1, z1, x2, y2, z2, pointCount);
        Lines.beginLine();
        for(int i = 0; i < pointCount; i++){
            Vec2 pos = Perspective.drawPos(points[i * 3], points[i * 3 + 1], points[i * 3 + 2]);
            Lines.linePoint(pos.x, pos.y);
        }
        Lines.endLine();
    }

    public static void lineAngleBase(float x, float y, float z, float length, float rotation, float rotationOffset, float tilt){
        Math3D.rotate(Tmp.v31, length, rotation, rotationOffset, tilt);
        float z2 = z + Tmp.v31.z;
        Vec2 pos = Perspective.drawPos(x, y, z);
        float x1 = pos.x;
        float y1 = pos.y;
        pos = Perspective.drawPos(x + Tmp.v31.x, y + Tmp.v31.y, z2);
        float x2 = pos.x;
        float y2 = pos.y;
        Lines.line(x1, y1, x2, y2);
    }

    public static void drawAimDebug(float x, float y, float z, float length, float rotation, float tilt, float spread){
        Lines.stroke(3f);
        Draw.color(Color.blue); //Down
        lineAngleBase(x, y, z, length, rotation, 0f, tilt - spread);
        Lines.stroke(6f);
        Draw.color(Pal.accent); //Center
        lineAngleBase(x, y, z, length, rotation, 0f, tilt);
        Lines.stroke(3f);
        Draw.color(Color.red); //Right
        lineAngleBase(x, y, z, length, rotation, -spread, tilt);
        Draw.color(Color.lime); //Left
        lineAngleBase(x, y, z, length, rotation, spread, tilt);
        Draw.color(Color.orange); //Up
        lineAngleBase(x, y, z, length, rotation, 0f, tilt + spread);
    }

    public static void drawDiskDebug(float x1, float y1, float x2, float y2, float z2, float rad){
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);

        Tmp.v31.set(Vec3.Z).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);
        Tmp.v32.set(rad, 0, 0).rotate(Vec3.Y, tilt).rotate(Vec3.Z, -rotation);

        Tmp.v32.rotate(Tmp.v31, Time.time * 2f);

        //Disk
        Lines.stroke(3f);
        Draw.color(Color.white);
        int vertCount = Lines.circleVertices(rad * Perspective.scale(x2, y2, z2));
        float[] verts = Math3D.diskVertices(x2, y2, z2, rotation, 0f, tilt, rad, vertCount);
        Lines.beginLine();
        for(int i = 0; i <= vertCount; i++){
            Vec2 pos = Perspective.drawPos(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2]);
            Lines.linePoint(pos.x, pos.y);
        }
        Lines.endLine(true);
        //Stuff
        Draw.color(Color.yellow);
        line(x2, y2, z2, x2 + Tmp.v31.x, y2 + Tmp.v31.y, z2 + Tmp.v31.z);
        Draw.color(Color.purple);
        line(x2, y2, z2, x2 + Tmp.v32.x, y2 + Tmp.v32.y, z2 + Tmp.v32.z);
    }

    public static void drawLineDebug(float x1, float y1, float z1, float x2, float y2, float z2){
        int pointCount = Math3D.linePointCounts(x1, y1, z1, x2, y2, z2);
        float[] points = Math3D.linePoints(x1, y1, z1, x2, y2, z2, pointCount);
        Lines.beginLine();
        for(int i = 0; i < pointCount; i++){
            float x = points[i * 3],
                y = points[i * 3 + 1];
            Vec2 pos = Perspective.drawPos(x, y, points[i * 3 + 2]);
            float hx = pos.x,
                hy = pos.y;
            Lines.linePoint(hx, hy);
            Lines.line(x, y, hx, hy);
        }
        Lines.endLine();
    }

    public static float layerOffset(float x, float y){
        float max = Math.max(camera.width, camera.height);
        return -dst(x, y, camera.position.x, camera.position.y) / max / 1000f;
    }

    public static float layerOffset(float cx, float cy, float tx, float ty){
        float angleTo = Angles.angle(cx, cy, tx, ty),
            angleCam = Angles.angle(cx, cy, camera.position.x, camera.position.y);
        float angleDist = Angles.angleDist(angleTo, angleCam);
        float max = Math.max(camera.width, camera.height);

        return layerOffset(cx, cy) + dst(cx, cy, tx, ty) * cosDeg(angleDist) / max / 1000f;
    }

    public static void highBloom(Runnable draw){
        highBloom(true, Draw.z(), draw);
    }

    public static void highBloom(float layer, Runnable draw){
        highBloom(true, layer, draw);
    }

    public static void highBloom(boolean bloom, Runnable draw){
        highBloom(bloom, Draw.z(), draw);
    }

    public static void highBloom(boolean bloom, float layer, Runnable draw){
        if(bloom){
            bloomQueue.add(new QueuedBloom(layer, draw));
        }else{
            float z = Draw.z();
            Draw.z(z + 0.01f);
            draw.run();
            Draw.z(z);
        }
    }

    public static void shadow(Runnable draw){
        shadowQueue.add(draw);
    }

    private static class QueuedBloom{
        public final float layer;
        public final Runnable draw;

        private QueuedBloom(float layer, Runnable draw){
            this.layer = layer;
            this.draw = draw;
        }
    }
}
