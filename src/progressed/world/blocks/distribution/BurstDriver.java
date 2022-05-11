package progressed.world.blocks.distribution;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.MassDriver.*;
import mindustry.world.meta.*;
import progressed.content.bullets.*;

import static mindustry.Vars.*;

public class BurstDriver extends Block{
    public float range;
    public float rotateSpeed = 0.04f;
    public float shootY = 7f;
    public int minDistribute = 10;

    public float recoil = 4f;
    public float restitution = 0.03f;
    public float elevation = -1f;

    public float reload = 100f;

    public float speed = 6.5f;
    public float lifetime = 100f;

    public Effect shootEffect = Fx.shootSmall;
    public Effect smokeEffect = Fx.shootSmallSmoke;
    public Effect receiveEffect = Fx.mine;
    public Sound shootSound = Sounds.shoot;
    public float shake = 1f;

    public int shots = 10;
    public float delay = 1f;
    public float xRand = 3f;

    public TextureRegion baseRegion;

    protected Vec2 tr = new Vec2();
    protected Vec2 recoilOffset = new Vec2();

    public BurstDriver(String name){
        super(name);

        update = true;
        solid = true;
        configurable = true;
        hasItems = true;
        hasPower = true;
        outlineIcon = true;
        sync = true;

        //point2 is relative
        config(Point2.class, (BurstDriverBuild tile, Point2 point) -> tile.link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY()));
        config(Integer.class, (BurstDriverBuild tile, Integer point) -> tile.link = point);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        stats.add(Stat.reload, 60f / (reload + 1) * shots, StatUnit.none);
    }

    @Override
    public void init(){
        super.init();

        if(elevation < 0) elevation = size / 2f;
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base", "mass-driver-base");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize, y * tilesize, range, Pal.accent);

        //check if a mass driver is selected while placing this driver
        if(!control.input.config.isShown()) return;
        Building selected = control.input.config.getSelected();
        if(selected == null || selected.block != this || !selected.within(x * tilesize, y * tilesize, range)) return;

        //if so, draw a dotted line towards it while it is in range
        float sin = Mathf.absin(Time.time, 6f, 1f);
        Tmp.v1.set(x * tilesize + offset, y * tilesize + offset).sub(selected.x, selected.y).limit((size / 2f + 1) * tilesize + sin + 0.5f);
        float x2 = x * tilesize - Tmp.v1.x, y2 = y * tilesize - Tmp.v1.y,
            x1 = selected.x + Tmp.v1.x, y1 = selected.y + Tmp.v1.y;
        int segs = (int)(selected.dst(x * tilesize, y * tilesize) / tilesize);

        Lines.stroke(4f, Pal.gray);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Lines.stroke(2f, Pal.placing);
        Lines.dashLine(x1, y1, x2, y2, segs);
        Draw.reset();
    }

    public class BurstDriverData implements Poolable{
        public BurstDriverBuild from, to;
        public Item item;

        @Override
        public void reset(){
            from = null;
            to = null;
        }
    }

    public class BurstDriverBuild extends Building{ //These are incompatable with Mass Drivers. I had to copy the entirety of Mass Driver so that this isn't an instanceof MassDriverBuild
        public int link = -1;
        public float rotation = 90, reloadCounter, curRecoil;
        public DriverState state = DriverState.idle;
        public OrderedSet<Tile> waitingShooters = new OrderedSet<>();

        public Tile currentShooter(){
            return waitingShooters.isEmpty() ? null : waitingShooters.first();
        }

        @Override
        public void updateTile(){
            Building link = world.build(this.link);
            boolean hasLink = linkValid();

            curRecoil = Mathf.lerpDelta(curRecoil, 0f, restitution);

            if(hasLink){
                this.link = link.pos();
            }

            //reload regardless of state
            if(reloadCounter > 0f){
                reloadCounter = Mathf.clamp(reloadCounter - edelta() / reload);
            }

            //cleanup waiting shooters that are not valid
            if(!shooterValid(currentShooter())){
                waitingShooters.remove(currentShooter());
            }

            //switch states
            if(state == DriverState.idle){
                //start accepting when idle and there's space
                if(!waitingShooters.isEmpty() && (itemCapacity - items.total() >= minDistribute)){
                    state = DriverState.accepting;
                }else if(hasLink){ //switch to shooting if there's a valid link.
                    state = DriverState.shooting;
                }
            }

            //dump when idle or accepting
            if(state == DriverState.idle || state == DriverState.accepting){
                dump();
            }

            ///skip when there's no power
            if(efficiency <= 0f){
                return;
            }

            if(state == DriverState.accepting){
                //if there's nothing shooting at this, bail - OR, items full
                if(currentShooter() == null || (itemCapacity - items.total() < minDistribute && !sandy())){
                    state = DriverState.idle;
                    return;
                }

                //align to shooter rotation
                rotation = Mathf.slerpDelta(rotation, tile.angleTo(currentShooter()), rotateSpeed * efficiency);
            }else if(state == DriverState.shooting){
                //if there's nothing to shoot at OR someone wants to shoot at this thing, bail
                if(!hasLink || (!waitingShooters.isEmpty() && (itemCapacity - items.total() >= minDistribute))){
                    state = DriverState.idle;
                    return;
                }

                float targetRotation = tile.angleTo(link);

                if(
                    (items.total() >= minDistribute || sandy()) && //must shoot minimum amount of items
                        link.block.itemCapacity - link.items.total() >= minDistribute //must have minimum amount of space
                ){
                    BurstDriverBuild other = (BurstDriverBuild)link;
                    other.waitingShooters.add(tile);

                    if(reloadCounter <= 0.0001f){

                        //align to target location
                        rotation = Mathf.slerpDelta(rotation, targetRotation, rotateSpeed * efficiency);

                        //fire when it's the first in the queue and angles are ready.
                        if(other.currentShooter() == tile &&
                            other.state == DriverState.accepting &&
                            Angles.near(rotation, targetRotation, 2f) && Angles.near(other.rotation, targetRotation + 180f, 2f)){
                            //actually fire
                            fire(other);
                            float timeToArrive = Math.min(lifetime, dst(other) / speed);
                            Time.run(timeToArrive, () -> {
                                //remove waiting shooters, it's done firing
                                other.waitingShooters.remove(tile);
                                other.state = DriverState.idle;
                            });
                            //driver is immediately idle
                            state = DriverState.idle;
                        }
                    }
                }
            }
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);

            recoilOffset.trns(rotation, -curRecoil);

            Drawf.shadow(region, x + recoilOffset.x - elevation, y + recoilOffset.y - elevation, rotation - 90);
            Draw.rect(region, x + recoilOffset.x, y + recoilOffset.y, rotation - 90);
        }

        @Override
        public void drawConfigure(){
            float sin = Mathf.absin(Time.time, 6f, 1f);

            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(x, y, (tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.accent);

            for(Tile shooter : waitingShooters){
                Drawf.circles(shooter.drawx(), shooter.drawy(), (tile.block().size / 2f + 1) * tilesize + sin - 2f, Pal.place);
                Drawf.arrow(shooter.drawx(), shooter.drawy(), x, y, size * tilesize + sin, 4f + sin, Pal.place);
            }

            if(linkValid()){
                Building target = world.build(link);
                Drawf.circles(target.x, target.y, (target.block().size / 2f + 1) * tilesize + sin - 2f, Pal.place);
                Drawf.arrow(x, y, target.x, target.y, size * tilesize + sin, 4f + sin);
            }

            Drawf.dashCircle(x, y, range, Pal.accent);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(this == other){
                configure(-1);
                return false;
            }

            if(link == other.pos()){
                configure(-1);
                return false;
            }else if(other.block instanceof BurstDriver && other.dst(tile) <= range && other.team == team){
                configure(other.pos());
                return false;
            }

            return true;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            //Burst drivers that output only cannot accept items
            return items.total() < itemCapacity && linkValid() && !sandy();
        }

        protected void fire(BurstDriverBuild target){
            //reset reload, use power.
            reloadCounter = 1f;

            for(int i = 0; i < Math.min(shots, sandy() ? shots : items.total()); i++){
                Time.run(i * delay, () -> {
                    if(!isValid() || efficiency <= 0) return;

                    BurstDriverData data = Pools.obtain(BurstDriverData.class, BurstDriverData::new);
                    boolean canShoot = false;
                    data.from = this;
                    data.to = target;
                    if(sandy()){
                        data.item = content.items().random();
                        canShoot = true;
                    }else{
                        Item item = items.take();
                        if(item != null){
                            data.item = item;
                            canShoot = true;
                        }
                    }

                    if(canShoot){
                        float angle = tile.angleTo(target);

                        tr.trns(angle, shootY, Mathf.range(xRand));

                        PMBullets.burstDriverOrb.create(this, team, x + tr.x, y + tr.y, angle, -1f, speed, lifetime, data);

                        shootEffect.at(x + tr.x, y + tr.y, angle);
                        smokeEffect.at(x + tr.x, y + tr.y, angle);
                        Effect.shake(shake, shake, this);
                        shootSound.at(tile, Mathf.random(0.9f, 1.1f));
                        curRecoil = recoil;
                    }
                });
            }
        }

        public void handleBurstItem(Bullet bullet, BurstDriverData data){
            //add item if possible
            int maxAdd = Math.min(1, itemCapacity * 2 - items.total());
            items.add(data.item, maxAdd);
            dump(data.item); //input is fast, attempt to dump immidiately

            Effect.shake(shake, shake, this);
            receiveEffect.at(bullet);

            reloadCounter = 1f;
            curRecoil = recoil;
            bullet.remove();
        }

        protected boolean shooterValid(Tile other){
            if(other == null) return true;
            if(!(other.build instanceof BurstDriverBuild entity)) return false;
            return entity.link == tile.pos() && tile.dst(other) <= range;
        }

        protected boolean linkValid(){
            if(link == -1) return false;
            Building link = world.build(this.link);
            return link instanceof BurstDriverBuild && link.team == team && within(link, range);
        }

        @Override
        public Point2 config(){
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        public boolean sandy(){
            return false;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(link);
            write.f(rotation);
            write.b((byte)state.ordinal());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            link = read.i();
            rotation = read.f();
            state = DriverState.all[read.b()];
        }
    }
}
