package progressed.content.effects;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
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
            float ang = angle(x, y) - 180f;
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fslope() * 5f);
        });
    }).layer(Layer.block + 0.5f),

    superSmeltsmoke = new Effect(15, e -> {
        randLenVectors(e.id, 14, 6f + e.fin() * 18f, (x, y) -> {
            color(Color.white, e.color, e.fin());
            Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 3f, 45);
        });
    }),

    teneliumFuse = new Effect(50f, e -> {
        color(Liquids.slag.color);
        alpha(e.fslope() * 0.8f);

        Mathf.rand.setSeed(e.id + 2);
        randLenVectors(e.id, 5, 3f, 9f * e.fin(), (x, y) -> {
            Fill.circle(e.x + x, e.y + y, Mathf.rand.random(0.65f, 1.5f));
        });
    }).layer(Layer.bullet - 1f);
}
