package progressed.world.blocks.defence.turret.payload;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;

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
        if(loadTime < 0) loadTime = size * tilesize / 4f;
        if(lineStart < 0) lineStart = shootLength + 1.5f;
        if(lineLength < 0) lineLength = loadTime + shootLength;
        if(lineSpacing < 0) lineSpacing = size * tilesize / 4f;
        clipSize = Math.max(clipSize, size * tilesize + (lineLength + lineStart) * 2f);

        super.init();
    }

    public class PayloadLaunchTurretBuild extends PayloadTurretBuild{
        public float charge;

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
        public void loadPayload(){
            if(loadProgress > -loadTime){
                loadProgress -= payloadSpeed * delta();
                loading = true;
            }else{
                loadProgress = -loadTime;
                loading = false;
            }
        }

        @Override
        protected void updateLaunching(){
            if(charging){
                charge += edelta();
                if(charge >= chargeTime){
                    charging = false;
                    charge = chargeTime;
                }
            }else{
                BulletType type = peekAmmo();

                if(loadProgress < shootLength){
                    loadProgress += type.speed * delta();
                    if(loadProgress >= shootLength){
                        loaded = false;
                        loadProgress = shootLength;
                    }
                }

                if(!loaded){
                    shoot(type);
                    shooting = false;
                    reload %= reloadTime;
                }
            }
        }

        @Override
        public float payloadOffset(){
            return loadProgress;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision == 1){
                rotation = read.f();
                loadProgress = read.f();
                charge = read.f();
                loaded = read.bool();
                charging = read.bool();
                shooting = read.bool();
            }else if(revision >= 2){
                charge = read.f();
            }
        }
    }
}