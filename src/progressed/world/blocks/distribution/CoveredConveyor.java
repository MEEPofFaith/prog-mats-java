package progressed.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.*;
import progressed.util.*;

public class CoveredConveyor extends Conveyor{
    public TextureRegion[] coverRegions = new TextureRegion[5];
    public TextureRegion inputRegion, outputRegion;

    public CoveredConveyor(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; i < 5; i++){
            coverRegions[i] = Core.atlas.find(name + "-cover-" + i);
        }
        inputRegion = Core.atlas.find(name + "-cover-in");
        outputRegion = Core.atlas.find(name + "-cover-out");
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawPlanRegion(req, list);

        int[] bits = getTiling(req, list);

        if(bits == null) return;

        TextureRegion region = coverRegions[bits[0]];
        Draw.rect(region, req.drawx(), req.drawy(), region.width * bits[1] * Draw.scl, region.height * bits[2] * Draw.scl, req.rotation * 90);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{regions[0][0], coverRegions[0]};
    }

    public class FloatingConveyorBuild extends ConveyorBuild{
        public boolean backCap, leftCap, rightCap, frontCap;

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.block - 0.08f);
            Draw.rect(coverRegions[blendbits], x, y, Vars.tilesize * blendsclx, Vars.tilesize * blendscly, rotation * 90);

            if(frontCap) Draw.rect(outputRegion, x, y, rotdeg());
            if(!backCap) Draw.rect(inputRegion, x, y, rotdeg());
            if(leftCap) Draw.rect(inputRegion, x, y, rotdeg() - 90f);
            if(rightCap) Draw.rect(inputRegion, x, y, rotdeg() + 90f);
        }

        @Override
        public void unitOn(Unit unit){
            //There is a cover, can't slide on this thing
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            frontCap = nextc == null || block != nextc.block;

            Building backB = back();
            backCap = blendbits == 1 || blendbits == 4 || PMUtls.relativeDirection(backB, this) == 0 && backB.block == block;

            Building leftB = left();
            leftCap = blendbits != 0 && PMUtls.relativeDirection(leftB, this) == 0 && leftB.block != block;

            Building rightB = right();
            rightCap = blendbits != 0 && PMUtls.relativeDirection(rightB, this) == 0 && rightB.block != block;
        }
    }
}
