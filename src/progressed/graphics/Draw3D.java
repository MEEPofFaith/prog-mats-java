package progressed.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

public class Draw3D{
    /**
     * Originally by
     * @author sunny
     * Modified by
     * @author MEEP
     * */
    public static void wall(float x1, float y1, float x2, float y2, float baseHeight, float height, Color baseColor, Color topColor){
        float c1f = baseColor.toFloatBits();
        float c2f = topColor.toFloatBits();
        float x3 = x1 + cameraXOffset(x1, height);
        float y3 = y1 + cameraYOffset(y1, height);
        float x4 = x2 + cameraXOffset(x2, height);
        float y4 = y2 + cameraYOffset(y2, height);
        x1 += cameraXOffset(x1, baseHeight);
        y1 += cameraYOffset(y1, baseHeight);
        x2 += cameraXOffset(x2, baseHeight);
        y2 += cameraYOffset(y2, baseHeight);

        Fill.quad(x1, y1, c1f, x2, y2, c1f, x4, y4, c2f, x3, y3, c2f);
    }

    public static void wall(float x1, float y1, float x2, float y2, float height, Color baseColor, Color topColor){
        wall(x1, y1, x2, y2, 0f, height, baseColor, topColor);
    }

    public static void cylinder(float x, float y, float rad, float baseHeight, float height, Color baseColor, Color topColor){
        int vert = Lines.circleVertices(rad);
        float space = 360f / vert;

        float c1f = baseColor.toFloatBits();
        float c2f = topColor.toFloatBits();

        for(int i = 0; i < vert; i++){
            float a = space * i, cos = Mathf.cosDeg(a), sin = Mathf.sinDeg(a), cos2 = Mathf.cosDeg(a + space), sin2 = Mathf.sinDeg(a + space);

            float x1 = x + rad * cos,
                y1 = y + rad * sin,
                x2 = x + rad * cos2,
                y2 = y + rad * sin2;

            float x3 = x1 + cameraXOffset(x1, height);
            float y3 = y1 + cameraYOffset(y1, height);
            float x4 = x2 + cameraXOffset(x2, height);
            float y4 = y2 + cameraYOffset(y2, height);
            x1 += cameraXOffset(x1, baseHeight);
            y1 += cameraYOffset(y1, baseHeight);
            x2 += cameraXOffset(x2, baseHeight);
            y2 += cameraYOffset(y2, baseHeight);

            Fill.quad(x1, y1, c1f, x2, y2, c1f, x4, y4, c2f, x3, y3, c2f);
        }
    }

    public static void cylinder(float x, float y, float rad, float height, Color baseColor, Color topColor){
        cylinder(x, y, rad, 0f, height, baseColor, topColor);
    }

    public static float cameraXOffset(float x, float height){
        return (x - Core.camera.position.x) * height;
    }

    public static float cameraYOffset(float y, float height){
        return (y - Core.camera.position.y) * height;
    }
}