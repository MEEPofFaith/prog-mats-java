package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.content.*;
import progressed.content.PMFx.*;
import progressed.entities.bullet.*;

public class GeomancyTurret extends PowerTurret{
    public float armX = 4f, armY;
    public TextureRegion armRegion, armHeatRegion;

    public int crackEffects = 3;
    public float crackStroke = 2f;
    public Color crackColor = Pal.darkerGray;
    public Color crackColor = PMPal.darkBrown;
    public Effect crackEffect = PMFx.groundCrack;

    public GeomancyTurret(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        armRegion = Core.atlas.find(name + "-arm");
        armHeatRegion = Core.atlas.find(name + "-arm-heat");
    }

    public class GeomancyTurretBuild extends PowerTurretBuild{
        public Vec2 strikePos = new Vec2();
        public float[] armRecoil = new float[2], armHeat = new float[2];

        @Override
        public void updateTile(){
            for(int i = 0; i < 2; i++){
                armRecoil[i] = Mathf.lerpDelta(armRecoil[i], 0f, restitution);
                armHeat[i] = Mathf.lerpDelta(armHeat[i], 0f, cooldown);
            }

            super.updateTile();
        }

        @Override
        protected void updateShooting(){
            if(!charging) super.updateShooting();
        }

        @Override
        protected void updateCooling(){
            if(!charging) super.updateCooling();
        }

        @Override
        public boolean shouldTurn(){
            return true;
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);
            Draw.color();

            Draw.z(Layer.turret);

            for(int arm : Mathf.zeroOne){
                tr2.trns(rotation - 90f, armX * Mathf.signs[arm], armY - armRecoil[arm]);
                Drawf.shadow(
                    armRegion,
                    x + tr2.x - elevation, y + tr2.y - elevation,
                    armRegion.width * xScl() * Mathf.signs[arm],
                    armRegion.height * yScl(),
                    rotation - 90
                );
            }

            Drawf.shadow(region, x - elevation, y - elevation, rotation - 90);
            Draw.rect(region, x, y, rotation - 90);
            if(heatRegion.found() && heat > 0.01f){
                Draw.alpha(heat);
                Draw.rect(heatRegion, x, y, rotation - 90);
                Draw.color();
            }

            for(int arm : Mathf.zeroOne){
                tr2.trns(rotation - 90f, armX * Mathf.signs[arm], armY - armRecoil[arm]);
                Draw.rect(
                    armRegion,
                    x + tr2.x, y + tr2.y,
                    armRegion.width * xScl() * Mathf.signs[arm],
                    armRegion.height * yScl(),
                    rotation - 90
                );

                if(armHeatRegion.found() && armHeat[arm] > 0.01f){
                    Draw.alpha(armHeat[arm]);
                    Draw.rect(
                        armHeatRegion,
                        x + tr2.x, y + tr2.y,
                        armHeatRegion.width * xScl() * Mathf.signs[arm],
                        armHeatRegion.height * yScl(),
                        rotation - 90
                    );
                    Draw.color();
                }
            }
        }

        //Why include these? Remove the xscl/yscl, spawn it out of a payload source, and see for yourself.
        public float xScl(){
            return Draw.scl * Draw.xscl;
        }

        public float yScl(){
            return Draw.scl * Draw.yscl;
        }

        @Override
        public void targetPosition(Posc pos){
            targetPos.set(pos); //Don't lead
        }

        @Override
        protected void shoot(BulletType type){
            charging = true;
            strikePos.set(targetPos).sub(x, y).limit(range).add(x, y); //Constrain to range
            Time.run(chargeTime, () -> {
                type.create(this, team, strikePos.x, strikePos.y, 0f);
                charging = false;
            });
            armRecoil[shotCounter % 2] = recoilAmount;
            armHeat[shotCounter % 2] = 1f;
            heat = 1f;
            effects();

            tr2.trns(rotation - 90f, armX * Mathf.signs[shotCounter % 2], shootLength).add(x, y);
            float
                dst = tr2.dst(strikePos),
                mdst = dst - ((PillarFieldBulletType)type).radius;
            if(mdst > 0){
                tr.set(tr2).lerp(strikePos, mdst / dst);
                for(int i = 0; i < crackEffects; i++){
                    crackEffect.at(tr2.x, tr2.y, angleTo(Tmp.v1), crackColor, new LightningData(new Vec2(tr), crackStroke));
                }
            }

            shotCounter++;
        }

        protected void effects(){
            tr.trns(rotation - 90f, armX * Mathf.signs[shotCounter % 2], shootLength);
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

            fshootEffect.at(x + tr.x, y + tr.y, rotation);
            fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, this);
            }

            recoil = recoilAmount;
        }
    }
}