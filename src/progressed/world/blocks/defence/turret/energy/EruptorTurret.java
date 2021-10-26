package progressed.world.blocks.defence.turret.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.PMFx.*;
import progressed.entities.bullet.energy.*;
import progressed.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class EruptorTurret extends PowerTurret{
    public final int lightningTimer = timers++;
    public float lightningInterval = 2f, lightningStroke = 4f;
    public Color lightningColor = Color.valueOf("ff9c5a");

    public int layers = 1;
    public Seq<EruptorCell> cells = new Seq<>();
    public float rangeExtention = 32f, extendSpeed = 2f;
    public float firingMoveFract = 0.5f, shootDuration = 100f;
    public float capCloseRate = 0.01f;


    public TextureRegion turretRegion, baseOutline;
    public TextureRegion[] cellRegions, capRegions, outlineRegions, heatRegions;
    
    protected Vec2 tr3 = new Vec2();

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
    public void load(){
        super.load();

        turretRegion = atlas.find(name + "-turret");
        baseOutline = atlas.find(name + "-base-outline");
        cellRegions = new TextureRegion[cells.size];
        capRegions = new TextureRegion[cells.size];
        outlineRegions = new TextureRegion[cells.size];
        heatRegions = new TextureRegion[cells.size];
        for(int i = 0; i < cells.size; i++){
            cellRegions[i] = atlas.find(name + "-cell-" + i);
            capRegions[i] = atlas.find(name + "-cap-" + i);
            outlineRegions[i] = atlas.find(name + "-outline-" + i);
            heatRegions[i] = atlas.find(name + "-cell-heat-" + i);
        }
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

        if(cells.size == 0){
            throw new RuntimeException(name + " does not have any cells!");
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
            baseRegion,
            atlas.find(name + "-icon")
        };
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

    public static class EruptorCell{
        public int layer = 1;
        public float xOffset, yOffset;

        public EruptorCell(float x, float y){
            xOffset = x;
            yOffset = y;
        }
        
        public EruptorCell(float x, float y, int layer){
            xOffset = x;
            yOffset = y;
            this.layer = layer;
        }
    }

    public class EruptorTurretBuild extends PowerTurretBuild{
        protected Bullet bullet;
        protected float bulletLife, length;
        protected float[] layerOpen = new float[layers];

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            tr2.trns(rotation, -recoil);
            float tx = x + tr2.x, ty = y + tr2.y;

            Drawf.shadow(baseOutline, tx - elevation, ty - elevation, rotation - 90f);
            for(int i = 0; i < cells.size; i++){
                EruptorCell cell = cells.get(i);
                tr3.trns(rotation - 90, cell.xOffset * layerOpen[cell.layer - 1], cell.yOffset * layerOpen[cell.layer - 1]);
                Drawf.shadow(outlineRegions[i], tx + tr3.x - elevation, ty + tr3.y - elevation, rotation - 90f);
            }

            Draw.rect(baseOutline, tx, ty, rotation - 90f);
            for(int i = 0; i < cells.size; i++){
                EruptorCell cell = cells.get(i);
                tr3.trns(rotation - 90, cell.xOffset * layerOpen[cell.layer - 1], cell.yOffset * layerOpen[cell.layer - 1]);
                Draw.rect(outlineRegions[i], tx + tr3.x, ty + tr3.y, rotation - 90f);
            }

            Draw.rect(turretRegion, tx, ty, rotation - 90f);

            if(heat > 0.00001f){
                Draw.blend(Blending.additive);
                Draw.color(heatColor, heat);
                Draw.rect(heatRegion, tx, ty, rotation - 90f);
                Draw.blend();
                Draw.color();
            }

            for(int i = 0; i < layers; i++){
                for(int j = 0; j < cells.size; j++){
                    EruptorCell cell = cells.get(j);
                    if((cell.layer - 1) != i) continue;
                    Draw.rect(cellRegions[j], tx, ty, rotation - 90f);
                    if(heat > 0.00001f){
                        Draw.blend(Blending.additive);
                        Draw.color(heatColor, heat);
                        Draw.rect(heatRegions[j], tx, ty, rotation - 90f);
                        Draw.blend();
                        Draw.color();
                    }
                }
                for(int j = 0; j < cells.size; j++){
                    EruptorCell cell = cells.get(j);
                    if((cell.layer - 1) != i) continue;
                    tr3.trns(rotation - 90, cell.xOffset * layerOpen[i], cell.yOffset * layerOpen[i]);
                    Draw.rect(capRegions[j], tx + tr3.x, ty + tr3.y, rotation - 90f);
                }
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(bulletLife <= 0 || bullet == null){
                for(int i = 0; i < layerOpen.length; i++){
                    layerOpen[i] = Mathf.lerpDelta(layerOpen[i], 0f, capCloseRate);
                }
            }

            if(bulletLife > 0 && bullet != null){
                wasShooting = true;
                tr.trns(rotation, length, 0f);
                bullet.set(x + tr.x, y + tr.y);
                bullet.time(0f);
                recoil = recoilAmount;
                heat = 1f;
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                for(int i = 0; i < layerOpen.length; i++){
                    float offset = Mathf.absin(bulletLife / 6f + Mathf.randomSeed(bullet.id * 2), 1f, 1f);
                    layerOpen[i] = i % 2 == 0 ? offset : 1f - offset;
                }
                extendTo(Math.min(range + rangeExtention, dst(targetPos)));
                if(timer(lightningTimer, lightningInterval)){
                    tr2.trns(rotation, shootLength - recoil);
                    PMFx.fakeLightning.at(x + tr2.x, y + tr2.y, angleTo(bullet), lightningColor, new LightningData(bullet, lightningStroke));
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