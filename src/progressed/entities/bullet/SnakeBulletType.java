package progressed.entities.bullet;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import progressed.entities.bullet.SnakeBulletType.SnakeBulletData.*;

public class SnakeBulletType extends BasicBulletType{
    public float spawnDelay;
    public int length = 3;

    public SnakeBulletType head;
    public SnakeBulletType body;
    public SnakeBulletType tail;

    public SnakeBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
    }

    @Override
    public void init(){
        super.init();

        if(body == null) body = this;
        if(head == null){
            try{
                head = (SnakeBulletType)body.clone();
                head.sprite += "-head";
            }catch(CloneNotSupportedException suck){
                throw new RuntimeException("very good language design", suck);
            }
        }
        if(tail == null){
            try{
                tail = (SnakeBulletType)body.clone();
                tail.sprite += "-tail";
            }catch(CloneNotSupportedException suck){
                throw new RuntimeException("very good language design", suck);
            }
        }

        length = Math.max(length, 3);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data == null){
            Bullet[] next = {null};
            for(int i = 0; i < length; i++){
                SnakeBulletType sType;
                SegmentType type;
                if(i == 0){
                    sType = head;
                    type = SegmentType.head;
                }else if(i == length - 1){
                    sType = tail;
                    type = SegmentType.tail;
                }else{
                    sType = body;
                    type = SegmentType.body;
                }

                Time.run(i * spawnDelay, () -> {
                    Bullet seg = sType.create(b, b.x, b.y, next[0] != null ? b.angleTo(next[0]) : b.rotation());
                    seg.data = new SnakeBulletData(type, next[0]);
                    next[0] = seg;
                });
            }
            b.remove();
        }
    }

    @Override
    public void despawned(Bullet b){
        if(b.data == null) return;
        super.despawned(b);
    }

    public void update(Bullet b){
        updateTrail(b);

        SnakeBulletData data = (SnakeBulletData)b.data;
        if(data.segmentType.equals(SegmentType.head)){
            if(homingPower > 0.0001f && b.time >= homingDelay){
                Teamc target;
                //home in on allies if possible
                if(healPercent > 0){
                    target = Units.closestTarget(null, b.x, b.y, homingRange,
                        e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                        t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                    );
                }else{
                    target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
                }

                if(target != null){
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                }
            }

            if(weaveMag > 0){
                b.vel.rotate(Mathf.sin(b.time + Mathf.PI * weaveScale / 2f, weaveScale, weaveMag * (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
            }
        }else if(data.followBullet != null && data.followBullet.isAdded()){
            b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(data.followBullet), homingPower * Time.delta * 50f));
        }

        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }
    }

    public Bullet create(@Nullable Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data){
        Bullet bullet = Bullet.create();
        bullet.type = this;
        bullet.owner = owner;
        bullet.team = team;
        bullet.time = 0f;
        bullet.initVel(angle, speed * velocityScl);
        if(backMove && data != null){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
        }else{
            bullet.set(x, y);
        }
        bullet.lifetime = lifetime * lifetimeScl;
        bullet.data = data;
        bullet.drag = drag;
        bullet.hitSize = hitSize;
        bullet.damage = (damage < 0 ? this.damage : damage) * bullet.damageMultiplier();
        //reset trail
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        bullet.add();

        if(keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        return bullet;
    }

    public static class SnakeBulletData{
        public SegmentType segmentType;
        public Bullet followBullet;

        public SnakeBulletData(SegmentType type, Bullet b){
            segmentType = type;
            followBullet = b;
        }

        public enum SegmentType{
            head, body, tail
        }
    }
}
