package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.entities.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class CrafterFx{
    public static Effect

    colliderFusion = new Effect(30f, e -> {
        color(Color.gray);
        e.scaled(15f, s -> {
            stroke(s.fout());
            Lines.circle(e.x, e.y, 3f * s.fout());
        });

        stroke(1f);

        randLenVectors(e.id, 16, 5f * e.fout(), e.rotation, 180f, (x, y) -> {
            float ang = angle(x, y, 0f, 0f);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fslope() * 5f);
        });
    }).layer(Layer.block + 0.5f),

    superSmeltsmoke = new Effect(15, e -> {
        randLenVectors(e.id, 14, 6f + e.fin() * 18f, (x, y) -> {
            color(Color.white, e.color, e.fin());
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 3f, 45);
        });
    });
}