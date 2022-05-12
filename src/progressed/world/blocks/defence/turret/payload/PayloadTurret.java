package progressed.world.blocks.defence.turret.payload;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class PayloadTurret extends PayloadMissileTurret{
    public float rotateSpeed = 5;

    public float recoil = 1f;
    public float restitution = 0.02f;
    public float shootCone = 8f;
    public float shootY = -1;
    public float rotOffset = 0f;

    public float chargeTime = 0f;

    protected Vec2 drawOffset = new Vec2();
    protected Vec2 recoilOffset = new Vec2();

    public TextureRegion baseRegion;
    public float elevation = -1f;

    public PayloadTurret(String name){
        super(name);

        outlineIcon = true;
        shootSound = Sounds.artillery;
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base", Core.atlas.find("block-" + size, "prog-mats-block-" + size));
    }

    @Override
    public void init(){
        if(shootY < 0) shootY = size * tilesize / 2f;
        if(elevation < 0) elevation = size / 2f;

        super.init();
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, inRegion, topRegion, region};
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, req.drawx(), req.drawy());
        Draw.rect(topRegion, req.drawx(), req.drawy());
        Draw.rect(region, req.drawx(), req.drawy());
    }

    public class PayloadTurretBuild extends PayloadMissileTurretBuild{
        public float rotation = 90f, curRecoil;
        public boolean charging, launching;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                }
            }

            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.turret);
            recoilOffset.trns(rotation, -curRecoil);
            drawTurret();
            drawHeat();

            Draw.reset();
        }

        @Override
        public void drawPayload(){
            if(payload != null){
                updatePayload();

                payRotation = rotation - 90f + rotOffset;

                Draw.z(hasArrived() ? Layer.turret + 0.01f : Layer.blockOver);
                //payload.draw() but with rotation
                Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f * payloadf());
                Draw.rect(payload.block().fullIcon, payload.x(), payload.y(), payRotation);
            }
        }

        public void drawTurret(){
            Drawf.shadow(region, x + recoilOffset.x - elevation, y + recoilOffset.y - elevation, rotation - 90f);
            Draw.z(Layer.turret + 0.02f);
            Draw.rect(region, x + recoilOffset.x, y + recoilOffset.y, rotation - 90f);
        }

        public void drawHeat(){
            if(heat >= 0.001f && heatRegion.found()){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, x + recoilOffset.x, y + recoilOffset.y, rotation - 90f);
                Draw.blend();
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;

            wasShooting = false;

            curRecoil = Mathf.lerpDelta(curRecoil, 0f, restitution);
            heat = Mathf.lerpDelta(heat, 0f, cooldown);

            if(unit != null){
                unit.health(health);
                unit.rotation(rotation);
                unit.team(team);
                unit.set(x, y);
            }

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(hasAmmo()){
                updateLoading();

                if(timer(timerTarget, targetInterval)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot = true;

                    if(isControlled()){ //player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                    }else if(logicControlled()){ //logic behavior
                        canShoot = logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);

                        if(Float.isNaN(rotation)){
                            rotation = 0;
                        }
                    }

                    float targetRot = angleTo(targetPos);

                    if(shouldTurn()){
                        turnToTarget(targetRot);
                    }

                    if(Angles.angleDist(rotation, targetRot) < shootCone && canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }

                if(launching) updateLaunching();
            }else{
                moveInPayload(false); //Rotating is done elsewhere
            }

            updateCooling();
        }

        public boolean shouldTurn(){
            return true;
        }

        public void updateLoading(){}

        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * baseReloadSpeed());
        }

        @Override
        protected void updateShooting(){
            reloadCounter += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();

            if(reloadCounter > reload){
                charging = launching = true;
            }
        }

        protected void updateLaunching(){
            shoot(peekAmmo());
            charging = launching = false;
            reloadCounter %= reload;
        }

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);
            curRecoil = 1f;
        }

        protected void bullet(BulletType type){
            float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(x, y, targetPos.x, targetPos.y) / type.range, minRange / type.range, range / type.range) : 1f;

            drawOffset.trns(rotation, -curRecoil + shootY);
            float angle = rotation + Mathf.range(inaccuracy + type.inaccuracy);
            type.create(this, team, x + drawOffset.x, y + drawOffset.y, angle, 1f + Mathf.range(velocityRnd), lifeScl);
        }

        @Override
        public void updatePayload(){
            if(payload != null){
                if(hasArrived()){
                    drawOffset.trns(rotation, -curRecoil + shootY);
                    payload.set(x + drawOffset.x, y + drawOffset.y, payRotation);
                }else{
                    payload.set(x + payVector.x, y + payVector.y, payRotation);
                }
            }
        }

        public float payloadf(){
            return Mathf.clamp(payVector.len() / (size * tilesize / 2f));
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(rotation);
            write.bool(charging);
            write.bool(launching);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 4){
                rotation = read.f();
                charging = read.bool();
                launching = read.bool();
            }
        }

        @Override
        public byte version(){
            return 4;
        }
    }
}
