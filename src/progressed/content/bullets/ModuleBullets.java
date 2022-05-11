package progressed.content.bullets;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.bullet.physical.*;
import progressed.graphics.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ModuleBullets{
    public static BulletType

    shotgunCopper, shotgunDense, shotgunTitanium, shotgunThorium,

    shotgunCopperCrit, shotgunDenseCrit, shotgunTitaniumCrit, shotgunThoriumCrit,

    pinpointPin,

    waterShotMini, cryoShotMini, slagShotMini, oilShotMini,

    irisOrb,

    swarmIncendiary, swarmBlast,

    lotusLance,

    ambrosiaPotion,

    reboundTitanium, reboundSurge,

    trifectaMissile,

    aresOrb,

    jupiterOrb;

    public static void load(){
        shotgunCopper = new BasicBulletType(5f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 240f;
        }};

        shotgunDense = new BasicBulletType(6.5f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 3;
            lifetime = 240f;
        }};

        shotgunTitanium = new BasicBulletType(6f, 16f){{
            width = 8;
            height = 10;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            lifetime = 240;
        }};

        shotgunThorium = new BasicBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            ammoMultiplier = 3;
            lifetime = 240f;
        }};

        shotgunCopperCrit = new CritBulletType(5f, 9){{
            width = 7f;
            height = 9f;
            lifetime = 240f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierce = pierceBuilding = false;

            critChance = 0.08f;
            critMultiplier = 2f;
            critEffect = OtherFx.miniCrit;
        }};

        shotgunDenseCrit = new CritBulletType(6.5f, 18){{
            width = 9f;
            height = 12f;
            reloadMultiplier = 0.6f;
            ammoMultiplier = 3;
            lifetime = 240f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierceBuilding = false;
            pierceCap = 3;

            critChance = 0.03f;
            critMultiplier = 7f;
            critEffect = OtherFx.miniCrit;
        }};

        shotgunTitaniumCrit = new CritBulletType(6f, 16f){{
            width = 8;
            height = 10;
            lifetime = 240;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierceBuilding = false;
            pierceCap = 2;

            critChance = 0.06f;
            critMultiplier = 3f;
            critEffect = OtherFx.miniCrit;
        }};

        shotgunThoriumCrit = new CritBulletType(7f, 29){{
            width = 10f;
            height = 13f;
            ammoMultiplier = 3;
            lifetime = 240f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            hitEffect = Fx.hitBulletSmall;
            trailLength = 3;
            pierceBuilding = false;
            pierceCap = 3;

            critChance = 0.05f;
            critMultiplier = 4f;
            critEffect = OtherFx.miniCrit;
        }};

        pinpointPin = new TargetBulletType(2.8f, 2f, 13f){
            final int streaks = 2, streakLen = 4;
            final float radius = 4f;

            {
                lifetime = 240f;
                hittable = absorbable = false;
                tStatus = PMStatusEffects.pinpointTarget;
                tStatusDuration = 4f * 60f;
                tHitEffect = despawnEffect = ModuleFx.pinpointHit;
                hitEffect = Fx.none;
            }

            @Override
            public void draw(Bullet b){
                Lines.stroke(0.5f, Pal.surge);
                Fill.circle(b.x, b.y, radius / 2.5f);
                for(int i = 0; i < streaks; i++){
                    Lines.beginLine();
                    for(int j = 0; j < streakLen; j++){
                        PMMathf.randomCirclePoint(Tmp.v1, radius).add(b);
                        Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    }
                    Lines.endLine();
                }
            }
        };

        waterShotMini = new LiquidBulletType(Liquids.water){{
            lifetime = 40f;
            speed = 3f;
            knockback = 0.3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
        }};

        cryoShotMini = new LiquidBulletType(Liquids.cryofluid){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
        }};

        slagShotMini = new LiquidBulletType(Liquids.slag){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
            damage = 2.5f;
        }};

        oilShotMini = new LiquidBulletType(Liquids.oil){{
            lifetime = 40f;
            speed = 3f;
            drag = 0.01f;
            puddleSize = 4f;
            orbSize = 2f;
            ammoMultiplier = 1.2f;
            statusDuration = 60f;
        }};

        irisOrb = new DelayBulletType(5f, 25f){
            {
                lifetime = 78f;
                hitSize = 3f;
                pierce = true;
                pierceCap = 2;
                hittable = false;
                homingPower = 0.15f;
                displayAmmoMultiplier = false;
                lightOpacity = 0.6f;
                lightColor = Pal.surge;
                shootEffect = smokeEffect = Fx.none;
                hitEffect = ModuleFx.irisHit;
                despawnEffect = ModuleFx.irisDespawn;
                trailEffect = ModuleFx.irisTrail;
                trailInterval = 1f;
                trailRotation = true;

                drag = 0.15f;
                launchedSpeed = 3f;
                launchedDrag = 0f;
            }

            @Override
            public void draw(Bullet b){
                drawTrail(b);

                Draw.color(Pal.surge);
                Fill.circle(b.x, b.y, 3f);
            }
        };

        swarmIncendiary = new MissileBulletType(4.2f, 19){{
            frontColor = Pal.lightishOrange;
            backColor = trailColor = Pal.lightOrange;
            homingPower = 0.12f;
            width = 4f;
            height = 4.5f;
            shrinkX = shrinkY = 0f;
            drag = -0.003f;
            homingRange = 80f;
            splashDamageRadius = 20f;
            splashDamage = 24f;
            ammoMultiplier = 5f;
            lifetime = 46f;
            hitEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
            makeFire = true;
            status = StatusEffects.burning;
        }};

        swarmBlast = new MissileBulletType(4.6f, 23){{
            frontColor = Pal.bulletYellow;
            backColor = trailColor = Pal.bulletYellowBack;
            homingPower = 0.15f;
            width = 4.5f;
            height = 5f;
            shrinkX = shrinkY = 0f;
            drag = -0.005f;
            homingRange = 80f;
            splashDamageRadius = 28f;
            splashDamage = 29f;
            ammoMultiplier = 5f;
            lifetime = 40f;
            hitEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
        }};

        lotusLance = new DelayBulletType(5f, 36f, "prog-mats-lance"){{
            frontColor = Color.white;
            backColor = trailColor = Pal.surge;
            width = height = 8f;
            shrinkX = shrinkY = 0;
            lifetime = 60f;
            drag = 0.15f;
            homingPower = 0.15f;
            trailLength = 5;
            trailWidth = 1f;
            hitEffect = despawnEffect = ModuleFx.hitLotus;
        }};

        ambrosiaPotion = new BasicBulletType(2.5f, 0f, "large-bomb"){{
            collides = collidesGround = collidesAir = false;
            frontColor = Color.white;
            backColor = trailColor = Pal.heal;
            scaleLife = true;
            width = height = 24f;
            shrinkX = shrinkY = 0.5f;
            spin = -2f;
            lightColor = Pal.heal;
            trailLength = 12;
            trailWidth = 2.5f;
            trailInterp = a -> 1f - a / 2f;

            fragBullets = 1;
            fragBullet = new HealFieldBulletType(){{
                lifetime = 8f * 60f;
                areaEffect = ModuleFx.healCross;
                lightColor = Pal.heal;
            }};
        }};

        reboundTitanium = new BoomerangBulletType(5f, 15f){{
            lifetime = 120f;
            width = height = 10.5f;
            lightColor = backColor = Items.titanium.color;
            riseStart = 3f;
            riseEnd = 4f;
            layer = Layer.turret + 0.015f;
            targetLayer = Layer.bullet - 1f;
            shootEffect = smokeEffect = Fx.none;
            reloadMultiplier = 4f;
            ammoMultiplier = 6;
            pierceCap = 4;
        }};

        reboundSurge = new BoomerangBulletType(5f, 84f){
            {
                lifetime = 134f;
                width = height = 12f;
                lightColor = backColor = Pal.surge;
                riseStart = 3f;
                riseEnd = 4f;
                layer = Layer.turret + 0.015f;
                targetLayer = Layer.bullet - 1f;
                shootEffect = smokeEffect = Fx.none;
                ammoMultiplier = 2;
                pierceCap = 6;
                lightning = 1;
                lightningLength = 7;
                lightningDamage = 26f;
            }

            @Override
            public void init(){
                super.init();

                despawnHit = false; //bruh
            }
        };

        trifectaMissile = new RocketBulletType(2.5f, 30f, "prog-mats-trifecta-missile"){{
            lifetime = 40f;
            acceleration = 0.05f;
            backSpeed = thrustDelay = 0f;

            splashDamage = 104f;
            splashDamageRadius = 4.5f * tilesize;

            homingDelay = 3f;
            homingPower = 0.15f;
            homingRange = 26f * tilesize;

            trailLength = 3;
            trailWidth = thrusterSize = 0.75f;
            trailParam = thrusterSize * 2f * 1.5f;
            trailOffset = thrusterOffset = 6f;

            layer = Layer.turret + 0.015f;
            riseStart = thrusterGrowth;
            riseEnd = thrusterGrowth + 10f;
            targetLayer = Layer.bullet - 1;
        }};

        aresOrb = new BasicBulletType(5f, 28f, "circle-bullet"){
            {
                scaleLife = true;
                lightOpacity = 0.7f;
                lightRadius = 70f;
                drawSize = 250f;
                shootEffect = ModuleFx.aresShoot;
                smokeEffect = Fx.shootBigSmoke2;
                lifetime = 60f;
                splashDamage = 174f;
                splashDamageRadius = 5f * 8f;
                makeFire = true;
                width = height = 12f;
                shrinkY = 0f;
                frontColor = Color.white;
                backColor = trailColor = hitColor = lightColor = Pal.remove;
                trailLength = 20;
                trailWidth = 6f;
                trailInterval = 3f;
                trailRotation = true;
                trailEffect = ModuleFx.aresTrail;
                hitShake = 4f;
                hitSound = Sounds.plasmaboom;
                hitEffect = ModuleFx.aresHit;
                status = StatusEffects.melting;

                fragBullets = 6;
                fragBullet = new ShrapnelBulletType(){{
                    damage = 53f;
                    length = 132f;
                    width = 12f;
                    toColor = Pal.remove;
                    displayAmmoMultiplier = false;
                    shootEffect = smokeEffect = ModuleFx.flameShoot;
                    makeFire = true;
                    status = StatusEffects.melting;
                }};
            }

            @Override
            public void hit(Bullet b, float x, float y){
                hitEffect.at(x, y, b.rotation(), hitColor);
                hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

                Effect.shake(hitShake, hitShake, b);

                if(fragBullet != null){
                    //You know, it is quite annoying to copy over the entirety of hit just to change one small thing.
                    float ra = Mathf.random(360f);
                    for(int i = 0; i < fragBullets; i++){
                        float a = ra + i * 360f / fragBullets;
                        fragBullet.create(b, x, y, a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                    }
                }

                if(puddleLiquid != null && puddles > 0){
                    for(int i = 0; i < puddles; i++){
                        Tile tile = world.tileWorld(x + Mathf.range(puddleRange), y + Mathf.range(puddleRange));
                        Puddles.deposit(tile, puddleLiquid, puddleAmount);
                    }
                }

                if(incendChance > 0 && Mathf.chance(incendChance)){
                    Damage.createIncend(x, y, incendSpread, incendAmount);
                }

                if(splashDamageRadius > 0 && !b.absorbed){
                    Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);

                    if(status != StatusEffects.none){
                        Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
                    }

                    if(healPercent > 0f){
                        indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                            Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
                            other.heal(healPercent / 100f * other.maxHealth());
                        });
                    }

                    if(makeFire){
                        indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
                    }
                }

                for(int i = 0; i < lightning; i++){
                    Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
                }
            }
        };

        jupiterOrb = new OrbBulletType(1f, 750f){

            {
                lifetime = 6f * 60f;
                radius = hitSize = 7f;
                pierce = pierceBuilding = true;
                hittable = false;
                homingPower = 0.05f;
                homingRange = 16f * 8f;
                displayAmmoMultiplier = false;
                lightOpacity = 0.6f;
                lightColor = Pal.lancerLaser;
                shootEffect = smokeEffect = Fx.none;
                hitEffect = ModuleFx.jupiterHit;
                despawnEffect = ModuleFx.jupiterDespawn;
                trailEffect = ModuleFx.jupiterTrail;
                trailInterval = 1f;
                trailRotation = true;
                driftTrailWidth = 4f;
            }
        };
    }
}
