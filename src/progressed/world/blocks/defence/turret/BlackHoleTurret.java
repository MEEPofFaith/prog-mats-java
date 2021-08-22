package progressed.world.blocks.defence.turret;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.Interp.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.util.*;
import progressed.world.meta.*;

public class BlackHoleTurret extends PowerTurret{
    public TextureRegion spaceRegion;

    protected PowIn pow = PMUtls.customPowIn(6);
    
    public BlackHoleTurret(String name){
        super(name);
        heatDrawer = tile -> {
            if(tile.heat <= 0.00001f) return;
            float r = pow.apply(tile.heat);
            float g = (Interp.pow3In.apply(tile.heat) + ((1f - Interp.pow3In.apply(tile.heat)) * 0.12f)) / 2f;
            float b = Interp.pow2Out.apply(tile.heat);
            float a = Interp.pow2Out.apply(tile.heat);
            Tmp.c1.set(r, g, b, a);
            Draw.color(Tmp.c1);
    
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, tile.x + tr2.x, tile.y + tr2.y, tile.rotation - 90);
            Draw.blend();
            Draw.color();
        };
    }

    @Override
    public void load(){
        super.load();

        spaceRegion = Core.atlas.find(name + "-space");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(OrderedMap.of(this, shootType)));
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.add("pm-reload", (BlackHoleTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reload / reloadTime)
        ));

        bars.add("pm-charge", (BlackHoleTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-charge", PMUtls.stringsFixed(Mathf.clamp(entity.charge) * 100f)),
            () -> Color.navy,
            () -> entity.charge
        ));
    }

    public class BlackHoleTurretBuild extends PowerTurretBuild{
        protected float alpha, charge;

        @Override
        public void draw(){
            super.draw();

            Draw.color(Tmp.c1.set(team.color).lerp(Color.black, 0.7f + Mathf.absin(10f, 0.2f)), alpha);
            Draw.rect(spaceRegion, x + tr2.x, y + tr2.y, rotation - 90f);
            Draw.reset();
        }

        @Override
        public void updateTile(){
            alpha = Mathf.lerpDelta(alpha, Mathf.num(consValid()), 0.1f);

            if(charging){
                charge = Mathf.clamp(charge + Time.delta / chargeTime);
            }else{
                charge = 0;
            }

            super.updateTile();
        }

        @Override
        protected void shoot(BulletType type){
            useAmmo();

            tr.trns(rotation, shootLength - recoil);
            chargeBeginEffect.at(x + tr.x, y + tr.y, rotation, team.color);
            chargeSound.at(x + tr.x, y + tr.y, 1);

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, shootLength - recoil);
                    chargeEffect.at(x + tr.x, y + tr.y, rotation, team.color);
                });
            }

            charging = true;

            Time.run(chargeTime, () -> {
                if(!isValid()) return;
                tr.trns(rotation, shootLength - recoil);
                recoil = recoilAmount;
                heat = 1f;
                bullet(type, rotation + Mathf.range(inaccuracy));
                effects();
                charging = false;
            });
        }
    }
}