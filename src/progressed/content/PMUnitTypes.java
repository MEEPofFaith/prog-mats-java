package progressed.content;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.ai.*;
import progressed.content.effects.*;
import progressed.entities.units.*;
import progressed.gen.entities.*;
import progressed.graphics.*;
import progressed.type.unit.*;
import progressed.type.weapons.*;
import progressed.ui.*;

@SuppressWarnings("unchecked")
public class PMUnitTypes{
    //Steal from Endless Rusting which stole from Progressed Materials in the past which stole from BetaMindy
    private static final Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] types = new Entry[]{
        prov(SignalFlareUnit.class, SignalFlareUnit::new),
        prov(SwordUnit.class, SwordUnit::new),
        prov(BuildingTetherLegsUnit.class, BuildingTetherLegsUnit::new),
        prov(SentryUnit.class, SentryUnit::new)
    };

    private static final ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();
    public static UnitType

    //TODO A chain of air units with DriftTrail shenanigans. Serpulo. Steal crab unit cyan pallet.
    echo, presence, ghoul, phantom, apparition,

    //TODO A chain of sword-based units. Ground. Serpulo or Erekir? Red pallet.
    puncture, penetration, incision, laceration, amputation,

    //sentry
    barrage, strikedown,

    //legged ground miner.
    draug,

    //signal flare
    flareSmall, flareMedium, flareLarge,

    //swords
    danceSword, masqueradeSword,

    //oof
    targetDummy,

    //sandy
    everythingUnit;

    /**
     * Internal function to flatmap {@code Class -> Prov} into an {@link Entry}.
     *
     * @author GlennFolker
     */
    private static <T extends Entityc> Entry<Class<T>, Prov<T>> prov(Class<T> type, Prov<T> prov){
        Entry<Class<T>, Prov<T>> entry = new Entry<>();
        entry.key = type;
        entry.value = prov;
        return entry;
    }

    /**
     * Setups all entity IDs and maps them into {@link EntityMapping}.
     *
     * @author GlennFolker
     */

    private static void setupID(){
        for(
            int i = 0,
            j = 0,
            len = EntityMapping.idMap.length;

            i < len;

            i++
        ){
            if(EntityMapping.idMap[i] == null){
                idMap.put(types[j].key, i);
                EntityMapping.idMap[i] = types[j].value;

                if(++j >= types.length) break;
            }
        }
    }

    /**
     * Retrieves the class ID for a certain entity type.
     *
     * @author GlennFolker
     */
    public static <T extends Entityc> int classID(Class<T> type){
        return idMap.get(type, -1);
    }

    public static void load(){
        setupID();

        //Region Sentry Units
        barrage = new SentryUnitType("barrage"){{
            lifetime = 39f * 60f;

            Weapon gunL = new Weapon(name + "-gun-r"){{
                top = false;
                rotate = false;
                mirror = false;
                alternate = true;
                otherSide = 1;

                x = 9.5f / 4f;
                y = 22f / 4f;
                shootX = -1f / 4f;
                shootY = 6f / 4f;

                reload = 6f;
                recoil = 3f / 4f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3f, 23f){{
                    lifetime = 80f;
                    buildingDamageMultiplier = 0.3f;
                    width = 7f;
                    height = 9f;
                    recoil = 0.1f;
                    homingPower = 0.03f;
                    homingRange = 120f;
                }};
            }};
            Weapon gunR = gunL.copy();
            gunR.name = name + "-gun-l";
            gunR.x *= -1;
            gunR.shootX *= -1;
            gunR.flipSprite = true;
            gunR.otherSide = 0;

            weapons.add(gunL, gunR);

            for(int i = 0; i < 4; i++){
                engines.add(new AnchorEngine(3f * Geometry.d8edge(i).x, 3f * Geometry.d8edge(i).y, 1.25f, i * 90f + 45f));
            }
        }};

        strikedown = new SentryUnitType("downpour"){{
            lifetime = 32f * 60f;
            range = maxRange = 40f * 8f;

            weapons.add(new RocketWeapon(name + "-rocket"){{
                x = 0f;
                shootY = 0f;
                mirror = false;
                rotate = false;
                reload = 120f;
                shootCone = 5f;
                shootSound = Sounds.missileSmall;
                layerOffset = -0.005f;

                bullet = new BulletType(0, 0){{
                    shootEffect = Fx.none;
                    smokeEffect = MissileFx.shootSmokeDownpour;
                    hitColor = Pal.sap;
                    recoil = 1f;

                    spawnUnit = new MissileUnitType("downpour-rocket"){{
                        speed = 6.4f;
                        maxRange = 9f;
                        lifetime = 60f * 1.2f;
                        missileAccelTime = 0.5f * 60f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.sapBulletBack;
                        engineLayer = Layer.effect;
                        engineOffset = 10;
                        engineSize = 1.5f;
                        health = 45;
                        loopSoundVolume = 0.1f;

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            shootOnDeath = true;
                            bullet = new ExplosionBulletType(260f, 21f){{
                                shootEffect = Fx.massiveExplosion;
                                buildingDamageMultiplier = 0.3f;
                            }};
                        }});
                    }};
                }};
            }});

            for(int i = 0; i < 4; i++){
                engines.add(new AnchorEngine(3f * Geometry.d8edge(i).x, 3f * Geometry.d8edge(i).y, 1.25f, i * 90f + 45f));
            }
        }};

        draug = new ErekirUnitType("draug"){{
            constructor = BuildingTetherLegsUnit::new;
            controller = u -> new DepotMinerAI();
            isEnemy = false;
            allowedInPayloads = false;
            logicControllable = false;
            playerControllable = false;
            hidden = true;
            hideDetails = false;
            hitSize = 14f;
            speed = 1f;
            rotateSpeed = 2.5f;
            health = 1300;
            armor = 5f;
            omniMovement = false;
            rotateMoveFirst = true;
            itemOffsetY = 5f;

            itemCapacity = 50;
            mineTier = 5;
            mineSpeed = 6f;
            mineWalls = true;
            mineItems = Seq.with(Items.beryllium, Items.graphite, Items.tungsten);

            allowLegStep = true;
            legCount = 6;
            legGroupSize = 3;
            legLength = 12f;
            lockLegBase = true;
            legContinuousMove = true;
            legExtension = -3f;
            legBaseOffset = 5f;
            legMaxLength = 1.1f;
            legMinLength = 0.2f;
            legForwardScl = 1f;
            legMoveSpace = 2.5f;
            hovering = true;

            weapons.add(new Weapon("prog-mats-draug-you-have-incurred-my-wrath-prepare-to-die"){{
                x = 22f / 4f;
                y = -3f;
                shootX = -3f / 4f;
                shootY = 4.5f / 4f;
                rotate = true;
                rotateSpeed = 35f;
                reload = 35f;
                shootSound = Sounds.laser;

                bullet = new LaserBulletType(){{
                    damage = 45f;
                    sideAngle = 30f;
                    sideWidth = 1f;
                    sideLength = 5.25f * 8;
                    length = 13.75f * 8f;
                    colors = new Color[]{Pal.heal.cpy().a(0.4f), Pal.heal, Color.white};
                }};
            }});

            abilities.add(new RegenAbility(){{
                //fully regen in 90 seconds
                percentAmount = 1f / (90f * 60f) * 100f;
            }});
        }};

        flareSmall = new SignalFlareUnitType("small-flare"){{
            health = 300f;
            hideDetails = false;
            attraction = 800f;
            flareY = 29f / 4f;
        }};

        flareMedium = new SignalFlareUnitType("medium-flare", 360f){{
            health = 900f;
            hideDetails = false;
            attraction = 11000f;
            flareY = 45f / 4f;
            flareEffectSize = 1.5f;
        }};

        flareLarge = new SignalFlareUnitType("large-flare", 420f){{
            health = 2700f;
            hideDetails = false;
            attraction = 26000f;
            flareY = 61f / 4f;
            flareEffectSize = 2f;
        }};

        danceSword = new SwordUnitType("dance-sword"){{
            baseY = -4f;
            tipY = 11f;
            damage = 65f;
            hitEffect = OtherFx.swordStab;
            trailLength = 5;
        }};

        masqueradeSword = new SwordUnitType("ball-sword"){{
            baseY = -33f / 4f;
            tipY = 89f / 4f;
            damage = 90f;
            hitEffect = OtherFx.swordStab;
            trailLength = 8;
            trailScl = 4;
            trailVel = 8;
        }};

        targetDummy = EntityRegistry.content("dummy", TargetDummy.class, name -> new DummyUnitType(name){{
            drag = 0.33f;
            hideDetails = false;
            hitSize = 52f / 4f;
            engineOffset = 7f;
            engineSize = 2f;
            for(int i = 0; i < 3; i++){
                engines.add(new UnitEngine(Geometry.d4x(i) * engineOffset, Geometry.d4y(i) * engineOffset, engineSize, i * 90));
            }
        }});

        everythingUnit = new UnitType("god"){
            {
                constructor = UnitEntity::create;
                alwaysUnlocked = true;
                hidden = !ProgMats.everything();
                flying = true;
                lowAltitude = true;
                mineSpeed = 100f;
                mineTier = 10000;
                buildSpeed = 10000f;
                drag = 0.05f;
                speed = 3.55f;
                rotateSpeed = 19f;
                accel = 0.11f;
                itemCapacity = 20000;
                health = 200000f;
                engineOffset = 5.5f;
                hitSize = 11f;
            }

            @Override
            public void setStats(){
                super.setStats();

                stats.remove(Stat.abilities);
                stats.remove(Stat.weapons);
                stats.add(Stat.abilities, t -> t.add(PMElements.everything()));
                stats.add(Stat.weapons, t -> t.add(PMElements.everything()));
            }

            @Override
            public void draw(Unit unit){
                super.draw(unit);

                if(!ProgMats.everything()){
                    Draw.z(Layer.overlayUI);
                    PMDrawf.text(unit.x, unit.y, false, -1, unit.team.color, Core.bundle.get("pm-sandbox-disabled"));
                }
            }
        };
    }
}
