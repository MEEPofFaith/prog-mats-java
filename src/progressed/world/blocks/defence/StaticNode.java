package progressed.world.blocks.defence;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.content.effects.*;
import progressed.entities.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class StaticNode extends Block{
    protected static BuildPlan otherReq;
    protected static int returnInt = 0;
    public final int shockTimer = timers++;

    public int laserRange = 20;
    public float damage, reload = 5f, cooldown = 0.1f;
    public float powerPerLink;
    public int maxNodes = 2;
    public boolean hitAir = true, hitGround = true;
    public StatusEffect status = StatusEffects.shocked;
    public float statusDuration = 10f * 60f;
    public LightningEffect lightningEffect = LightningFx.staticLightning;
    public Effect shockEffect = OtherFx.staticSpark;
    public float minValue = 0.75f;

    public TextureRegion laser, laserEnd;

    public StaticNode(String name){
        super(name);

        configurable = hasPower = consumesPower = true;
        outputsPower = false;
        canOverdrive = false;
        solid = true;
        update = true;
        drawDisabled = false;
        noUpdateDisabled = false;
        swapDiagonalPlacement = true;

        config(Integer.class, (entity, value) -> {
            IntSeq links = ((StaticNodeBuild)entity).links;
            StaticNodeBuild other = (StaticNodeBuild)world.build(value);
            boolean contains = links.contains(value), valid = other != null;

            // (t) = target, (b) = base
            if(contains){
                //unlink
                links.removeValue(value);
                if(valid) other.links.removeValue(entity.pos());
            }else if(linkValid(entity, other) && valid && links.size < maxNodes){
                //add other to self
                if(!links.contains(other.pos())){
                    links.add(other.pos());
                }

                //add self to other
                if(other.team == entity.team){
                    if(!other.links.contains(entity.pos())){
                        other.links.add(entity.pos());
                    }
                }
            }
        });

        config(Point2[].class, (tile, value) -> {
            StaticNodeBuild t = (StaticNodeBuild)tile;
            t.links.clear();

            IntSeq old = new IntSeq(t.links);

            //clear old
            for(int i = 0; i < old.size; i++){
                int cur = old.get(i);
                configurations.get(Integer.class).get(tile, cur);
            }

            //set new
            for(Point2 p : value){
                int newPos = Point2.pack(p.x + tile.tileX(), p.y + tile.tileY());
                configurations.get(Integer.class).get(tile, newPos);
            }
        });
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.powerRange, laserRange, StatUnit.blocks);
        stats.add(Stat.powerConnections, maxNodes, StatUnit.none);

        stats.remove(Stat.powerUse);
        stats.add(Stat.powerUse, Core.bundle.format("stat.pm-static-power", powerPerLink * 60f));

        stats.add(Stat.targetsAir, hitAir);
        stats.add(Stat.targetsGround, hitGround);

        stats.add(Stat.ammo, PMStatValues.staticDamage(damage, reload, status));
    }

    @Override
    public void init(){
        consumePowerDynamic(b -> ((StaticNodeBuild)b).powerUse());
        clipSize = Math.max(clipSize, (laserRange + 1f) * tilesize * 2f);

        super.init();
    }

    @Override
    public void load(){
        super.load();

        laser = Core.atlas.find("prog-mats-rainbow-laser");
        laserEnd = Core.atlas.find("prog-mats-rainbow-laser-end");
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("connections", (StaticNodeBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.powerlines", entity.links.size, maxNodes),
            () -> entity.team.color,
            () -> (float)entity.links.size / (float)maxNodes
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);

        Tile tile = world.tile(x, y);

        if(tile == null) return;

        Lines.stroke(1f);
        Draw.color(Pal.placing);
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, laserRange * tilesize);

        getPotentialLinks(tile, player.team(), other -> {
            Draw.color(Tmp.c1.set(player.team().color), Renderer.laserOpacity);
            staticLine(tile.team(), x * tilesize + offset, y * tilesize + offset, other.x, other.y, size, other.block.size, true, false);

            Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
        });

        Draw.reset();
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> overlaps(world.tile(point.x, point.y), world.tile(other.x, other.y)));
    }

    public boolean staticLine(Team team, float x1, float y1, float x2, float y2, int size1, int size2, boolean drawLaser, boolean drawOther, boolean attack){
        float angle1 = Angles.angle(x1, y1, x2, y2),
            len1 = size1 * tilesize / 2f - 1.5f, len2 = size2 * tilesize / 2f - 1.5f;
        Tmp.v1.trns(angle1, len1);
        Tmp.v2.trns(angle1, len2);

        float space = Math.min(size1, size2), scale = 0.25f; //In case I want to change these

        if(drawLaser){
            Tmp.v3.trns(angle1 - 90f, space);
            Drawf.laser(laser, laserEnd,
                x1 + Tmp.v1.x + Tmp.v3.x, y1 + Tmp.v1.y + Tmp.v3.y,
                x2 - Tmp.v2.x + Tmp.v3.x, y2 - Tmp.v2.y + Tmp.v3.y,
                scale
            );

            if(drawOther){
                Tmp.v3.trns(angle1 + 90f, space);
                Drawf.laser(laser, laserEnd,
                    x2 - Tmp.v2.x + Tmp.v3.x, y2 - Tmp.v2.y + Tmp.v3.y,
                    x1 + Tmp.v1.x + Tmp.v3.x, y1 + Tmp.v1.y + Tmp.v3.y,
                    scale
                );
            }
        }

        if(attack){
            Tmp.v3.trns(angle1 - 90f, space);
            float bx = x1 + Tmp.v1.x,
                by = y1 + Tmp.v1.y;
            float dst = Mathf.dst(bx + Tmp.v3.x, by + Tmp.v3.y, x2 - Tmp.v2.x + Tmp.v3.x, y2 - Tmp.v2.y + Tmp.v3.y);

            boolean hit = PMDamage.collideLine(damage, team, shockEffect, status, statusDuration,
                bx + Tmp.v3.x, by + Tmp.v3.y, angle1, dst,
                hitGround, hitAir
            );
            if(hit){
                Tmp.v4.trns(angle1, dst).add(bx, by);
                lightningEffect.at(bx + Tmp.v3.x, by + Tmp.v3.y, Tmp.v4.x, Tmp.v4.y, team.color);
            }

            return hit;
        }
        return false;
    }

    public boolean staticLine(Team team, float x1, float y1, float x2, float y2, int size1, int size2, boolean drawLaser, boolean attack){
        return staticLine(team, x1, y1, x2, y2, size1, size2, drawLaser, true, attack);
    }

    protected boolean overlaps(float srcx, float srcy, Tile other, Block otherBlock, float range){
        return Intersector.overlaps(Tmp.cr1.set(srcx, srcy, range), Tmp.r1.setCentered(other.worldx() + otherBlock.offset, other.worldy() + otherBlock.offset,
            otherBlock.size * tilesize, otherBlock.size * tilesize));
    }

    protected boolean overlaps(float srcx, float srcy, Tile other, float range){
        return Intersector.overlaps(Tmp.cr1.set(srcx, srcy, range), other.getHitbox(Tmp.r1));
    }

    protected boolean overlaps(Building src, Building other, float range){
        return overlaps(src.x, src.y, other.tile(), range);
    }

    protected boolean overlaps(Tile src, Tile other, float range){
        return overlaps(src.drawx(), src.drawy(), other, range);
    }

    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;
        return Intersector.overlaps(Tmp.cr1.set(src.worldx() + offset, src.worldy() + offset, laserRange * tilesize), Tmp.r1.setSize(size * tilesize).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    protected void getPotentialLinks(Tile tile, Team team, Cons<Building> others){
        Boolf<Building> valid = other -> other != null && other.tile() != tile && other.block == this &&
            overlaps(tile.x * tilesize + offset, tile.y * tilesize + offset, other.tile(), laserRange * tilesize) && other.team == team &&
            !(other instanceof StaticNodeBuild obuild && obuild.links.size >= ((StaticNode)obuild.block).maxNodes) &&
            !Structs.contains(Edges.getEdges(size), p -> { //do not link to adjacent buildings
                Tile t = world.tile(tile.x + p.x, tile.y + p.y);
                return t != null && t.build == other;
            });

        tempBuilds.clear();

        Geometry.circle(tile.x, tile.y, laserRange + 2, (x, y) -> {
            Building other = world.build(x, y);
            if(valid.get(other) && !tempBuilds.contains(other)){
                tempBuilds.add(other);
            }
        });

        tempBuilds.sort((a, b) -> {
            int type = -Boolean.compare(a.block instanceof StaticNode, b.block instanceof StaticNode);
            if(type != 0) return type;
            return Float.compare(a.dst2(tile), b.dst2(tile));
        });

        returnInt = 0;

        tempBuilds.each(valid, t -> {
            if(returnInt++ < maxNodes){
                others.get(t);
            }
        });
    }

    @Override
    public void drawPlanConfigTop(BuildPlan req, Eachable<BuildPlan> list){
        if(req.config instanceof Point2[] ps){
            Draw.color(Tmp.c1.set(player.team().color), Renderer.laserOpacity);
            for(Point2 point : ps){
                int px = req.x + point.x, py = req.y + point.y;
                otherReq = null;
                list.each(other -> {
                    if(other.block != null
                        && (px >= other.x - ((other.block.size - 1) / 2) && py >= other.y - ((other.block.size - 1) / 2) && px <= other.x + other.block.size / 2 && py <= other.y + other.block.size / 2)
                        && other != req && other.block.hasPower){
                        otherReq = other;
                    }
                });

                if(otherReq == null || otherReq.block == null) continue;

                staticLine(player == null ? Team.sharded : player.team(), req.drawx(), req.drawy(), otherReq.drawx(), otherReq.drawy(), size, otherReq.block.size, true, false);
            }
            Draw.color();
        }
    }

    public boolean linkValid(Building tile, Building link){
        return linkValid(tile, link, true);
    }

    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes){
        if(tile == link || link == null || !(link.block instanceof StaticNode node) || tile.team != link.team || tile.block != link.block) return false;

        if(overlaps(tile, link, laserRange * tilesize) || overlaps(link, tile, node.laserRange * tilesize)){
            if(checkMaxNodes){
                StaticNodeBuild n = (StaticNodeBuild)link;
                return n.links.size < node.maxNodes || n.links.contains(tile.pos());
            }
            return true;
        }
        return false;
    }

    public class StaticNodeBuild extends Building{
        public IntSeq links = new IntSeq();
        public boolean active;

        @Override
        public void placed(){
            if(net.client()) return;

            getPotentialLinks(tile, team, other -> {
                if(!links.contains(other.pos())){
                    configureAny(other.pos());
                }
            });

            super.placed();
        }

        @Override
        public void dropped(){
            links.clear();
        }

        @Override
        public void onRemoved(){
            //Clear links
            while(links.size > 0){
                configure(links.get(0));
            }

            super.onRemoved();
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(canConsume() && Groups.unit.contains(u -> u.team != team) && timer(shockTimer, reload / efficiency)){
                for(int i : links.items){
                    Building link = world.build(i);

                    if(linked(link)){
                        active = staticLine(team, x, y, link.x, link.y, size, link.block.size, false, true);
                    }
                }
            }
        }

        public float powerUse(){
            if(!active || links.size <= 0) return 0;
            return powerPerLink * links.size;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(linkValid(this, other)){
                configure(other.pos());
                return false;
            }

            if(this == other){
                if(((StaticNodeBuild)other).links.size == 0){
                    int[] total = {0};
                    getPotentialLinks(tile, team, link -> {
                        if(total[0]++ < maxNodes){
                            configure(link.pos());
                        }
                    });
                }else{
                    while(links.size > 0){
                        configure(links.get(0));
                    }
                }
                deselect();
                return false;
            }

            return true;
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            Lines.stroke(1f);

            Draw.color(team.color);
            Drawf.circles(x, y, laserRange * tilesize);
            Draw.reset();
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            Drawf.circles(x, y, laserRange * tilesize);
            
            Draw.color(team.color);

            for(int i : links.items){
                Building link = world.build(i);

                if(link != this && linkValid(this, link, false) && linked(link)){
                    Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                }
            }
            
            Draw.reset();
        }

        @Override
        public void draw(){
            super.draw();

            if(Mathf.zero(Renderer.laserOpacity)) return;

            Draw.z(Layer.power);

            for(int i : links.items){
                Building link = world.build(i);
        
                if(!linkValid(this, link) || !linked(link)) continue;

                Draw.color(Tmp.c1.set(team.color).mul(minValue + efficiency * (1f - minValue)), Renderer.laserOpacity);

                staticLine(team, x, y, link.x, link.y, size, link.block.size, true, false, false);
            }

            Draw.reset();
        }

        protected boolean linked(Building other){
            return other != null && links.contains(other.pos());
        }

        @Override
        public Point2[] config(){
            Point2[] out = new Point2[links.size];
            for(int i = 0; i < out.length; i++){
                out[i] = Point2.unpack(links.get(i)).sub(tile.x, tile.y);
            }
            return out;
        }

        @Override
        public void write(Writes write){
            write.s(links.size);
            for(int i = 0; i < links.size; i++){
                write.i(links.get(i));
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                links.clear();
                short amount = read.s();
                for(int i = 0; i < amount; i++){
                    links.add(read.i());
                }
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }
}
