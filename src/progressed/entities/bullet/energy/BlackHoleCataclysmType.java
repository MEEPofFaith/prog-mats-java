package progressed.entities.bullet.energy;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.entities.bullet.energy.BlackHoleBulletType.*;
import progressed.graphics.*;

public class BlackHoleCataclysmType extends BulletType{
    public float growTime = 20f, secondaryGrowTime = 180f, fadeTime = 90f;

    public BlackHoleCataclysmType(){
        super(0f, 0f);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        lifetime = 60f * 60f; //Minute of death he he he
        drawSize = 2048f;
    }

    @Override
    public void update(Bullet b){
        //[radius, uForce, uScaledForce, bForce, bScaledForce, range, c1, c2, world load]
        CataclysmData data = (CataclysmData)b.data;

        float shrink = 1 - Mathf.curve(b.time, b.lifetime - fadeTime, b.lifetime);
        float scl = Mathf.curve(b.time, 0f, growTime) * shrink;
        float suctionRadius = (data.r + data.rg) * scl;

        if(b.timer(1, 2f)){
            Units.nearbyEnemies(null, b.x - suctionRadius, b.y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, unit -> {
                if(unit.within(b.x, b.y, suctionRadius + unit.hitSize / 2f)){
                    float angle = b.angleTo(unit);
                    Tmp.v2.set(b.x + Angles.trnsx(angle, data.r), b.y + Angles.trnsy(angle, data.r));
                    float dist = !unit.within(b.x, b.y, data.r) ? unit.dst(Tmp.v2) : 0f;
                    Vec2 impulse = Tmp.v1.trns(unit.angleTo(Tmp.v2), data.f * scl + (1f - dist / data.rg) * data.sF * scl);
                    if(data.f < 0f || data.sF < 0f) impulse.rotate(180f);
                    unit.impulseNet(impulse);

                    if(unit.within(b.x, b.y, data.r * scl)){
                        unit.set(b);
                        unit.vel.set(0f, 0f);
                        unit.destroy();
                    }
                }
            });

            Groups.bullet.intersect(b.x - suctionRadius, b.y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, other -> {
                if(other != null && Mathf.within(b.x, b.y, other.x, other.y, suctionRadius) && b != other && other.type.speed > 0.01f && !BlackHoleBulletType.checkType(other.type)){
                    float angle = b.angleTo(other);
                    Tmp.v2.set(b.x + Angles.trnsx(angle, data.r), b.y + Angles.trnsy(angle, data.r));
                    float dist = !other.within(b.x, b.y, data.r) ? other.dst(Tmp.v2) : 0f;
                    Vec2 impulse = Tmp.v1.trns(other.angleTo(Tmp.v2), data.bF * scl + (1f - dist / data.rg) * data.bSF * scl);
                    if(data.bF < 0f || data.bSF < 0f) impulse.rotate(180f);
                    other.vel().add(impulse);

                    //manually move bullets to simulate velocity for remote players
                    if(other.isRemote()){
                        other.move(impulse.x, impulse.y);
                    }

                    if(Mathf.within(b.x, b.y, other.x, other.y, data.r * scl)){
                        absorbBullet(other);
                    }
                }
            });

            PMDamage.trueEachBlock(b.x, b.y, data.r * scl, Building::kill);
        }

        if(b.time < growTime * 2f){ //*inhales* SPAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACE
            PMDamage.trueEachTile(b.x, b.y, (data.r - 8f) * scl, tile -> {
                if(tile.floor() != Blocks.space){
                    tile.setAir();
                    tile.setFloorNet(Blocks.space);
                }
            });
        }else if(!data.space){
            Events.fire(new WorldLoadEvent());
            data.space = true;
        }
    }

    @Override
    public void draw(Bullet b){
        //[radius, uForce, uScaledForce, bForce, bScaledForce, range, c1, c2]
        CataclysmData data = (CataclysmData)b.data;

        Color[] colors = new Color[]{data.c1, data.c2};
        Color[] darkenedColors = new Color[]{Tmp.c1.set(colors[0]).lerp(Color.black, 0.5f), Tmp.c2.set(colors[1]).lerp(Color.black, 0.5f)};

        float shrink = 1 - Mathf.curve(b.time, b.lifetime - fadeTime, b.lifetime);
        float scl = Mathf.curve(b.time, 0f, growTime) * shrink;
        float grow2 = Interp.pow2Out.apply(Mathf.curve(b.time, 0f, secondaryGrowTime));
        float radius = data.r * scl;

        Draw.z(Layer.max);
        Fill.light(b.x, b.y, 60, radius, Tmp.c1.set(darkenedColors[0]).lerp(darkenedColors[1], Mathf.absin(Time.time + Mathf.randomSeed(b.id), 10f, 1f)), Color.black);

        Angles.randLenVectors(b.id * 2, Mathf.round((data.r + data.rg) / 3f), (data.r + data.rg), (x, y) -> {
            float offset = Mathf.randomSeed((long)(b.id * Mathf.randomSeed((long)x) * Mathf.randomSeed((long)y)));
            float tx = x * grow2;
            float ty = y * grow2;
            Fill.light(b.x + tx + Mathf.range(1f), b.y + ty + Mathf.range(1f), 60, data.r / 10f * grow2 * shrink, Tmp.c1.set(darkenedColors[0]).lerp(darkenedColors[1], Mathf.absin(Time.time + offset, 10f, 1f)), Color.black);
        });
    }

    @Override
    public void despawned(Bullet b){
        // Do nothing
    }

    @Override
    public void hit(Bullet b, float x, float y){
        // Do nothing
    }

    public void absorbBullet(Bullet other){
        EnergyFx.blackHoleAbsorb.at(other.x, other.y);
        if(other.type.trailLength > 0 && other.trail != null && other.trail.size() > 0){
            if(other.trail instanceof PMTrail t){
                TrailFadeFx.PMTrailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, t.copyPM());
            }else{
                Fx.trailFade.at(other.x, other.y, other.type.trailWidth, other.type.trailColor, other.trail.copy());
            }
        }
        other.type = PMBullets.absorbed;
        other.absorb();
    }
}
