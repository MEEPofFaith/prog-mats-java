package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import progressed.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.util.Tmp.*;

public class ModuleFx{
    public static Effect

    overdriveParticle = new Effect(100f, e -> {
        color(PMPal.overdrive);

        Fill.square(e.x, e.y, e.fslope() * 1.5f + 0.14f, 45f);
    });
}
