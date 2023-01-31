package progressed.entities.units;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import progressed.content.*;
import progressed.type.unit.*;
import progressed.world.blocks.defence.turret.SignalFlareTurret.*;

public class SignalFlareUnit extends UnitEntity implements Scaled{
    public Building building;
    public float time, lifetime, height = 0f;

    @Override
    public void update(){
        super.update();

        time = Math.min(time + Time.delta, lifetime);

        if(time >= lifetime){
            kill();
        }

        if(!dead && health > 0f && fin() < 1f){
            SignalFlareUnitType fType = (SignalFlareUnitType)type;
            if(fType.flareEffect != Fx.none && Mathf.chanceDelta(fType.flareEffectChance)){
                fType.flareEffect.at(
                    x + fType.flareX,
                    y + fType.flareY * height,
                    fType.flareEffectSize,
                    team.color
                );
            }
        }

        rotation = 90f;
        height = Mathf.lerpDelta(height, 1f, ((SignalFlareUnitType)type).growSpeed);
    }

    @Override
    public float fin(){
        return time / lifetime;
    }

    @Override
    public float healthf(){
        return health / type.health;
    }

    @Override
    public void clampHealth(){
        // do nothing
    }

    @Override
    public void destroy(){
        SignalFlareTurretBuild s = (SignalFlareTurretBuild)building;
        if(s != null){
            s.flares.remove(this);
        }
        type.deathExplosionEffect.at(x, y, height, type.cellColor(this).cpy());
        remove();
    }

    @Override
    public void killed(){
        wasPlayer = isLocal();
        health = 0f;
        dead = true;
    }

    @Override
    public boolean targetable(Team targeter){
        return fin() < 1 && team != targeter;
    }

    @Override
    public boolean hittable(){
        return fin() < 1;
    }

    @Override
    public boolean isFlying(){
        return true; //Always target
    }

    @Override
    public boolean isGrounded(){
        return true; //Always target
    }

    @Override
    public void impulse(float x, float y){
        // cannot move
    }

    @Override
    public void impulseNet(Vec2 v){
        // cannot move
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.f(time);
        write.f(lifetime);
        write.f(height);
        TypeIO.writeBuilding(write, building);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        time = read.f();
        lifetime = read.f();
        height = read.f();
        building = TypeIO.readBuilding(read);
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(SignalFlareUnit.class);
    }
}
