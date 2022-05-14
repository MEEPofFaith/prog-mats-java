package progressed.world.blocks.defence.turret.energy;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.Interp.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import progressed.util.*;
import progressed.world.meta.*;

public class BlackHoleTurret extends PowerTurret{
    public TextureRegion spaceRegion;

    protected PowIn pow = Interp.pow5In;
    
    public BlackHoleTurret(String name){
        super(name);

        drawer = new DrawTurret(){
            @Override
            public void drawHeat(Turret block, TurretBuild build){
                if(build.heat <= 0.00001f || !heat.found()) return;

                float r = pow.apply(build.heat);
                float g = (Interp.pow3In.apply(build.heat) + ((1f - Interp.pow3In.apply(build.heat)) * 0.12f)) / 2f;
                float b = Interp.pow2Out.apply(build.heat);
                float a = Interp.pow2Out.apply(build.heat);
                Tmp.c1.set(r, g, b, a);

                Drawf.additive(heat, Tmp.c1, build.x + build.recoilOffset.x, build.y + build.recoilOffset.y, build.drawrot(), Layer.turretHeat);
            }
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
                charge = Mathf.clamp(charge + Time.delta / shoot.firstShotDelay);
            }else{
                charge = 0;
            }

            super.updateTile();
        }
    }
}
