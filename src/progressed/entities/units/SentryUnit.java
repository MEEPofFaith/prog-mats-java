package progressed.entities.units;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import progressed.content.*;
import progressed.type.unit.*;

public class SentryUnit extends TimedKillUnit{
    public Vec2 anchorVel = new Vec2();
    public float anchorX, anchorY, anchorRot;
    public float anchorDrag;

    @Override
    public void setType(UnitType type){
        super.setType(type);

        anchorDrag = ((SentryUnitType)type).anchorDrag;
    }

    @Override
    public void update(){
        super.update();

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

        //similar to impulseNet, does not factor in mass
        Tmp.v1.set(anchorX, anchorY).sub(x, y).limit(dst(anchorX, anchorY) * ((SentryUnitType)type).pullScale * Time.delta);
        vel.add(Tmp.v1);

        //manually move units to simulate velocity for remote players
        if(isRemote()) move(Tmp.v1);
    }

    @Override
    public void set(float x, float y){
        super.set(x, y);

        anchorX = x;
        anchorY = y;
    }

    @Override
    public void rotation(float rotation){
        super.rotation(rotation);
        anchorRot = rotation;
    }

    @Override
    public void wobble() {
        anchorX += Mathf.sin(Time.time + (float)(id() % 10 * 12), 25f, 0.05f) * Time.delta * elevation;
        anchorY += Mathf.cos(Time.time + (float)(id() % 10 * 12), 25f, 0.05f) * Time.delta * elevation;
    }

    @Override
    public void write(Writes write){
        super.write(write);

        TypeIO.writeVec2(write, anchorVel);
        write.f(anchorRot);
        write.f(anchorX);
        write.f(anchorY);
    }

    @Override
    public void writeSync(Writes write){
        super.writeSync(write);

        TypeIO.writeVec2(write, anchorVel);
        write.f(anchorRot);
        write.f(anchorX);
        write.f(anchorY);
    }

    @Override
    public void read(Reads read){
        super.read(read);

        anchorVel = TypeIO.readVec2(read, anchorVel);
        anchorRot = read.f();
        anchorX = read.f();
        anchorY = read.f();
    }

    @Override
    public void readSync(Reads read){
        super.readSync(read);

        if(!isLocal()){
            anchorVel = TypeIO.readVec2(read, anchorVel);
            anchorRot = read.f();
            anchorX = read.f();
            anchorY = read.f();
        }else{
            TypeIO.readVec2(read, anchorVel);
            read.f();
            read.f();
            read.f();
        }
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(SentryUnit.class);
    }
}
