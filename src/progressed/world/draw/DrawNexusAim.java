package progressed.world.draw;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.energy.NexusTurret.*;

import static progressed.graphics.DrawPseudo3D.*;

public class DrawNexusAim extends DrawBlock{
    public int beams = 1;
    public int points = 4;
    public float radius = 8f, pointRotation = 0f, beamRotation = 0f;
    public float pointWidth = 2f, pointInLen = 1f, pointOutLen = 1f;
    public float ringStroke = 2f, beamStroke = 1f;
    public float height = 1f;
    public Color color = PMPal.nexusLaser;
    public float layer = Layer.weather + 0.5f;
    public boolean alwaysBloom;
    public Func<NexusTurretBuild, Float> drawScale = b -> (float)Mathf.floorPositive(b.warmup());

    @Override
    public void draw(Building build){
        if(!(build instanceof NexusTurretBuild b)) return;

        float tx = b.targetPos.x, ty = b.targetPos.y;

        float z = Draw.z();
        if(layer > 0){
            Draw.z(layer + DrawPseudo3D.layerOffset(b.x, b.y, tx, ty));
        }else{
            Draw.z(z + DrawPseudo3D.layerOffset(b.x, b.y, tx, ty));
        }

        if(alwaysBloom){
            PMDrawf.bloom(() -> drawAim(b));
        }else{
            drawAim(b);
        }

        Draw.z(z);
    }

    public void drawAim(NexusTurretBuild b){
        float scl = drawScale.get(b);
        if(scl <= 0.01f) return;

        Tmp.v1.set(b.targetPos).sub(b).limit(b.range()).add(b);
        float tx = Tmp.v1.x, ty = Tmp.v1.y;

        float rad = radius * scl;
        Draw.color(color);
        if(radius > 0.01f){
            Lines.stroke(ringStroke * scl);
            Lines.circle(tx, ty, rad);
            if(points >= 1){
                float pW = pointWidth * scl, pInLen = pointInLen * scl, pOutLen = pointOutLen * scl;
                for(int i = 0; i < points; i++){
                    float rot = pointRotation + i * (360f / points);
                    Drawf.tri(tx + Angles.trnsx(rot, rad), ty + Angles.trnsy(rot, rad), pW, pInLen, rot + 180f);
                    Drawf.tri(tx + Angles.trnsx(rot, rad), ty + Angles.trnsy(rot, rad), pW, pOutLen, rot);
                }
            }
        }

        Lines.stroke(beamStroke * scl);
        float nx = xHeight(b.x, height),
            ny = yHeight(b.y, height);
        if(beams <= 1){ //Single beam to center
            Lines.line(nx, ny, tx, ty);
        }else{ //Multiple beams to ring
            for(int i = 0; i < beams; i++){
                float rot = beamRotation + i * (360f / beams);
                Lines.line(nx, ny, tx + Angles.trnsx(rot, rad), ty + Angles.trnsy(rot, rad));
            }
        }
    }
}
