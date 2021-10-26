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
    public float loadLength = -1f, width = -1f;
    public float lineStart = -1f, lineLength = -1f;
    public int arrows = 2;

    public PayloadLaunchTurret(String name){
        super(name);

        outlineIcon = true;
    }

    @Override
    public void init(){
        if(loadLength < 0) loadLength = size * tilesize / 4f;
        if(lineStart < 0) lineStart = shootLength + 1.5f;
        if(lineLength < 0) lineLength = loadLength + shootLength;
        if(width < 0) width = size * tilesize / 4f;
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
                float w = width + width * (1f - fin);

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

        protected void updateLaunching(){
            if(charging){
                charge += edelta();
                if(charge >= chargeTime){
                    charging = false;
                    charge = chargeTime;
                }
            }else{
                super.updateLaunching();
            }
        }

        protected void bullet(BulletType type){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x, y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            tr.trns(rotation, -recoil + loadProgress);
            float angle = rotation + Mathf.range(inaccuracy + type.inaccuracy);
            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        @Override
        public void updatePayload(){
            if(payload != null){
                if(hasArrived()){
                    tr.trns(rotation, -recoil + loadProgress);
                    payload.set(x + tr.x, y + tr.y, payRotation);
                }else{
                    payload.set(x + payVector.x, y + payVector.y, payRotation);
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(loadProgress);
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