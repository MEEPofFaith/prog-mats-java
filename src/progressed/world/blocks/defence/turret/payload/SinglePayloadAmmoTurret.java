package progressed.world.blocks.defence.turret.payload;

import arc.math.geom.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.payloads.*;
import progressed.world.draw.*;

import static mindustry.Vars.*;

public class SinglePayloadAmmoTurret extends PayloadAmmoTurret{
    public float payloadSpeed = 0.7f;
    public float minLoadWarmup = 0f;

    public SinglePayloadAmmoTurret(String name){
        super(name);

        maxAmmo = 1;
        linearWarmup = true;

        drawer = new DrawPayloadTurret();
    }

    public class SinglePayloadAmmoTurretBuild extends PayloadTurretBuild{
        public Payload payload;
        public Vec2 payVector = new Vec2();

        @Override
        public void updateTile(){
            if(moveInPayload()){
                payloads.add(payload.content());
                payload = null;
            }

            super.updateTile();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.payload == null && shootWarmup <= minLoadWarmup && super.acceptPayload(source, payload);
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            this.payload = payload;
            this.payVector.set(source).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
        }

        public void updatePayload(){
            if(payload != null){
                payload.set(x + payVector.x, y + payVector.y, payload.rotation());
            }
        }

        public boolean moveInPayload(){
            if(payload == null) return false;

            updatePayload();

            payVector.approach(Vec2.ZERO, payloadSpeed * delta());

            return hasArrived();
        }

        public boolean hasArrived(){
            return payVector.isZero(0.01f);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(payVector.x);
            write.f(payVector.y);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                payVector.set(read.f(), read.f());
                payload = Payload.read(read);
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}
