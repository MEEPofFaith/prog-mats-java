package progressed.entities;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class PMDamage{
    private static Tile furthest;
    private static Rect rect = new Rect();
    private static Rect hitrect = new Rect();
    private static Vec2 tr = new Vec2();
    private static Seq<Unit> units = new Seq<>();
    private static IntSet collidedBlocks = new IntSet();
    private static Building tmpBuilding;
    private static Unit tmpUnit;
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
    
    public static Seq<Healthc> allNearbyEnemies(Team team, float x, float y, float radius){
        Seq<Healthc> targets = new Seq<>();

        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f, unit -> {
            if(Mathf.within(x, y, unit.x, unit.y, radius) && !unit.dead){
                targets.add(unit);
            }
        });
        
        trueEachBlock(x, y, radius, build -> {
            if(build.team != team && !build.dead && build.block != null){
                targets.add(build);
            }
        });

        return targets;
    }

    public static void allNearbyEnemies(Team team, float x, float y, float radius, Cons<Healthc> cons){
        allNearbyEnemies(team, x, y, radius).each(cons);
    }

    public static boolean checkForTargets(Team team, float x, float y, float radius){
        check = false;

        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f, unit -> {
            if(Mathf.within(x, y, unit.x, unit.y, radius) && !unit.dead){
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

    /**
     * Damages entities in a line.
     * Only enemie units of the specified team are damaged.
     */
    public static boolean staticDamage(float damage, Team team, Effect effect, StatusEffect status, float statusDuration, float x, float y, float angle, float length, boolean air, boolean ground){
        tr.trns(angle, length);

        rect.setPosition(x, y).setSize(tr.x, tr.y);
        float x2 = tr.x + x, y2 = tr.y + y;

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
        return check;
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
        Seq<Healthc> targets = allNearbyEnemies(team, x, y, radius);
        targets.each(t -> {
            if(t instanceof Unit u){
                if(u.isFlying() && air || u.isGrounded() && ground){
                    u.damage(damage);
                }
            }else if(t instanceof Building b){
                if(ground){
                    b.damage(damage * buildDmbMult);
                }
            }
        });
    }

    public static void completeDamage(Team team, float x, float y, float radius, float damage){
        completeDamage(team, x, y, radius, damage, 1f, true, true);
    }

    /**
     * Casts forward in a line.
     * @return the first encountered object.
     */
    public static Healthc linecast(boolean ground, boolean air, Team team, float x, float y, float angle, float length){
        tr.trns(angle, length);
        
        tmpBuilding = null;

        if(ground){
            world.raycastEachWorld(x, y, x + tr.x, y + tr.y, (cx, cy) -> {
                Building tile = world.build(cx, cy);
                if(tile != null && tile.team != team){
                    tmpBuilding = tile;
                    return true;
                }
                return false;
            });
        }

        rect.setPosition(x, y).setSize(tr.x, tr.y);
        float x2 = tr.x + x, y2 = tr.y + y;

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

        tmpUnit = null;

        Units.nearbyEnemies(team, rect, e -> {
            if((tmpUnit != null && e.dst2(x, y) > tmpUnit.dst2(x, y)) || !e.checkTarget(ground, air)) return;

            e.hitbox(hitrect);
            Rect other = hitrect;
            other.y -= expand;
            other.x -= expand;
            other.width += expand * 2;
            other.height += expand * 2;

            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, other);

            if(vec != null){
                tmpUnit = e;
            }
        });

        if(tmpBuilding != null && tmpUnit != null){
            if(Mathf.dst2(x, y, tmpBuilding.getX(), tmpBuilding.getY()) <= Mathf.dst2(x, y, tmpUnit.getX(), tmpUnit.getY())){
                return tmpBuilding;
            }
        }else if(tmpBuilding != null){
            return tmpBuilding;
        }

        return tmpUnit;
    }
}
