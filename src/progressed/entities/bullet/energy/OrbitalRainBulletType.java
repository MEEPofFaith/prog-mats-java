package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.graphics.*;

public class OrbitalRainBulletType extends OrbitalStrikeBulletType{
    public float rainRadius = 64f, rainStartDelay = -1f, rainDelay = 10f, rainEndDelay = -1f;
    public int amount = 12;
    public float rainGrowTime = 10f, rainShrinkTime = 20f;
    public float tiltScl = 0.75f;
    public int points = 4;
    public float pointRotateSpeed = -0.5f, pointWidth = 12f, pointInLength = 16f, pointOutLength = 24f;
    public Color rainAreaColor;

    @Override
    public void init(){
        super.init();

        if(rainAreaColor == null) rainAreaColor = bottomColor;
        if(rainStartDelay < 0) rainStartDelay = rainGrowTime;
        if(rainEndDelay < 0) rainEndDelay = lifetime + rainShrinkTime;
        drawSize = Math.max(drawSize, (rainRadius + radius + pointOutLength + 4f) * 2f);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data == null){
            float[] pos = {b.x, b.y};
            for(int i = 0; i < amount; i++){
                Time.run(rainStartDelay + i * rainDelay, () -> {
                    Tmp.v1.rnd((float)(rainRadius * Math.sqrt(Mathf.random())));
                    create(b.owner, b.team, b.x + Tmp.v1.x, b.y + Tmp.v1.y, 0f, -1f, 0f, 1f, pos);
                });
            }
            b.lifetime(rainStartDelay + amount * rainDelay + rainEndDelay);
        }
    }

    @Override
    public void draw(Bullet b){
        //TODO
    }

    @Override
    public void despawned(Bullet b){
        if(b.data == null) return;

        super.despawned(b);
    }
}
