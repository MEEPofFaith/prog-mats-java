package progressed.world.blocks.defence;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class IgneousPillar extends Block{
    public float growTime = 10f, minDelay = 60f, maxDelay = 90f;

    public float damage = 70f, radius = 24f;
    public boolean damageAll;
    public boolean damageGround = true, damageAir = true;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 10f * 60f;

    public int glowVariants = 1;
    public int[] glowWeights;
    public TextureRegion[] glowRegions;
    public Color glowColorFrom = Pal.darkerGray, glowColorTo = Pal.lightPyraFlame;

    Seq<TextureRegion> weightedGlowRegions = new Seq<>();

    public IgneousPillar(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);

        update = true;
        solid = true;
        destructible = breakable = alwaysReplace = rebuildable = targetable = false;
        destroyEffect = Fx.flakExplosionBig; //TODO effect
    }

    @Override
    public boolean synthetic(){
        return false;
    }

    @Override
    public boolean canBeBuilt(){
        return false;
    }

    @Override
    public boolean canBreak(Tile tile){
        return false;
    }

    @Override
    public void load(){
        super.load();

        glowRegions = new TextureRegion[glowVariants];
        for(int i = 0; i < glowVariants; i++){
            glowRegions[i] = Core.atlas.find(name + "-glow-" + i);
        }

        if(glowWeights != null){ //there's probably a better way to do this
            weightedGlowRegions.clear();
            for(int i = 0; i < glowRegions.length; i++){
                for(int j = 0; j < glowWeights[i]; j++){
                    weightedGlowRegions.add(glowRegions[i]);
                }
            }
        }
    }

    public class IgneousPillarBuild extends Building{
        public float delay, growTimer, boomTimer;

        @Override
        public void created(){
            super.created();

            delay = Mathf.random(minDelay, maxDelay);
        }

        @Override
        public void draw(){
            Draw.alpha(growf());
            super.draw();

            Draw.color(glowColorFrom, glowColorTo, timerf());
            int seed = id + pos();
            if(glowWeights != null){
                Draw.rect(
                    weightedGlowRegions.get(Mathf.randomSeed(seed, 0, weightedGlowRegions.size - 1)),
                    x, y, block.rotate ? rotdeg() : 0f
                );
            }else{
                Draw.rect(
                    glowRegions[Mathf.randomSeed(seed, 0, glowRegions.length - 1)],
                    x, y, block.rotate ? rotdeg() : 0f
                );
            }

            Draw.reset();
        }

        @Override
        public void updateTile(){
            super.updateTile();

            growTimer += Time.delta;

            if(growTimer >= growTime){
                boomTimer += Time.delta;

                if(boomTimer >= delay) kill();
            }
        }

        @Override
        public void onDestroyed(){
            if(status != StatusEffects.none){
                Damage.status(damageAll ? null : team, x, y, radius, status, statusDuration, damageAir, damageGround);
            }
            Damage.damage(damageAll ? null : team, x, y, radius, damage, damageAir, damageGround);

            destroyEffect.at(x, y);
        }

        public float growf(){
            return Mathf.clamp(growTimer / growTime);
        }

        public float timerf(){
            return Mathf.clamp(boomTimer / delay);
        }

        @Override
        public void damage(float damage){
            //no
        }

        @Override
        public void damage(float amount, boolean withEffect){
            //no
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(delay);
            write.f(boomTimer);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            delay = read.f();
            boomTimer = read.f();
        }
    }
}