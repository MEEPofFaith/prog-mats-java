package progressed.entities.comp;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.gen.entities.*;
import progressed.type.unit.*;
import progressed.world.blocks.defence.turret.SignalFlareTurret.*;

@EntityComponent
@EntityDef({SignalFlareUnitc.class, Unitc.class})
abstract class SignalFlareUnitComp implements Unitc, Scaled{
    @Import float x, y, health, rotation;
    @Import boolean dead, wasPlayer;
    @Import UnitType type;
    @Import Team team;

    Building building;
    float time, lifetime, height = 0f;

    @Override
    public void update(){
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
    @Replace
    public float healthf(){
        return health / type.health;
    }

    @Override
    @Replace
    public void clampHealth(){
        //Do nothing
    }

    @Override
    @Replace
    public void destroy(){
        SignalFlareTurretBuild s = (SignalFlareTurretBuild)building;
        if(s != null){
            s.flares.remove((SignalFlareUnitc)self());
        }
        type.deathExplosionEffect.at(x, y, height, type.cellColor(self()).cpy());
        remove();
    }

    @Override
    @Replace
    public void killed(){
        wasPlayer = isLocal();
        health = 0f;
        dead = true;
    }

    @Override
    @Replace
    public boolean targetable(Team targeter){
        return fin() < 1 && team != targeter;
    }

    @Override
    @Replace
    public boolean hittable(){
        return fin() < 1;
    }

    @Override
    @Replace
    public boolean isFlying(){
        return true; //Always target
    }

    @Override
    @Replace
    public boolean isGrounded(){
        return true; //Always target
    }

    @Override
    @Replace
    public void impulse(float x, float y){
        // cannot move
    }

    @Override
    @Replace
    public void impulseNet(Vec2 v){
        // cannot move
    }
}
