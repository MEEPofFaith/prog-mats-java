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

public class MagmaPylon extends Block{
    public float minDelay = 30, maxDelay = 120;

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

    public MagmaPylon(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);

        update = true;
        solid = true;
        breakable = alwaysReplace = rebuildable = targetable = false;
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

    public class MagmaPylonBuild extends Building{
        public float delay, timer;

        @Override
        public void created(){
            super.created();

            delay = Mathf.random(minDelay, maxDelay);
        }

        @Override
        public void draw(){
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
        }

        @Override
        public void updateTile(){
            super.updateTile();

            timer += Time.delta;

            if(timer >= delay) explode();
        }

        public void explode(){
            if(status != StatusEffects.none){
                Damage.status(damageAll ? null : team, x, y, radius, status, statusDuration, damageAir, damageGround);
            }
            Damage.damage(damageAll ? null : team, x, y, radius, damage, damageAir, damageGround);

            kill();
        }

        public float timerf(){
            return timer / delay;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(delay);
            write.f(timer);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            delay = read.f();
            timer = read.f();
        }
    }
}