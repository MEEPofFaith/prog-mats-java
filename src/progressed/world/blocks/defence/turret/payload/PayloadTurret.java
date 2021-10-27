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

    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float shootCone = 8f;
    public float shootLength = -1;

    public float chargeTime = -1f;
    public float loadTime = 20f;

    protected Vec2 tr = new Vec2();
    protected Vec2 tr2 = new Vec2();

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

        baseRegion = Core.atlas.find(name + "-base", "block-" + size);
    }

    @Override
    public void init(){
        if(shootLength < 0) shootLength = size * tilesize / 2f;
        if(elevation < 0) elevation = size / 2f;

        super.init();
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, inRegion, topRegion, region};
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, req.drawx(), req.drawy());
        Draw.rect(topRegion, req.drawx(), req.drawy());
        Draw.rect(region, req.drawx(), req.drawy());
    }

    public class PayloadTurretBuild extends PayloadMissileTurretBuild{
        public float rotation = 90f, recoil, loadProgress;
        public boolean charging, loaded, shooting;

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

            tr2.trns(rotation, -recoil);

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            drawTurret();

            drawHeat();

            Draw.reset();
        }

        @Override
        public void drawPayload(){
            if(payload != null){
                updatePayload();

                if(loaded){
                    payRotation = rotation - 90f;
                }

                Draw.z((hasArrived()) ? Layer.turret + 0.01f : Layer.blockOver);
                //payload.draw() but with rotation
                Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f);
                Draw.rect(payload.block().fullIcon, payload.x(), payload.y(), payRotation);
            }
        }

        public void drawTurret(){
            Draw.z(Layer.turret);
            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90f);
            Draw.z(Layer.turret + 0.02f);
            Draw.rect(region, x + tr2.x, y + tr2.y, rotation - 90f);
        }

        public void drawHeat(){
            if(heat >= 0.001f && heatRegion.found()){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, x + tr2.x, y + tr2.y, rotation - 90f);
                Draw.blend();
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;

            wasShooting = false;

            recoil = Mathf.lerpDelta(recoil, 0f, restitution);
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
                if(!loaded){
                    boolean loading;
                    if(loadProgress > -loadTime){
                        loadProgress -= payloadSpeed * delta();
                        loading = true;
                    }else{
                        loadProgress = -loadTime;
                        loading = false;
                    }

                    boolean rotating;
                    if(!Angles.within(payRotation, rotation - 90f, 0.01f)){
                        payRotation = Angles.moveToward(payRotation, rotation - 90f, payloadRotateSpeed * edelta());
                        rotating = true;
                    }else{
                        rotating = false;
                    }

                    if(!loading && !rotating){
                        loaded = true;
                    }
                }

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

                if(shooting){
                    updateLaunching();
                }
            }else{
                moveInPayload(false); //Rotating is done elsewhere
            }

            if(acceptCoolant){
                updateCooling();
            }
        }

        public boolean shouldTurn(){
            return loaded;
        }

        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * delta() * baseReloadSpeed());
        }

        @Override
        protected void updateShooting(){
            reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();

            if(reload > reloadTime && loaded && !shooting){
                charging = true;
                shooting = true;
            }
        }

        protected void updateLaunching(){
            BulletType type = peekAmmo();

            if(loadProgress < shootLength){
                loadProgress += peekAmmo().speed * delta();
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

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);
            recoil = recoilAmount;
            loadProgress = 0f;
        }

        protected void bullet(BulletType type){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x, y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            tr.trns(rotation, -recoil);
            float angle = rotation + Mathf.range(inaccuracy + type.inaccuracy);
            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        @Override
        public void updatePayload(){
            if(payload != null){
                if(hasArrived()){
                    tr.trns(rotation, -recoil);
                    payload.set(x + tr.x, y + tr.y, payRotation);
                }else{
                    payload.set(x + payVector.x, y + payVector.y, payRotation);
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(rotation);
            write.f(loadProgress);
            write.bool(loaded);
            write.bool(charging);
            write.bool(shooting);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                rotation = read.f();
                loadProgress = read.f();
                loaded = read.bool();
                charging = read.bool();
                shooting = read.bool();
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}