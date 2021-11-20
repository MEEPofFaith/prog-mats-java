package progressed.world.blocks.defence.turret.apotheosis;

import arc.*;
import arc.audio.*;
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
    public float boostFalloff = 1f;
    public float powerUse = 1f;

    public float startLength, effectLength, endLength;
    public Color placeLine = PMPal.apotheosisLaser;
    public Color[] colors = PMPal.apotheosisLaserColors;
    public Color laserLightColor = PMPal.apotheosisLaser;
    public float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
    public float[] strokes = {2f, 1.7f, 1.2f, 0.6f};
    public float[] lenscales = {0.90f, 0.95f, 0.98f, 1f};
    public float width = 1f, oscScl = 3f, oscMag = 0.2f, spaceMag = 35f;
    public float lightStroke = 6f;
    public float activeScl = 4f;
    public Effect activateEffect = PMFx.apotheosisChargerBlast;
    public Sound shootSound = Sounds.laser;

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
        lightColor = PMPal.apotheosisLaser;

        config(Integer.class, (ApotheosisChargeTowerBuild tile, Integer value) -> {
            Building other = world.build(value);
            int pos = tile.pos();
            if(other instanceof ApotheosisNexusBuild o && o.within(tile, range * tilesize)){
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
                tile.scl = 1f;
            }
        });

        config(Point2.class, (tile, value) -> configurations.get(Integer.class).get(tile, Point2.pack(value.x + tile.tileX(), value.y + tile.tileY())));

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
    public void setStats(){
        super.setStats();

        stats.add(Stat.range, range, StatUnit.blocks);
        stats.add(Stat.ammo, s -> {
            s.row();
            s.table(st -> {
                st.left().defaults().padRight(3).left();
                st.add(Core.bundle.format("bullet.pm-continuoussplashdamage", "+" + damageBoost * 12, "+" + Strings.fixed(radiusBoost / tilesize, 1)));
                st.row();
                st.add(Core.bundle.format("bullet.pm-flare-lifetime", "+" + Strings.fixed(durationBoost / 60f, 2)));
                if(boostFalloff < 1){
                    st.row();
                    st.add(Core.bundle.format("pm-apotheosis-falloff", (int)(boostFalloff * 100)));
                    st.row();
                    st.add(Core.bundle.get("pm-apotheosis-falloff-notice")).padLeft(12);
                }
            }).padTop(-9).left().get().background(Tex.underline);
        });
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, ApotheosisChargeTowerBuild::isActive);
        super.init();

        clipSize = Math.max(clipSize, (range + 2) * tilesize * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Drawf.circles(x * tilesize + offset, y * tilesize + offset, range * tilesize, PMPal.apotheosisLaser);
    }

    @Override
    public void drawRequestConfigTop(BuildPlan req, Eachable<BuildPlan> list){
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
        public float rotation = 90f, scl = 1f;
        public int nexus;
        public boolean connected, fullLaser;

        @Override
        public void placed(){
            super.placed();

            if(getNexus() == null){
                Building find = indexer.findTile(team, x, y, range * tilesize, b -> b instanceof ApotheosisNexusBuild);
                if(find != null){
                    configure(find.pos());
                }
            }
        }

        public boolean isActive(){
            return connected && getNexus() != null && getNexus().isActive();
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Draw.rect(region, x, y, rotation - 90f);

            if(getNexus() != null){
                Draw.z(Layer.effect + 0.0005f);
                Tmp.v1.trns(rotation, startLength);
                float baseLen = fullLaser ? (dst(getNexus()) - getNexusBlock().laserRadius - startLength) : (endLength - startLength) * Interp.pow3Out.apply(Mathf.clamp(chargef() * 3f));
                float wScl = (1f + (activeScl - 1f) * Mathf.clamp((chargef() - (1f/3f)) * 1.5f)) * scl * efficiency();

                for(int s = 0; s < colors.length; s++){
                    Draw.color(Tmp.c1.set(colors[s]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
                    for(int i = 0; i < tscales.length; i++){
                        Tmp.v2.trns(rotation + 180f, (lenscales[i] - 1f) * spaceMag);
                        Lines.stroke((width + Mathf.absin(Time.time, oscScl, oscMag)) * wScl * strokes[s] * tscales[i]);
                        Lines.lineAngle(x + Tmp.v1.x + Tmp.v2.x, y + Tmp.v1.y + Tmp.v2.y, rotation, baseLen * lenscales[i], false);
                    }
                }

                Tmp.v2.trns(rotation, baseLen * 1.1f);

                Drawf.light(team, x + Tmp.v1.x, y + Tmp.v1.y, x + Tmp.v2.x, y + Tmp.v2.y, lightStroke, lightColor, 0.7f);
                Draw.reset();
            }
        }

        @Override
        public void drawConfigure(){
            Drawf.circles(x, y, tile.block().size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f), PMPal.apotheosisLaser);
            Drawf.circles(x, y, range * tilesize, PMPal.apotheosisLaser);

            for(int x = (int)(tile.x - range - 2); x <= tile.x + range + 2; x++){
                for(int y = (int)(tile.y - range - 2); y <= tile.y + range + 2; y++){
                    Building link = world.build(x, y);

                    if(link instanceof ApotheosisNexusBuild){
                        if(getNexus() == link){
                            Drawf.square(link.x, link.y, link.block.size * tilesize / 2f + 1f, PMPal.apotheosisLaserDark);
                            Drawf.dashLine(placeLine, this.x, this.y, link.x, link.y);
                        }
                    }
                }
            }

            Draw.reset();
        }

        @Override
        public void drawSelect(){
            Drawf.circles(x, y, range * tilesize, PMPal.apotheosisLaser);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            checkNexus();
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

        public void checkNexus(){
            if(getNexus() == null){
                connected = false;
                fullLaser = false;
                scl = 1f;
            }else if(!getNexus().chargers.contains(pos())){
                getNexus().chargers.add(pos());
            }
        }

        public void activate(){
            if(connected){
                fullLaser = true;
                Tmp.v1.trns(rotation, effectLength);
                activateEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation);
                shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y, Mathf.random(0.8f, 1.2f), 0.5f);
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
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            nexus = read.i();
            connected = read.bool();
            rotation = read.f();
        }
    }
}