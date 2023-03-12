package progressed.world.blocks.storage;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.*;
import progressed.content.effects.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CrashCore extends CoreBlock{
    public float crashHealth = -1f;
    public Sound explosionSound = Sounds.titanExplosion;
    public float explosionSoundPitchMin = 0.9f, explosionSoundPitchMax = 1.1f;
    public float explosionSoundVolume = 1f;
    public Effect landExplosion = MissileFx.missileExplosion;
    public TextureRegion squareShadow;

    public CrashCore(String name){
        super(name);

        hasShadow = false;
    }

    static{
        Events.run(Trigger.newGame, () -> {
            if(player.bestCore() instanceof CrashCoreBuild c){
                c.landed = false;

                //Since the thruster time is never set to 1, separately handle exploding when the animation is skipped.
                if(!(!settings.getBool("skipcoreanimation") && !state.rules.pvp)){
                    ((CrashCore)c.block).explode(c);
                }
            }
        });
    }

    @Override
    public void init(){
        super.init();

        if(crashHealth < 0) crashHealth = health / 10f;
    }

    @Override
    public void load(){
        super.load();
        squareShadow = Core.atlas.find("square-shadow");
    }

    @Override
    protected TextureRegion[] icons(){
        return teamRegion.found() ? new TextureRegion[]{region, teamRegions[Team.sharded.id]} : new TextureRegion[]{region};
    }

    @Override
    public void drawLanding(CoreBuild build, float x, float y){
        if(renderer.isLaunching()){
            super.drawLanding(build, x, y);
            return;
        }

        float fin = 1f - renderer.getLandTime() / coreLandDuration;
        fin = Mathf.curve(fin, 0.875f, 1f);
        float fout = 1f - fin;

        float scl = Scl.scl(4f) / renderer.getDisplayScale();
        float dst = 400f * fout;
        float ang = Mathf.randomSeed(build.id, 112.5f, 157.5f);
        Tmp.v1.trns(ang, dst);

        Draw.scl(scl);
        Draw.alpha(1f - fout);
        Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y);

        if(teamRegions[build.team.id] == teamRegion){
            Draw.color(build.team.color);
            Draw.alpha(1f - fout);
        }
        Draw.rect(teamRegions[build.team.id], x + Tmp.v1.x, y + Tmp.v1.y);

        Draw.color();
        Draw.scl();
        Draw.reset();
    }

    public void explode(CrashCoreBuild core){
        landExplosion.at(core);
        explosionSound.at(core.x, core.y, Mathf.random(explosionSoundPitchMin, explosionSoundPitchMax), explosionSoundVolume);
        core.health = crashHealth;
        core.landed = true;
        core.tile.getLinkedTiles(t -> {
            //0 ~ 360 -> -360/16 ~ 360/16
            float ang = core.angleTo(t.worldx(), t.worldy()) / 8f - 360f / 16f;
            for(int i = 0; i < 8; i++){
                if(Mathf.chance(0.8f)){
                    Fx.coreLandDust.at(t.worldx(), t.worldy(), ang + Mathf.range(15f), Tmp.c1.set(t.floor().mapColor).mul(1.5f + Mathf.range(0.15f)));
                }
            }
        });
    }

    public class CrashCoreBuild extends CoreBuild{
        boolean landed = true;

        @Override
        public void draw(){
            if(!landed) return;

            Draw.z(Layer.block - 1);
            Draw.color(BlockRenderer.shadowColor);
            float rad = 1.6f;
            float size = block.size * tilesize;
            Draw.rect(squareShadow, x, y, size * rad * Draw.xscl, size * rad * Draw.yscl);
            Draw.color();
            Draw.z(Layer.block);


            super.draw();
        }

        @Override
        public void drawLight(){
            if(!landed) return;

            super.drawLight();
        }

        @Override
        public void updateTile(){
            if(thrusterTime == 1f){
                explode(this);
            }

            super.updateTile();
        }

        @Override
        public void updateLandParticles(){
            if(renderer.isLaunching()){
                super.updateLandParticles();
            }
        }
    }
}
