package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class MinigunTurret extends ItemTurret{
    public float windupSpeed = 0.00625f, windDownSpeed = 0.0125f, minFiringSpeed = 3f, logicSpeedScl = 0.25f, maxSpeed = 30f;
    public float barX, barY, barStroke, barLength;
    public float barWidth = 1.5f, barHeight = 0.75f;

    public MinigunTurret(String name){
        super(name);

        drawer = new DrawTurret(){
            TextureRegion barrel, barrelOutline;

            @Override
            public void getRegionsToOutline(Block block, Seq<TextureRegion> out){
                super.getRegionsToOutline(block, out);
                out.add(barrel);
            }

            @Override
            public void load(Block block){
                super.load(block);

                barrel = Core.atlas.find(block.name + "-barrel");
                barrelOutline = Core.atlas.find(block.name + "-barrel-outline");
            }

            @Override
            public void drawTurret(Turret block, TurretBuild build){
                if(!(build instanceof MinigunTurretBuild m)) return;

                Vec2 v = Tmp.v1;

                Draw.z(Layer.turret- 0.01f);
                Draw.rect(outline, build.x + m.recoilOffset.x, build.y + m.recoilOffset.y, build.drawrot());
                for(int i = 0; i < 4; i++){
                    Draw.z(Layer.turret - 0.01f);
                    v.trns(m.rotation - 90f, barWidth * Mathf.cosDeg(m.spin - 90 * i), barHeight * Mathf.sinDeg(m.spin - 90 * i)).add(m.recoilOffset);
                    Draw.rect(barrelOutline, m.x + v.x, m.y + v.y, m.drawrot());
                    Draw.z(Layer.turret - 0.005f - Mathf.sinDeg(m.spin - 90 * i) / 1000f);
                    Draw.rect(barrel, m.x + v.x, m.y + v.y, m.drawrot());
                    if(m.heats[i] > 0.001f){
                        Drawf.additive(heat, heatColor.write(Tmp.c1).a(m.heat), m.x + v.x, m.y + v.y, m.drawrot(), Layer.turretHeat);
                    }
                }

                Draw.z(Layer.turret);
                super.drawTurret(block, build);

                if(m.speedf() > 0.0001f){
                    Draw.color(m.barColor());
                    Lines.stroke(barStroke);
                    for(int i = 0; i < 2; i++){
                        v.trns(m.drawrot(), barX * Mathf.signs[i], barY).add(m.recoilOffset);
                        Lines.lineAngle(m.x + v.x, m.y + v.y, m.rotation, barLength * Mathf.clamp(m.speedf()), false);
                    }
                }
            }

            @Override
            public void drawHeat(Turret block, TurretBuild build){
                //Don't
            }
        };
    }

    @Override
    public void setStats(){
        super.setStats();
        
        stats.remove(Stat.reload);
        float minValue = minFiringSpeed / 90f * 60f * shoot.shots;
        float maxValue = maxSpeed / 90f * 60f * shoot.shots;
        stats.add(Stat.reload, PMUtls.stringsFixed(minValue) + " - " + PMUtls.stringsFixed(maxValue) + StatUnit.perSecond.localized());
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("pm-minigun-speed", (MinigunTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-minigun-speed", PMUtls.stringsFixed(entity.speedf() * 100f)),
            entity::barColor,
            entity::speedf
        ));
    }

    public class MinigunTurretBuild extends ItemTurretBuild{
        protected float[] heats = {0f, 0f, 0f, 0f};
        protected float spinSpeed, spin;

        public Color barColor(){
            return spinSpeed > minFiringSpeed ? team.color : team.palette[2];
        }

        @Override
        public void updateTile(){
            boolean notShooting = !hasAmmo() || !isShooting() || !isActive();
            if(notShooting){
                spinSpeed = Mathf.approachDelta(spinSpeed, 0, windDownSpeed);
            }

            if(spinSpeed > getMaxSpeed()){
                spinSpeed = Mathf.approachDelta(spinSpeed, getMaxSpeed(), windDownSpeed);
            }

            for(int i = 0; i < 4; i++){
                heats[i] = Math.max(heats[i] - Time.delta / cooldownTime, 0);
            }
            
            super.updateTile();
        }

        @Override
        protected void updateShooting(){
            if(!hasAmmo()) return;

            spinSpeed = Mathf.approachDelta(spinSpeed, getMaxSpeed(), windupSpeed * peekAmmo().reloadMultiplier * timeScale);

            if(reloadCounter >= 90 && spinSpeed > minFiringSpeed){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter = spin % 90;

                heats[Mathf.floor(spin - 90) % 360 / 90] = 1f;
            }
        }

        @Override
        protected void updateReload(){
            boolean shooting = hasAmmo() && isShooting() && isActive();
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            float add = spinSpeed * multiplier;
            if(shooting && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : 1f;
                coolant.update(this);
                add += coolant.amount * edelta() * capacity * coolantMultiplier;

                if(Mathf.chance(0.06 * coolant.amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
            spin += add;
            reloadCounter += add;
        }

        protected float getMaxSpeed(){
            return maxSpeed * (!isControlled() && logicControlled() && logicShooting ? logicSpeedScl : 1f);
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
