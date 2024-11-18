package progressed.graphics.perspective;

import arc.math.*;
import arc.math.geom.*;

import static arc.Core.*;

public class Perspective{
    private static final Vec2 offsetPos = new Vec2();
    public static  float viewportOffset = 8f;
    public static  float fov = 45f;

    public static boolean canDraw(float z){
        return z < cameraHeight();
    }

    public static Vec2 drawPos(float x, float y, float z){
        //viewport
        Vec2 v = viewportSize();
        float vw = v.x, vh = v.y;
        float cx = camera.position.x, cy = camera.position.y;
        float cz = cameraHeight();

        x -= cx;
        y -= cy;
        z = cz - z;

        float vx = x / z * viewportOffset,
            vy = y / z * viewportOffset;

        offsetPos.set(vx / vw * camera.width, vy / vh * camera.height).add(cx, cy);
        return offsetPos;
    }

    /** Calculates the camera height based on FOV and the size of the vanilla camera. */
    public static  float cameraHeight(){
        float width = Math.max(camera.width, camera.height) / 2f;
        //TOA
        return (float)(width / Math.tan(fov / 2f * Mathf.degRad));
    }

    /** Calculates the size of the viewport. */
    public static  Vec2 viewportSize(){
        float v1 = (float)(Math.tan(fov / 2f * Mathf.degRad) * viewportOffset * 2f);
        if(camera.width >= camera.height){
            float v2 = v1 * (camera.height / camera.width);
            return offsetPos.set(v1, v2);
        }else{
            float v2 = v1 * (camera.width / camera.height);
            return offsetPos.set(v2, v1);
        }
    }
}
