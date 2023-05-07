package progressed.world.blocks.sandbox.defence;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.ui.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class SandboxWall extends Block{
    protected static final Vec2 configVec = new Vec2();

    public final int DPSUpdateTime = timers++;
    public float resetTime = 120f;
    public Color lightningColor = Pal.surge;
    public Sound lightningSound = Sounds.spark;
    public boolean flashHit = true;
    public Color flashColor = Color.white;
    public Sound deflectSound = Sounds.none;
    public TextureRegion colorRegion, lightningRegion, deflectRegion, insulatingRegion, armorRegion;

    public SandboxWall(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        solid = true;
        destructible = true;
        placeableLiquid = true;
        group = BlockGroup.walls;
        canOverdrive = false;
        drawDisabled = false;
        priority = TargetPriority.wall;

        //it's a wall of course it's supported everywhere
        envEnabled = Env.any;

        schematicPriority = 10;
        configurable = saveConfig = update = noUpdateDisabled = true;

        config(int[].class, (SandboxWallBuild tile, int[] data) -> tile.data.readIntArray(data));
        config(Integer.class, (SandboxWallBuild tile, Integer i) -> {
            tile.toggle(i);
            if(i == 1 && tile.deflection()){
                tile.hit = 0f;
            }
        });
        config(Vec2.class, (SandboxWallBuild tile, Vec2 data) -> {
            tile.data.configure((int)data.x, data.y);
        });

        configClear(SandboxWallBuild::resetModes);
    }

    @Override
    public void load(){
        super.load();

        colorRegion = Core.atlas.find(name + "-color");

        lightningRegion = Core.atlas.find(name + "-lightning");
        deflectRegion = Core.atlas.find(name + "-deflection");
        insulatingRegion = Core.atlas.find(name + "-insulating");
        armorRegion = Core.atlas.find(name + "-armor");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.health);

        stats.add(Stat.health, t -> t.add(PMElements.infinity()));
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("health");
    }

    @Override
    public void drawPlanConfig(BuildPlan req, Eachable<BuildPlan> list){
        if(req.config instanceof int[] modes){
            //draw floating items to represent active mode
            if(modes[0] == 1){
                Draw.rect(lightningRegion, req.drawx(), req.drawy());
            }
            if(modes[1] == 1){
                Draw.rect(deflectRegion, req.drawx(), req.drawy());
            }
            if(modes[2] == 1){
                Draw.rect(insulatingRegion, req.drawx(), req.drawy());
            }
            if(modes[3] == 1 && Float.intBitsToFloat(modes[8]) > 0){
                Draw.rect(armorRegion, req.drawx(), req.drawy());
            }
        }
    }

    public class SandboxWallBuild extends Building{
        public float hit;
        public float total, reset = resetTime, time, DPS;
        public SandboxWallData data = new SandboxWallData();

        @Override
        public void updateTile(){
            super.updateTile();

            if(DPSTesting()){
                time += Time.delta;
                reset += Time.delta;

                if(timer(DPSUpdateTime, 20)) DPS = total / time * 60f;

                if(reset >= resetTime){
                    total = 0f;
                    time = 0f;
                    DPS = 0f;
                }
            }
        }

        @Override
        public void draw(){
            float speed = Core.settings.getInt("pm-strobespeed") / 2f;

            if(variants == 0){
                Draw.rect(region, x, y);
                Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
                Draw.rect(colorRegion, x, y);
            }
            Draw.reset();

            //draw flashing white overlay if enabled
            if(flashHit && deflection() && hit >= 0.0001f){
                Draw.color(flashColor);
                Draw.alpha(hit * 0.5f);
                Draw.blend(Blending.additive);
                Fill.rect(x, y, tilesize * size, tilesize * size);
                Draw.blend();
                Draw.reset();

                hit = Mathf.clamp(hit - Time.delta / 10f);
            }

            if(lightning()){
                Draw.rect(lightningRegion, x, y);
            }
            if(deflection()){
                Draw.rect(deflectRegion, x, y);
            }
            if(insulating()){
                Draw.rect(insulatingRegion, x, y);
            }
            if(DPSTesting()){
                if(armored()){
                    Draw.rect(armorRegion, x, y);
                }

                Draw.z(Layer.overlayUI);
                float dm = state.rules.blockHealth(team);
                String text = (time > 0 ? (Mathf.zero(dm) ? "Infinity" : PMUtls.round(DPS)) : "---") + " DPS";
                PMDrawf.text(x, y, false, size * tilesize, team.color, text);
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            float damage = bullet.damage() * bullet.type().buildingDamageMultiplier;
            if(!bullet.type.pierceArmor){
                damage = Damage.applyArmor(damage, data.armor);
            }

            damage(bullet.team, damage);
            Events.fire(bulletDamageEvent.set(self(), bullet));

            hit = 1f;

            //create lightning if necessary
            if(lightning() && Mathf.chance(data.lightningChance)){
                Lightning.create(team, lightningColor, data.lightningDamage, x, y, bullet.rotation() + 180f, data.lightningLength);
                lightningSound.at(tile, Mathf.random(0.9f, 1.1f));
            }

            //deflect bullets if necessary
            if(deflection()){
                //slow bullets are not deflected
                if(bullet.vel().len() <= 0.1f || !bullet.type.reflectable) return true;

                //bullet reflection chance depends on bullet damage
                if(!Mathf.chance(data.deflectChance / bullet.damage())) return true;

                //make sound
                deflectSound.at(tile, Mathf.random(0.9f, 1.1f));

                //translate bullet back to where it was upon collision
                bullet.trns(-bullet.vel.x, -bullet.vel.y);

                float penX = Math.abs(x - bullet.x), penY = Math.abs(y - bullet.y);

                if(penX > penY){
                    bullet.vel.x *= -1;
                }else{
                    bullet.vel.y *= -1;
                }

                bullet.owner(this);
                bullet.team(team);
                bullet.time(bullet.time() + 1f);

                //disable bullet collision by returning false
                return false;
            }

            return true;
        }

        @Override
        public boolean absorbLasers(){
            return insulating();
        }

        @Override
        public boolean isInsulated(){
            return insulating();
        }

        @Override
        public void damage(float damage){
            float dm = state.rules.blockHealth(team);
            lastDamageTime = Time.time;

            if(!DPSTesting()) return;
            reset = 0f;

            total += Mathf.zero(dm) ? 1 : damage / dm;
        }

        @Override
        public void kill(){
            //haha no
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                t.background(Styles.black6);
                t.defaults().left();
                t.table(i -> {
                    i.defaults().left();
                    i.image(Icon.power.getRegion()).size(32f).scaling(Scaling.fit);
                    i.add("@pm-sandbox-wall.mode-lightning").padLeft(8f);
                });
                t.row();
                t.table(b -> {
                    b.defaults().left();
                    addToggleButton(b, 0);
                    b.row();
                    b.table(f -> {
                        f.defaults().left();
                        addTextField(f, addColon("pm-sandbox-wall.lightning.chance"), "" + data.lightningChance, TextFieldFilter.floatsOnly, 4);
                        f.row();
                        addTextField(f, addColon("pm-sandbox-wall.lightning.damage"), "" + data.lightningDamage, TextFieldFilter.floatsOnly, 5);
                        f.row();
                        addTextField(f, addColon("pm-sandbox-wall.lightning.length"), "" + data.lightningLength, TextFieldFilter.digitsOnly, 6);
                        f.row();
                    });
                }).padLeft(32f);
                t.row();

                t.table(i -> {
                    i.defaults().left();
                    i.image(Icon.rotate.getRegion()).size(32f).scaling(Scaling.fit);
                    i.add("@pm-sandbox-wall.mode-deflection").padLeft(8f);
                });
                t.row();
                t.table(b -> {
                    b.defaults().left();
                    addToggleButton(b, 1);
                    b.row();
                    b.table(f -> {
                        f.defaults().left();
                        addTextField(f, addColon("pm-sandbox-wall.deflection.chance"), "" + data.deflectChance, TextFieldFilter.floatsOnly, 7);
                    });
                }).padLeft(32f);
                t.row();

                t.table(i -> {
                    i.defaults().left();
                    i.image(Icon.eyeOff.getRegion()).size(32f).scaling(Scaling.fit);
                    i.add("@pm-sandbox-wall.mode-insulation").padLeft(8f);
                });
                t.row();
                t.table(b -> {
                    b.defaults().left();
                    addToggleButton(b, 2);
                }).padLeft(32f);
                t.row();

                t.table(i -> {
                    i.defaults().left();
                    i.image(Icon.modePvp.getRegion()).size(32f).scaling(Scaling.fit);
                    i.add("@pm-sandbox-wall.mode-dpstesting").padLeft(8f);
                });
                t.row();
                t.table(b -> {
                    b.defaults().left();
                    addToggleButton(b, 3);
                    b.row();
                    b.table(f -> {
                        f.defaults().left();
                        addTextField(f, addColon("stat.armor"), "" + data.armor, TextFieldFilter.floatsOnly, 8);
                    });
                }).padLeft(32f);
            }).top().grow().margin(8f);
        }

        public void addToggleButton(Table t, int mode){
            CheckBox box = new CheckBox("@mod.enable");
            box.changed(() -> configure(mode));
            box.setChecked(data.activeMode(mode));
            box.update(() -> box.setChecked(data.activeMode(mode)));
            box.left();
            t.add(box);
        }

        public void addTextField(Table t, String title, String text, TextFieldFilter filter, int mode){
            t.add(title);
            t.field(text, filter, s -> configure(configVec.set(mode, Strings.parseFloat(s)))).width(200f).padLeft(8f);
        }

        public String addColon(String entry){
            return Core.bundle.get(entry) + ":";
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(this == other){
                deselect();
                resetModes();
                return false;
            }

            return true;
        }

        @Override
        public Object config(){
            return data.toIntArray();
        }

        public void toggle(int i){
            data.toggle(i);

            //Reset DPS testing
            if(i == 3 && DPSTesting()){
                total = 0f;
                time = 0f;
                DPS = 0f;
                reset = resetTime;
            }
        }

        public boolean lightning(){
            return data.lightning && data.lightningChance > 0f;
        }

        public boolean deflection(){
            return data.deflecting && data.deflectChance > 0f;
        }

        public boolean insulating(){
            return data.insulated;
        }

        public boolean DPSTesting(){
            return data.dpsTesting;
        }

        public boolean armored(){
            return data.dpsTesting && data.armor > 0f;
        }

        public void resetModes(){
            data.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            
            data.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 4){
                data.read(read);
                return;
            }

            //Discard old data
            if(revision == 3){
                TypeIO.readInts(read);
                return;
            }

            byte[] oldModes = new byte[4];
            if(revision == 1){
                read.b(oldModes, 0, 3);
            }
            if(revision == 2){
                read.b(oldModes);
            }
        }

        @Override
        public byte version(){
            return 4;
        }
    }

    public static class SandboxWallData{
        public boolean lightning, deflecting, insulated, dpsTesting;
        public float lightningChance = 0.05f, lightningDamage = 20f;
        public int lightningLength = 17;
        public float deflectChance = 10f;
        public float armor;

        public boolean activeMode(int mode){
            return switch(mode){
                case 0 -> lightning;
                case 1 -> deflecting;
                case 2 -> insulated;
                case 3 -> dpsTesting;
                default -> false;
            };
        }

        public void toggle(int mode){
            switch(mode){
                case 0 -> lightning = !lightning;
                case 1 -> deflecting = !deflecting;
                case 2 -> insulated = !insulated;
                case 3 -> dpsTesting = !dpsTesting;
            };
        }

        public void reset(){
            lightning = deflecting = insulated = dpsTesting = false;
        }

        public int[] toIntArray(){
            return new int[]{
                Mathf.num(lightning), Mathf.num(deflecting), Mathf.num(insulated), Mathf.num(dpsTesting),
                Float.floatToIntBits(lightningChance), Float.floatToIntBits(lightningDamage), lightningLength,
                Float.floatToIntBits(deflectChance),
                Float.floatToIntBits(armor)
            };
        }

        public void readIntArray(int[] data){
            if(data.length != 9) return;

            lightning = !Mathf.booleans[data[0]];
            deflecting = !Mathf.booleans[data[1]];
            insulated = !Mathf.booleans[data[2]];
            dpsTesting = !Mathf.booleans[data[3]];

            lightningChance = Float.intBitsToFloat(data[4]);
            lightningDamage = Float.intBitsToFloat(data[5]);
            lightningLength = data[6];

            deflectChance = Float.intBitsToFloat(data[7]);

            armor = Float.intBitsToFloat(data[8]);
        }

        public void configure(int value, float data){
            switch(value){
                case 4 -> lightningChance = data;
                case 5 -> lightningDamage = data;
                case 6 -> lightningLength = (int)data;

                case 7 -> deflectChance = data;

                case 8 -> armor = data;
            }
        }

        public void write(Writes write){
            write.bool(lightning);
            write.bool(deflecting);
            write.bool(insulated);
            write.bool(dpsTesting);

            write.f(lightningChance);
            write.f(lightningDamage);
            write.i(lightningLength);

            write.f(deflectChance);

            write.f(armor);
        }

        public void read(Reads read){
            lightning = read.bool();
            deflecting = read.bool();
            insulated = read.bool();
            dpsTesting = read.bool();

            lightningChance = read.f();
            lightningDamage = read.f();
            lightningLength = read.i();

            deflectChance = read.f();

            armor = read.f();
        }
    }
}
