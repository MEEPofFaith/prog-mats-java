package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import progressed.entities.bullet.explosive.BallisticMissileBulletType.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class MissileTurret extends ItemTurret{
    public float[][] shootLocs = {{0f, 0f}};
    public TextureRegion[] heatRegions;
    public boolean reloadBar;

    public MissileTurret(String name){
        super(name);
        shootEffect = smokeEffect = Fx.none;
    }

    @Override
    public void load(){
        super.load();
        heatRegions = new TextureRegion[shootLocs.length];
        for(int i = 0; i < heatRegions.length; i++){
            heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void init(){
        super.init();
        shots = shootLocs.length;
    }

    
            
    @Override
    public void setBars(){
        super.setBars();
        if(reloadBar){
            bars.add("pm-reload", (MissileTurretBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
                () -> entity.team.color,
                () -> Mathf.clamp(entity.reload / reloadTime)
            ));
        }
    }

    public class MissileTurretBuild extends ItemTurretBuild{
        protected boolean firing;
        protected boolean[] hasShoot = new boolean[shots];
        protected float speedScl;
        protected float[] heats = new float[shots];
        protected int currentAmmo;

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(hasAmmo() && peekAmmo() != null){
                Draw.draw(Draw.z(), () -> {
                    for(int i = 0; i < Math.min(shots, currentAmmo); i++){
                        if(!hasShoot[i]){
                            Drawf.construct(shootLocs[i][0] + x, shootLocs[i][1] + y, ((BasicBulletType)peekAmmo()).frontRegion, team.color, 0f, reload / reloadTime, speedScl, reload);
                        }
                    }
                });
            }

            for(int i = 0; i < heats.length; i++){
                if(Core.atlas.isFound(heatRegions[i]) && heats[i] > 0.001f){
                    Draw.color(heatColor, heats[i]);
                    Draw.blend(Blending.additive);
                    Draw.rect(heatRegions[i], x, y);
                    Draw.blend();
                    Draw.color();
                }
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            for(int i = 0; i < heats.length; i++){
                heats[i] = Mathf.lerpDelta(heats[i], 0f, cooldown);
            }
              
            if(reload < reloadTime && hasAmmo() && consValid() && !firing){
                speedScl = Mathf.lerpDelta(speedScl, 1f, 0.05f);
            }else{
                speedScl = Mathf.lerpDelta(speedScl, 0f, 0.05f);
            }
        }

        @Override
        protected void updateCooling(){
            if(!firing && hasAmmo() && consValid()){
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

                Liquid liquid = liquids.current();
        
                float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, ((reloadTime - reload) / coolantMultiplier) / liquid.heatCapacity)) * baseReloadSpeed();
                reload += used * liquid.heatCapacity * coolantMultiplier;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        protected void updateShooting(){
            if(hasAmmo() && consValid()){
                if(reload > reloadTime && !firing){
                    BulletType type = peekAmmo();
                  
                    shoot(type);
                }else if(!firing){
                    reload += Time.delta * peekAmmo().reloadMultiplier * baseReloadSpeed();
                }
            }
        }

        @Override
        protected void shoot(BulletType type){
            firing = true;
      
            for(int i = 0; i < Math.min(shots, totalAmmo); i++){
                final int sel = i;
                Time.run(burstSpacing * i, () -> {
                    if(!isValid() || !hasAmmo()) return;
                    float tx = shootLocs[sel][0] + x;
                    float ty = shootLocs[sel][1] + y;
                    
                    type.create(this, team, tx, ty, rotation + Mathf.range(inaccuracy), -1f, 1f + Mathf.range(velocityInaccuracy), 1f, new ArcMissileData(tx, ty));
                    effects();
                    useAmmo();
                    heats[sel] = 1f;
                    hasShoot[sel] = true;
                });
            }
            
            Time.run(burstSpacing * Math.min(shots, totalAmmo), () -> {
                reload = 0f;
                firing = false;
                for(int i = 0; i < hasShoot.length; i++){
                    hasShoot[i] = false;
                }
                currentAmmo = totalAmmo;
            });
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return !firing && super.acceptItem(source, item);
        }

        @Override
        public void handleItem(Building source, Item item){
            super.handleItem(source, item);

            reload = 0f;
            if(!firing) currentAmmo += ammoTypes.get(item).ammoMultiplier;
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = targetRot;
        }
    }
}
