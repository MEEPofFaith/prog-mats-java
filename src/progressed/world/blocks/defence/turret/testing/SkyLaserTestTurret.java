package progressed.world.blocks.defence.turret.testing;

import progressed.entities.bullet.pseudo3d.*;
import progressed.graphics.*;

import static mindustry.Vars.*;

public class SkyLaserTestTurret extends FreeTurret{
    public SkyLaserTestTurret(String name){
        super(name);

        float bRange = 80f * tilesize;
        range = bRange;
        shootType = new SkyBeamBulletType(3f, 600f){{
            lifetime = bRange / speed;
            radius = 1.5f * tilesize;
            drag = -0.01f;
            height = 2400;
        }};
        reload = 45f;
        shootY = 0f;
    }

    public class SkyLaserTestTurretBuild extends FreeTurretBuild{
        @Override
        public void draw(){
            super.draw();

            SkyBeamBulletType type = (SkyBeamBulletType)shootType;
            Draw3D.line(x, y, type.height, targetPos.x, targetPos.y, 0);
            Draw3D.drawDiskDebug(targetPos.x, targetPos.y, x, y, type.height, type.radius);
        }
    }
}
