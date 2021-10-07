package progressed.entities.units.entity;

import arc.math.*;
import arc.struct.*;
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

        if(getPad() == null){
            kill(); //No pad, nothing to do.
        }
    }

    public float chargeCapacity(){
        return ((DroneUnitType)type).chargeCapacity;
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

    @Override
    public boolean isAI(){
        return false; //Make game think it's not an AI so that players can't control it.
    }

    public float estimateUse(int route){
        Building origin = world.build(routes.get(route * 2));
        Building destination = world.build(routes.get(route * 2 + 1));
        float toOrigin = dst(origin);
        float toDestination = origin.dst(destination);
        return ((toOrigin + toDestination) / type.speed) * ((DroneUnitType)(type)).powerUse;
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
        charging, //Heading to pad
        origin, //Heading to start point
        destination; //Heading to end point

        public static final DroneState[] all = values();
    }
}