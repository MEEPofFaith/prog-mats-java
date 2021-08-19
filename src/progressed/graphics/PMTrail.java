package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.*;

public class PMTrail{ //Taken from Project Unity and modified a bit
    public int length;

    private final Seq<Vec3> points;
    private float lastX = -1, lastY = -1;

    public PMTrail(int length){
        this.length = length;
        points = new Seq<>(length);
    }

    public void clear(){
        points.clear();
    }

    public void draw(Color color, float width){
        Draw.color(color);

        for(int i = 0; i < points.size - 1; i++){
            Vec3 c = points.get(i);
            Vec3 n = points.get(i + 1);
            float size = width / length;
            float sclc = ((float)i / (points.size - 1f)) * length;
            float scln = ((i + 1f) / (points.size - 1f)) * length;

            float cx = Mathf.sin(c.z) * sclc * size,
                cy = Mathf.cos(c.z) * sclc * size,
                nx = Mathf.sin(n.z) * scln * size,
                ny = Mathf.cos(n.z) * scln * size;
            Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
        }

        Draw.reset();
    }

    public void update(float x, float y, float rotation){
        if(points.size > length){
            Pools.free(points.first());
            points.remove(0);
        }

        points.add(Pools.obtain(Vec3.class, Vec3::new).set(x, y, -rotation * Mathf.degRad));
    }

    public void update(float x, float y){
        float angle = -Angles.angle(x, y, lastX, lastY);

        update(x, y, angle);

        lastX = x;
        lastY = y;
    }

    public PMTrail copy(){ // Not this though, this I make myself
        PMTrail trail = new PMTrail(length);
        points.each(p -> trail.update(p.x, p.y, p.z));
        return trail;
    }
}