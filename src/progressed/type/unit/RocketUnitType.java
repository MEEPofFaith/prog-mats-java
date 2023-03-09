package progressed.type.unit;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.effect.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.unit.*;
import progressed.ai.*;
import progressed.content.effects.*;

public class RocketUnitType extends MissileUnitType{
    public static final Effect rocketShoot = new MultiEffect(Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
        lifetime = 10f;
        strokeFrom = 4f;
        sizeTo = 130f;
    }});
    
    public float targetDelay = 0f;

    public RocketUnitType(String name, boolean addSmokeTrail){
        super(name);

        speed = 8f;
        maxRange = 6f;
        lifetime = 3.1f * 60f;
        controller = u -> new DelayMissileAI();
        engineLayer = Layer.effect;
        engineSize = 3.1f;
        engineOffset = 10f;
        rotateSpeed = 0.5f;
        trailLength = 18;
        missileAccelTime = 2f * 60f;
        lowAltitude = true;
        outlineColor = Pal.darkerMetal;
        loopSound = Sounds.missileTrail;
        loopSoundVolume = 0.6f;
        deathSound = Sounds.largeExplosion;

        fogRadius = 6f;

        health = 210;

        if(addSmokeTrail){
            abilities.add(new MoveEffectAbility(){{
                effect = MissileFx.rocketTrailSmoke;
                rotateEffect = true;
                y = -9f;
                color = Color.grays(0.6f).lerp(Pal.redLight, 0.5f).a(0.4f);
                interval = 4f;
            }});
        }
    }

    @Override
    public boolean targetable(Unit unit, Team targeter){
        return super.targetable(unit, targeter) && ((TimedKillUnit)unit).time >= targetDelay;
    }

    @Override
    public boolean hittable(Unit unit){
        return super.hittable(unit) && ((TimedKillUnit)unit).time >= targetDelay;
    }
}
