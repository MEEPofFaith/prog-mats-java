package progressed.content.bullets;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.effects.*;
import progressed.entities.bullet.physical.*;
import progressed.graphics.*;

public class SniperBullets{
    public static BulletType

    sniperBoltSilicon, sniperBoltTitanium, sniperBoltThorium, sniperBoltSurge, sniperBoltTenliumFrag, sniperBoltTenelium;

    public static void load(){
        sniperBoltSilicon = new CritBulletType(12f, 300f){{
            lifetime = 48f;
            knockback = 4f;
            width = 5f;
            height = 8f;
            pierceCap = 20;
            reloadMultiplier = 1.2f;
            critChance = 0.2f;
            critMultiplier = 2f;
            bouncing = true;
        }};

        sniperBoltTitanium = new CritBulletType(13f, 500f){{
            lifetime = 45f;
            knockback = 5f;
            width = 7f;
            height = 12f;
            pierceCap = 26;
            reloadMultiplier = 1.7f;
            critChance = 0.15f;
            critMultiplier = 2.5f;
        }};

        sniperBoltThorium = new CritBulletType(12f, 800f){{
            lifetime = 51f;
            knockback = 5f;
            width = 8f;
            height = 14f;
            pierceCap = 30;
            critChance = 0.1f;
            critMultiplier = 4.5f;
        }};

        sniperBoltSurge = new CritBulletType(14f, 1000f){{
            frontColor = Color.valueOf("F3E979");
            backColor = hitColor = Color.valueOf("D99F6B");
            lifetime = 42f;
            width = 10f;
            height = 19f;
            trailLength = 11;
            pierceCap = 40;
            knockback = 7f;
            reloadMultiplier = 0.7f;
            lightning = 5;
            lightningLength = 3;
            lightningLengthRand = 2;
            lightningDamage = 40f;
            critChance = 0.20f;
            critMultiplier = 5f;
        }};

        sniperBoltTenliumFrag = new CritBulletType(13f, 160f){{
            lifetime = 23f;
            knockback = 3f;
            width = 6f;
            height = 14f;
            pierceCap = 12;
            critMultiplier = 3f;
            critEffect = OtherFx.miniCrit;
        }};

        sniperBoltTenelium = new CritBulletType(13f, 800f){
            final float fragInacc = 5f;

            {
                lifetime = 23f;
                knockback = 4f;
                width = 9f;
                height = 16f;
                pierceCap = 13;
                fragBullets = 5;
                fragBullet = sniperBoltTenliumFrag;
                fragVelocityMin = 0.8f;
                fragVelocityMax = 1.2f;
                fragCone = 30f;
                critChance = 0.1f;
                critMultiplier = 3f;
                despawnHitEffects = false;
            }

            @Override
            public void hit(Bullet b, float x, float y){
                //Don't frag on hit

                b.hit = true;
                if(!((CritBulletData)b.data).despawned){
                    hitEffect.at(x, y, b.rotation(), hitColor);
                    hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
                }

                Effect.shake(hitShake, hitShake, b);
            }

            @Override
            public void removed(Bullet b){
                CritBulletData data = (CritBulletData)b.data;
                for(int i = 0; i < fragBullets; i++){
                    float a = b.rotation() + ((fragCone / fragBullets) * (i - (fragBullets - 1f) / 2f)) + Mathf.range(fragInacc);
                    if(fragBullet instanceof CritBulletType critB){
                        Bullet bullet = critB.create(b.owner, b.team, b.x, b.y, a, -1f, Mathf.random(fragVelocityMin, fragVelocityMax), 1f, new CritBulletData(data.crit));
                        bullet.trail = ((PMTrail)(b.trail)).copyPM();
                        if(b.collided.size > 0) bullet.collided.add(b.collided.peek());
                    }
                }
                Sounds.missile.at(b, Mathf.random(0.9f, 1.1f));
            }
        };
    }
}