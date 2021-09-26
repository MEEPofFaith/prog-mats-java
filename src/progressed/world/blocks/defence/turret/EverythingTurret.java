package progressed.world.blocks.defence.turret;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.Interp.*;
import arc.math.*;
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
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class EverythingTurret extends PowerTurret{
    public float startingBias = 0.1f, maxBias = 6000f, growSpeed = 1.004f, shrinkSpeed = 0.05f;

    protected Seq<BulletData> bullets = new Seq<>();
    protected PowOut pow = PMUtls.customPowOut(20);

    public EverythingTurret(String name){
        super(name);
        requirements(Category.turret, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        shootLength = 0f;
        targetInterval = 1;
        minRange = 0f;
        shootType = Bullets.standardCopper;
        powerUse = 246810f/60f;
    }

    @Override
    public void init(){
        super.init();

        content.units().each(u -> {
            u.weapons.each(w -> {
                if(w.bullet != null && !w.bullet.killShooter){
                    BulletType bul = w.bullet;
                    BulletData data = new BulletData(bul, w.shootSound, bul.shootEffect, bul.smokeEffect, w.shake, bul.lifetime);
                    if(!bullets.contains(data)){
                        bullets.add(data);
                    }
                }
            });
        });
        content.blocks().each(b -> {
            if(b != this && b instanceof Turret){
                if(b instanceof LaserTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : block.shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : block.smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shootShake, bul.lifetime + block.shootDuration, true);
                    if(!bullets.contains(data)){
                        bullets.add(data);
                    }
                }else if(b instanceof PowerTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shootShake, bul.lifetime);
                    if(!bullets.contains(data)){
                        bullets.add(data);
                    }
                }else if(b instanceof ItemTurret block){
                    content.items().each(i -> {
                        if(block.ammoTypes.get(i) != null){
                            BulletType bul = block.ammoTypes.get(i);
                            Effect fshootEffect = block.shootEffect == Fx.none ? bul.shootEffect : shootEffect;
                            Effect fsmokeEffect = block.smokeEffect == Fx.none ? bul.smokeEffect : smokeEffect;
                            BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shootShake, bul.lifetime);
                            if(!bullets.contains(data)){
                                bullets.add(data);
                            }
                        }
                    });
                }
            }
        });

        bullets.sort(b -> PMUtls.bulletDamage(b.bullet, b.lifetime));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, "[red]Everything");
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-everything-strength", (EverythingTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-everything-strength", PMUtls.stringsFixed(entity.bias / maxBias * 100f)),
            () -> entity.team.color,
            () -> entity.bias / maxBias
        ));
    }

    public class EverythingTurretBuild extends PowerTurretBuild{
        protected float bias = startingBias, drawRot = Mathf.random(360f);
        protected int selectedBullet; //guaranteed desync since bullets are random - won't be fixed and probably isn't too important

        @Override
        public void updateTile(){
            super.updateTile();
            
            float lerp = pow.apply(bias / maxBias);

            for(int i = 0; i < 1f + lerp * 100f; i++){
                if(Mathf.chanceDelta(1f)){ //I'm just copying over code I have no idea what the hell I'm looking at.
                    PMFx.everythingGunSwirl.at(x, y,
                        Mathf.random(lerp * 45f, lerp * 720f), team.color,
                        new float[]{
                            (4f + (lerp * 16f)) + Mathf.sin((Time.time + Mathf.randomSeed(id)) / 30f) * (lerp * 6f),
                            lerp * 420f + Mathf.sin((Time.time + Mathf.randomSeed(id + 1)) / 30f) * (lerp * 80f)
                        }
                    );
                }
            }
            
            drawRot = Mathf.mod(drawRot - Time.delta * lerp * 110f, 360f);
      
            if(isShooting() && consValid()){
                bias = Mathf.clamp(bias * Mathf.pow(growSpeed, edelta()), 0f, maxBias);
            }else{
                bias = Mathf.lerpDelta(bias, startingBias, shrinkSpeed);
            }
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);
            Draw.z(Layer.turret);
            Drawf.shadow(region, x - elevation, y - elevation, drawRot);
            Drawf.spinSprite(region, x, y, drawRot);
        }

        @Override
        protected void updateShooting(){
            if(reload >= reloadTime && !charging){
                selectedBullet = Mathf.clamp(Mathf.floor(1f / (((1f / Mathf.random()) - 1f) / bias + 1f) * bullets.size), 0, bullets.size - 1);

                BulletType type = peekAmmo();

                shoot(type);

                reload = 0f;
            }else{
                reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }

        @Override
        protected void bullet(BulletType type, float angle){
            BulletData data = bullets.get(selectedBullet);
            float lifeScl = type.scaleVelocity ? Mathf.clamp(Mathf.dst(x + tr.x, y + tr.y, targetPos.x, targetPos.y) / type.range(), minRange / type.range(), range / type.range()) : 1f;
            float laserLifeScl = data.continuousBlock ? data.lifetime / type.lifetime : 1f;

            type.create(this, team, x + tr.x, y + tr.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl * laserLifeScl);
        }

        @Override
        protected void effects(){
            BulletData data = bullets.get(selectedBullet);
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
            return bullets.get(selectedBullet).bullet;
        }

        @Override
        public BulletType peekAmmo(){
            return bullets.get(selectedBullet).bullet;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(bias);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                bias = read.f();
            }
        }
    }

    public static class BulletData{
        public BulletType bullet;
        public Sound shootSound;
        public Effect shootEffect, smokeEffect;
        public float shake, lifetime;
        public boolean continuousBlock;

        public BulletData(BulletType b, Sound s, Effect shE, Effect smE, float shake, float lifetime, boolean cont){
            bullet = b;
            shootSound = s;
            shootEffect = shE;
            smokeEffect = smE;
            this.shake = shake;
            this.lifetime = lifetime;
            continuousBlock = cont;
        }

        public BulletData(BulletType b, Sound s, Effect shE, Effect smE, float shake, float lifetime){
            this(b, s, shE, smE, shake, lifetime, false);
        }
    }
}
