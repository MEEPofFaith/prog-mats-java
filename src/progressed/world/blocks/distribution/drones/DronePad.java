package progressed.world.blocks.distribution.drones;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import progressed.entities.units.*;
import progressed.entities.units.entity.*;
import progressed.world.blocks.distribution.drones.stations.DroneStation.*;
import progressed.world.blocks.distribution.drones.stations.ItemDroneStation.*;
import progressed.world.blocks.distribution.drones.stations.LiquidDroneStation.*;
import progressed.world.blocks.distribution.drones.stations.PayloadDroneStation.*;

import java.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class DronePad extends Block{
    static final Rand rand = new Rand();

    public int maxRoutes = 5;
    public float constructTime = 180f;
    public float constructPowerUse = 1f;
    public float chargeRate = 3f;

    public Color laserColor = Pal.powerLight, laserColorTop = Color.white;
    public float chargeX, chargeY;
    public float beamWidth = 1f;

    public DroneUnitType droneType;

    public TextureRegion arrowRegion;
    public TextureRegion laser, laserEnd, laserTop, laserTopEnd;

    protected Vec2 tr = new Vec2(), tr2 = new Vec2();

    public DronePad(String name){
        super(name);

        update = true;
        configurable = true;
        hasPower = true;

        config(IntSeq.class, (DronePadBuild b, IntSeq i) -> b.routes = IntSeq.with(i.toArray()));
    }

    @Override
    public void init(){
        consumes.add(new DronePadConsumePower());
        super.init();
    }

    @Override
    public void load(){
        super.load();

        arrowRegion = atlas.find(name + "-arrow", "bridge-arrow");
        laser = atlas.find("laser-white");
        laserEnd = atlas.find("laser-white-end");
        laserTop = atlas.find("laser-top");
        laserTopEnd = atlas.find("laser-top-end");
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("drone-charge", (DronePadBuild entity) -> new Bar("pm-drone-pad-charge", Pal.powerBar, entity::chargef));
    }

    public class DronePadBuild extends Building{
        public int selRoute = -1, selEnd = -1;
        public float build, buildup, chargeup, total;
        public boolean constructing, charging;
        public Vec2[] lastEnds = new Vec2[]{new Vec2(), new Vec2(), new Vec2(), new Vec2()};
        public IntSeq routes;
        public DroneUnitEntity drone;

        @Override
        public void created(){
            super.created();

            int[] fill = new int[maxRoutes * 2];
            Arrays.fill(fill, -1);
            routes = IntSeq.with(fill);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            buildup = Mathf.lerpDelta(buildup, Mathf.num(constructing), 0.15f);
            chargeup = Mathf.lerpDelta(chargeup, Mathf.num(charging), 0.15f);

            for(int i = 0; i < maxRoutes; i++){
                DroneStationBuild o = getStation(i, 0);
                DroneStationBuild d = getStation(i, 1);
                if(o != null && !o.connected){
                    o.connect(0);
                }
                if(o != null && d != null){
                    if(d.getClass() != o.getClass()){
                        disconnectStation(i, 1);
                    }else if(!d.connected){
                        d.connect(1);
                    }
                }
            }

            if(drone != null && drone.dead){
                drone = null;
                charging = false;
            }

            if(drone != null && charging){
                drone.recharge(edelta() * chargeRate);
            }

            if(drone == null && consValid()){
                constructing = true;
                build += edelta();
                if(build >= constructTime){
                    build = 0f;
                    constructing = false;
                    drone = (DroneUnitEntity)droneType.spawn(team, this);
                    drone.rotation(90);
                    drone.pad = pos();
                    drone.updateRoutes();
                    drone.charge = ((DroneUnitType)(drone.type)).chargeCapacity;
                }
            }
            total += edelta() * buildup;
        }

        public float chargef(){
            return drone != null ? drone.chargef() : 0f;
        }

        @Override
        public void draw(){
            super.draw();

            if(buildup > 0.01){
                Draw.draw(Layer.blockOver, () -> {
                    Drawf.construct(x, y, droneType.fullIcon, 0f, Math.max(build / constructTime, 0.02f), buildup, total);
                });
            }

            if(chargeup > 0.01f){ //Why do I feel like this'll kill low-end devices?
                Draw.z((droneType.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) + 0.5f);
                for(int i = 0; i < 2; i++){
                    int j = 0;
                    for(int xflip: Mathf.signs){
                        for(int yflip: Mathf.signs){
                            float originX = x + chargeX * xflip, originY = y + chargeY * yflip; //A casual yoink from repair points later...

                            if(charging && drone != null){
                                rand.setSeed(id + drone.id() + j);

                                lastEnds[j].set(drone).sub(originX, originY);
                                lastEnds[j].setLength(Math.max(2f, lastEnds[j].len()));

                                lastEnds[j].add(tr.trns(
                                    rand.random(360f) + Time.time / 2f,
                                    Mathf.sin(Time.time + rand.random(200f), 55f, rand.random(drone.hitSize() * 0.2f, drone.hitSize() * 0.45f))
                                ).rotate(drone.rotation()));

                                lastEnds[j].add(originX, originY);
                            }

                            if(i == 0){
                                Draw.color(laserColor);
                                Drawf.laser(team, laser, laserEnd, originX, originY, lastEnds[j].x, lastEnds[j].y, chargeup * beamWidth);
                            }else{
                                Draw.color(laserColorTop);
                                Drawf.laser(team, laserTop, laserTopEnd, originX, originY, lastEnds[j].x, lastEnds[j].y, chargeup * beamWidth);
                            }

                            j++;
                        }
                    }
                }
            }
        }

        @Override
        public void drawSelect(){
            routes.each(r -> {
                DroneStationBuild s = getStation(r);
                if(s != null){
                    float drawSize = s.block().size * tilesize / 2f + 2f;
                    Drawf.select(s.x, s.y, drawSize, s.selectColor());
                    if(s.acceptEnd != -1) drawArrow(s.x, s.y, s.isOrigin(), drawSize / 2f, s.selectColor());
                }
            });

            drawConnections();

            routes.each(r -> {
                DroneStationBuild s = getStation(r);
                if(s != null){
                    s.drawSelect();
                }
            });

            if(drone != null){
                Draw.z((droneType.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) + 1f);
                Draw.mixcol(Pal.accent, 1f);
                Draw.rect(drone.type.fullIcon, drone.x, drone.y, drone.rotation - 90);
                for(int i = 0; i < 4; i++){
                    float rot = i * 90f + 45f + (-Time.time) % 360f;
                    float length = drone.hitSize() * 1.5f + 2.5f;
                    Draw.rect("select-arrow", drone.getX() + Angles.trnsx(rot, length), drone.getY() + Angles.trnsy(rot, length), length / 1.9f, length / 1.9f, rot - 135f);
                }
                Draw.reset();
            }
        }

        @Override
        public void drawConfigure(){
            Drawf.select(x, y, size * tilesize / 2f + 2f + Mathf.absin(Time.time, 4f, 1f), team.color);

            Groups.build.each(b -> b instanceof DroneStationBuild s && (!(s.connected || s.active) || routes.contains(s.pos())), b -> {
                DroneStationBuild s = (DroneStationBuild)b;
                float drawSize = s.block().size * tilesize / 2f + 2f;
                Drawf.select(s.x, s.y, drawSize, s.selectColor());
                if(s.acceptEnd != -1) drawArrow(s.x, s.y, s.isOrigin(), drawSize / 2f, s.selectColor());
            });

            drawConnections();

            Groups.build.each(b -> b instanceof DroneStationBuild s && (!(s.connected || s.active) || routes.contains(s.pos())), b -> {
                DroneStationBuild s = (DroneStationBuild)b;
                s.drawSelect();
            });
        }

        public void drawArrow(float x, float y, boolean up, float size, Color color){
            int s = Mathf.sign(up);
            float sh = size / 2f;

            Lines.stroke(2.5f, Pal.gray);
            for(int side : Mathf.signs){
                Lines.line(
                    x - sh * side, y - sh * s / 2f,
                    x, y + sh * s / 2f
                );
            }
            Lines.stroke(1f, color);
            for(int side : Mathf.signs){
                Lines.line(
                    x - sh * side, y - sh * s / 2f,
                    x, y + sh * s / 2f
                );
            }
            Draw.reset();
        }

        public void drawConnections(){
            float aw = arrowRegion.width / 4f + 3f,
                ah = arrowRegion.height / 4f + 3f;
            float p1 = (Time.time % 100f) / 100f, p2 = (p1 + 0.5f) % 1;
            for(int i = 0; i < maxRoutes; i++){
                DroneStationBuild o = getStation(i, 0);
                DroneStationBuild d = getStation(i, 1);
                if(o != null && d != null){
                    float oRad = o.block().size * 4f - 2f;
                    float dRad = d.block().size * 4f - 2f;
                    float ang = o.angleTo(d);

                    float calc = 1f + (1f - Mathf.sinDeg(Mathf.mod(ang, 90f) * 2)) * (Mathf.sqrt2 - 1f);
                    tr.trns(ang, (oRad / Mathf.sqrt2) * calc);
                    tr2.trns(ang + 180, (dRad / Mathf.sqrt2) * calc);

                    float ox = o.x + tr.x, oy = o.y + tr.y,
                        dx = d.x + tr2.x, dy = d.y + tr2.y;

                    float ax1 = Mathf.lerp(ox, dx, p1), ay1 = Mathf.lerp(oy, dy, p1),
                        ax2 = Mathf.lerp(ox, dx, p2), ay2 = Mathf.lerp(oy, dy, p2);


                    Lines.stroke(2.5f, Pal.gray);
                    Lines.square(o.x, o.y, oRad, 45f);
                    Lines.square(d.x, d.y, dRad, 45f);
                    Lines.line(ox, oy, dx, dy);
                    Draw.rect(arrowRegion, ax1, ay1, aw, ah, ang);
                    Draw.rect(arrowRegion, ax2, ay2, aw, ah, ang);

                    Lines.stroke(1f, o.selectColor());
                    Lines.square(o.x, o.y, oRad, 45f);
                    Lines.square(d.x, d.y, dRad, 45f);
                    Lines.line(ox, oy, dx, dy);
                    Draw.rect(arrowRegion, ax1, ay1, ang);
                    Draw.rect(arrowRegion, ax2, ay2, ang);

                    Draw.reset();
                }
            }
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.black5, t -> {
                for(int i = 0; i < maxRoutes; i++){
                    int ii = i;
                    t.add(bundle.format("pm-drone-route", i + 1)).left();
                    t.table(d -> {
                        routeSelectionButton(d, Icon.upload, "pm-drone-select-origin", ii, 0);
                        routeSelectionButton(d, Icon.download, "pm-drone-select-destination", ii, 1);
                        Image img = d.image(getIcon(ii)).size(32).get();
                        img.update(() -> {
                            TextureRegion icon = getIcon(ii);
                            ((TextureRegionDrawable)img.getDrawable()).setRegion(icon);
                            img.layout();
                            DroneStationBuild s = getEithor(ii);
                            img.setColor(s != null ? s.selectColor() : Color.white);
                        });

                        ImageButton deleteButton = d.button(
                            Icon.trash, Styles.clearTransi, () -> {
                                disconnectRoute(ii);
                                deselect();
                            }
                        ).size(40).tooltip("@pm-drone-clear-route").get();
                        deleteButton.getImageCell().size(32);
                    }).top().padLeft(6);
                    t.row();
                }
            });
        }

        public void routeSelectionButton(Table d, TextureRegionDrawable icon, String key, int route, int end){
            ImageButton destButton = d.button(
                icon, Styles.clearToggleTransi, () -> {}
            ).size(40).tooltip(t -> {
                t.setBackground(Styles.black5);
                t.label(() -> bundle.format(key, getStationName(route, end))).pad(4f);
            }).get();
            destButton.getImageCell().size(32);
            destButton.changed(() -> {
                if(selRoute == route && selEnd == end){
                    deselect();
                }else{
                    selRoute = route;
                    selEnd = end;
                }
            });
            destButton.update(() -> {
                destButton.setChecked(selRoute == route && selEnd == end);
                DroneStationBuild s = getStation(route, end);
                ImageButtonStyle is = destButton.getStyle();
                is.imageUpColor = is.imageDownColor = is.imageOverColor = s != null ? s.selectColor() : Color.white;
                destButton.layout();
            });
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(other instanceof DroneStationBuild s && selRoute >= 0 && selEnd >= 0 && s.canConnect(selEnd)){
                DroneStationBuild sel = getStation(selRoute, selEnd);
                DroneStationBuild otherEnd = getStation(selRoute, 1 - selEnd);
                if((otherEnd == null || otherEnd.block() == other.block()) && (!s.connected || routes.contains(s.pos()))){
                    if(sel != null){
                        disconnectStation(selRoute, selEnd);
                    }
                    if(sel != other){
                        connectStation(selRoute, selEnd, other.pos());
                    }
                    selEnd = -1;
                }
                return false;
            }
            deselect();
            return true;
        }

        @Override
        public boolean canPickup(){
            return false; //no
        }

        @Override
        public void remove(){
            for(int i = 0; i < maxRoutes; i++){
                disconnectRoute(i);
            }

            super.remove();
        }

        public void deselect(){
            selRoute = selEnd = -1;
        }

        public void connectStation(int route, int end, int pos){
            DroneStationBuild s = getStation(pos);
            if(s != null){
                s.connect(end);
                routes.set(route * 2 + end, pos);
                configure(routes);
            }
        }

        public void disconnectRoute(int route){
            disconnectStation(route, 0);
            disconnectStation(route, 1);
        }

        public void disconnectStation(int route, int end){
            DroneStationBuild s = getStation(route, end);
            if(s != null && routes.count(s.pos()) == 1){
                s.disconnect();
                if(drone == null || drone.curRoute != route) s.configure(-1);
            }
            routes.set(route * 2 + end, -1);
            configure(routes);
        }

        public boolean hasConnection(int route){
            return routes.get(route * 2) != -1 || routes.get(route * 2 + 1) != -1;
        }

        public DroneStationBuild getStation(int route, int end){
            return world.build(routes.get(route * 2 + end)) instanceof DroneStationBuild s ? s : null;
        }

        public DroneStationBuild getStation(int pos){
            return world.build(pos) instanceof DroneStationBuild s ? s : null;
        }

        public DroneStationBuild getEithor(int route){
            DroneStationBuild s = getStation(route, 0);
            if(s == null) s = getStation(route, 1);
            return s;
        }

        public TextureRegion getIcon(int route){
            if(hasConnection(route)){
                DroneStationBuild s = getEithor(route);

                if(s instanceof ItemDroneStationBuild){
                    return Icon.distribution.getRegion();
                }
                if(s instanceof LiquidDroneStationBuild){
                    return Icon.liquid.getRegion();
                }
                if(s instanceof PayloadDroneStationBuild){
                    return Icon.units.getRegion();
                }
            }
            return atlas.find("clear");
        }

        public String getStationName(int route, int end){
            DroneStationBuild s = getStation(route, end);
            return s != null ? " " + bundle.format("pm-drone-station", s.stationName.toString()) : "";
        }

        public int maxRoutes(){
            return maxRoutes;
        }

        public float powerUse(){
            return drone == null ? constructPowerUse : charging ? chargeRate : 0f;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.bool(constructing);
            write.bool(charging);
            write.f(build);

            write.i(routes.size);
            for(int i = 0; i < routes.size; i++){
                write.i(routes.get(i));
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            constructing = read.bool();
            charging = read.bool();
            build = read.f();

            int len = read.i();
            routes = new IntSeq();
            routes.setSize(len);
            for(int i = 0; i < len; i++){
                routes.set(i, read.i());
            }
        }
    }

    protected class DronePadConsumePower extends ConsumePower{
        public DronePadConsumePower(){
            super();
        }

        @Override
        public float requestedPower(Building entity){
            if(entity instanceof DronePadBuild s){
                return s.powerUse();
            }

            return super.requestedPower(entity);
        }
    }
}
