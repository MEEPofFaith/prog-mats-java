package progressed.world.blocks.defence.turret.apotheosis;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.apotheosis.ApotheosisNexus.*;

import static mindustry.Vars.*;

@SuppressWarnings("unchecked")
public class ApotheosisChargeTower extends Block{
    protected static BuildPlan otherReq;

    public float range = 10f;
    public float damageBoost, radiusBoost, speedBoost, durationBoost;
    public float powerUse = 1f;

    public float startLength, effectLength, endLength;
    public Color placeLine = PMPal.apotheosisLaser;
    public Color[] colors = {Color.valueOf("CD423855"), Color.valueOf("CD4238aa"), PMPal.apotheosisLaser, Color.white};
    public Color laserLightColor = PMPal.apotheosisLaser;
    public float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
    public float[] strokes = {2f, 1.5f, 1f, 0.3f};
    public float[] lenscales = {0.90f, 0.95f, 0.98f, 1f};
    public float width = 1f, oscScl = 3f, oscMag = 0.2f, spaceMag = 35f;
    public float activeScl = 4f;
    public Effect activateEffect = PMFx.apotheosisChargerBlast;

    public TextureRegion baseRegion;

    public ApotheosisChargeTower(String name){
        super(name);

        configurable = true;
        canOverdrive = false;
        update = true;
        hasPower = true;
        solid = true;
        group = BlockGroup.turrets;
        outlineIcon = true;
        schematicPriority = -1;

        config(Integer.class, (ApotheosisChargeTowerBuild tile, Integer value) -> {
            Building other = world.build(value);
            int pos = tile.pos();
            if(other instanceof ApotheosisNexusBuild o && ((ApotheosisNexus)o.block).chargeTower == tile.block && o.within(tile, range * tilesize)){
                if(o.chargers.contains(pos)){
                    o.chargers.removeValue(pos);
                    tile.nexus = -1;
                }else{
                    if(tile.getNexus() != null){
                        tile.getNexus().chargers.removeValue(pos);
                        tile.getNexus().connectedChargers.removeValue(pos);
                    }
                    o.chargers.add(pos);
                    tile.nexus = o.pos();
                    tile.rotation = tile.angleTo(o);
                }
                tile.connected = false;
                tile.fullLaser = false;
            }
        });

        config(Point2.class, (tile, value) -> configurations.get(Integer.class).get(tile, Point2.pack(value.x +  + tile.tileX(), value.y +  + tile.tileY())));

        configClear((ApotheosisChargeTowerBuild tile) -> {
           if(tile.getNexus() != null){
               tile.getNexus().chargers.removeValue(tile.pos());
               tile.nexus = -1;
               tile.connected = false;
           }
        });
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base", Core.atlas.find("prog-mats-block-" + size, "block-" + size));
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisChargeTowerBuild::isActive);
        super.init();

        clipSize = Math.max(clipSize, (range + 2) * tilesize * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, range * tilesize);
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list){
        otherReq = null;
        list.each(other -> {
            if(other.block instanceof ApotheosisNexus && req != other && req.config instanceof Point2 p && p.equals(other.x - req.x, other.y - req.y)){
                otherReq = other;
            }
        });

        if(otherReq != null){
            Drawf.dashLine(placeLine, req.drawx(), req.drawy(), otherReq.drawx(), otherReq.drawy());
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    public class ApotheosisChargeTowerBuild extends Building{
        public float rotation = 90f;
        public int nexus;
        public boolean connected, fullLaser;

        public boolean isActive(){
            return connected && getNexus() != null && getNexus().isActive();
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Draw.rect(region, x, y, rotation - 90f);

            if(getNexus() != null){
                Tmp.v1.trns(rotation, startLength);
                Draw.z(Layer.effect);
                PMDrawf.laser(team, x + Tmp.v1.x, y + Tmp.v1.y, fullLaser ? (dst(getNexus()) - getNexusBlock().laserRadius - startLength) : (endLength - startLength) * Interp.pow3Out.apply(Mathf.clamp(chargef() * 3f)), width, rotation, 1f + (activeScl - 1f) * Mathf.clamp((chargef() - (1f/3f)) * 1.5f), tscales, strokes, lenscales, oscScl, oscMag, spaceMag, colors, laserLightColor);
            }
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
            Drawf.circles(x, y, range * tilesize);

            for(int x = (int)(tile.x - range - 2); x <= tile.x + range + 2; x++){
                for(int y = (int)(tile.y - range - 2); y <= tile.y + range + 2; y++){
                    Building link = world.build(x, y);

                    if(link instanceof ApotheosisNexusBuild){
                        if(getNexus() == link){
                            Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, Pal.place);
                            Drawf.dashLine(placeLine, this.x, this.y, link.x, link.y);
                        }
                    }
                }
            }

            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.circles(x, y, range * tilesize);
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(getNexus() != null){
                rotation = angleTo(getNexus());
            }
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(self() == other){
                configure(null);
                return true;
            }else if(other instanceof ApotheosisNexusBuild){
                configure(other.pos());
                return false;
            }
            return true;
        }

        public ApotheosisNexus getNexusBlock(){
            return world.tile(nexus).block() instanceof ApotheosisNexus b ? b : null;
        }

        public ApotheosisNexusBuild getNexus(){
            return world.build(nexus) instanceof ApotheosisNexusBuild b ? b : null;
        }

        public float chargef(){
            return getNexus() != null && connected ? getNexus().chargef() : 0f;
        }

        public void activate(){
            if(connected){
                fullLaser = true;
                Tmp.v1.trns(rotation, effectLength);
                activateEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation);
            }
        }

        @Override
        public void pickedUp(){
            super.pickedUp();
            configure(null);
        }

        @Override
        public Object config(){
            return Point2.unpack(nexus).sub(tile.x, tile.y);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(nexus);
            write.bool(connected);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            nexus = read.i();
            connected = read.bool();
        }
    }
}