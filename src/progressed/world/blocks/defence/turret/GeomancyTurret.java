package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.entities.bullet.*;
import progressed.graphics.*;
import progressed.world.meta.*;

public class GeomancyTurret extends PowerTurret{
    public float armX = 4f, armY;

    public int crackEffects = 2;
    public Color crackColor = PMPal.darkBrown;
    public LightningEffect crackEffect = LightningFx.groundCrack;
    public Effect slamEffect = OtherFx.concretionSlam;

    public GeomancyTurret(String name){
        super(name);

        accurateDelay = true;

        drawer = new DrawTurret(){
            public final TextureRegion[]
                armRegions = new TextureRegion[2],
                armOutlines = new TextureRegion[2],
                armHeatRegions = new TextureRegion[2];

            @Override
            public void load(Block block){
                super.load(block);

                for(int arm : Mathf.zeroOne){
                    armRegions[arm] = Core.atlas.find(block.name + "-arm-" + arm);
                    armOutlines[arm] = Core.atlas.find(block.name + "-arm-" + arm + "-outline");
                    armHeatRegions[arm] = Core.atlas.find(block.name + "-arm-heat-" + arm);
                }
            }

            @Override
            public void getRegionsToOutline(Block block, Seq<TextureRegion> out){
                super.getRegionsToOutline(block, out);
                out.add(region);
                out.add(armRegions);
            }

            @Override
            public void draw(Building build){
                GeomancyTurret turret = (GeomancyTurret)build.block;
                GeomancyTurretBuild tb = (GeomancyTurretBuild)build;

                Draw.rect(base, build.x, build.y);
                Draw.color();

                Draw.z(Layer.turret - 0.02f);

                Drawf.shadow(region, build.x - turret.elevation, build.y - turret.elevation, tb.drawrot());
                for(int arm : Mathf.zeroOne){
                    Tmp.v1.trns(tb.drawrot(), armX * Mathf.signs[arm], armY - armRecoil(tb, arm));
                    Drawf.shadow(armRegions[arm], build.x + Tmp.v1.x - turret.elevation, build.y + Tmp.v1.y - turret.elevation, tb.drawrot());
                }

                Draw.z(Layer.turret);

                tb.recoilOffset.setZero();
                drawTurret(turret, tb);
                drawHeat(turret, tb);
                drawArms(tb);
            }

            public void drawArms(GeomancyTurretBuild build){
                for(int arm : Mathf.zeroOne){
                    Tmp.v1.trns(build.drawrot(), armX * Mathf.signs[arm], armY - armRecoil(build, arm));
                    Draw.z(Layer.turret - 0.01f);
                    Draw.rect(outline, build.x, build.y, build.drawrot());
                    Draw.rect(armOutlines[arm], build.x + Tmp.v1.x, build.y + Tmp.v1.y, build.drawrot());
                    Draw.z(Layer.turret);
                    Draw.rect(armRegions[arm], build.x + Tmp.v1.x, build.y + Tmp.v1.y, build.drawrot());

                    if(build.armHeat[arm] <= 0.00001f || !armHeatRegions[arm].found()) continue;
                    Drawf.additive(armHeatRegions[arm], heatColor.write(Tmp.c1).a(build.armHeat[arm]), build.x + Tmp.v1.x, build.y + Tmp.v1.y, build.drawrot(), Layer.turretHeat);
                }
            }

            public float armRecoil(GeomancyTurretBuild b, int side){
                return Mathf.pow(b.armRecoil[side], recoilPow) * recoil;
            }
        };
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ObjectMap.of(this, shootType)));
    }

    public class GeomancyTurretBuild extends PowerTurretBuild{
        public Vec2 strikePos = new Vec2();
        public boolean charging;
        public float[] armRecoil = new float[2], armHeat = new float[2];

        @Override
        public void updateTile(){
            for(int i = 0; i < 2; i++){
                armRecoil[i] = Math.max(armRecoil[i] - Time.delta / recoilTime , 0);
                armHeat[i] = Math.max(armHeat[i] - Time.delta / cooldownTime, 0);
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
        protected void shoot(BulletType type){
            strikePos.set(targetPos).sub(x, y).limit(range).add(x, y); //Constrain to range

            shoot.shoot(totalShots, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                if(delay > 0f){
                    Time.run(delay, () -> bullet(type, strikePos.x, strikePos.y, angle, mover));
                }else{
                    bullet(type, strikePos.x, strikePos.y, angle, mover);
                }
                totalShots++;

                Tmp.v1.trns(rotation - 90f, armX * Mathf.signs[totalShots % 2], shootY).add(x, y);
                if(shoot.firstShotDelay > 0){
                    float
                        dst = Tmp.v1.dst(strikePos),
                        mdst = dst - ((PillarFieldBulletType)(type)).radius;
                    if(mdst > 0){
                        Tmp.v2.set(Tmp.v1).lerp(strikePos, mdst / dst);
                        for(int i = 0; i < crackEffects; i++){
                            crackEffect.at(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, crackColor);
                        }
                    }
                }

                Tile t = Vars.world.tileWorld(Tmp.v1.x, Tmp.v1.y);
                if(t != null && t.floor() != null){
                    slamEffect.at(Tmp.v1.x, Tmp.v1.y, rotation, t.floor().mapColor);
                }

                armRecoil[totalShots % 2] = armHeat[totalShots % 2] = heat = 1f;
            });
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets--;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            handleBullet(type.create(this, team, xOffset, yOffset, 0f), 0f, 0f, angleOffset);
        }

        @Override
        public boolean charging(){
            return charging;
        }
    }
}
