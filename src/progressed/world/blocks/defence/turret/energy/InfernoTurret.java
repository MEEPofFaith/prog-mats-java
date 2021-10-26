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
import progressed.entities.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.energy.EruptorTurret.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class InfernoTurret extends PowerTurret{
    protected final Seq<Healthc> targets = new Seq<>();
    public final int lightningTimer = timers++;
    public float lightningInterval = 2f, lightningStroke = 3f;
    public Color lightningColor = Color.valueOf("ff9c5a");

    public int layers = 1;
    public Seq<EruptorCell> cells = new Seq<>();
    public float windUp = 0.1f, windDown = 0.01f;
    public float rangeExtention = 32f;
    public float shootDuration = 60f;
    public float capCloseRate = 0.01f;

    public TextureRegion bottomRegion, sideRegion, sideOutline, sideHeat;
    public TextureRegion[] cellRegions, capRegions, outlineRegions, heatRegions;

    protected Vec2 tr3 = new Vec2();

    public InfernoTurret(String name){
        super(name);

        canOverdrive = false;
        targetAir = targetGround = true;
        cooldown = restitution = 0.01f;
        shootCone = 360f;
        ammoUseEffect = Fx.none;
        shootSound = Sounds.none;
        ambientSound = Sounds.beam;
        ambientSoundVolume = 2f;
        heatColor = Color.valueOf("f08913");

        consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.01f)).update(false);
        coolantMultiplier = 1f;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.booster);
        stats.add(Stat.input, StatValues.boosters(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes.liquidfilters.get(l.id)));
        stats.remove(Stat.inaccuracy);
        stats.add(Stat.shots, "@stat.pm-inferno");
    }

    @Override
    public void load(){
        super.load();

        bottomRegion = atlas.find(name + "-bottom");
        sideRegion = atlas.find(name + "-side");
        sideOutline = atlas.find(name + "-side-outline");
        sideHeat = atlas.find(name + "-side-heat");
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

        if(minRange < 0) minRange = size * tilesize * 2f;

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
        bars.add("pm-reload", (InfernoTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp((reloadTime - entity.reload) / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> 1f - Mathf.clamp(entity.reload / reloadTime)
        ));
        bars.add("pm-shoot-duration", (InfernoTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-shoot-duration", PMUtls.stringsFixed(Mathf.clamp((entity.bulletLife / shootDuration) * 100f))),
            () -> lightningColor,
            () -> Mathf.clamp(entity.bulletLife / shootDuration)
        ));
    }

    public class InfernoTurretBuild extends PowerTurretBuild{
        protected Seq<Bullet> bullets = new Seq<>();
        protected float bulletLife, speed;
        protected float[] layerOpen = new float[cells.size];

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            Drawf.shadow(bottomRegion, x - elevation, y - elevation);
            for(int i = 0; i < 4; i++){
                tr3.trns(rotation + i * 90f, recoil);
                Drawf.shadow(sideOutline, x + tr3.x - elevation, y + tr3.y - elevation, rotation - 90f + i * 90f);
            }
            for(int i = 0; i < cells.size; i++){
                EruptorCell cell = cells.get(i);
                for(int j = 0; j < 4; j++){
                    tr2.trns(rotation - 90f + j * 90f, cell.xOffset * layerOpen[cell.layer - 1], cell.yOffset * layerOpen[cell.layer - 1]);
                    tr3.trns(rotation + j * 90f, recoil);
                    Drawf.shadow(outlineRegions[j], x + tr2.x + tr3.x - elevation, y + tr2.y + tr3.y - elevation, rotation - 90f + j * 90f);
                }
            }

            Draw.rect(bottomRegion, x, y);
            for(int i = 0; i < cells.size; i++){
                EruptorCell cell = cells.get(i);
                for(int j = 0; j < 4; j++){
                    tr2.trns(rotation - 90f + j * 90f, cell.xOffset * layerOpen[cell.layer - 1], cell.yOffset * layerOpen[cell.layer - 1]);
                    tr3.trns(rotation + j * 90f, recoil);
                    Draw.rect(outlineRegions[j], x + tr2.x + tr3.x, y + tr2.y + tr3.y, rotation - 90f + j * 90f);
                }
            }
            
            Draw.rect(bottomRegion, x, y);

            if(heat > 0.00001f){
                Draw.blend(Blending.additive);
                Draw.color(heatColor, heat);
                Draw.rect(heatRegion, x, y);
                Draw.blend();
                Draw.color();
            }

            for(int i = 0; i < 4; i++){
                tr3.trns(rotation + i * 90f, recoil);
                Draw.rect(sideOutline, x + tr3.x, y + tr3.y, rotation - 90f + i * 90f);
            }
            for(int i = 0; i < 4; i++){
                tr3.trns(rotation + i * 90f, recoil);
                Draw.rect(sideRegion, x + tr3.x, y + tr3.y, rotation - 90f + i * 90f);
                if(heat > 0.00001f){
                    Draw.blend(Blending.additive);
                    Draw.color(heatColor, heat);
                    Draw.rect(sideHeat, x + tr3.x, y + tr3.y, rotation - 90f + i * 90f);
                    Draw.blend();
                    Draw.color();
                }
            }

            for(int i = 0; i < layers; i++){
                for(int j = 0; j < cells.size; j++){
                    EruptorCell cell = cells.get(j);
                    if((cell.layer - 1) != i) continue;
                    for(int k = 0; k < 4; k++){
                        tr3.trns(rotation + k * 90f, recoil);
                        Draw.rect(cellRegions[j], x + tr3.x, y + tr3.y, rotation - 90f + k * 90f);
                        if(heat > 0.00001f){
                            Draw.blend(Blending.additive);
                            Draw.color(heatColor, heat);
                            Draw.rect(heatRegions[j], x + tr3.x, y + tr3.y, rotation - 90f + k * 90f);
                            Draw.blend();
                            Draw.color();
                        }
                    }
                }
                for(int j = 0; j < cells.size; j++){
                    EruptorCell cell = cells.get(j);
                    if((cell.layer - 1) != i) continue;
                    for(int k = 0; k < 4; k++){
                        tr2.trns(rotation - 90f + k * 90f, cell.xOffset * layerOpen[cell.layer - 1], cell.yOffset * layerOpen[cell.layer - 1]);
                        tr3.trns(rotation + k * 90f, recoil);
                        Draw.rect(capRegions[j], x + tr2.x + tr3.x, y + tr2.y + tr3.y, rotation - 90f + k * 90f);
                    }
                }
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(bulletLife <= 0 || bullets.size == 0){
                speed = Mathf.lerpDelta(speed, 0f, windDown);
                for(int i = 0; i < layerOpen.length; i++){
                    layerOpen[i] = Mathf.lerpDelta(layerOpen[i], 0f, capCloseRate);
                }
            }

            if(bulletLife > 0 && bullets.size > 0){
                wasShooting = true;
                speed = Mathf.lerpDelta(speed, rotateSpeed, windUp);
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                for(int i = 0; i < layerOpen.length; i++){
                    float offset = Mathf.absin(bulletLife / 6f + Mathf.randomSeed(bullets.get(0).id * 2), 1f, 1f);
                    layerOpen[i] = i % 2 == 0 ? offset : 1f - offset;
                }
                heat = 1f;
                boolean lightning = timer(lightningTimer, lightningInterval);
                bullets.each(b -> {
                    b.time(0f);
                    if(lightning){
                        tr.trns(rotation + Mathf.random(3) * 90f, shootLength + recoil);
                        Vec2 tmp1 = Tmp.v1.set(x + tr.x, y + tr.y);
                        PMFx.fakeLightning.at(tmp1.x, tmp1.y, tmp1.angleTo(b), lightningColor, new LightningData(b, lightningStroke));
                    }
                    if(bulletLife <= 0f){
                        bullets.remove(b);
                    }
                });
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

            rotation -= speed * Time.delta;
        }

        @Override
        protected void updateCooling(){
            //Do nothing, cooling is irrelevant here
        }

        @Override
        protected void updateShooting(){
            if(bulletLife > 0 && bullets.size > 0){
                return;
            }

            if(reload <= 0 && (consValid() || cheating())){
                BulletType type = peekAmmo();

                shoot(type);

                reload = reloadTime;
            }
        }

        @Override
        protected void shoot(BulletType type){
            targets.clear();
            bullets.clear();

            PMDamage.allNearbyEnemies(team, x, y, range + rangeExtention, targets::add);

            if(targets.size > 0){
                bulletLife = shootDuration;
                recoil = recoilAmount; //Use recoil for the side expantion
                targets.each(t -> {
                    bullets.add(type.create(tile.build, team, t.x(), t.y(), 0f));
                });
            }
        }

        @Override
        public boolean shouldTurn(){
            return false;
        }

        @Override
        public boolean shouldAmbientSound(){
            return bulletLife > 0 && bullets.size > 0;
        }

        @Override
        public boolean canControl(){
            return false;
        }
    }
}