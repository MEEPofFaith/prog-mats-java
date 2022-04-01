package progressed.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;

import static mindustry.Vars.*;

public class CoveredConveyor extends Conveyor{
    public TextureRegion[] coverRegions = new TextureRegion[5];

    public CoveredConveyor(String name){
        super(name);
        floating = true;
        placeableLiquid = true;
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
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawRequestRegion(req, list);

        int[] bits = getTiling(req, list);

        if(bits == null) return;

        TextureRegion region = coverRegions[bits[0]];
        Draw.rect(region, req.drawx(), req.drawy(), region.width * bits[1] * Draw.scl, region.height * bits[2] * Draw.scl, req.rotation * 90);
    }

    public class FloatingConveyorBuild extends ConveyorBuild{
        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.block - 0.08f);
            Draw.rect(coverRegions[blendbits], x, y, coverRegions[blendbits].width / 4f * blendsclx, coverRegions[blendbits].height / 4f * blendscly, rotation * 90);
        }

        @Override
        public void unitOn(Unit unit){
            //There is a cover, can't slide on this thing
        }
    }
}
