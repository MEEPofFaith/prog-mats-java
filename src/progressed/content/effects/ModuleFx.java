package progressed.content.effects;

import arc.graphics.g2d.*;
import mindustry.entities.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;

public class ModuleFx{
    public static Effect

    overdriveParticle = new Effect(100f, e -> {
        color(PMPal.overdrive);

        Fill.square(e.x, e.y, e.fslope() * 1.5f + 0.14f, 45f);
    });
}
