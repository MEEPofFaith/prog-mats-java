package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.content.effects.UtilFx.*;
import progressed.entities.bullet.*;
import progressed.graphics.*;
import progressed.world.meta.*;

public class GeomancyTurret extends PowerTurret{
    public float armX = 4f, armY;

    public int crackEffects = 2;
    public float crackStroke = 1.5f, crackWidth = 16f;
    public Color crackColor = PMPal.darkBrown;
    public Effect crackEffect = UtilFx.groundCrack;
    public Effect slamEffect = OtherFx.concretionSlam;

    public TextureRegion turretRegion;
    public TextureRegion[]
        armRegions = new TextureRegion[2],
        armOutlines = new TextureRegion[2],
        armHeatRegions = new TextureRegion[2];

    public GeomancyTurret(String name){
        super(name);

        accurateDelay = true;
    }

    @Override
    public void load(){
        super.load();

        turretRegion  = Core.atlas.find(name + "-turret");
        for(int arm : Mathf.zeroOne){
            armRegions[arm] = Core.atlas.find(name + "-arm-" + arm);
            armOutlines[arm] = Core.atlas.find(name + "-arm-outline-" + arm);
            armHeatRegions[arm] = Core.atlas.find(name + "-arm-heat-" + arm);
        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        Outliner.outlineRegion(packer, turretRegion, outlineColor, name + "-turret");
        Outliner.outlineRegions(packer, armRegions, outlineColor, name + "-arm-outline");
    }

    public class GeomancyTurretBuild extends PowerTurretBuild{
        public Vec2 strikePos = new Vec2();
        public boolean charging;
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
            if(!charging()) super.updateShooting();
        }

        @Override
        protected void updateCooling(){
            if(!charging()) super.updateCooling();
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
                recoilOffset.trns(rotation - 90f, armX * Mathf.signs[arm], armY - armRecoil[arm]);
                Drawf.shadow(
                    armRegions[arm],
                    x + recoilOffset.x - elevation, y + recoilOffset.y - elevation,
                    rotation - 90
                );
            }
            Drawf.shadow(turretRegion, x - elevation, y - elevation, rotation - 90);

            for(int arm : Mathf.zeroOne){
                recoilOffset.trns(rotation - 90f, armX * Mathf.signs[arm], armY - armRecoil[arm]);
                Draw.rect(
                    armOutlines[arm],
                    x + recoilOffset.x, y + recoilOffset.y,
                    rotation - 90
                );
            }

            Draw.rect(turretRegion, x, y, rotation - 90);
            if(heatRegion.found() && heat > 0.01f){
                Draw.alpha(heat);
                Draw.rect(heatRegion, x, y, rotation - 90);
                Draw.color();
            }

            for(int arm : Mathf.zeroOne){
                recoilOffset.trns(rotation - 90f, armX * Mathf.signs[arm], armY - armRecoil[arm]);
                Draw.rect(
                    armRegions[arm],
                    x + recoilOffset.x, y + recoilOffset.y,
                    rotation - 90
                );

                TextureRegion armHeatRegion = armHeatRegions[arm];
                if(armHeatRegion.found() && armHeat[arm] > 0.01f){
                    Draw.alpha(armHeat[arm]);
                    Draw.rect(
                        armHeatRegion,
                        x + recoilOffset.x, y + recoilOffset.y,
                        rotation - 90
                    );
                    Draw.color();
                }
            }
        }

        //Should only ever be one at a time. If more, uh, just don't
        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            strikePos.set(targetPos).sub(x, y).limit(range).add(x, y); //Constrain to range
            bullet.set(strikePos);
            armRecoil[totalShots % 2] = armHeat[totalShots % 2] = 1f;

            recoilOffset.trns(rotation - 90f, armX * Mathf.signs[totalShots % 2], shootY).add(x, y);
            float
                dst = recoilOffset.dst(strikePos),
                mdst = dst - ((PillarFieldBulletType)(bullet.type)).radius;
            if(mdst > 0){
                Tmp.v1.set(recoilOffset).lerp(strikePos, mdst / dst);
                for(int i = 0; i < crackEffects; i++){
                    crackEffect.at(recoilOffset.x, recoilOffset.y, angleTo(Tmp.v1), crackColor, new LightningData(new Vec2(Tmp.v1), crackStroke, shoot.firstShotDelay / 2f, true, crackWidth));
                }
            }

            Floor f = Vars.world.tileWorld(x + Tmp.v1.x, y + Tmp.v1.y).floor();
            if(f != null){
                slamEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, f.mapColor);
            }
        }

        protected void effects(){
            Tmp.v1.trns(rotation - 90f, armX * Mathf.signs[totalShots % 2], shootY);
            Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
            Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;

            Floor f = Vars.world.tileWorld(x + Tmp.v1.x, y + Tmp.v1.y).floor();
            if(f != null){
                slamEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, f.mapColor);
            }

            fshootEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation);
            fsmokeEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation);
            shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));

            if(shake > 0){
                Effect.shake(shake, shake, this);
            }

            curRecoil = 1f;
        }

        @Override
        public boolean charging(){
            return charging;
        }
    }
}
