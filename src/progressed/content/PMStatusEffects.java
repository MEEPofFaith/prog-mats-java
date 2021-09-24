package progressed.content;

import arc.graphics.*;
import arc.struct.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.type.*;

public class PMStatusEffects implements ContentList{
    public static StatusEffect
    emp,

    //Anti-vaxxers are quivering in fear
    vcFrenzy, vcDisassembly, vcWeaken, vcCorvus;

    @Override
    public void load(){
        emp = new ParalyzeStatusEffect("emp"){{
            color = Color.valueOf("7fabff");
            speedMultiplier = 0.4f;
            reloadMultiplier = 0.7f;
            damage = 0.04f;
            rotationRand = 8f;
            cooldown = 60f * 5f;
        }};

        //Anti-vaxxers are quivering in fear
        vcFrenzy = new ExclusiveStatusEffect("frenzy"){{
            color = Pal.lightOrange;
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

        afterLoad();
    }

    public void afterLoad(){
        ((ExclusiveStatusEffect)vcFrenzy).exclusives = Seq.with(vcDisassembly, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcDisassembly).exclusives = Seq.with(vcFrenzy, vcWeaken, vcCorvus);
        ((ExclusiveStatusEffect)vcWeaken).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcCorvus);
        ((ExclusiveStatusEffect)vcCorvus).exclusives = Seq.with(vcFrenzy, vcDisassembly, vcWeaken);
    }
}