package progressed.entities.units;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.UnitType.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.graphics.*;

public class DriftTrailUnit extends UnitEntity{
    public Seq<DriftTrail> driftTrails;
    static Seq<DriftEngine> tempEngines = new Seq<>();

    @Override
    public void update(){
        super.update();

        if(type.engines.contains(e -> e instanceof DriftEngine)){
            getDriftEngines();

            if(driftTrails == null){
                driftTrails = new Seq<>();
                tempEngines.each(d -> {
                    driftTrails.add(new DriftTrail(d.trailLength));
                });
            }

            for(int i = 0; i < tempEngines.size; i++){
                DriftEngine e = tempEngines.get(i);
                Vec2 vel = e.vel(this);
                float scale = type.useEngineElevation ? elevation : 1f;
                float width = e.trailWidth <= 0 ? (e.radius + Mathf.absin(2f, e.radius / 4f)) * scale : e.trailWidth;
                driftTrails.get(i).update(
                    x + Angles.trnsx(rotation - 90f, e.x, e.y), y + Angles.trnsy(rotation - 90f, e.x, e.y),
                    width, vel, e.trailDrag
                );
            }
        }
    }

    @Override
    public void remove(){
        if(driftTrails != null){
            Color tColor = type.engineColor == null ? team.color : type.engineColor;
            driftTrails.each(t -> t.size() > 0, t -> UtilFx.driftTrailEngineFade.at(x, y, 1f, tColor, t.copy()));
        }

        super.remove();
    }

    public Seq<DriftEngine> getDriftEngines(){
        tempEngines.clear();
        type.engines.each(e -> e instanceof DriftEngine, (DriftEngine d) -> tempEngines.add(d));

        return tempEngines;
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(DriftTrailUnit.class);
    }

    public static class DriftEngine extends UnitEngine{
        public int trailLength;
        public float trailWidth, trailVel, trailInherit, trailDrag;

        public DriftEngine(float x, float y, float radius, float rotation){
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.rotation = rotation;
        }

        public DriftEngine(){
        }

        public DriftEngine setTrail(int length, float width, float vel, float inherit, float drag){
            trailLength = length;
            trailWidth = width;
            trailVel = vel;
            trailInherit = inherit;
            trailDrag = drag;
            return this;
        }

        public DriftEngine setTrail(int length, float vel, float inherit, float drag){
            return setTrail(length, -1, vel, inherit, drag);
        }

        public DriftEngine setTrail(int length, float vel, float inherit){
            return setTrail(length, vel, inherit, 0f);
        }

        public Vec2 vel(Unit u){
            Tmp.v1.trns(angle(u), trailVel);
            if(trailInherit > 0.01f){
                Tmp.v2.set(u.vel).scl(trailInherit);
                Tmp.v1.add(Tmp.v2);
            }
            return Tmp.v1;
        }

        public float angle(Unit u){
            return u.rotation - 90f + rotation; //TODO sine swaying
        }

        public DriftEngine copy(){
            try{
                return (DriftEngine)clone();
            }catch(CloneNotSupportedException pain){
                throw new RuntimeException("end me", pain);
            }
        }
    }
}
