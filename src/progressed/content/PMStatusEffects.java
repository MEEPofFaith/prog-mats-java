package progressed.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.content.effects.*;
import progressed.type.statuseffects.*;

import static mindustry.content.StatusEffects.*;

public class PMStatusEffects{
    public static StatusEffect
    //Misc
    incendiaryBurn, pinpointTarget,

    //Anti-vaxxers are quivering in fear
    vcFrenzy, vcDisassembly, vcWeaken, vcCorvus,

    //Buff Zone effects
    speedBoost, strengthBoost;

    public static void load(){
        incendiaryBurn = new PMStatusEffect("incend-burn"){{
            color = Pal.lightPyraFlame;
            damage = 3.6f;
            effect = MissileFx.incendBurning;
            transitionDamage = 14f;

            init(() -> {
                opposite(wet, freezing);
                affinity(tarred, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);
                    Fx.burning.at(unit.x + Mathf.range(unit.bounds() / 2f), unit.y + Mathf.range(unit.bounds() / 2f));
                    result.set(incendiaryBurn, Math.min(time + result.time, 450f));
                });
            });
        }};

        pinpointTarget = new PMStatusEffect("pinpoint-target"){
            {
                color = Pal.remove;
                healthMultiplier = 0.5f;
            }

            @Override
            public void draw(Unit u){
                float r = u.hitSize * 0.5f;

                Draw.z(Layer.effect);
                Lines.stroke(1.5f, color);
                Lines.circle(u.x, u.y, r);

                int points = 4;
                float offset = Mathf.randomSeed(u.id, 360f) - Time.time;
                for(int i = 0; i < points; i++){
                    float a = i * 360f / points + offset;
                    Lines.lineAngleCenter(u.x + Angles.trnsx(a, r), u.y + Angles.trnsy(a, r), a, 4f);
                }
            }
        };

        //Anti-vaxxers are quivering in fear
        vcFrenzy = new ExclusiveStatusEffect("frenzy"){{
            color = Color.valueOf("E25656");
            damageMultiplier = 3f;
            speedMultiplier = 3f;
            reloadMultiplier = 3f;
            healthMultiplier = 0.25f;
        }};

        vcDisassembly = new ExclusiveStatusEffect("disassembly"){{
            color = Color.darkGray;
            reloadMultiplier = 0.6f;
            speedMultiplier = 0.7f;
            healthMultiplier = 0.6f;
            damage = 1f;
        }};

        vcWeaken = new ExclusiveStatusEffect("weaken"){{
            color = Pal.sapBulletBack;
            healthMultiplier = 0.7f;
            damageMultiplier = 0.5f;
        }};

        vcCorvus = new ExclusiveStatusEffect("corvus-19"){{ //lmao
            color = Pal.heal;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.8f;
            damage = 8f;
            hideDetails = false;
        }};

        //buff zone effects
        speedBoost = new PMStatusEffect("speed-boost"){{
            color = Pal.lancerLaser;
            speedMultiplier = 1.25f;
            reloadMultiplier = 1.25f;
            effect = Fx.overclocked;
        }};

        strengthBoost = new PMStatusEffect("strength-boost"){{
            color = Pal.redderDust;
            damageMultiplier = 1.25f;
            effect = Fx.overclocked;
        }};

        afterLoad();
    }

    public static void afterLoad(){
        ((ExclusiveStatusEffect)vcFrenzy).exclusives = Seq.with(vcDisassembly, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcDisassembly).exclusives = Seq.with(vcFrenzy, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcWeaken).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcCorvus);
        ((ExclusiveStatusEffect)vcCorvus).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcWeaken);
    }
}
