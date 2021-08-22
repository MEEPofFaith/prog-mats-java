package progressed.world.blocks.crafting;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.production.*;
import progressed.content.*;
import progressed.util.*;

public class ColliderCrafter extends GenericCrafter{
    public Color pC1, pC2, heatColor = Pal.accent;
    public float collidePoint = 12f, collideSep = 3f;
    public float collideStart = 0.5f, collideEnd = 0.95f;
    public float lengthStart, lengthEnd = 72f;
    public float strokeStart = 1.25f, strokeEnd = 0.75f;
    public float[] strokes = {1f, 0.9f, 0.75f, 0.55f};
    public float particleSize = 1.05f;
    public float lightOpacity = 1f;
    public float startRot = 2700f;
    public float cooldown = 0.03f;

    public TextureRegion bottomRegion, colliderRegion, glassRegion, topRegion;
    public TextureRegion[] heatRegions = new TextureRegion[2], lightRegions = new TextureRegion[2];

    public ColliderCrafter(String name){
        super(name);
        ambientSound = Sounds.none;
        hasPower = true;
        baseExplosiveness = 5f;
    }

    @Override
    public void load(){
        super.load();
        bottomRegion = Core.atlas.find(name + "-bottom");
        colliderRegion = Core.atlas.find(name + "-collider");
        glassRegion = Core.atlas.find(name + "-glass");
        topRegion = Core.atlas.find(name + "-top");
        for(int i = 0; i < 2; i++){
            heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
            lightRegions[i] = Core.atlas.find(name + "-light-" + i);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
            bottomRegion,
            colliderRegion,
            glassRegion,
            topRegion
        };
    }

    @Override
    public void init(){
        super.init();
        if(pC1 == null) pC1 = consumes.getItem().items[0].item.color;
        if(pC2 == null) pC2 = consumes.getItem().items[1].item.color;
    }

    public class ColliderCrafterBuild extends GenericCrafterBuild{
        protected Seq<Color> colors = Seq.with(pC1, pC2);
        protected float[] length = new float[2], curRot = new float[2], dist = new float[2];
        protected float stroke, heat, beforeHeat;
        protected boolean collided, released, reset;

        @Override
        public void draw(){
            Draw.z(Layer.block);
            Draw.rect(bottomRegion, x, y);

            if(progress > collideStart){
                for(int i = 0; i < 2; i++){
                    float[][] points = {
                        {x + Angles.trnsx(curRot[i], dist[i]), y + Angles.trnsy(curRot[i], dist[i])},
                        {x + Angles.trnsx(curRot[i] + length[i] * 0.25f, dist[i]), y + Angles.trnsy(curRot[i] + length[i] * 0.25f, dist[i])},
                        {x + Angles.trnsx(curRot[i] + length[i] * 0.5f, dist[i]), y + Angles.trnsy(curRot[i] + length[i] * 0.5f, dist[i])},
                        {x + Angles.trnsx(curRot[i] + length[i] * 0.75f, dist[i]), y + Angles.trnsy(curRot[i] + length[i] * 0.75f, dist[i])},
                        {x + Angles.trnsx(curRot[i] + length[i], dist[i]), y + Angles.trnsy(curRot[i] + length[i], dist[i])}
                    };

                    Draw.color(colors.get(i));
                    Fill.circle(points[0][0], points[0][1], stroke * particleSize);
                    for(int j = 0; j < 4; j++){
                        Lines.stroke(stroke * strokes[j]);
                        Lines.line(points[j][0], points[j][1], points[j + 1][0], points[j + 1][1]);
                    }
                    Draw.color();
                }
            }

            Draw.z(Layer.blockOver);
            Draw.rect(colliderRegion, x, y);

            if(heat > 0.00001f){
                Draw.blend(Blending.additive);
                Draw.color(heatColor, heat);
                Draw.rect(heatRegions[0], x ,y);
                Draw.blend();
                Draw.color();
            }

            Drawf.light(team, x, y, lightRegions[0], heatColor, heat * lightOpacity);

            Draw.rect(glassRegion, x, y);
            Draw.rect(topRegion, x, y);

            if(heat > 0.00001f){
                Draw.blend(Blending.additive);
                Draw.color(heatColor, heat);
                Draw.rect(heatRegions[1], x ,y);
                Draw.blend();
                Draw.color();
            }

            Drawf.light(team, x, y, lightRegions[1], heatColor, heat * lightOpacity);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if((progress >= 1f || progress < collideStart) && !reset){
                colors.shuffle();

                collided = released = false;
                reset = true;
                beforeHeat = heat;
            }
            
            float before = Mathf.curve(progress, 0f, collideStart);
            float colliding = Mathf.curve(progress, collideStart, collideEnd);
            float interpColliding = Interp.pow2In.apply(Mathf.curve(progress, collideStart, collideEnd));
            float after = Mathf.curve(progress, collideEnd, 1f);

            stroke = Mathf.lerp(strokeStart, strokeEnd, interpColliding);

            for(int i = 0; i < 2; i++){
                int side = Mathf.signs[i];

                curRot[i] = startRot * (1f - interpColliding) * side - 90f;
                dist[i] = Mathf.lerp(collidePoint + collideSep * side, collidePoint, colliding);

                if(progress <= collideStart && progress >= 0f){
                    length[i] = 0f;
                }else if(progress > collideStart && progress < collideEnd){
                    length[i] = Mathf.lerp(lengthStart, lengthEnd, interpColliding);
                }else if(progress >= collideEnd && progress <= 1f){
                    length[i] = Mathf.lerp(lengthEnd, 0f, after);
                }
                length[i] *= side;
                length[i] = PMUtls.equalArcLen(collidePoint, dist[i], length[i]);
            }

            if(progress <= collideStart && progress >= 0f){
                heat = Mathf.lerp(beforeHeat, 1f, before);
            }else{
                heat = Mathf.lerpDelta(heat, 0f, cooldown);
            }

            if(progress >= collideStart && progress <= collideEnd && !released){
                Sounds.railgun.at(x, y + collidePoint, Mathf.random(1.4f, 1.6f), Mathf.random(0.1f, 0.3f));
                released = true;
            }

            if(progress >= collideEnd && !collided){
                PMFx.colliderFusion.at(x, y - collidePoint);
                Sounds.bang.at(x, y - collidePoint, Mathf.random(1.4f, 1.6f), Mathf.random(0.3f, 0.5f));
                collided = true;
                reset = false;
            }
        }
    }
}