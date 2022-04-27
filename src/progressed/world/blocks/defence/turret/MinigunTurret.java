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
    public float windupSpeed = 0.0001875f, windDownSpeed = 0.003125f, minFiringSpeed = 3f, logicSpeedScl = 0.25f, maxSpeed = 30f;
    public float barX, barY, barStroke, barLength;
    public float width = 1.5f, height = 0.75f;
    public float[] shootLocs; //TODO This can be replaced with shot patterns when v7 gets merged

    public TextureRegion barrelRegion, barrelOutline, bodyRegion, bodyOutline;

    public MinigunTurret(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        barrelRegion = Core.atlas.find(name + "-barrel");
        barrelOutline = Core.atlas.find(name + "-barrel-outline");
        bodyRegion = Core.atlas.find(name + "-body");
        bodyOutline = Core.atlas.find(name + "-body-outline");
    }

    @Override
    public void createIcons(MultiPacker packer){
        Outliner.outlineRegion(packer, barrelRegion, outlineColor, name + "-barrel-outline");
        Outliner.outlineRegion(packer, bodyRegion, outlineColor, name + "-body-outline");
        super.createIcons(packer);
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.remove(Stat.reload);
        float minValue = minFiringSpeed / 90f * 60f * shootLocs.length;
        float maxValue = maxSpeed / 90f * 60f * shootLocs.length;
        stats.add(Stat.reload, PMUtls.stringsFixed(minValue) + " - " + PMUtls.stringsFixed(maxValue) + StatUnit.perSecond.localized());
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-minigun-speed", (MinigunTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-minigun-speed", PMUtls.stringsFixed(entity.speedf() * 100f + entity.speedf() * 0.01f)),
            entity::barColor,
            entity::speedf
        ));
    }

    public class MinigunTurretBuild extends ItemTurretBuild{
        protected float[] heats = {0f, 0f, 0f, 0f};
        protected float spinSpeed, spin;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret - 0.2f);

            tr2.trns(rotation, -recoil);

            for(int i = 0; i < 4; i++){
                Tmp.v1.trns(rotation, width * Mathf.cosDeg(spin - 90 * i), height * Mathf.sinDeg(spin - 90 * i)).add(tr2);
                Drawf.shadow(barrelOutline, x + Tmp.v1.x - elevation, y + Tmp.v1.y - elevation, rotation - 90f);
            }
            Drawf.shadow(bodyOutline, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90f);
            Draw.rect(bodyOutline, x + tr2.x, y + tr2.y, rotation - 90f);

            for(int i = 0; i < 4; i++){
                Draw.z(Layer.turret - 0.2f);
                Tmp.v1.trns(rotation - 90f, width * Mathf.cosDeg(spin - 90 * i), height * Mathf.sinDeg(spin - 90 * i)).add(tr2);
                Draw.rect(barrelOutline, x + Tmp.v1.x, y + Tmp.v1.y, rotation - 90f);
                Draw.z(Layer.turret - 0.1f - Mathf.sinDeg(spin - 90 * i) / 100f);
                Draw.rect(barrelRegion, x + Tmp.v1.x, y + Tmp.v1.y, rotation - 90f);
                if(heats[i] > 0.001f){
                    Draw.blend(Blending.additive);
                    Draw.color(heatColor, heats[i]);
                    Draw.rect(heatRegion, x + Tmp.v1.x, y + Tmp.v1.y, rotation - 90f);
                    Draw.blend();
                    Draw.color();
                }
            }

            Draw.z(Layer.turret);

            Draw.rect(bodyRegion, x + tr2.x, y + tr2.y, rotation - 90f);

            if(speedf() > 0.0001f){
                Draw.color(barColor());
                Lines.stroke(barStroke);
                for(int i = 0; i < 2; i++){
                    tr2.trns(rotation - 90f, barX * Mathf.signs[i], barY - recoil);
                    Lines.lineAngle(x + tr2.x, y + tr2.y, rotation, barLength * Mathf.clamp(speedf()), false);
                }
            }
        }

        public Color barColor(){
            return spinSpeed > minFiringSpeed ? team.color : team.palette[2];
        }

        @Override
        public void updateTile(){
            if(!hasAmmo() || !isShooting() || !isActive()){
                spinSpeed = Mathf.lerpDelta(spinSpeed, 0, windDownSpeed);
            }

            if(spinSpeed > getMaxSpeed() && logicShooting){
                spinSpeed = Mathf.lerpDelta(spinSpeed, getMaxSpeed(), windDownSpeed);
            }

            float add = spinSpeed * (hasAmmo() ? peekAmmo().reloadMultiplier : 1f) * Time.delta;
            spin += add;
            reload += add;
            for(int i = 0; i < 4; i++){
                heats[i] = Mathf.lerpDelta(heats[i], 0f, cooldown);
            }
            
            super.updateTile();
        }

        @Override
        protected void updateShooting(){
            if(!hasAmmo()) return;

            float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;
            Liquid liquid = liquids.current();

            float used = Math.min(liquids.get(liquid), maxUsed * Time.delta) * baseReloadSpeed();
            if(spinSpeed < getMaxSpeed()) spinSpeed = Mathf.lerpDelta(spinSpeed, getMaxSpeed(), windupSpeed * (1 + used) * liquid.heatCapacity * coolantMultiplier * peekAmmo().reloadMultiplier * timeScale);
            liquids.remove(liquid, used);

            if(reload >= 90 && spinSpeed > minFiringSpeed){
                BulletType type = peekAmmo();

                shoot(type);

                reload = spin % 90;

                heats[Mathf.floor(spin - 90) % 360 / 90] = 1f;
                Log.info(Mathf.floor(spin) % 360 / 90);
            }
        }
        
        @Override
        protected void shoot(BulletType type){
            for(float shootLoc: shootLocs){
                if(hasAmmo()){
                    tr.trns(rotation - 90, shootLoc, shootLength - recoil);
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

        protected float getMaxSpeed(){
            return maxSpeed * (!isControlled() && logicShooting ? logicSpeedScl : 1f);
        }

        protected float speedf(){
            return spinSpeed / maxSpeed;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(spinSpeed);
            write.f(spin % 360f);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                spinSpeed = read.f();

                if(revision >= 3){
                    spin = read.f();
                }
            }
        }

        @Override
        public byte version(){
            return 3;
        }
    }
}
