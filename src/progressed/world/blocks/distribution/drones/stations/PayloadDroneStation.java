package progressed.world.blocks.distribution.drones.stations;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;
import progressed.entities.units.entity.*;

import static mindustry.Vars.*;

public class PayloadDroneStation extends DroneStation{
    public float payloadSpeed = 0.7f, payloadRotateSpeed = 5f;
    public float maxPayloadSize = 3f;

    public PayloadDroneStation(String name){
        super(name);

        hasItems = hasLiquids = false;
        acceptsPayload = outputsPayload = true;
        rotate = true;
        selectColor = Pal.lightOrange;
        defName = "Payload";
    }

    @Override
    public void init(){
        super.init();

        if(maxPayloadSize < 0) maxPayloadSize = size;
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(region, req.drawx(), req.drawy());
        Draw.rect(output, req.drawx(), req.drawy(), req.rotation * 90);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.payloadCapacity, maxPayloadSize, StatUnit.blocksSquared);
    }

    @Override
    public void load(){
        super.load();

        input = Core.atlas.find(name + "-in", "factory-in-" + size);
        output = Core.atlas.find(name + "-out", "factory-out-" + size);
    }

    //Time to yoink PayloadBlock code
    public static boolean blends(Building build, int direction){
        int size = build.block.size;
        int trns = build.block.size/2 + 1;
        Building accept = build.nearby(Geometry.d4(direction).x * trns, Geometry.d4(direction).y * trns);
        return accept != null &&
            accept.block.outputsPayload &&

            //if size is the same, block must either be facing this one, or not be rotating
            ((accept.block.size == size
                && Math.abs(accept.tileX() - build.tileX()) % size == 0 //check alignment
                && Math.abs(accept.tileY() - build.tileY()) % size == 0
                && ((accept.block.rotate && accept.tileX() + Geometry.d4(accept.rotation).x * size == build.tileX() && accept.tileY() + Geometry.d4(accept.rotation).y * size == build.tileY())
                || !accept.block.rotate
                || !accept.block.outputFacing)) ||

                //if the other block is smaller, check alignment
                (accept.block.size != size &&
                    (accept.rotation % 2 == 0 ? //check orientation; make sure it's aligned properly with this block.
                        Math.abs(accept.y - build.y) <= Math.abs(size * tilesize - accept.block.size * tilesize)/2f : //check Y alignment
                        Math.abs(accept.x - build.x) <= Math.abs(size * tilesize - accept.block.size * tilesize)/2f   //check X alignment
                    )) && (!accept.block.rotate || accept.front() == build || !accept.block.outputFacing) //make sure it's facing this block
            );
    }

    public static void pushOutput(Payload payload, float progress){
        float thresh = 0.55f;
        if(progress >= thresh){
            boolean legStep = payload instanceof UnitPayload u && u.unit.type.allowLegStep;
            float size = payload.size(), radius = size/2f, x = payload.x(), y = payload.y(), scl = Mathf.clamp(((progress - thresh) / (1f - thresh)) * 1.1f);

            Groups.unit.intersect(x - size/2f, y - size/2f, size, size, u -> {
                float dst = u.dst(payload);
                float rs = radius + u.hitSize/2f;
                if(u.isGrounded() && u.type.allowLegStep == legStep && dst < rs){
                    u.vel.add(Tmp.v1.set(u.x - x, u.y - y).setLength(Math.min(rs - dst, 1f)).scl(scl));
                }
            });
        }
    }

    //CODE STEALING GO BRRRRRR
    public class PayloadDroneStationBuild extends DroneStationBuild{
        public @Nullable Payload payload;
        public float payRotation;
        public boolean carried;

        @Override
        public void updateTile(){
            super.updateTile();

            if(!loading){
                if(dumping || !isOrigin()){
                    moveOutPayload();
                    if(dumping && payload == null){
                        dumping = false;
                    }
                }else if(isOrigin()){
                    moveInPayload();
                }
            }
        }

        @Override
        public boolean payloadCheck(int conveyorRotation){
            return !isOrigin();
        }

        @Override
        public void resetLoading(){
            loaded = false;
        }

        @Override
        public void loadCargo(DroneUnitEntity d){
            super.loadCargo(d);
            d.addPayload(takePayload());
        }

        @Override
        public void takeCargo(DroneUnitEntity d){
            super.takeCargo(d);
            loadVector.trns(angleTo(loadPoint), dst(loadPoint) * load);
            if(d.payloads().any()) payload = d.payloads().first();
            d.payloads().clear();
        }

        @Override
        public boolean ready(){
            return active || connected && (isOrigin() ? payload != null && hasArrived() : payload == null);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            if(isOrigin()){
                boolean fallback = true;
                for(int i = 0; i < 4; i++){
                    if(blends(i)){
                        Draw.rect(input, x, y, (i * 90) - 180);
                        fallback = false;
                    }
                }
                if(fallback) Draw.rect(input, x, y, rotation * 90);
            }
            if(!isOrigin() || dumping){
                Draw.rect(output, x, y, rotdeg());
            }

            Draw.z(loading ? (lowFlier ? Layer.flyingUnitLow : Layer.flyingUnit) - 1 : Layer.blockOver);
            drawPayload();
        }

        public boolean acceptUnitPayload(Unit unit){
            return false;
        }

        @Override
        public boolean canControlSelect(Unit player){
            return !player.spawnedByCore && this.payload == null && acceptUnitPayload(player) && player.tileOn() != null && player.tileOn().build == this;
        }

        @Override
        public void onControlSelect(Unit player){
            float x = player.x, y = player.y;
            handleUnitPayload(player, p -> payload = p);
            this.loadVector.set(x, y).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            this.payRotation = player.rotation;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return isOrigin() && this.payload == null && payload.fits(maxPayloadSize);
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            this.payload = payload;
            this.loadVector.set(source).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            this.payRotation = payload.rotation();

            updatePayload();
        }

        @Override
        public Payload getPayload(){
            return payload;
        }

        @Override
        public void pickedUp(){
            carried = true;
        }

        @Override
        public void drawTeamTop(){
            carried = false;
        }

        @Override
        public Payload takePayload(){
            Payload t = payload;
            payload = null;
            return t;
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            if(payload != null && !carried) payload.dump();
        }

        public boolean blends(int direction){
            return PayloadBlock.blends(this, direction);
        }

        public void updatePayload(){
            if(payload != null){
                payload.set(x + loadVector.x, y + loadVector.y, payRotation);
            }
        }

        /** @return true if the payload is in position. */
        public boolean moveInPayload(){
            return moveInPayload(true);
        }

        /** @return true if the payload is in position. */
        public boolean moveInPayload(boolean rotate){
            if(payload == null) return false;

            updatePayload();

            if(rotate){
                payRotation = Angles.moveToward(payRotation, rotate ? rotdeg() : 90f, payloadRotateSpeed * edelta());
            }
            loadVector.approach(Vec2.ZERO, payloadSpeed * delta());

            return hasArrived();
        }

        public void moveOutPayload(){
            if(payload == null) return;

            updatePayload();

            Vec2 dest = Tmp.v1.trns(rotdeg(), size * tilesize/2f);

            payRotation = Angles.moveToward(payRotation, rotdeg(), payloadRotateSpeed * edelta());
            loadVector.approach(dest, payloadSpeed * delta());

            Building front = front();
            boolean canDump = front == null || !front.tile().solid();
            boolean canMove = front != null && (front.block.outputsPayload || front.block.acceptsPayload);

            if(canDump && !canMove){
                pushOutput(payload, 1f - (loadVector.dst(dest) / (size * tilesize / 2f)));
            }

            if(loadVector.within(dest, 0.001f)){
                loadVector.clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);

                if(canMove){
                    if(movePayload(payload)){
                        payload = null;
                    }
                }else if(canDump){
                    dumpPayload();
                }
            }
        }

        public void dumpPayload(){
            //translate payload forward slightly
            float tx = Angles.trnsx(payload.rotation(), 0.1f), ty = Angles.trnsy(payload.rotation(), 0.1f);
            payload.set(payload.x() + tx, payload.y() + ty, payload.rotation());

            if(payload.dump()){
                payload = null;
            }else{
                payload.set(payload.x() - tx, payload.y() - ty, payload.rotation());
            }
        }

        public boolean hasArrived(){
            return loadVector.isZero(0.01f);
        }

        public void drawPayload(){
            if(payload != null){
                updatePayload();
                payload.draw();
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(payRotation);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            payRotation = read.f();
            payload = Payload.read(read);
        }
    }
}