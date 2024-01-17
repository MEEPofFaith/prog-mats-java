package progressed.graphics.renders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class PMRenderer{
    private static final BlackHoleRenderer blackHoleRenderer = new BlackHoleRenderer();
    private static final DimRenderer dimRenderer = new DimRenderer();

    public static void init(){
        Events.run(Trigger.drawOver, () -> {
            Draw.draw(Layer.shields + 1f, blackHoleRenderer::draw);
            Draw.draw(Layer.playerName + 1f, dimRenderer::draw);
        });
    }

    public static void blackHole(float x, float y, float inRadius, float outRadius, Color color){
        if(headless) return;
        blackHoleRenderer.add(x, y, inRadius, outRadius, color);
    }

    public static void dimAlpha(float a){
        dimRenderer.updateAlpha(a);
    }

    public static void dimGlow(float x, float y, float radius, float opacity){
        dimRenderer.add(x, y, radius, opacity);
    }

    public static void dimGlow(Position pos, float radius, float opacity){
        dimGlow(pos.getX(), pos.getY(), radius, opacity);
    }

    public static void dimGlow(float x, float y, TextureRegion region, float opacity){
        dimGlow(x, y, region, 0f, opacity);
    }

    public static void dimGlow(float x, float y, TextureRegion region, float rotation, float opacity){
        dimRenderer.add(x, y, region, rotation, opacity);
    }

    public static void dimGlowLine(float x, float y, float x2, float y2){
        dimRenderer.line(x, y, x2, y2, 30, 0.3f);
    }

    public static void dimGlowLine(float x, float y, float x2, float y2, float stroke, float alpha){
        dimRenderer.line(x, y, x2, y2, stroke, alpha);
    }
}
