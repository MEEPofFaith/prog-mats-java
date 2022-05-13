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
        public float drawrot(){
            return Mathf.round(super.drawrot() + (360f / sides / 2f), 360f / sides);
        }
    }
}
