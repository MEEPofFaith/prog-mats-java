package progressed.world.blocks.defence.turret;

import arc.math.*;
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
        public void updateTile(){
            super.updateTile();

            recoilOffset.trns(drawrot() + 90f, -Mathf.pow(curRecoil, recoilPow) * recoil);
        }

        @Override
        public float drawrot(){
            float rot = Mathf.mod(rotation - 90f, 360f);
            return Mathf.round(rot + (360f / sides / 2f), 360f / sides);
        }
    }
}
