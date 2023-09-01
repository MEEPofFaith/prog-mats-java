package progressed.entities.comp;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.gen.entities.*;
import progressed.type.unit.*;

@EntityComponent
@EntityDef({SentryUnitc.class, Unitc.class, TimedKillc.class})
abstract class SentryUnitComp implements Unitc, TimedKillc{
    @Import float x, y, rotation, elevation, dragMultiplier;
    @Import int id;
    @Import Vec2 vel;
    @Import UnitType type;

    Vec2 anchorVel = new Vec2();
    float anchorX, anchorY, anchorRot;
    float anchorDrag;

    @Override
    public void setType(UnitType type){
        anchorDrag = ((SentryUnitType)type).anchorDrag;
    }

    @Override
    public void update(){
        if(!Vars.net.client() || this.isLocal()){
            float offset = anchorX;
            float range = anchorY;
            anchorX += anchorVel.x * Time.delta; //I'm sure letting the anchors overlap won't be problematic in anyway.
            anchorY += anchorVel.y * Time.delta;
            if(Mathf.equal(offset, anchorX)){
                anchorVel.x = 0f;
            }
            if(Mathf.equal(range, anchorY)){
                anchorVel.y = 0f;
            }

            anchorVel.scl(Math.max(1f - anchorDrag * Time.delta, 0f));
        }

        anchorDrag = ((SentryUnitType)type).anchorDrag * (isGrounded() ? floorOn().dragMultiplier : 1f) * dragMultiplier * Vars.state.rules.dragMultiplier;

        //Pull unit to anchor.
        //Similar to impulseNet, does not factor in mass
        Tmp.v1.set(anchorX, anchorY).sub(x, y).limit(dst(anchorX, anchorY) * ((SentryUnitType)type).pullScale * Time.delta);
        vel.add(Tmp.v1);

        //manually move units to simulate velocity for remote players
        if(isRemote()) move(Tmp.v1);

        //Pull anchor to unit
        Tmp.v1.set(x, y).sub(anchorX, anchorY).limit(dst(anchorX, anchorY) * ((SentryUnitType)type).anchorPullScale * Time.delta);
        anchorVel.add(Tmp.v1);

        if(isRemote()){
            anchorX += Tmp.v1.x;
            anchorY += Tmp.v1.y;
        }
    }

    @Override
    public void set(float x, float y){
        anchorX = x;
        anchorY = y;
    }

    @Override
    public void rotation(float rotation){
        this.rotation = rotation; //Improperly replaces rotation. Glenn will fix later.
        anchorRot = rotation;
    }

    @Override
    @Replace
    public float prefRotation(){
        return rotation;
    }

    @Override
    @Replace
    public void wobble() {
        anchorX += Mathf.sin(Time.time + (float)(id % 10 * 12), 25f, 0.05f) * Time.delta * elevation;
        anchorY += Mathf.cos(Time.time + (float)(id % 10 * 12), 25f, 0.05f) * Time.delta * elevation;
    }
}
