package progressed.content.effects;

import arc.graphics.*;
import mindustry.graphics.*;
import progressed.entities.*;

public class LightningFx{
    public static LightningEffect

    groundCrack = new LightningEffect(20f, 500f, 1.5f).layer(Layer.debris - 0.01f).extend(true).width(10f),

    staticLightning = new LightningEffect(10f, 500f, 2f){{
        colorFrom = Color.white;
    }}.layer(Layer.bullet + 0.01f).width(5f),

    teslaLightning = new LightningEffect(10f, 500f, 3.5f){{
        colorFrom = Color.white;
    }}.layer(Layer.bullet + 0.01f).shrink(true),

    flameBeam = new LightningEffect(10f, 500f, 3f){{
        colorFrom = Color.white;
    }}.layer(Layer.bullet + 0.01f).width(16f).shrink(true),

    blazeBeam = new LightningEffect(10f, 500f, 4f){{
        colorFrom = Color.white;
    }}.layer(Layer.bullet + 0.01f).width(20f).shrink(true);
}
