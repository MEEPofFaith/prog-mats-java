package progressed.world.blocks.defence.turret.payload;

import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;
import progressed.world.blocks.payloads.*;
import progressed.world.draw.*;
import progressed.world.meta.*;

import static mindustry.Vars.*;

public class SinglePayloadAmmoTurret extends PayloadAmmoTurret{
    public float payloadSpeed = 0.7f;
    public float minLoadWarmup = 1f;

    public SinglePayloadAmmoTurret(String name){
        super(name);

        maxAmmo = 1;
        shootEffect = smokeEffect = Fx.none;
        outlinedIcon = 3;

        drawer = new DrawPayloadTurret(true);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, PMStatValues.ammo(ammoTypes, true));
    }

    public void setWarmupTime(float seconds){
        minLoadWarmup = 0f;
        minWarmup = 1f;
        linearWarmup = true;
        shootWarmupSpeed = 1 / (seconds * 60f);
    }

    public void setUsers(){
        for(var entry : ammoTypes.copy().entries()){
            if(entry.key instanceof Missile m){
                m.user = this;
                m.bullet = entry.value;
            }
        }
    }

    public class SinglePayloadAmmoTurretBuild extends PayloadTurretBuild{
        public Payload payload;
        public float payLen;
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
            payLen = payVector.len();
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

        public float payloadf(){
            return payVector.len() / payLen;
        }

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);

            if(minLoadWarmup < 0.999f){ //Ensure that it doesn't attempt to load before resetting.
                shootWarmup -= shootWarmupSpeed * Time.delta;
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(payVector.x);
            write.f(payVector.y);
            write.f(payLen);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                payVector.set(read.f(), read.f());
                payLen = read.f();
                payload = Payload.read(read);
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }
}
