package progressed.entities.comp;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.ai.*;
import progressed.content.effects.*;
import progressed.gen.entities.*;
import progressed.graphics.*;
import progressed.type.unit.*;

import static mindustry.Vars.*;

@EntityComponent
@EntityDef({SwordUnitc.class, Unitc.class, BuildingTetherc.class})
abstract class SwordUnitComp implements Unitc, BuildingTetherc{
    @Import float x, y, rotation, elevation;
    @Import Vec2 vel;
    @Import UnitType type;
    @Import Team team;
    @Import UnitController controller;
    @Import Building building;

    IntSeq collided = new IntSeq();
    float lastBaseX = Float.NEGATIVE_INFINITY, lastBaseY;
    int orbitPos;
    float heat;
    transient @SyncLocal DriftTrail[] driftTrails;

    @Override
    public void update(){
        if(sAI() != null && sAI().shouldDamage() && lastBaseX != Float.NEGATIVE_INFINITY){
            float
                tipX = x + Angles.trnsx(rotation, stype().tipY),
                tipY = y + Angles.trnsy(rotation, stype().tipY);

            if(type.targetGround) tileRaycast(World.toTile(lastBaseX), World.toTile(lastBaseY), World.toTile(tipX), World.toTile(tipY));
            unitRayCast(lastBaseX, lastBaseY, tipX, tipY);

            if(driftTrails != null){
                float
                    trailX = x + Angles.trnsx(rotation, stype().trailY),
                    trailY = y + Angles.trnsy(rotation, stype().trailY);
                Tmp.v1.set(vel).scl(stype().trailInheritVel);
                for(int i = 0; i < 2; i++){
                    float tRot = rotation + stype().trailAngle * Mathf.signs[i];
                    Tmp.v2.trns(tRot, stype().trailVel).add(Tmp.v1);
                    driftTrails[i].update(trailX, trailY, Tmp.v2);
                }
            }

            heat = Math.min(heat + Time.delta / stype().heatupTime, 1f);
        }else{
            if(driftTrails != null && driftTrails[0].size() > 0){
                for(DriftTrail trail : driftTrails){
                    TrailFadeFx.driftTrailFade.at(x, y, type.trailScl, type.trailColor, trail.copy());
                    trail.clear();
                }
            }

            heat = Math.max(heat - Time.delta / stype().cooldownTime, 0f);
        }

        lastBaseX = x + Angles.trnsx(rotation, stype().baseY);
        lastBaseY = y + Angles.trnsy(rotation, stype().baseY);
    }

    @Override
    public void remove(){
        if(driftTrails != null){
            for(DriftTrail trail: driftTrails){
                if(trail.size() > 0) TrailFadeFx.driftTrailFade.at(x, y, type.trailScl, type.trailColor, trail.copy());
            }
        }
    }

    @Override
    @Replace(69)
    public void damage(float v){
        //Don't flash
    }

    public void unitRayCast(float x1, float y1, float x2, float y2){
        Vec2 tr = Tmp.v1;
        Rect rect = Tmp.r1;
        float angle = Angles.angle(x1, y1, x2, y2);

        tr.trnsExact(angle, Mathf.dst(x1, y1, x2, y2));

        rect.setPosition(x, y).setSize(tr.x, tr.y);

        if(rect.width < 0){
            rect.x += rect.width;
            rect.width *= -1;
        }

        if(rect.height < 0){
            rect.y += rect.height;
            rect.height *= -1;
        }

        float expand = 3f;

        rect.y -= expand;
        rect.x -= expand;
        rect.width += expand * 2;
        rect.height += expand * 2;

        Cons<Unit> cons = e -> {
            e.hitbox(Tmp.r2);

            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, Tmp.r2.grow(expand * 2));

            if(vec != null && stype().damage > 0){
                stype().hitEffect.at(vec.x, vec.y, rotation, team.color);
                e.damage(damage());
                e.apply(stype().status, stype().statusDuration);
                collided.add(e.id);
            }
        };

        Units.nearbyEnemies(team, rect, u -> {
            if(u.checkTarget(stype().targetAir, stype().targetGround) && !collided.contains(u.id)) cons.get(u);
        });
    }

    //copy-paste of BulletComp#tileRaycast, because I don't know what I'm doing;
    public void tileRaycast(int x1, int y1, int x2, int y2){
        int x = x1, dx = Math.abs(x2 - x), sx = x < x2 ? 1 : -1;
        int y = y1, dy = Math.abs(y2 - y), sy = y < y2 ? 1 : -1;
        int e2, err = dx - dy;
        int ww = world.width(), wh = world.height();

        while(x >= 0 && y >= 0 && x < ww && y < wh){
            Building build = world.build(x, y);

            if(build != null && isAdded() && build.team != team && !hasCollided(build.id)){
                collided.add(build.id);
                stype().hitEffect.at(World.unconv(x), World.unconv(y), rotation, team.color);
                build.damage(damage());
            }

            if(x == x2 && y == y2) break;

            e2 = 2 * err;
            if(e2 > -dy){
                err -= dy;
                x += sx;
            }

            if(e2 < dx){
                err += dx;
                y += sy;
            }
        }
    }

    public void clearCollided(){
        collided.clear();
    }

    public boolean hasCollided(int id){
        return collided.size != 0 && collided.contains(id);
    }

    @Override
    @Replace
    public float speed(){
        SwordAI sAI = sAI();
        if(sAI == null){ //From Unitc
            float strafePenalty = isGrounded() || !isPlayer() ? 1f : Mathf.lerp(1f, type.strafePenalty, Angles.angleDist(vel().angle(), rotation) / 180f);
            float boost = Mathf.lerp(1f, type.canBoost ? type.boostMultiplier : 1f, elevation);
            return type.speed * strafePenalty * boost * floorSpeedMultiplier();
        }
        return sAI.speed();
    }

    public void orbitPos(int orbitPos){
        this.orbitPos = orbitPos;
    }

    public float damage(){
        return stype().damage * state.rules.blockDamage(team) * building.efficiency;
    }

    public SwordUnitType stype(){
        return (SwordUnitType)type;
    }

    public SwordAI sAI(){
        return controller instanceof SwordAI s ? s : null;
    }
}
