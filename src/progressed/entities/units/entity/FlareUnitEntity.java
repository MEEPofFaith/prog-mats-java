package progressed.entities.units.entity;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.content.*;
import progressed.entities.units.*;

public class FlareUnitEntity extends SentryUnitEntity{
    public float animation = 1f, height = 0f;

    @Override
    public float durationf(){
        return duration / ((FlareUnitType)type).duration;
    }

    @Override
    public void clampDuration(){
        duration = Mathf.clamp(duration, 0f, ((FlareUnitType)type).duration);
    }

    @Override
    public void update(){
        type.update(self());

        // simulate falling over
        if(dead || health < 0f || durationf() <= 0f){
            animation -= type.fallSpeed * Time.delta;

            if(animation <= 0f){
                destroy();
            }
        }

        rotation = 90f - Mathf.clamp(1f - animation) * 90f;

        hitTime -= Time.delta / hitDuration;
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
        // do nothing
        remove();
    }

    @Override
    public void killed(){
        wasPlayer = isLocal();
        health = 0f;
        dead = true;
    }

    @Override
    public boolean checkTarget(boolean air, boolean ground){
        try{
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            StackTraceElement load = traces[2];
            if(TractorBeamTurret.TractorBeamBuild.class.isAssignableFrom(Class.forName(load.getClassName(), false, loader))){
              return false;
            }
        }catch(ClassNotFoundException e){ //Ignored
            return true;
        }
        
        return true;
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
    public void impulse(Vec2 v){
        // cannot move
    }

    @Override
    public void impulseNet(Vec2 v){
        // cannot move
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.f(animation);
        write.f(height);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        animation = read.f();
        height = read.f();
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(FlareUnitEntity.class);
    }
}