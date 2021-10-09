package progressed.entities.units.entity;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import progressed.content.*;
import progressed.entities.units.*;
import progressed.world.blocks.distribution.drones.DronePad.*;
import progressed.world.blocks.distribution.drones.stations.DroneStation.*;

import java.util.*;

import static mindustry.Vars.*;

public class DroneUnitEntity extends PayloadUnit{
    public int pad, curRoute;
    public float charge, load;
    public boolean arrived;
    public IntSeq routes;
    public Teamc target;
    public DroneState state = DroneState.idle;
    public DroneCargo cargo = new DroneCargo();

    @Override
    public void update(){
        if(state == DroneState.dropoff && vel.len() > 0.01f){
            charge -= Time.delta * getType().powerUse * (vel.len() / type.speed);
        }

        if(getPad() == null){
            kill(); //No pad, nothing to do.
        }

        super.update();
    }

    public float chargef(){
        return charge / chargeCapacity();
    }

    public DroneUnitType getType(){
        return (DroneUnitType)type;
    }

    public float loadSpeed(){
        return getType().loadSpeed;
    }

    public float chargeCapacity(){
        return getType().chargeCapacity;
    }

    public void recharge(float amount){
        charge = Mathf.clamp(charge + amount, 0, chargeCapacity());
    }

    public boolean charged(){
        return charge >= chargeCapacity();
    }

    public DronePadBuild getPad(){
        return world.build(pad) instanceof DronePadBuild d ? d : null;
    }

    public void updateRoutes(){
        routes = new IntSeq(getPad().routes);
    }

    public void nextRoute(){
        curRoute++;
        if(curRoute >= getPad().maxRoutes()){
            curRoute = 0;
        }
    }

    public boolean hasRoutes(){
        for(int i = 0; i < getPad().maxRoutes(); i++){
            if(checkCompleteRoute(i)) return true;
        }
        return false;
    }

    public boolean checkCompleteRoute(int route){
        return (routes.get(route * 2) != -1 && getStation(route, 0).ready()) && (routes.get(route * 2 + 1) != -1 && getStation(route, 1).ready());
    }

    public DroneStationBuild getStation(int route, int end){
        return (DroneStationBuild)(world.build(routes.get(route * 2 + end)));
    }

    public DroneStationBuild getStation(){
        return switch(state){
            case pickup -> getStation(curRoute, 0);
            case dropoff -> getStation(curRoute, 1);
            default -> null;
        };
    }

    @Override
    public boolean isAI(){
        return false; //Make game think it's not an AI so that players can't control it.
    }

    public float estimateUse(int route){
        Building origin = world.build(routes.get(route * 2));
        Building destination = world.build(routes.get(route * 2 + 1));
        return (origin.dst(destination) / type.speed) * ((DroneUnitType)(type)).powerUse;
    }

    @Override
    public void write(Writes write){
        super.write(write);

        write.i(pad);
        write.i(curRoute);
        write.f(charge);
        write.f(load);
        write.bool(arrived);
        write.b((byte)state.ordinal());

        write.i(routes.size);
        for(int i = 0; i < routes.size; i++){
            write.i(routes.get(i));
        }

        write.bool(target != null);
        if(target != null){
            write.f(target.x());
            write.f(target.y());
        }

        cargo.write(write);
    }

    @Override
    public void read(Reads read){
        super.read(read);

        pad = read.i();
        curRoute = read.i();
        charge = read.f();
        load = read.f();
        arrived = read.bool();
        state = DroneState.all[read.b()];

        int len = read.i();
        routes = new IntSeq();
        routes.setSize(len);
        for(int i = 0; i < len; i++){
            routes.set(i, read.i());
        }

        if(read.bool()){
            target = world.buildWorld(read.f(), read.f());
        }

        cargo.read(read);

        ((DronePadBuild)(world.build(pad))).drone = this;
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(DroneUnitEntity.class);
    }

    public static class DroneCargo{
        public int[] itemCargo = new int[content.items().size];
        public LiquidStack liquidCargo;
        public float liquidCapacity;

        public DroneCargo(){}

        public void load(int[] items){
            itemCargo = items;
        }

        public void load(LiquidStack liquid){
            liquidCargo = liquid;
        }

        public boolean hasItems(){
            for(int item : itemCargo){
                if(item > 0) return true;
            }
            return false;
        }

        public boolean hasLiquid(){
            return liquidCargo != null;
        }

        public void empty(){
            Arrays.fill(itemCargo, 0);
            liquidCargo = null;
            liquidCapacity = 0;
        }

        public void write(Writes write){
            int amount = 0;
            for(int item : itemCargo){
                if(item > 0) amount++;
            }

            write.s(amount); //amount of items

            for(int i = 0; i < itemCargo.length; i++){
                if(itemCargo[i] > 0){
                    write.s(i); //item ID
                    write.i(itemCargo[i]); //item amount
                }
            }

            write.bool(liquidCargo != null);
            if(liquidCargo != null){
                write.s(liquidCargo.liquid.id);
                write.f(liquidCargo.amount);
            }
            write.f(liquidCapacity);
        }

        public void read(Reads read){
            //just in case, reset items
            Arrays.fill(itemCargo, 0);
            int count = read.s();

            for(int j = 0; j < count; j++){
                int itemid = read.s();
                int itemamount = read.i();
                itemCargo[content.item(itemid).id] = itemamount;
            }

            if(read.bool()){
                int liquidId = read.s();
                float liquidAmount = read.f();
                liquidCargo = new LiquidStack(content.liquid(liquidId), liquidAmount);
            }
            liquidCapacity = read.f();
        }
    }

    public enum DroneState{
        /** 0 - Heading to drone pad to charge */
        charging,
        /** 1 - Heading to order origin station */
        pickup,
        /** 2 - Heading to order drop off station */
        dropoff,
        /** 3 - No routes are ready; return to pad to charge */
        idle;

        public static final DroneState[] all = values();
    }
}