package progressed.world.blocks.defence.turret.payload;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadRocketTurret extends PayloadTurret{
    public float doorOffset = 0, doorLength = -1f, doorWidth = -1f;
    public float riseTime = 60f;
    public float minScl = 0.875f;
    public Color[] doorColors = {PMPal.lightGray, PMPal.darkGray, PMPal.midGray};
    public boolean leadTargets = true;

    public TextureRegion turretTop;

    public PayloadRocketTurret(String name){
        super(name);

        shootSound = Core.settings.getBool("pm-farting") ? Sounds.wind3 : PMSounds.rocketLaunch;
    }

    @Override
    public void load(){
        super.load();
        turretTop = Core.atlas.find(name + "-turret-top");
    }

    @Override
    public void init(){
        super.init();

        if(doorLength < 0) doorLength = size * tilesize / 2f;
        if(doorWidth < 0) doorWidth = size * tilesize / 8f;
    }

    public class PayloadRocketTurretBuild extends PayloadTurretBuild{
        public float riseProgress;

        @Override
        public void drawPayload(){
            if(payload != null){
                updatePayload();

                if(loaded){
                    payRotation = rotation - 90f + rotOffset;
                }

                boolean ready = hasArrived() && loaded;
                float a = ready ? Mathf.curve(risef(), 0.375f, 0.625f) : 1f;
                Draw.z(Layer.blockOver);
                Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f, ready ? 1 - a : a);
                if(ready) Draw.z(Layer.turret + 0.02f);
                //payload.draw() but with rotation
                Draw.alpha(a);
                TextureRegion pRegion = payload.block().fullIcon;
                float scl = minScl + a * (1 - minScl);
                Draw.rect(pRegion, payload.x(), payload.y(), pRegion.width / 4f * scl, pRegion.height / 4f * scl, payRotation);
                Draw.color();
            }
        }

        public void drawTurret(){
            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90f);
            Draw.rect(region, x + tr2.x, y + tr2.y, rotation - 90f);
            for(int num : Mathf.zeroOne){
                float scl = Mathf.curve(risef(), 0f, 0.375f) - Mathf.curve(risef(), 0.625f, 1f);
                Draw.color(doorColors[num], doorColors[2], Interp.pow2Out.apply(scl));
                tr.trns(rotation - 90f, (doorWidth / 4f + doorWidth / 4f * scl) * Mathf.signs[num], doorOffset);
                Fill.rect(
                    x + tr2.x + tr.x, y + tr2.y + tr.y,
                    doorWidth / 2f * (1f - scl), doorLength,
                    rotation - 90f
                );
            }
            Draw.color();
            Draw.rect(turretTop, x + tr2.x, y + tr2.y, rotation - 90f);
        }

        @Override
        public void targetPosition(Posc pos){
            if(!hasAmmo() || pos == null) return;
            if(leadTargets){
                BulletType bullet = peekAmmo();

                var offset = Tmp.v1.setZero();

                if(pos instanceof Hitboxc h){
                    offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
                }

                targetPos.set(Predict.intercept(this, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
            }

            if(!leadTargets || targetPos.isZero()){
                targetPos.set(pos);
            }
        }


        @Override
        public void updateLoading(){
            super.updateLoading();
            if(loaded){
                if(riseProgress < riseTime){
                    riseProgress += delta();
                }else{
                    riseProgress = riseTime;
                }
            }
        }

        @Override
        protected void updateShooting(){
            if(riseProgress >= riseTime) super.updateShooting();
        }

        @Override
        protected void updateLaunching(){
            super.updateLaunching();
            riseProgress = 0f;
        }

        @Override
        public void loadPayload(){
            if(!rotating) super.loadPayload();
        }

        public float risef(){
            return riseProgress / riseTime;
        }

        @Override
        public float payloadOffset(){
            return loadProgress / loadTime * shootLength;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(riseProgress);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            riseProgress = read.f();
        }
    }
}