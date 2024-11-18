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

import static arc.Core.*;
import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public class Draw3D{
    public static final float shadowFadeEnd = 300f, zToShadowScl = 3f / shadowFadeEnd;
    public static final float shadowLayer = Layer.flyingUnit + 1;
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

    public static void drawAimDebug(float x, float y, float z, float length, float rotation, float tilt, float spread){
        Lines.stroke(3f);
        Draw.color(Color.blue); //Down
        Lines3D.lineAngleBase(x, y, z, length, rotation, 0f, tilt - spread);
        Lines.stroke(6f);
        Draw.color(Pal.accent); //Center
        Lines3D.lineAngleBase(x, y, z, length, rotation, 0f, tilt);
        Lines.stroke(3f);
        Draw.color(Color.red); //Right
        Lines3D.lineAngleBase(x, y, z, length, rotation, -spread, tilt);
        Draw.color(Color.lime); //Left
        Lines3D.lineAngleBase(x, y, z, length, rotation, spread, tilt);
        Draw.color(Color.orange); //Up
        Lines3D.lineAngleBase(x, y, z, length, rotation, 0f, tilt + spread);
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
        float[] verts = Fill3D.diskVertices(x2, y2, z2, rotation, 0f, tilt, rad, vertCount);
        for(int i = 0; i <= vertCount; i += 3){
            float px2, py2, pz2;
            if(i == vertCount - 3){ //TODO Make a Lines3D.circle
                px2 = verts[0];
                py2 = verts[1];
                pz2 = verts[2];
            }else{
                px2 = verts[i + 3];
                py2 = verts[i + 3 + 1];
                pz2 = verts[i + 3 + 2];
            }
            Lines3D.line(verts[i], verts[i + 1], verts[i + 2], px2, py2, pz2);
        }
        //Stuff
        Draw.color(Color.yellow);
        Lines3D.line(x2, y2, z2, x2 + Tmp.v31.x, y2 + Tmp.v31.y, z2 + Tmp.v31.z);
        Draw.color(Color.purple);
        Lines3D.line(x2, y2, z2, x2 + Tmp.v32.x, y2 + Tmp.v32.y, z2 + Tmp.v32.z);
    }

    public static void drawLineDebug(float x1, float y1, float z1, float x2, float y2, float z2){
        Lines3D.line(x1, y1, z1, x2, y2, z2);

        int pointCount = Lines3D.linePointCounts(x1, y1, z1, x2, y2, z2);
        float[] points = Lines3D.linePoints(x1, y1, z1, x2, y2, z2, pointCount);
        for(int i = 0; i < points.length; i += 3){
            float x = points[i],
                y = points[i + 1];
            Lines3D.line(x, y, 0, x, y, points[i + 2]);
        }
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
