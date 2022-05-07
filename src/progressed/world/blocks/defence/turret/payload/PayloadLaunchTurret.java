package progressed.world.blocks.defence.turret.payload;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadLaunchTurret extends PayloadTurret{
    public float uncharge = 0.25f;
    public float lineSpacing = -1f, lineStart = -1f, lineLength = -1f;
    public int arrows = 2;

    public PayloadLaunchTurret(String name){
        super(name);

        outlineIcon = true;
    }

    @Override
    public void init(){
        if(lineStart < 0) lineStart = size * tilesize / 2f + 1.5f;
        if(lineLength < 0) lineLength = size * tilesize / 2f;
        if(lineSpacing < 0) lineSpacing = size * tilesize / 4f;
        clipSize = Math.max(clipSize, size * tilesize + (lineLength + lineStart) * 2f);

        super.init();
    }

    public class PayloadLaunchTurretBuild extends PayloadTurretBuild{
        public float charge, materialization;

        @Override
        public void drawPayload(){
            if(payload != null){
                updatePayload();

                if(hasArrived()){
                    payRotation = rotation - 90f + rotOffset;
                    Draw.draw(Layer.turret + 0.01f, () -> {
                        PMShaders.materializeShader.region = payload.block().fullIcon;
                        PMShaders.materializeShader.progress = Mathf.clamp(materialization / chargeTime * 2f);
                        PMShaders.materializeShader.color = team.color;
                        PMShaders.materializeShader.offset = 0.1f;

                        Draw.shader(PMShaders.materializeShader);
                        Draw.rect(payload.block().fullIcon, payload.x(), payload.y(), payRotation);
                        Draw.shader();

                        Draw.reset();
                    });
                }else{
                    Draw.z(Layer.blockOver);
                    //payload.draw() but with rotation
                    Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f * payloadf());
                    Draw.rect(payload.block().fullIcon, payload.x(), payload.y(), payRotation);
                }
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(charge > 0){
                Draw.z(Layer.effect);

                float fin = Interp.pow2Out.apply(charge / chargeTime);
                float w = lineSpacing + lineSpacing * (1f - fin);

                Tmp.v1.trns(rotation, lineStart, -w);
                Tmp.v2.trns(rotation, lineStart, w);

                Lines.stroke(fin * 1.2f, Pal.accent);
                Lines.lineAngle(x + Tmp.v1.x + tr2.x, y + Tmp.v1.y + tr2.y, rotation, lineLength);
                Lines.lineAngle(x + Tmp.v2.x + tr2.x, y + Tmp.v2.y + tr2.y, rotation, lineLength);

                Draw.scl(fin * 1.1f);
                for(int i = 0; i < arrows; i++){
                    Tmp.v3.trns(rotation, lineStart + lineLength / (arrows + 1) * (i + 1));
                    Draw.rect("bridge-arrow", x + Tmp.v3.x + tr2.x, y + Tmp.v3.y + tr2.y, rotation);
                }
                Draw.scl();
            }

            Draw.reset();
        }

        @Override
        public void updateTile(){
            if(!charging) charge = Mathf.lerpDelta(charge, 0f, uncharge);

            super.updateTile();
        }

        @Override
        protected void updateLaunching(){
            if(charging){
                charge += edelta();
                materialization += edelta();
                if(charge >= chargeTime){
                    charge = chargeTime;
                    materialization = 0f;
                    super.updateLaunching();
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
            write.f(materialization);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 4){
                charge = read.f();
                materialization = read.f();
            }
        }
    }
}
