package progressed.entities.bullet;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.entities.bullet.SnakeBulletType.SnakeBulletData.*;

public class SnakeBulletType extends BasicBulletType{
    public int length = 3;
    public float spawnDelay= 5f;
    public float followPower = 0.85f;
    public boolean setSegments = true;

    public SnakeBulletType head;
    public SnakeBulletType body;
    public SnakeBulletType tail;

    public SnakeBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
        backMove = false;
    }

    @Override
    public void init(){
        super.init();

        if(setSegments){
            if(body == null){
                body = (SnakeBulletType)copy();
                body.setSegments = false;
                body.backMove = true;
            }
            if(head == null){
                head = (SnakeBulletType)body.copy();
                head.sprite += "-head";
                head.setSegments = false;
                head.backMove = true;
            }
            if(tail == null){
                tail = (SnakeBulletType)body.copy();
                tail.sprite += "-tail";
                head.setSegments = false;
                head.backMove = true;
            }
        }

        length = Math.max(length, 3);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data == null){
            Bullet[] next = {null};
            float x = b.x;
            float y = b.y;
            for(int i = 0; i < length; i++){
                int ii = i;

                Time.run(i * spawnDelay, () -> {
                    SnakeBulletType bType = body;
                    SegmentType sType = SegmentType.body;
                    if(ii == 0){
                        bType = head;
                        sType = SegmentType.head;
                    }else if(ii == length - 1){
                        bType = tail;
                        sType = SegmentType.tail;
                    }

                    Bullet seg = bType.create(
                        b.owner, b.team,
                        x, y, next[0] != null ? next[0].angleTo(x, y) + 180f : b.rotation(),
                        -1f, 1f, 1f,
                        new SnakeBulletData(sType, next[0])
                    );
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
        }else if(data.followBullet != null){
            if(data.followBullet.isAdded()){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(data.followBullet), followPower * Time.delta * 50f));
            }else{
                data.followBullet = null;
            }
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
