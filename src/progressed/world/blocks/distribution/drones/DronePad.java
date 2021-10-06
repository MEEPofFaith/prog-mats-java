package progressed.world.blocks.distribution.drones;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import progressed.world.blocks.distribution.drones.stations.DroneStation.*;

import java.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class DronePad extends Block{
    public int maxRoutes = 5;

    public DronePad(String name){
        super(name);

        update = true;
        configurable = true;

        config(IntSeq.class, (DronePadBuild b, IntSeq i) -> b.routes = IntSeq.with(i.toArray()));
    }

    public class DronePadBuild extends Building{
        public int selRoute = -1, selEnd = -1;
        public IntSeq routes;

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
        }

        @Override
        public void drawSelect(){
            routes.each(r -> {
                DroneStationBuild s = getStation(r);
                if(s != null){
                    float drawSize = s.block().size * tilesize / 2f + 2f;
                    Drawf.select(s.x, s.y, drawSize, team.color);
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

            Groups.build.each(b -> b instanceof DroneStationBuild, b -> {
                DroneStationBuild s = (DroneStationBuild)b;
                float drawSize = s.block().size * tilesize / 2f + 2f;
                Drawf.select(s.x, s.y, drawSize, team.color);
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
                    ImageButton originButton = d.button(
                        Icon.upload, Styles.clearToggleTransi, () -> {}
                    ).size(40).tooltip(t -> {
                        t.setBackground(Styles.black5);
                        t.label(() -> bundle.format("pm-drone-select-origin", getStationName(ii, 0)));
                    }).get();
                    originButton.getImageCell().size(32);
                    originButton.changed(() -> {
                        if(selRoute == ii && selEnd == 0){
                            selRoute = -1;
                            selEnd = -1;
                        }else{
                            selRoute = ii;
                            selEnd = 0;
                        }
                    });
                    originButton.update(() -> originButton.setChecked(selRoute == ii && selEnd == 0));

                    ImageButton destinationButton = d.button(
                        Icon.download, Styles.clearToggleTransi, () -> {}
                    ).size(40).tooltip(t -> {
                        t.setBackground(Styles.black5);
                        t.label(() -> bundle.format("pm-drone-select-origin", getStationName(ii, 1)));
                    }).get();
                    destinationButton.getImageCell().size(32);
                    destinationButton.changed(() -> {
                        if(selRoute == ii && selEnd == 1){
                            selRoute = -1;
                            selEnd = -1;
                        }else{
                            selRoute = ii;
                            selEnd = 1;
                        }
                    });
                    destinationButton.update(() -> destinationButton.setChecked(selRoute == ii && selEnd == 1));

                    ImageButton deleteButton = d.button(
                        Icon.trash, Styles.clearTransi, () -> disconnectRoute(ii)
                    ).size(40).tooltip("@pm-drone-clear-route").get();
                    deleteButton.getImageCell().size(32);
                }).top().padLeft(6);
                table.row();
            }
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
            if(other instanceof DroneStationBuild s && selRoute >= 0 && selEnd >= 0 && s.canConnect(selEnd)){
                DroneStationBuild sel = getStation(selRoute, selEnd);
                if(sel != null){
                    disconnectStation(selRoute, selEnd);
                }
                if(sel != other){
                    connectStation(selRoute, selEnd, other.pos());
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

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(routes.size);
            for(int i = 0; i < routes.size; i++){
                write.i(routes.get(i));
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            for(int i = 0, n = read.i(); i < n; i++){
                routes.set(i, read.i());
            }
        }
    }
}
