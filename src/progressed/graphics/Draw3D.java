package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import progressed.util.*;

import static arc.Core.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public class Draw3D{
    /** Arbitrary value that translates z coordinate in world units to camera offset height. */
    public static final float zToOffset = 1f/48f/tilesize;
    public static final float zFadeBegin = 300f, zFadeEnd = 5000f;
    public static final float scaleFadeBegin = 1.5f, scaleFadeEnd = 7f;
    private static final Color tmpCol = new Color();
    private static final Seq<QueuedBloom> bloomQueue = new Seq<>();

    public static void init(){
        Events.run(Trigger.drawOver, () -> {
            Bloom bloom = renderer.bloom;
            if(bloom != null && bloomQueue.any()){
                bloomQueue.sort(q -> q.layer);
                Draw.draw(PMLayer.skyBloom, () -> {
                    bloom.capture();
                    for(QueuedBloom b : bloomQueue){
                        b.draw.run();
                    }
                    bloom.render();
                });
            }
            bloomQueue.clear();
        });
    }

    public static void tube(float x, float y, float rad, float height, Color baseColorLight, Color baseColorDark, Color topColorLight, Color topColorDark){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;
        float angle = Math3D.tubeStartAngle(x, y, x(x, height), y(y, height), rad, rad * hScale(height));

        for(int i = 0; i < vert; i++){
            float a = angle + space * i, cos = cosDeg(a), sin = sinDeg(a), cos2 = cosDeg(a + space), sin2 = sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;

            float x3 = x(x1, height),
                y3 = y(y1, height),
                x4 = x(x2, height),
                y4 = y(y2, height);
            
            float cLerp1 = 1f - Angles.angleDist(a, 45f) / 180f,
                cLerp2 = 1f - Angles.angleDist(a + space, 45f) / 180f;
            float bc1f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp1).toFloatBits(),
                tc1f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp1).toFloatBits(),
                bc2f = tmpCol.set(baseColorLight).lerp(baseColorDark, cLerp2).toFloatBits(),
                tc2f = tmpCol.set(topColorLight).lerp(topColorDark, cLerp2).toFloatBits();

            Fill.quad(x1, y1, bc1f, x2, y2, bc2f, x4, y4, tc2f, x3, y3, tc1f);
        }
    }

    public static void tube(float x, float y, float rad, float height, Color baseColor, Color topColor){
        tube(x, y, rad, height, baseColor, baseColor, topColor, topColor);
    }

    public static void slantTube(float x1, float y1, float x2, float y2, float z2, float rad, Color baseColor, Color topColor, float offset){
        //Draw
        int verts = Lines.circleVertices(rad * hScale(z2));
        float rotation = Angles.angle(x2, y2, x1, y1);
        float tilt = 90f - Angles.angle(Mathf.dst(x1, y1, x2, y2), z2);
        float startAngle = Math3D.tubeStartAngle(x(x2, z2), y(y2, z2), x1, y1, rad * hScale(z2), rad);
        float[] castVerts = Math3D.castVertices(x1, y1, rotation, startAngle, tilt, rad, verts);
        float[] diskVerts = Math3D.diskVertices(x2, y2, z2, rotation, startAngle, tilt, rad, verts);
        float hAlpha = scaleAlpha(z2 * offset);
        float baseCol = Tmp.c1.set(baseColor).mulA(hAlpha).toFloatBits();
        float topCol = Tmp.c1.set(topColor).mulA(hAlpha).toFloatBits();
        for(int i = 0; i < verts - 1; i++){
            int i2 = i + 1;
            float bx1 = castVerts[i * 2],
                by1 = castVerts[i * 2 + 1],
                bx2 = castVerts[i2 * 2],
                by2 = castVerts[i2 * 2 + 1];
            float tz1 = diskVerts[i * 3 + 2],
                tz2 = diskVerts[i2 * 3 + 2];
            float tx1 = x(diskVerts[i * 3], tz1),
                ty1 = y(diskVerts[i * 3 + 1], tz1),
                tx2 = x(diskVerts[i2 * 3], tz2),
                ty2 = y(diskVerts[i2 * 3 + 1], tz2);
            if(offset > 0f){
                tx1 = Mathf.lerp(tx1, bx1, offset);
                ty1 = Mathf.lerp(ty1, by1, offset);
                tx2 = Mathf.lerp(tx2, bx2, offset);
                ty2 = Mathf.lerp(ty2, by2, offset);
            }

            Fill.quad(
                bx1, by1, baseCol,
                bx2, by2, baseCol,
                tx2, ty2, topCol,
                tx1, ty1, topCol
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
        Lines.line(
            x(x1, z1), y(y1, z1),
            x(x2, z2), y(y2, z2)
        );
    }

    public static void drawLineSegments(float x1, float y1, float z1, float x2, float y2, float z2){
        drawLineSegments(x1, y1, z1, x2, y2, z2, Math3D.linePointCounts(x1, y1, z1, x2, y2, z2));
    }

    public static void drawLineSegments(float x1, float y1, float z1, float x2, float y2, float z2, int pointCount){
        float[] points = Math3D.linePoints(x1, y1, z1, x2, y2, z2, pointCount);
        Lines.beginLine();
        for(int i = 0; i < pointCount; i++){
            float z = points[i * 3 + 2];
            Lines.linePoint(x(points[i * 3], z), y(points[i * 3 + 1], z));
        }
        Lines.endLine();
    }

    public static void lineAngleBase(float x, float y, float height, float length, float rotation, float rotationOffset, float tilt){
        Math3D.rotate(Tmp.v31, length, rotation, rotationOffset, tilt);
        float h2 = height + Tmp.v31.z;
        float x1 = x(x, height);
        float y1 = y(y, height);
        float x2 = x(x + Tmp.v31.x, h2);
        float y2 = y(y + Tmp.v31.y, h2);
        Lines.line(x1, y1, x2, y2);
    }

    public static void drawAimDebug(float x, float y, float height, float length, float rotation, float tilt, float spread){
        Lines.stroke(3f);
        Draw.color(Color.blue); //Down
        lineAngleBase(x, y, height, length, rotation, 0f, tilt - spread);
        Lines.stroke(6f);
        Draw.color(Pal.accent); //Center
        lineAngleBase(x, y, height, length, rotation, 0f, tilt);
        Lines.stroke(3f);
        Draw.color(Color.red); //Right
        lineAngleBase(x, y, height, length, rotation, -spread, tilt);
        Draw.color(Color.lime); //Left
        lineAngleBase(x, y, height, length, rotation, spread, tilt);
        Draw.color(Color.orange); //Up
        lineAngleBase(x, y, height, length, rotation, 0f, tilt + spread);
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
        int vertCount = Lines.circleVertices(rad * hScale(z2));
        float[] verts = Math3D.diskVertices(x2, y2, z2, rotation, 0f, tilt, rad, vertCount);
        Lines.beginLine();
        for(int i = 0; i <= vertCount; i++){
            float vZ = verts[i * 3 + 2];
            Lines.linePoint(x(verts[i * 3], vZ), y(verts[i * 3 + 1], vZ));
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
                y = points[i * 3 + 1],
                z = points[i * 3 + 2];
            float hx = x(x, z),
                hy = y(y, z);
            Lines.linePoint(hx, hy);
            Lines.line(x, y, hx, hy);
        }
        Lines.endLine();
    }

    public static float x(float x, float z){
        if(z <= 0) return x;
        return x + xOffset(x, z);
    }

    public static float y(float y, float z){
        if(z <= 0) return y;
        return y + yOffset(y, z);
    }

    public static float xOffset(float x, float z){
        return (x - camera.position.x) * hMul(z);
    }

    public static float yOffset(float y, float z){
        return (y - camera.position.y) * hMul(z);
    }

    public static float hScale(float z){
        return 1f + hMul(z);
    }

    public static float hMul(float z){
        return height(z) * renderer.getDisplayScale();
    }

    public static float height(float z){
        return z * zToOffset;
    }

    public static float zAlpha(float z){
        return 1f - Mathf.curve(z, zFadeBegin, zFadeEnd);
    }

    public static float scaleAlpha(float z){
        return 1f - Mathf.curve(hMul(z), scaleFadeBegin, scaleFadeEnd);
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

    private static class QueuedBloom{
        public final float layer;
        public final Runnable draw;

        private QueuedBloom(float layer, Runnable draw){
            this.layer = layer;
            this.draw = draw;
        }
    }
}
