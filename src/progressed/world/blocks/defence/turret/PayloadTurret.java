package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class PayloadTurret extends PayloadMissileTurret{
    public float rotateSpeed = 5;

    public float recoilAmount = 1f;
    public float restitution = 0.02f;
    public float uncharge = 0.25f;
    public float shootCone = 8f;
    public float loadLength = -1f, shootLength = -1, width = -1f;
    public float lineStart = -1f, lineLength = -1f;
    public int arrows = 2;

    public float chargeTime = 60f;

    protected Vec2 tr = new Vec2();
    protected Vec2 tr2 = new Vec2();

    protected TextureRegion baseRegion;
    public float elevation = -1f;

    public PayloadTurret(String name){
        super(name);

        outlineIcon = true;
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base", "block-" + size);
    }

    @Override
    public void init(){
        if(loadLength < 0) loadLength = size * tilesize / 4f;
        if(shootLength < 0) shootLength = size * tilesize / 2f;
        if(lineStart < 0) lineStart = shootLength + 1.5f;
        if(lineLength < 0) lineLength = loadLength + shootLength;
        if(width < 0) width = size * tilesize / 4f;
        if(elevation < 0) elevation = size / 2f;
        clipSize = Math.max(clipSize, size * tilesize + (lineLength + lineStart) * 2f);

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
        public float rotation = 90f, recoil, payLength, charge;
        public boolean loaded, charging, shooting;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90f) - 180f);
                }
            }

            if(payload != null){
                updatePayload();

                if(loaded){
                    payRotation = rotation - 90f;
                }

                Draw.z((hasArrived() || shooting) ? Layer.turret + 0.01f : Layer.blockOver);
                //payload.draw()
                Drawf.shadow(payload.x(), payload.y(), payload.size() * 2f);
                Draw.rect(payload.block().fullIcon, payload.x(), payload.y(), payRotation);
            }

            tr2.trns(rotation, -recoil);

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.turret);
            Drawf.shadow(region, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90f);
            Draw.z(Layer.turret + 0.02f);
            Draw.rect(region, x + tr2.x, y + tr2.y, rotation - 90f);

            if(heat >= 0.001f && heatRegion.found()){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, x + tr2.x, y + tr2.y, rotation - 90f);
                Draw.blend();
                Draw.color();
            }

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
            if(!validateTarget()) target = null;

            wasShooting = false;

            recoil = Mathf.lerpDelta(recoil, 0f, restitution);
            heat = Mathf.lerpDelta(heat, 0f, cooldown);
            if(!charging){
                charge = Mathf.lerpDelta(charge, 0f, uncharge);
            }

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
                    if(payLength > -loadLength){
                        payLength -= payloadSpeed * delta();
                        loading = true;
                    }else{
                        payLength = -loadLength;
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

                    turnToTarget(targetRot);

                    if(Angles.angleDist(rotation, targetRot) < shootCone && canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }

                if(shooting){
                    if(charging){
                        charge += edelta();
                        if(charge >= chargeTime){
                            charging = false;
                            charge = chargeTime;
                        }
                    }else{
                        BulletType type = peekAmmo();

                        if(payLength < shootLength){
                            payLength += peekAmmo().speed * delta();
                            if(payLength >= shootLength){
                                loaded = false;
                                payLength = shootLength;
                            }
                        }

                        if(!loaded){
                            shoot(type);
                            shooting = false;
                            reload %= reloadTime;
                        }
                    }
                }
            }else{
                moveInPayload(false); //Rotating is done elsewhere
            }

            if(acceptCoolant){
                updateCooling();
            }
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

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);
            recoil = recoilAmount;
            payLength = 0f;
        }

        protected void bullet(BulletType type){
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x, y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;

            tr.trns(rotation, -recoil + payLength);
            float angle = rotation + Mathf.range(inaccuracy + type.inaccuracy);
            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
        }

        @Override
        public void updatePayload(){
            if(payload != null){
                if(hasArrived()){
                    tr.trns(rotation, -recoil + payLength);
                    payload.set(x + tr.x, y + tr.y, payRotation);
                }else{
                    payload.set(x + payVector.x, y + payVector.y, payRotation);
                }
            }
        }
    }
}
