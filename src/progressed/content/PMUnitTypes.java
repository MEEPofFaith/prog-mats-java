package progressed.content;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.content.effects.*;
import progressed.entities.bullet.explosive.*;
import progressed.entities.units.*;
import progressed.graphics.*;
import progressed.type.unit.*;
import progressed.type.weapons.*;
import progressed.ui.*;

@SuppressWarnings("unchecked")
public class PMUnitTypes{
    //Steal from Endless Rusting which stole from Progressed Materials in the past which stole from BetaMindy
    private static final Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] types = new Entry[]{
        prov(SentryUnit.class, SentryUnit::new),
        prov(FlareUnit.class, FlareUnit::new),
        prov(SwordUnit.class, SwordUnit::new)
    };

    private static final ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();
    public static UnitType

        //TODO A chain of air units with DriftTrail shenanigans. Serpulo or Erekir? Steal crab unit cyan pallet.
        echo, presence, ghoul, phantom, apparition,

    //TODO A chain of sword-based units. Ground or air? Serpulo or Erekir? Red pallet.
    puncture, penetration, incision, laceration, amputation,

    //sentry
    barrage, downpour,

    //signal flare
    flareSmall, flareMedium, flareLarge,

    //swords
    danceSword, masqueradeSword,

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
            health = 500f;
            duration = 39f * 60f;

            weapons.add(new Weapon(name + "-gun"){{
                top = false;
                rotate = false;
                alternate = true;

                x = 11f / 4f;
                y = 18f / 4f;
                shootX = -2.5f / 4f;
                shootY = 3f;
                layerOffset = -0.01f;

                reload = 6f;
                recoil = 3f / 4f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3f, 20f){{
                    lifetime = 80f;
                    buildingDamageMultiplier = 0.3f;
                    width = 7f;
                    height = 9f;
                    homingPower = 0.03f;
                    homingRange = 120f;
                }};
            }});

            engineSize = 3f;
            engineOffset = 5f;
            setEnginesMirror(new UnitEngine(24f / 4f, 21f / 4f, 2f, 45f));
        }};

        downpour = new SentryUnitType("downpour"){{
            health = 300f;
            duration = 32f * 60f;

            weapons.add(new RocketWeapon(name + "-rocket"){{
                x = 19f / 4f;
                y = -5f / 4f;
                mirror = true;
                alternate = false;
                reload = 60f;
                shootCone = 5f;
                shootSound = Sounds.missile;
                layerOffset = -0.01f;

                bullet = new RocketBulletType(4.5f, 48f, name){{
                    lifetime = 60f;
                    backSpeed = 0.25f;
                    splashDamage = 260f;
                    splashDamageRadius = 18f;
                    buildingDamageMultiplier = 0.3f;
                    thrusterOffset = 5f;
                    thrusterSize = 1f;
                    homingPower = 0.25f;
                    homingRange = 48f * 8f;
                    trailLength = 3;
                    layer = Layer.flyingUnit - 1f;
                    hitSound = Sounds.explosion;
                }};
            }});

            engineSize = 3f;
            engineOffset = 5f;
            setEnginesMirror(new UnitEngine(24f / 4f, 21f / 4f, 2f, 45f));
        }};

        flareSmall = new FlareUnitType("small-flare"){{
            health = 300f;
            hideDetails = false;
            attraction = 800f;
            flareY = 29f / 4f;
        }};

        flareMedium = new FlareUnitType("medium-flare", 360f){{
            health = 900f;
            hideDetails = false;
            attraction = 11000f;
            flareY = 45f / 4f;
            flareEffectSize = 1.5f;
        }};

        flareLarge = new FlareUnitType("large-flare", 420f){{
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
                    PMDrawf.text(unit.x, unit.y, false, unit.team.color, Core.bundle.get("pm-sandbox-disabled"));
                }
            }
        };
    }
}
