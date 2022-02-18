package progressed.world.blocks.defence.turret.sandbox;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.Interp.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.ProgMats.*;
import progressed.content.effects.*;
import progressed.graphics.*;
import progressed.ui.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class EverythingTurret extends PowerTurret{
    public float growSpeed = 0.00055f, shrinkSpeed = 0.0025f, levelScl = 0.375f, levelSclMax = 0.125f;
    public int swirlEffects = 2;
    public float swirlSizeBase = 1f, swirlSize = 5f, swirlSizeScl = 3f, swirlRad = 24f, swirlRadScl = 8f;

    protected PowOut pow = Interp.pow3Out;

    public EverythingTurret(String name){
        super(name);
        requirements(
            Category.turret,
            ProgMats.everything() ? BuildVisibility.sandboxOnly : BuildVisibility.hidden,
            ItemStack.empty
        );
        alwaysUnlocked = true;

        shootLength = 0f;
        targetInterval = 1;
        minRange = 0f;
        shootType = Bullets.standardCopper;
        powerUse = 1f;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, t ->  t.add(PMElements.everything()));
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-everything-strength", (EverythingTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-everything-strength", PMUtls.stringsFixed(entity.levelf() * 100f)),
            () -> entity.team.color,
            entity::levelf
        ));
    }

    public class EverythingTurretBuild extends PowerTurretBuild{
        public float level, drawRot = Mathf.random(360f);
        public int selectedBullet; //guaranteed desync since bullets are random - won't be fixed and probably isn't too important

        @Override
        public void updateTile(){
            if(!ProgMats.everything()) return;

            super.updateTile();

            float levelf = levelf();

            for(int i = 0; i < swirlEffects; i++){
                if(Mathf.chanceDelta(1f)){
                    float sin = Mathf.sin(Time.time + Mathf.randomSeed(id), 50f / Mathf.PI2);
                    float l = levelf + 0.005f;
                    EnergyFx.everythingGunSwirl.at(x, y,
                        Mathf.random(l * 45f, l * 720f), team.color,
                        new float[]{
                            swirlSizeBase + levelf * swirlSize + sin * levelf * swirlSizeScl,
                            levelf * swirlRad + sin * levelf * swirlRadScl
                        }
                    );
                }
            }
            
            drawRot = Mathf.mod(drawRot - Time.delta * levelf * rotateSpeed, 360f);
      
            if(isShooting() && consValid()){
                level = Mathf.approachDelta(level, 1f, growSpeed);
            }else{
                level = Mathf.approachDelta(level, 0f, shrinkSpeed);
            }
        }

        public float levelf(){
            return pow.apply(level);
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.turret);
            Drawf.shadow(region, x - elevation, y - elevation, drawRot);
            Drawf.spinSprite(region, x, y, drawRot);

            if(!ProgMats.everything()){
                Draw.z(Layer.overlayUI);
                PMDrawf.text(x, y + size * tilesize / 2f + 3, team.color, Core.bundle.get("pm-sandbox-disabled"));
            }
        }

        @Override
        protected void updateShooting(){
            if(reload >= reloadTime && !charging){
                float levelf = levelf() * (1 + levelSclMax),
                    min = Mathf.clamp(levelf - levelScl) * ProgMats.allBullets.size,
                    max = Mathf.clamp(levelf) * ProgMats.allBullets.size;
                selectedBullet = Mathf.clamp(Mathf.floor(Mathf.random(min, max)), 0, ProgMats.allBullets.size - 1);

                BulletType type = peekAmmo();

                shoot(type);

                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        protected void bullet(BulletType type, float angle){
            BulletData data = ProgMats.allBullets.get(selectedBullet);
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;
            float laserLifeScl = data.continuousBlock ? data.lifetime / type.lifetime : 1f;

            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl * laserLifeScl);
        }

        @Override
        protected void effects(){
            BulletData data = ProgMats.allBullets.get(selectedBullet);
            data.shootEffect.at(x, y, rotation, team.color);
            data.smokeEffect.at(x, y, rotation, team.color);
            data.shootSound.at(x, y, Mathf.random(0.9f, 1.1f));

            float shake = data.shake;
            if(shake > 0){
                Effect.shake(shake, shake, this);
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        public BulletType useAmmo(){
            return ProgMats.allBullets.get(selectedBullet).bulletType;
        }

        @Override
        public BulletType peekAmmo(){
            return ProgMats.allBullets.get(selectedBullet).bulletType;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(level);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                level = read.f();
            }
        }
    }
}
