package progressed.world.blocks.payloads;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class Missile extends Block{
    public BulletType explosionBullet;
    public int explosions = 1;
    public float explosionArea = 0f;
    public float maxDelay;

    public Block prev;
    public float powerUse, constructTime = -1;
    public boolean displayCampaign = true;

    public float elevation = -1f;

    public boolean outlined;

    public TextureRegion topRegion, outlineRegion;

    public Missile(String name){
        super(name);

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.units;
        health = 50; //volatile, do not destroy
        researchCostMultiplier = 5f;
        solid = true;
        update = true;
        hasShadow = false;
        rebuildable = false;
        drawDisabled = false;
        squareSprite = false;
    }

    @Override
    public void init(){
        if(constructTime < 0) constructTime = buildCost;
        if(elevation < 0) elevation = size / 3f;
        if(explosionArea < 0) explosionArea = size * tilesize;

        super.init();
    }

    @Override
    public void load(){
        super.load();

        outlineRegion = Core.atlas.find(name + "-outline");
        topRegion = Core.atlas.find(name + "-top", region);
    }

    @Override
    public void createIcons(MultiPacker packer){
        if(outlined){
            Outliner.outlineRegion(packer, region, outlineColor, name + "-outline");
            outlineRegion = Core.atlas.find(name + "-outline");
        }
        super.createIcons(packer);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            outlined ? outlineRegion : region,
            topRegion
        };
    }

    @Override
    public boolean canBeBuilt(){
        return false;
    }

    public class MissileBuild extends Building{
        boolean exploded = false; //Temporary measure against setting a `killShooter = true` bullet as the explosion bullet.

        @Override
        public void draw(){
            TextureRegion reg = outlined ? outlineRegion : region;
            Draw.z(Layer.blockUnder - 1f);
            Drawf.shadow(reg, x - elevation, y - elevation);
            Draw.z(Layer.block);
            Draw.rect(reg, x, y);
        }

        @Override
        public void drawCracks(){
            if(explosionBullet != null){
                float f = Mathf.clamp(healthf());
                Tmp.c1.set(Color.red).lerp(Color.white, f + Mathf.absin(Time.time, Math.max(f * 5f, 1f), 1f - f));
                Draw.color(Tmp.c1);
                Draw.rect(topRegion, x, y);
            }
        }

        @Override
        public void onDestroyed(){
            if(exploded) return;
            super.onDestroyed();

            //Kaboom
            explode();

            exploded = true;
        }

        public void explode(){
            if(explosionBullet != null){
                for(int i = 0; i < explosions; i++){
                    Time.run(Mathf.random(maxDelay), () -> {
                        PMMathf.randomCirclePoint(Tmp.v1, explosionArea);

                        if(explosionBullet.spawnUnit != null){ //Spawn and kill spawnUnit, if it exists
                            Unit spawned = explosionBullet.spawnUnit.create(team);
                            spawned.set(x + Tmp.v1.x, y + Tmp.v1.y);
                            spawned.add();
                            spawned.kill();
                            return;
                        }

                        Bullet b = explosionBullet.create(this, Team.derelict, x + Tmp.v1.x, y + Tmp.v1.y, 0f, 0f, 0f);
                        b.remove(); //Instantly explode
                    });
                }
            }
        }
    }
}
