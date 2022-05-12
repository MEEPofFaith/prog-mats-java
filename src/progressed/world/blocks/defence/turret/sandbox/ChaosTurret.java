package progressed.world.blocks.defence.turret.sandbox;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.Interp.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.util.*;

public class ChaosTurret extends PowerTurret{
    public float shootDuration;

    protected PowIn pow = Interp.pow5In;

    public ChaosTurret(String name){
        super(name);
        requirements(Category.turret, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        heatDrawer = tile -> {
            if(tile.heat <= 0.00001f) return;
            float r = Interp.pow2Out.apply(tile.heat);
            float g = Interp.pow3In.apply(tile.heat) + ((1f - Interp.pow3In.apply(tile.heat)) * 0.12f);
            float b = pow.apply(tile.heat);
            float a = Interp.pow2Out.apply(tile.heat);
            Tmp.c1.set(r, g, b, a);
            Draw.color(Tmp.c1);
    
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, tile.x + recoilOffset.x, tile.y + recoilOffset.y, tile.rotation - 90);
            Draw.blend();
            Draw.color();
        };
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("pm-reload", (ChaosTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reloadCounter / reload)
        ));
    }

    public class ChaosTurretBuild extends PowerTurretBuild{
        protected Bullet bullet;

        @Override
        public void updateTile(){
            super.updateTile();

            if(active()){
                heat = 1f;
                curRecoil = 1f;
                wasShooting = true;
            }
        }

        @Override
        protected void updateCooling(){
            if(canConsume() && !active()){
                super.updateCooling();
            }
        }

        @Override
        protected void updateShooting(){
            if(canConsume() && !active()){
                if(reload >= reload && !charging()){
                    BulletType type = peekAmmo();

                    shoot(type);

                    reload = 0f;
                }else{
                    reload += delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
                }
            }
        }

        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                this.bullet = bullet;
            }
        }

        public boolean active(){
            return bullet != null && bullet.time < bullet.lifetime;
        }
    }
}
