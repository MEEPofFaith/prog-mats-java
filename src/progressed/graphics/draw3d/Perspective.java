package progressed.graphics.draw3d;

import arc.math.*;
import arc.math.geom.*;
import progressed.util.*;

import static arc.Core.*;

public class Perspective{
    private static final Vec2 offsetPos = new Vec2();
    /** Viewport offset from the camera height in world units. */
    public static float viewportOffset = 8f;
    /** Field of View in degrees */
    public static float fov = settings.getInt("pm-fov", 60);

    /** @return If the z coordinate is below the viewport height. */
    public static boolean canDraw(float z){
        return z < cameraZ() - viewportOffset;
    }

    /** @return Perspective projected coordinates to draw at. */
    public static Vec2 drawPos(float x, float y, float z){
        //viewport
        Vec2 v = viewportSize();
        float vw = v.x, vh = v.y;
        float cx = camera.position.x, cy = camera.position.y;
        float cz = cameraZ();

        x -= cx;
        y -= cy;
        z = cz - z;

        float vx = x / z * viewportOffset,
            vy = y / z * viewportOffset;

        offsetPos.set(vx / vw * camera.width, vy / vh * camera.height).add(cx, cy);
        return offsetPos;
    }

    /** Multiplicative size scale at a point. */
    public static float scale(float x, float y, float z){
        float cx = camera.position.x, cy = camera.position.y;
        float cz = cameraZ();

        x -= cx;
        y -= cy;
        z = cz - z;

        float px = x / z * cz;
        float py = y / z * cz;

        float c1 = Mathf.dst(x, y), c2 = Mathf.dst(px, py);
        float d1 = Mathf.dst(z, c1), d2 = Mathf.dst(cz, c2);

        return d2 / d1;
    }

    /** Fade out based on distance to viewport. */
    public static float alpha(float x, float y, float z){
        float cx = camera.position.x, cy = camera.position.y;
        float cz = cameraZ();

        float d1 = Math3D.dst(x, y, z, cx, cy, cz); //Distance between camera point

        x -= cx;
        y -= cy;
        float pz = cz - z;

        float vx = x / pz * viewportOffset,
            vy = y / pz * viewportOffset;

        float d2 = Math3D.dst(vx, vy, z); //Distance between camera and viewport pos

        float dst = d1 - d2;
        float fadeDst = (cz - viewportOffset) / 8f;

        if(dst > fadeDst){
            return 1f;
        }else if(!canDraw(z)){ //Behind viewport, should be 0
            return 0f;
        }else{
            return Mathf.clamp(dst / fadeDst);
        }
    }

    /**
     * Calculates the camera z coordinate based on FOV and the size of the vanilla camera.
     * @return camera z coordinate
     * */
    public static float cameraZ(){
        float width = Math.max(camera.width, camera.height) / 2f;
        //TOA
        return (float)(width / Math.tan(fov / 2f * Mathf.degRad));
    }

    /**
     * @return viewport z coordinate
     */
    public static float viewportZ(){
        return cameraZ() - viewportOffset;
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
