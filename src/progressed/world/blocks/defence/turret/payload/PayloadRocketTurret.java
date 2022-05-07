package progressed.world.blocks.defence.turret.payload;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.*;
import progressed.content.*;
import progressed.entities.bullet.explosive.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class PayloadRocketTurret extends PayloadTurret{
    public float doorOffset = 0, doorLength = -1f, doorWidth = -1f;
    public float riseTime = 60f;
    public float minScl = 0.875f;
    public Color[] doorColors = {PMPal.lightGray, PMPal.darkGray, PMPal.midGray};
    public boolean leadTargets = true;

    public TextureRegion turretRegion, turretTop, fullRegion;

    public PayloadRocketTurret(String name){
        super(name);

        outlinedIcon = 3;
        shootSound = ProgMats.farting() ? PMSounds.gigaFard : PMSounds.rocketLaunch;
    }

    @Override
    public void load(){
        super.load();
        turretRegion = Core.atlas.find(name + "-turret");
        turretTop = Core.atlas.find(name + "-turret-top");
        fullRegion = Core.atlas.find(name + "-icon");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            baseRegion, inRegion, topRegion,
            region, fullRegion
        };
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, req.drawx(), req.drawy());
        Draw.rect(topRegion, req.drawx(), req.drawy());
        Draw.rect(region, req.drawx(), req.drawy());
        Draw.rect(fullRegion, req.drawx(), req.drawy());
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

                boolean ready = hasArrived();
                if(ready) payRotation = rotation - 90f + rotOffset;
                float a = ready ? Mathf.curve(risef(), 0.375f, 0.625f) : 1f;
                Draw.z(Layer.blockOver);
                Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f * payloadf());
                if(ready) Draw.z(Layer.turret + 0.01f);
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
                float scl = Mathf.curve(risef(), 0f, 0.375f) - Mathf.curve(risef(), 0.625f, 1f),
                    progress = Interp.sineIn.apply(scl);
                Draw.color(doorColors[2], doorColors[num], progress);
                tr.trns(rotation - 90f, (doorWidth / 4f + doorWidth / 4f * progress) * Mathf.signs[num], doorOffset);
                Fill.rect(
                    x + tr2.x + tr.x, y + tr2.y + tr.y,
                    doorWidth / 2f * (1f - progress), doorLength,
                    rotation - 90f
                );
            }
            Draw.color();
            Draw.rect(turretRegion, x + tr2.x, y + tr2.y, rotation - 90f);
            Draw.z(Layer.turret + 0.02f);
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

        protected void findTarget(){
            Sortf sort = peekAmmo() instanceof RocketBulletType b ? b.unitSort : unitSort;
            if(targetAir && !targetGround){
                target = Units.bestEnemy(team, x, y, range, e -> !e.dead() && !e.isGrounded(), sort);
            }else{
                target = Units.bestTarget(team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> true, sort);
            }
        }

        @Override
        public void updateLoading(){
            super.updateLoading();
            if(riseProgress < riseTime){
                riseProgress += delta();
                riseProgress = Mathf.clamp(riseProgress, 0f, riseTime);
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

        public float risef(){
            return riseProgress / riseTime;
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
