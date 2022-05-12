package progressed.world.blocks.defence.turret.energy;

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

    protected PowIn pow = Interp.pow5In;
    
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
            Draw.rect(heatRegion, tile.x + recoilOffset.x, tile.y + recoilOffset.y, tile.rotation - 90);
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
        addBar("pm-reload", (BlackHoleTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reloadCounter / reload)
        ));

        addBar("pm-charge", (BlackHoleTurretBuild entity) -> new Bar(
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
            Draw.rect(spaceRegion, x + recoilOffset.x, y + recoilOffset.y, rotation - 90f);
            Draw.reset();
        }

        @Override
        public void updateTile(){
            alpha = Mathf.lerpDelta(alpha, Mathf.num(canConsume()), 0.1f);

            if(charging()){
                charge = Mathf.clamp(charge + Time.delta / chargeTime);
            }else{
                charge = 0;
            }

            super.updateTile();
        }
    }
}
