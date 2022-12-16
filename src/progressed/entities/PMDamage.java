package progressed.entities;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class PMDamage{
    private static final Rect rect = new Rect();
    private static final Rect hitrect = new Rect();
    private static final Vec2 tr = new Vec2(), seg1 = new Vec2(), seg2 = new Vec2();
    private static final Seq<Unit> units = new Seq<>();
    private static final Seq<Bullet> bullets = new Seq<>();
    private static final IntSet collidedBlocks = new IntSet();
    private static Tile furthest;
    private static Building tmpBuilding;
    private static Unit tmpUnit;
    private static float tmpFloat;
    private static int tmpInt;
    private static boolean check;

    public static void trueEachBlock(float wx, float wy, float range, Cons<Building> cons){
        collidedBlocks.clear();
        int tx = World.toTile(wx);
        int ty = World.toTile(wy);

        int tileRange = Mathf.floorPositive(range / tilesize);

        for(int x = tx - tileRange - 2; x <= tx + tileRange + 2; x++){
            for(int y = ty - tileRange - 2; y <= ty + tileRange + 2; y++){
                if(Mathf.within(x * tilesize, y * tilesize, wx, wy, range)){
                    Building other = world.build(x, y);
                    if(other != null && !collidedBlocks.contains(other.pos())){
                        cons.get(other);
                        collidedBlocks.add(other.pos());
                    }
                }
            }
        }
    }

    public static void trueEachTile(float wx, float wy, float range, Cons<Tile> cons){
        collidedBlocks.clear();
        int tx = World.toTile(wx);
        int ty = World.toTile(wy);

        int tileRange = Mathf.floorPositive(range / tilesize);

        for(int x = tx - tileRange - 2; x <= tx + tileRange + 2; x++){
            for(int y = ty - tileRange - 2; y <= ty + tileRange + 2; y++){
                if(Mathf.within(x * tilesize, y * tilesize, wx, wy, range)){
                    Tile other = world.tile(x, y);
                    if(other != null && !collidedBlocks.contains(other.pos())){
                        cons.get(other);
                        collidedBlocks.add(other.pos());
                    }
                }
            }
        }
    }

    public static void allNearbyEnemies(Team team, float x, float y, float radius, Cons<Healthc> cons){
        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f, unit -> {
            if(unit.within(x, y, radius + unit.hitSize / 2f) && !unit.dead){
                cons.get(unit);
            }
        });

        trueEachBlock(x, y, radius, build -> {
            if(build.team != team && !build.dead && build.block != null){
                cons.get(build);
            }
        });
    }

    public static boolean checkForTargets(Team team, float x, float y, float radius){
        check = false;

        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f, unit -> {
            if(unit.within(x, y, radius + unit.hitSize / 2f) && !unit.dead){
                check = true;
            }
        });

        trueEachBlock(x, y, radius, build -> {
            if(build.team != team && !build.dead && build.block != null){
                check = true;
            }
        });

        return check;
    }

    public static Teamc bestTarget(Team team, float cx, float cy, float x, float y, float range, Boolf<Unit> unitPred, Boolf<Building> tilePred, Sortf sort){
        if(team == Team.derelict) return null;

        Unit unit = findEnemyUnit(team, cx, cy, x, y, range, unitPred, sort);
        if(unit != null){
            return unit;
        }else{
            return findEnemyTile(team, cx, cy, x, y, range, tilePred);
        }
    }

    public static Unit findEnemyUnit(Team team, float cx, float cy, float x, float y, float range, Boolf<Unit> pred, Sortf unitSort){
        tmpUnit = null;
        tmpFloat = Float.NEGATIVE_INFINITY;

        Units.nearbyEnemies(team, cx - range, cy - range, range * 2f, range * 2f, unit -> {
            float cost = unitSort.cost(unit, x, y);
            if(!unit.dead && tmpFloat < cost && unit.within(cx, cy, range + unit.hitSize / 2f) && pred.get(unit)){
                tmpUnit = unit;
                tmpFloat = cost;
            }
        });

        return tmpUnit;
    }

    public static Building findEnemyTile(Team team, float cx, float cy, float x, float y, float range, Boolf<Building> pred){
        tmpBuilding = null;
        tmpFloat = 0;

        trueEachBlock(cx, cy, range, b -> {
            if(!(b.team() == team || (b.team() == Team.derelict && !state.rules.coreCapture)) && pred.get(b)){
                //if a block has the same priority, the closer one should be targeted
                float dist = b.dst(x, y) - b.hitSize() / 2f;
                if(tmpBuilding == null ||
                    //if its closer and is at least equal priority
                    (dist < tmpFloat && b.block.priority >= tmpBuilding.block.priority) ||
                    // block has higher priority (so range doesnt matter)
                    (b.block.priority > tmpBuilding.block.priority)){
                    tmpBuilding = b;
                    tmpFloat = dist;
                }
            }
        });

        return tmpBuilding;
    }

    public static boolean collideLine(float damage, Team team, Effect effect, StatusEffect status, float statusDuration, float x, float y, float angle, float length, boolean ground, boolean air){
        return collideLine(damage, team, effect, status, statusDuration, x, y, angle, length, ground, air, false);
    }

    /**
     * Damages entities in a line.
     * Only enemies of the specified team are damaged.
     */
    public static boolean collideLine(float damage, Team team, Effect effect, StatusEffect status, float statusDuration, float x, float y, float angle, float length, boolean ground, boolean air, boolean buildings){
        tr.trnsExact(angle, length);

        rect.setPosition(x, y).setSize(tr.x, tr.y);
        float x2 = x + tr.x, y2 = y + tr.y;

        if(rect.width < 0){
            rect.x += rect.width;
            rect.width *= -1;
        }

        if(rect.height < 0){
            rect.y += rect.height;
            rect.height *= -1;
        }

        float expand = 3f;

        rect.y -= expand;
        rect.x -= expand;
        rect.width += expand * 2;
        rect.height += expand * 2;

        check = false;

        Cons<Unit> cons = e -> {
            e.hitbox(hitrect);

            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitrect.grow(expand * 2));

            if(vec != null && damage > 0){
                effect.at(vec.x, vec.y, angle, team.color);
                e.damage(damage);
                e.apply(status, statusDuration);
                check = true;
            }
        };

        units.clear();

        Units.nearbyEnemies(team, rect, u -> {
            if(u.checkTarget(air, ground)){
                units.add(u);
            }
        });

        units.sort(u -> u.dst2(x, y));
        units.each(cons);

        if(buildings){
            collidedBlocks.clear();

            Intc2 collider = (cx, cy) -> {
                Building tile = world.build(cx, cy);
                boolean collide = tile != null && collidedBlocks.add(tile.pos());

                if(collide && damage > 0 && tile.team != team){
                    effect.at(tile.x, tile.y, angle, team.color);
                    tile.damage(damage);
                    check = true;
                }
            };

            seg1.set(x, y);
            seg2.set(seg1).add(tr);
            World.raycastEachWorld(x, y, seg2.x, seg2.y, (cx, cy) -> {
                collider.get(cx, cy);

                for(Point2 p : Geometry.d4){
                    Tile other = world.tile(p.x + cx, p.y + cy);
                    if(other != null && Intersector.intersectSegmentRectangle(seg1, seg2, other.getBounds(Tmp.r1))){
                        collider.get(cx + p.x, cy + p.y);
                    }
                }
                return false;
            });
        }

        return check;
    }

    public static float bulletCollideLine(float x, float y, float angle, float length, Team team, float damage, int max, Effect effect, Color color){
        tr.trnsExact(angle, length);

        rect.setPosition(x, y).setSize(tr.x, tr.y);
        float x2 = x + tr.x, y2 = y + tr.y;

        if(rect.width < 0){
            rect.x += rect.width;
            rect.width *= -1;
        }

        if(rect.height < 0){
            rect.y += rect.height;
            rect.height *= -1;
        }

        float expand = 3f;

        rect.y -= expand;
        rect.x -= expand;
        rect.width += expand * 2;
        rect.height += expand * 2;

        Cons<Bullet> cons = b -> {
            b.hitbox(hitrect);

            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitrect.grow(expand * 2));

            if(vec != null && damage > 0){
                effect.at(vec.x, vec.y, angle, color);
                if(b.damage() > damage){
                    b.damage(b.damage() - damage);
                }else{
                    b.remove();
                }
                tmpInt++;
                tmpFloat = b.dst(x, y);
            }
        };

        bullets.clear();
        tmpFloat = 0;
        tmpInt = 0;

        Groups.bullet.intersect(rect.x, rect.y, rect.width, rect.height, b -> {
            if(b.type().hittable && b.team != team){
                bullets.add(b);
            }
        });

        bullets.sort(b -> b.dst2(x, y));
        bullets.each(b -> {
            if(tmpInt < max || max <= 0){
                cons.get(b);
            }
        });

        if(tmpInt < max){
            tmpFloat = length;
        }

        return tmpFloat;
    }

    /** Like Damage.findLaserLength, but uses an (x, y) coord instead of bullet position */
    public static float findLaserLength(float x, float y, float angle, Team team, float length){
        Tmp.v1.trns(angle, length);

        furthest = null;

        boolean found = world.raycast(World.toTile(x), World.toTile(y), World.toTile(x + Tmp.v1.x), World.toTile(y + Tmp.v1.y),
        (tx, ty) -> (furthest = world.tile(tx, ty)) != null && furthest.team() != team && furthest.block().absorbLasers);

        return found && furthest != null ? Math.max(6f, Mathf.dst(x, y, furthest.worldx(), furthest.worldy())) : length;
    }

    public static void completeDamage(Team team, float x, float y, float radius, float damage, float buildDmbMult, boolean air, boolean ground){
        allNearbyEnemies(team, x, y, radius, t -> {
            if(t instanceof Unit u){
                if(u.isFlying() && air || u.isGrounded() && ground){
                    u.damage(damage);
                }
            }else if(t instanceof Building b){
                if(ground){
                    b.damage(team, damage * buildDmbMult);
                }
            }
        });
    }

    public static void completeDamage(Team team, float x, float y, float radius, float damage){
        completeDamage(team, x, y, radius, damage, 1f, true, true);
    }

    /**
     * Casts forward in a line.
     * @return the collision point of the first encountered object.
     */
    public static Vec2 linecast(boolean ground, boolean air, Team team, float x, float y, float angle, float length){
        tr.trnsExact(angle, length);
        
        tmpBuilding = null;

        if(ground){
            seg1.set(x, y);
            seg2.set(seg1).add(tr);
            World.raycastEachWorld(x, y, seg2.x, seg2.y, (cx, cy) -> {
                Building tile = world.build(cx, cy);
                if(tile != null && tile.team != team){
                    tmpBuilding = tile;
                    Tmp.v1.set(cx * tilesize, cy * tilesize);
                    return true;
                }
                return false;
            });
        }

        float expand = 3f;

        rect.setPosition(x, y).setSize(tr.x, tr.y).normalize().grow(expand * 2f);
        float x2 = tr.x + x, y2 = tr.y + y;

        tmpUnit = null;

        Units.nearbyEnemies(team, rect, e -> {
            if((tmpUnit != null && e.dst2(x, y) > tmpUnit.dst2(x, y)) || !e.checkTarget(ground, air)) return;

            e.hitbox(hitrect);
            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitrect.grow(expand * 2));

            if(vec != null){
                tmpUnit = e;
                Tmp.v2.set(vec);
            }
        });

        if(tmpBuilding != null && tmpUnit != null){
            if(Mathf.dst2(x, y, Tmp.v1.x, Tmp.v1.y) <= Mathf.dst2(x, y, Tmp.v2.x, Tmp.v2.y)){
                return Tmp.v1;
            }
        }else if(tmpBuilding != null){
            return Tmp.v1;
        }else if(tmpUnit != null){
            return Tmp.v2;
        }

        return tr.add(x, y);
    }
}
