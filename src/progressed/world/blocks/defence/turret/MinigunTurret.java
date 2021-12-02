package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.util.*;

public class MinigunTurret extends ItemTurret{
    public float windupSpeed, windDownSpeed, minFiringSpeed, maxSpeed = 1f;
    public float barX, barY, barStroke, barLength;
    public float[] shootLocs;
    public Color c1 = Color.darkGray;

    public TextureRegion[] turretRegions = new TextureRegion[3], heatRegions = new TextureRegion[12];

    public MinigunTurret(String name){
        super(name);
        outlineIcon = false; //frame 1 is used for icon
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < 3; i++){
            turretRegions[i] = Core.atlas.find(name + "-frame-" + i);
        }
        region = turretRegions[0];
        for(int i = 0; i < 12; i++){
            heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        Outliner.outlineRegions(packer, turretRegions, outlineColor, name + "-frame");
        super.createIcons(packer);
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.remove(Stat.reload);
        float minValue = 60f / (3f / minFiringSpeed) * shootLocs.length;
        float maxValue = 60f / 3f * maxSpeed * shootLocs.length;
        stats.add(Stat.reload, PMUtls.stringsFixed(minValue) + " - " + PMUtls.stringsFixed(maxValue));
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-minigun-speed", (MinigunTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-minigun-speed", PMUtls.stringsFixed(entity.speedf() * 100f + entity.speedf() * 0.01f)),
            () -> entity.speedf() > minFiringSpeed ? entity.team.color : Tmp.c1.set(c1).lerp(entity.team.color, Mathf.curve(entity.frameSpeed, 0f, minFiringSpeed) / 2f),
            entity::speedf
        ));
    }

    public class MinigunTurretBuild extends ItemTurretBuild{
        protected float[] heats = {0f, 0f, 0f, 0f};
        protected int[] heatFrames = {0, 0, 0, 0};
        protected int frame;
        protected float frameSpeed, trueFrame;
        protected boolean shouldShoot;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            tr2.trns(rotation, -recoil);

            int f = Mathf.clamp(frame, 0, 3);
            Drawf.shadow(turretRegions[f], x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90f);
            Draw.rect(turretRegions[f], x + tr2.x, y + tr2.y, rotation - 90f);

            for(int i = 0; i < 4; i++){
                if(heats[i] > 0.001f){
                    Draw.blend(Blending.additive);
                    Draw.color(heatColor, heats[i]);
                    Draw.rect(heatRegions[heatFrames[i]], x + tr2.x, y + tr2.y, rotation - 90f);
                    Draw.blend();
                    Draw.color();
                }
            }

            if(speedf() > 0.0001f){
                Draw.color(speedf() > minFiringSpeed ? team.color : Tmp.c1.set(c1).lerp(team.color, Mathf.curve(speedf(), 0f, minFiringSpeed) / 2f));
                Lines.stroke(barStroke);
                for(int i = 0; i < 2; i++){
                    tr2.trns(rotation - 90f, barX * Mathf.signs[i], barY - recoil);
                    Lines.lineAngle(x + tr2.x, y + tr2.y, rotation, barLength * Mathf.clamp(speedf()));
                }
            }
        }

        @Override
        public void updateTile(){
            if(!hasAmmo() || !isShooting() || !isActive()){
                frameSpeed = Mathf.lerpDelta(frameSpeed, 0, windDownSpeed);
            }

            trueFrame = trueFrame + frameSpeed * (hasAmmo() ? peekAmmo().reloadMultiplier : 1f) * Time.delta;
            frame = Mathf.floor(trueFrame % 3f);
            for(int i = 0; i < 4; i++){
                heatFrames[i] = Mathf.mod(Mathf.floor(trueFrame % 12) - (i * 3), 12);
                heats[i] = Mathf.lerpDelta(heats[i], 0f, cooldown);
            }

            if(frame != 0){
                shouldShoot = true;
            }
            
            super.updateTile();
        }

        @Override
        protected void updateShooting(){
            if(hasAmmo()){
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

                Liquid liquid = liquids.current();

                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((reloadTime - reload) / coolantMultiplier) / liquid.heatCapacity)) * baseReloadSpeed();
                frameSpeed = Mathf.lerpDelta(frameSpeed, maxSpeed, windupSpeed * (1 + used) * liquid.heatCapacity * coolantMultiplier * peekAmmo().reloadMultiplier * timeScale);
                liquids.remove(liquid, used);

                if(frame == 0 && shouldShoot && speedf() > minFiringSpeed){
                    BulletType type = peekAmmo();

                    shoot(type);

                    shouldShoot = false;

                    heats[Mathf.floor(trueFrame) % 12 / 3] = 1f;
                }
            }
        }
        
        @Override
        protected void shoot(BulletType type){
            for(int i = 0; i < shootLocs.length; i++){
                if(hasAmmo()){
                    tr.trns(rotation - 90, shootLocs[i], shootLength);
                    bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy));
                    effects();
                    useAmmo();
                }
            }
        }

        @Override
        protected void updateCooling(){
            //Do nothing, cooling is already in `updateShooting()`
        }

        protected float speedf(){
            return frameSpeed / maxSpeed;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(frameSpeed);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                frameSpeed = read.f();
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}