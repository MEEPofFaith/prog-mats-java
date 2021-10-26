package progressed.entities.bullet.physical;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

public class InjectorBulletType extends BasicBulletType{
    public Vaccine[] vaccines;
    public boolean nanomachines;

    public InjectorBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
    }

    public InjectorBulletType(float speed, float damage){
        this(speed, damage, "prog-mats-syringe");
    }

    @Override
    public void init(){
        super.init();

        if(vaccines == null){
            throw new RuntimeException("Injector bullet " + this + " does not have any nanomachines!");
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        b.data = vaccines[Mathf.random(vaccines.length - 1)];
    }

    @Override
    public void draw(Bullet b){
        Draw.z(Layer.bullet - 0.03f);
        Draw.color();
        Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() - 90f);
        Draw.color(((Vaccine)(b.data)).status.color);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() - 90f);
        Draw.reset();
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float initialHealth){
        super.hitEntity(b, entity, initialHealth);

        if(entity instanceof Unit unit){
            Vaccine v = (Vaccine)b.data;
            unit.apply(v.status, v.duration);
        }
    }

    public static class Vaccine{
        public StatusEffect status;
        public float duration;

        public Vaccine(StatusEffect status, float duration){
            this.status = status;
            this.duration = duration;
        }

        public Vaccine(StatusEffect status){
            this(status, 60f * 5f);
        }
    }
}