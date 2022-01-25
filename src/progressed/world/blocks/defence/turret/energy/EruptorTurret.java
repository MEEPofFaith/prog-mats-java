package progressed.world.blocks.defence.turret.energy;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.content.effects.UtilFx.*;
import progressed.entities.bullet.energy.*;
import progressed.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class EruptorTurret extends PowerTurret{
    public final int lightningTimer = timers++;
    public float lightningInterval = 2f, lightningStroke = 4f;
    public Color lightningColor = Color.valueOf("ff9c5a");

    public float rangeExtention = 32f, extendSpeed = 2f;
    public float firingMoveFract = 0.5f, shootDuration = 100f;

    public EruptorTurret(String name){
        super(name);

        canOverdrive = false;
        targetAir = targetGround = true;
        cooldown = restitution = 0.01f;
        ammoUseEffect = Fx.none;
        shootSound = Sounds.none;
        loopSound = Sounds.beam;
        loopSoundVolume = 2f;
        heatColor = Color.valueOf("f08913");

        consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.01f)).update(false);
        coolantMultiplier = 1f;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.booster);
        stats.add(Stat.input, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes.liquidfilters.get(l.id)));
    }

    @Override
    public void init(){
        super.init();

        if(minRange < 0){
            if(shootType instanceof MagmaBulletType m){
                minRange = size * tilesize + m.radius + 12f;
            }else{
                minRange = size * tilesize * 2f;
            }
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-reload", (EruptorTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp((reloadTime - entity.reload) / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> 1f - Mathf.clamp(entity.reload / reloadTime)
        ));
        bars.add("pm-shoot-duration", (EruptorTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-shoot-duration", PMUtls.stringsFixed(Mathf.clamp((entity.bulletLife / shootDuration) * 100f))),
            () -> lightningColor,
            () -> Mathf.clamp(entity.bulletLife / shootDuration)
        ));
    }

    public class EruptorTurretBuild extends PowerTurretBuild{
        protected Bullet bullet;
        protected float bulletLife, length;

        @Override
        public void updateTile(){
            super.updateTile();

            if(bulletLife > 0 && bullet != null){
                wasShooting = true;
                tr.trns(rotation, length, 0f);
                bullet.set(x + tr.x, y + tr.y);
                bullet.time(0f);
                recoil = recoilAmount;
                heat = 1f;
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                extendTo(Math.min(range + rangeExtention, dst(targetPos)));
                if(timer(lightningTimer, lightningInterval)){
                    tr2.trns(rotation, shootLength - recoil);
                    UtilFx.lightning.at(x + tr2.x, y + tr2.y, angleTo(bullet), lightningColor, new LightningData(bullet, lightningStroke));
                }
                if(bulletLife <= 0f){
                    bullet = null;
                }
            }else if(reload > 0f){
                wasShooting = true;
                Liquid liquid = liquids.current();
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

                float used = (cheating() ? maxUsed * Time.delta : Math.min(liquids.get(liquid), maxUsed * Time.delta)) * liquid.heatCapacity * coolantMultiplier;
                reload -= used;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        public void extendTo(float targetLength){
            length = PMUtls.moveToward(length, targetLength, extendSpeed * edelta(), minRange, range + rangeExtention);
        }

        @Override
        protected void updateCooling(){
            //Do nothing, cooling is irrelevant here
        }

        @Override
        protected void updateShooting(){
            if(bulletLife > 0 && bullet != null){
                return;
            }

            if(reload <= 0 && (consValid() || cheating())){
                BulletType type = peekAmmo();

                shoot(type);

                reload = reloadTime;
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency() * rotateSpeed * delta() * (bulletLife > 0f ? firingMoveFract : 1f));
        }
        
        @Override
        protected void bullet(BulletType type, float angle){
            length = Math.min(range + rangeExtention, dst(targetPos));
            tr.trns(rotation, length, 0f);
            bullet = type.create(tile.build, team, x + tr.x, y + tr.y, angle);
            bulletLife = shootDuration;
        }

        @Override
        public boolean shouldActiveSound(){
            return bulletLife > 0 && bullet != null;
        }
    }
}