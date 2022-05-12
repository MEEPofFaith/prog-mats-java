package progressed.world.blocks.defence.turret.energy;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.content.*;

public class BitTurret extends PowerTurret{
    public int sides = 8;

    public BitTurret(String name){
        super(name);
        shootSound = PMSounds.pixelShoot;
    }

    public class BitTurretBuild extends PowerTurretBuild{
        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);
            
            Draw.z(Layer.turret);

            float drawRot = Mathf.mod(rotation - 90f, 360f);
            float rot = Mathf.round(drawRot + (360f / sides / 2f), 360f / sides);
            Tmp.v1.trns(rot + 90, -curRecoil);

            Drawf.shadow(region, x + Tmp.v1.x - elevation, y + Tmp.v1.y - elevation, rot);
            Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y, rot);

            if(Core.atlas.isFound(heatRegion) && heat > 0.01){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, x + Tmp.v1.x, y + Tmp.v1.y, rot);
                Draw.blend();
                Draw.color();
            }
        }
    }
}
