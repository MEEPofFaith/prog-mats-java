package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.util.*;
import progressed.world.meta.*;

import static arc.Core.*;

public class SniperTurret extends ItemTurret{
    public int partCount = 3;
    public float split, chargeMoveFract = 0.9f;

    public TextureRegion[] outlines, connectors, parts, heats, cHeats;

    public SniperTurret(String name){
        super(name);

        cooldown = 0.01f;
        unitSort = (u, x, y) -> -u.maxHealth + u.dst2(x, y) / 6400f;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes));
    }

    @Override
    public void load(){
        super.load();

        outlines = new TextureRegion[partCount];
        parts = new TextureRegion[partCount];
        connectors = new TextureRegion[partCount - 1];
        heats = new TextureRegion[partCount];
        cHeats = new TextureRegion[partCount - 1];
        
        for(int i = 0; i < partCount; i++){
            outlines[i] = atlas.find(name + "-outline-" + i);
            parts[i] = atlas.find(name + "-part-" + i);
            heats[i] = atlas.find(name + "-heat-" + i);
            if(i < partCount - 1){
                connectors[i] = atlas.find(name + "-connector-" + i);
                cHeats[i] = atlas.find(name + "-connector-heat-" + i);
            }
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        
        bars.add("pm-reload", (SniperTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reload / reloadTime)
        ));

        bars.add("pm-charge", (SniperTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-charge", PMUtls.stringsFixed(Mathf.clamp(entity.charge) * 100f)),
            () -> Pal.surge,
            () -> entity.charge
        ));
    }

    public class SniperTurretBuild extends ItemTurretBuild{
        protected float charge;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            tr2.trns(rotation, -recoil);

            for(int i = 0; i < partCount; i++){
                float tx = Angles.trnsx(rotation, split * charge * i);
                float ty = Angles.trnsy(rotation, split * charge * i);
                Drawf.shadow(outlines[i], x + tr2.x + tx - elevation, y + tr2.y + ty - elevation, rotation - 90);
            }

            for(int i = 0; i < partCount; i++){
                float tx = Angles.trnsx(rotation, split * charge * i);
                float ty = Angles.trnsy(rotation, split * charge * i);
                Draw.rect(outlines[i], x + tr2.x + tx, y + tr2.y + ty, rotation - 90);
            }

            for(int i = 0; i < partCount - 1; i++){
                if(Core.atlas.isFound(connectors[i])){
                    float tx = Angles.trnsx(rotation, split * charge * (i + 0.5f));
                    float ty = Angles.trnsy(rotation, split * charge * (i + 0.5f));
                    Draw.rect(connectors[i], x + tr2.x + tx, y + tr2.y + ty, rotation - 90);
                    if(heat > 0.001f){
                        if(Core.atlas.isFound(cHeats[i])){
                            Draw.color(heatColor, heat);
                            Draw.blend(Blending.additive);
                            Draw.rect(cHeats[i], x + tr2.x + tx, y + tr2.y + ty, rotation - 90);
                            Draw.blend();
                            Draw.color();
                        }
                    }
                }
            }

            for(int i = 0; i < partCount; i++){
                float tx = Angles.trnsx(rotation, split * charge * i);
                float ty = Angles.trnsy(rotation, split * charge * i);
                Draw.rect(parts[i], x + tr2.x + tx, y + tr2.y + ty, rotation - 90);
            }

            if(heat > 0.001f){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                for(int i = 0; i < partCount; i++){
                    if(Core.atlas.isFound(heats[i])){
                        float tx = Angles.trnsx(rotation, split * charge * i);
                        float ty = Angles.trnsy(rotation, split * charge * i);
                        Draw.rect(heats[i], x + tr2.x + tx, y + tr2.y + ty, rotation - 90);
                    }
                }
                Draw.blend();
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            if(charging){
                charge = Mathf.clamp(charge + Time.delta / chargeTime);
            }else{
                charge = 0;
            }
    
            super.updateTile();
        }

        @Override
        protected void updateShooting(){
            if(consValid()){
                if(reload >= reloadTime && !charging){
                    BulletType type = peekAmmo();
        
                    shoot(type);
                }else if(hasAmmo() && reload < reloadTime){
                    reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
                }
            }
        }

        @Override
        protected void shoot(BulletType type){
            tr.trns(rotation, shootLength + split * (partCount - 1f));
            chargeBeginEffect.at(x + tr.x, y + tr.y, rotation);
            chargeSound.at(x + tr.x, y + tr.y, 1);

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength + split * (partCount - 1f));
                    chargeEffect.at(x + tr.x, y + tr.y, rotation);
                });
            }

            charging = true;

            Time.run(chargeTime, () -> {
                if(!isValid()) return;
                tr.trns(rotation, shootLength + split * (partCount - 1f));
                recoil = recoilAmount;
                heat = 1f;
                bullet(type, rotation + Mathf.range(inaccuracy));
                useAmmo();
                effects();
                reload = 0;
                charging = false;
            });
        }

        @Override
        protected void updateCooling(){
            if(hasAmmo() && consValid()){
                super.updateCooling();
            }
        }
        
        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency() * rotateSpeed * delta() * (charging ? (1 - chargeMoveFract * charge) : 1));
        }
        
        @Override
        public boolean shouldTurn(){
            return true;
        }
    }
}