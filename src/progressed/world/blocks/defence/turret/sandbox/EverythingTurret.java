package progressed.world.blocks.defence.turret.sandbox;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.Interp.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
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

    public String basePrefix = "";
    public TextureRegion baseRegion;

    public EverythingTurret(String name){
        super(name);
        requirements(
            Category.turret,
            ProgMats.everything() ? BuildVisibility.sandboxOnly : BuildVisibility.hidden,
            ItemStack.empty
        );
        alwaysUnlocked = true;

        shootY = 0f;
        targetInterval = 1;
        minRange = 0f;
        shootType = Bullets.placeholder;
        drawer = new DrawDefault();
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.ammo);
        stats.remove(Stat.health);

        stats.add(Stat.ammo, t ->  t.add(PMElements.everything()));
        stats.add(Stat.health, t -> t.add(PMElements.infinity()));
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("health");
        addBar("pm-everything-strength", (EverythingTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-everything-strength", PMUtls.stringsFixed(entity.levelf() * 100f)),
            () -> entity.team.color,
            entity::levelf
        ));
    }

    @Override
    public void load(){
        region = Core.atlas.find(name);
        baseRegion = Core.atlas.find(name + "-base");
        if(!baseRegion.found() && minfo.mod != null) baseRegion = Core.atlas.find(minfo.mod.name + "-" + basePrefix + "block-" + size);
        if(!baseRegion.found()) baseRegion = Core.atlas.find(basePrefix + "-block-" + size);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        //Do not
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
      
            if(isShooting() && canConsume()){
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
            if(reloadCounter >= reload && !charging()){
                float levelf = levelf() * (1 + levelSclMax),
                    min = Mathf.clamp(levelf - levelScl) * ProgMats.allBullets.size,
                    max = Mathf.clamp(levelf) * ProgMats.allBullets.size;
                selectedBullet = Mathf.clamp(Mathf.floor(Mathf.random(min, max)), 0, ProgMats.allBullets.size - 1);

                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter = 0f;
            }else{
                reloadCounter += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset, shootY + yOffset),
                bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset, shootY + yOffset),
                shootAngle = rotation + angleOffset + Mathf.range(inaccuracy);

            BulletData data = ProgMats.allBullets.get(selectedBullet);
            float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(x + recoilOffset.x, y + recoilOffset.y, targetPos.x, targetPos.y) / type.range, minRange / type.range, range / type.range) : 1f;
            float laserLifeScl = data.continuousBlock ? data.lifetime / type.lifetime : 1f;

            handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl * laserLifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, angleOffset);

            data.shootEffect.at(x, y, rotation, team.color);
            data.smokeEffect.at(x, y, rotation, team.color);
            data.shootSound.at(x, y, Mathf.random(0.9f, 1.1f));

            float shake = data.shake;

            if(shake > 0){
                Effect.shake(shake, shake, this);
            }

            curRecoil = 1f;
            heat = 1f;

            if(!consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }

        @Override
        public void damage(float damage){
            //haha no
        }

        @Override
        public void kill(){
            //haha no
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
