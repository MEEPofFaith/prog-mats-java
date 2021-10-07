package progressed.world.blocks.distribution.drones;

import arc.graphics.g2d.*;
import arc.math.*;
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

import java.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class DronePad extends Block{
    public int maxRoutes = 5;
    public float constructTime = 180f;
    public float constructPowerUse = 1f;
    public float chargeRate = 12f;
    public DroneUnitType droneType;

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

    public class DronePadBuild extends Building{
        public int selRoute = -1, selEnd = -1;
        public float progress, warmup, totalProgress;
        public boolean loadUnit;
        public float ux, uy;
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
                    if(o != null){
                        o.configure(true); //Keep locked as long as at least 1 controller uses it.
                    }else{
                        disconnectStation(i, j);
                    }
                }
            }

            if(loadUnit){
                drone = (DroneUnitEntity)Groups.unit.find(e -> Mathf.equal(e.x, ux) && Mathf.equal(e.y, uy));
                loadUnit = false;
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
                        if(s.accepting()){
                            Draw.rect(Icon.upload.getRegion(), s.x, s.y, drawSize, drawSize);
                        }else{
                            Draw.rect(Icon.download.getRegion(), s.x, s.y, drawSize, drawSize);
                        }
                    }
                    s.drawSelect();
                }
            });

            drawConnections();
        }

        @Override
        public void drawConfigure(){
            Drawf.select(x, y, size * tilesize / 2f + 2f + Mathf.absin(Time.time, 4f, 1f), team.color);

            Groups.build.each(b -> b instanceof DroneStationBuild s && (!s.connected || routes.contains(s.pos())), b -> {
                DroneStationBuild s = (DroneStationBuild)b;
                float drawSize = s.block().size * tilesize / 2f + 2f;
                Drawf.select(s.x, s.y, drawSize, s.selectColor());
                if(s.connected){
                    if(s.accepting()){
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
            for(int i = 0; i < maxRoutes; i++){
                DroneStationBuild o = getStation(i, 0);
                DroneStationBuild d = getStation(i, 1);
                if(o != null && d != null){
                    Drawf.dashLine(team.color, o.x, o.y, d.x, d.y);
                    float dist = o.dst(d);
                    for(int j = 0; j < 3; j++){
                        Drawf.arrow(o.x, o.y, d.x, d.y, dist / 4f * (j + 1), 3f, team.color);
                    }
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
                t.label(() -> bundle.format(key, getStationName(route, end)));
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
                s.configure(true); //Lock
                s.configure(end); //Enable acceptance for origin
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
                s.configure(false); //Unlock
                s.configure(-1); //Disable acceptance
            }
            routes.set(route * 2 + end, -1);
            configure(routes);
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

            write.bool(drone != null);
            write.f(drone.x);
            write.f(drone.y);
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

            loadUnit = read.bool();
            if(loadUnit){
                ux = read.f();
                uy = read.f();
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
