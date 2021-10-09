package progressed.world.blocks.distribution.drones;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
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
    public int maxRoutes = 5;
    public float constructTime = 180f;
    public float constructPowerUse = 1f;
    public float chargeRate = 3f;
    public DroneUnitType droneType;

    public TextureRegion arrowRegion;

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
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("drone-charge", (DronePadBuild entity) -> new Bar("pm-drone-pad-charge", Pal.powerBar, entity::chargef));
    }

    public class DronePadBuild extends Building{
        public int selRoute = -1, selEnd = -1;
        public float progress, warmup, totalProgress;
        public boolean constructing, charging;
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

            warmup = Mathf.lerpDelta(warmup, Mathf.num(constructing), 0.15f);

            for(int i = 0; i < maxRoutes; i++){
                for(int j = 0; j < 2; j++){
                    DroneStationBuild o = getStation(i, j);
                    if(o == null){
                        disconnectStation(i, j);
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
                progress += edelta();
                totalProgress += edelta();
                if(progress >= constructTime){
                    progress = 0f;
                    constructing = false;
                    drone = (DroneUnitEntity)droneType.spawn(team, this);
                    drone.rotation(90);
                    drone.pad = pos();
                    drone.updateRoutes();
                    drone.charge = ((DroneUnitType)(drone.type)).chargeCapacity;
                }
            }
        }

        public float chargef(){
            return drone != null ? drone.chargef() : 0f;
        }

        @Override
        public void draw(){
            super.draw();

            Draw.draw(Layer.blockOver, () -> {
                Drawf.construct(x, y, droneType.fullIcon, team.color, 0f, progress, 1f, totalProgress);
            });
        }

        @Override
        public void drawSelect(){
            routes.each(r -> {
                DroneStationBuild s = getStation(r);
                if(s != null){
                    float drawSize = s.block().size * tilesize / 2f + 2f;
                    Drawf.select(s.x, s.y, drawSize, s.selectColor());
                    if(s.connected){
                        if(s.isOrigin()){
                            Draw.rect(Icon.upload.getRegion(), s.x, s.y, drawSize, drawSize);
                        }else{
                            Draw.rect(Icon.download.getRegion(), s.x, s.y, drawSize, drawSize);
                        }
                    }
                    s.drawSelect();
                }
            });

            drawConnections();

            if(drone != null){
                Draw.z(Layer.overlayUI);
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
                if(s.connected){
                    if(s.isOrigin()){
                        Draw.rect(Icon.upload.getRegion(), s.x, s.y, drawSize, drawSize);
                    }else{
                        Draw.rect(Icon.download.getRegion(), s.x, s.y, drawSize, drawSize);
                    }
                }
                s.drawSelect();
            });

            drawConnections();
        }

        public void drawConnections(){
            Draw.z(Layer.power);
            float aw = arrowRegion.width / 4f + 3f,
                ah = arrowRegion.height / 4f + 3f;
            float p1 = (Time.time % 100f) / 100f, p2 = (p1 + 0.5f) % 1;
            for(int i = 0; i < maxRoutes; i++){
                DroneStationBuild o = getStation(i, 0);
                DroneStationBuild d = getStation(i, 1);
                if(o != null && d != null){
                    float oRad = o.block().size * 2;
                    float dRad = d.block().size * 2;
                    float ang = o.angleTo(d);

                    tr.trns(ang, oRad);
                    tr2.trns(ang + 180, dRad);

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
            table.setBackground(Styles.black5);
            for(int i = 0; i < maxRoutes; i++){
                int ii = i;
                table.add(bundle.format("pm-drone-route", i + 1)).left();
                table.table(d -> {
                    routeSelectionButton(d, Icon.upload, "pm-drone-select-origin", ii, 0);
                    routeSelectionButton(d, Icon.download, "pm-drone-select-destination", ii, 1);
                    d.image(() -> {
                        if(hasConnection(ii)){
                            DroneStationBuild s = getStation(ii, 0);
                            if(s == null) s = getStation(ii, 1);

                            if(s instanceof ItemDroneStationBuild){
                                return ui.getIcon("item").getRegion(); //TODO why don't you exist
                            }
                            if(s instanceof LiquidDroneStationBuild){
                                return Icon.liquid.getRegion();
                            }
                            if(s instanceof PayloadDroneStationBuild){
                                return Icon.units.getRegion();
                            }
                        }
                        return atlas.find("clear");
                    }).size(32);

                    ImageButton deleteButton = d.button(
                        Icon.trash, Styles.clearTransi, () -> disconnectRoute(ii)
                    ).size(40).tooltip("@pm-drone-clear-route").get();
                    deleteButton.getImageCell().size(32);
                }).top().padLeft(6);
                table.row();
            }
        }

        public void routeSelectionButton(Table d, TextureRegionDrawable icon, String key, int route, int end){
            ImageButton destinationButton = d.button(
                icon, Styles.clearToggleTransi, () -> {}
            ).size(40).tooltip(t -> {
                t.setBackground(Styles.black5);
                t.label(() -> bundle.format(key, getStationName(route, end))).pad(8f);
            }).get();
            destinationButton.getImageCell().size(32);
            destinationButton.changed(() -> {
                if(selRoute == route && selEnd == end){
                    selRoute = -1;
                    selEnd = -1;
                }else{
                    selRoute = route;
                    selEnd = end;
                }
            });
            destinationButton.update(() -> destinationButton.setChecked(selRoute == route && selEnd == end));
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
                }
                return false;
            }
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
            write.f(progress);

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
            progress = read.f();

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
