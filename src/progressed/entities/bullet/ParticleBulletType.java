package progressed.entities.bullet;

import arc.audio.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.graphics.*;

public class ParticleBulletType extends BulletType{
    public Sound particleSound = Sounds.none;
    public float particleSoundChance = 0.5f, particleSoundMinPitch = 0.7f, particleSoundMaxPitch = 1f;

    public ParticleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = reflectable = collidesTiles = false;
        pierce = true;
        trailEffect = PMFx.particle;
        trailChance = 0.5f;
        lifetime = 32f;
        hitEffect = despawnEffect = Fx.none;
    }

    public ParticleBulletType(){
        this(8f, 0f);
    }

    @Override
    public void update(Bullet b){
        if(Mathf.chanceDelta(particleSoundChance) && particleSound != Sounds.none){
            particleSound.at(b.x, b.y, Mathf.random(particleSoundMinPitch, particleSoundMaxPitch), 1f);
        }
        super.update(b);
    }
}