package progressed.entities.units.entity;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.entities.units.*;
import progressed.world.blocks.distribution.drones.DronePad.*;

import static mindustry.Vars.*;

public class DroneUnitEntity extends UnitEntity{
    public int pad, curRoute;
    public float charge;
    public boolean stopped, arrived;
    public IntSeq routes;
    public Teamc target;
    public DroneState state = DroneState.charging;

    @Override
    public void update(){
        super.update();

        if(state == DroneState.dropoff && vel.len() > 0.01f){
            charge -= Time.delta * getType().powerUse * (vel.len() / type.speed);
        }

        if(getPad() == null){
            kill(); //No pad, nothing to do.
        }
    }

    public float chargef(){
        return charge / chargeCapacity();
    }

    public DroneUnitType getType(){
        return (DroneUnitType)type;
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
        return routes.get(route * 2) != -1 && routes.get(route * 2 + 1) != -1;
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
        write.bool(stopped);
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
    }

    @Override
    public void read(Reads read){
        super.read(read);

        pad = read.i();
        curRoute = read.i();
        charge = read.f();
        stopped = read.bool();
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

        ((DronePadBuild)(world.build(pad))).drone = this;
    }

    @Override
    public int classId(){
        return PMUnitTypes.classID(DroneUnitEntity.class);
    }

    public enum DroneState{
        /** Heading to drone pad to charge */
        charging,
        /** Heading to order origin station */
        pickup,
        /** Heading to order drop off station */
        dropoff;

        public static final DroneState[] all = values();
    }
}