package progressed.world.blocks.defence.turret.energy;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.audio.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;

public class AimLaserTurret extends PowerTurret{
    public float aimStroke = 2f, aimRnd;
    public float chargeVolume = 1f, minPitch = 1f, maxPitch = 1f, shootSoundVolume = 1f;
    public float warningDelay, warningVolume = 1f;
    public Sound warningSound = Sounds.none;

    public AimLaserTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.reload);
        stats.add(Stat.reload, 60f / (reloadTime + chargeTime + 1) * (alternate ? 1 : shots), StatUnit.none);
    }

    @Override
    public void init(){
        clipSize = Math.max(clipSize, (shootType.range() + shootLength + aimRnd + aimStroke * 2f + 3f) * 2f);
        super.init();
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-reload", (AimLaserTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reload / reloadTime)
        ));

        bars.add("pm-charge", (AimLaserTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-charge", PMUtls.stringsFixed(Mathf.clamp(entity.charge) * 100f)),
            () -> Color.sky,
            () -> entity.charge
        ));
    }

    public class AimLaserTurretBuild extends PowerTurretBuild{
        protected float charge, drawCharge, alpha;
        protected PitchedSoundLoop sound = new PitchedSoundLoop(chargeSound, chargeVolume);
        
        public boolean isAI(){
            return !(isControlled() || (logicControlled() && logicShooting));
        }

        @Override
        public void draw(){
            super.draw();

            //a n x i e t y
            if(alpha > 0.01f){
                Draw.mixcol();

                Draw.z(Layer.effect);

                float c = Interp.pow2Out.apply(drawCharge);
                float a = Interp.pow2Out.apply(alpha);

                Lines.stroke(aimStroke * a, team.color);

                float dst = shootType.range() + shootLength;
                Healthc box = PMDamage.linecast(targetGround, targetAir, team, x, y, rotation, dst);

                Tmp.v1.trns(rotation, shootLength - recoil);
                if(isAI() && target instanceof Unit){
                    Tmp.v2.trns(rotation, dst(targetPos)).limit(dst);
                }else if(box != null){
                    Tmp.v2.trns(rotation, dst(box)).limit(dst);
                }else{
                    Tmp.v2.trns(rotation, dst);
                }
                Tmp.v3.rnd(Mathf.random(aimRnd * (1f - c)));
                Tmp.v4.set(Tmp.v2).add(Tmp.v3);

                PMDrawf.vecLine(x, y, Tmp.v1, Tmp.v4, false);

                if(isAI() && target instanceof Unit u){
                    Draw.alpha(0.75f);

                    Lines.line(u.x, u.y, targetPos.x, targetPos.y);
                    Fill.circle(u.x, u.y, aimStroke / 2f * a);
                    Fill.circle(targetPos.x, targetPos.y, aimStroke / 2f * a);

                    Draw.mixcol(team.color, 1f);
                    Draw.rect(u.type.fullIcon, targetPos.x, targetPos.y, u.rotation - 90f);
                    Draw.mixcol();
                    Draw.alpha(1f);
                }

                Fill.circle(x + Tmp.v1.x, y + Tmp.v1.y, aimStroke / 2f * a);
                Fill.circle(x + Tmp.v4.x, y + Tmp.v4.y, aimStroke / 2f * a);
                Lines.circle(x + Tmp.v4.x, y + Tmp.v4.y, aimStroke * 2f * (0.5f + a / 2f));

                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            alpha = Mathf.lerpDelta(alpha, 0f, 0.05f);

            if(charging){
                charge = Mathf.clamp(charge + Time.delta / chargeTime);
                alpha = Math.max(alpha, charge);
                drawCharge = charge;
            }

            sound.update(x, y, chargeVolume * charge, Mathf.lerp(minPitch, maxPitch, charge));

            super.updateTile();
        }

        @Override
        protected void updateCooling(){
            if(!charging){
                super.updateCooling();
            }
        }
        @Override
        protected void updateShooting(){
            if(!charging){
                super.updateShooting();
            }
        }

        @Override
        protected void shoot(BulletType type){
            useAmmo();
            tr.trns(rotation, shootLength);
            drawCharge = 0;
            chargeBeginEffect.at(x + tr.x, y + tr.y, rotation, team.color, self());

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength);
                    chargeEffect.at(x + tr.x, y + tr.y, rotation, team.color, self());
                });
            }

            charging = true;

            Time.run(chargeTime - warningDelay, () -> {
                if(!isValid()) return;
                warningSound.at(x, y, 1f, warningVolume);
            });
            Time.run(chargeTime, () -> {
                if(!isValid()) return;
                tr.trns(rotation, shootLength);
                recoil = recoilAmount;
                heat = 1f;
                bullet(type, rotation + Mathf.range(inaccuracy));
                effects();
                charging = false;
                charge = 0;
            });
        }
        
        @Override
        protected void effects(){
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

            fshootEffect.at(x + tr.x, y + tr.y, rotation);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f), shootSoundVolume);

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }

            recoil = recoilAmount;
        }

        @Override
        public boolean shouldTurn(){
            return true;
        }

        @Override
        public void remove(){
            sound.stop();
            super.remove();
        }
    }
}