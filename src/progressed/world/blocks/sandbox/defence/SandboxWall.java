package progressed.world.blocks.sandbox.defence;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.ui.*;

import java.util.*;

import static mindustry.Vars.*;

public class SandboxWall extends Wall{
    public final int DPSUpdateTime = timers++;
    public float rotateSpeed = 6f, rotateRadius = 2.5f, iconSize = 3f;
    public float resetTime = 120f;

    protected String[] buttonLabels = {"@pm-sandbox-wall-mode.sparking", "@pm-sandbox-wall-mode.reflecting", "@pm-sandbox-wall-mode.insulating", "@pm-sandbox-wall-mode.DPS-testing"};
    public TextureRegion colorRegion, sparkRegion, reflectRegion, insulatingRegion, armorRegion;

    public SandboxWall(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        lightningDamage = 5000f;
        lightningLength = 10;
        flashHit = insulated = absorbLasers = true;
        schematicPriority = 10;
        configurable = saveConfig = update = noUpdateDisabled = true;

        config(int[].class, (SandboxWallBuild tile, int[] b) -> tile.modes = b.clone());
        config(Integer.class, (SandboxWallBuild tile, Integer i) -> {
            tile.toggle(i);
            if(i == 1 && tile.reflecting()){
                tile.hit = 0f;
            }
            if(i == 3){
                tile.rebuild();
            }
        });
        config(Float.class, (SandboxWallBuild tile, Float f) -> {
            tile.modes[4] = Mathf.floorPositive(f);
        });

        configClear(SandboxWallBuild::resetModes);
    }

    @Override
    public void load(){
        super.load();

        colorRegion = Core.atlas.find(name + "-color");

        sparkRegion = Core.atlas.find(name + "-sparking");
        reflectRegion = Core.atlas.find(name + "-reflecting");
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
                Draw.rect(sparkRegion, req.drawx(), req.drawy());
            }
            if(modes[1] == 1){
                Draw.rect(reflectRegion, req.drawx(), req.drawy());
            }
            if(modes[2] == 1){
                Draw.rect(insulatingRegion, req.drawx(), req.drawy());
            }
            if(modes[3] == 1 && modes[4] > 0){
                Draw.rect(armorRegion, req.drawx(), req.drawy());
            }
        }
    }

    public class SandboxWallBuild extends WallBuild{
        public float total, reset = resetTime, time, DPS;
        public int[] modes = new int[5];
        public Table gui = new Table();

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
            if(flashHit && reflecting() && hit >= 0.0001f){
                Draw.color(flashColor);
                Draw.alpha(hit * 0.5f);
                Draw.blend(Blending.additive);
                Fill.rect(x, y, tilesize * size, tilesize * size);
                Draw.blend();
                Draw.reset();

                hit = Mathf.clamp(hit - Time.delta / 10f);
            }

            if(sparking()){
                Draw.rect(sparkRegion, x, y);
            }
            if(reflecting()){
                Draw.rect(reflectRegion, x, y);
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
                String text = (time > 0 ? (Mathf.zero(dm) ? "Infinity" : Strings.autoFixed(DPS, 2)) : "---") + " DPS";
                PMDrawf.text(x, y + size * tilesize / 2f + 3f, team.color, text);
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            damage(bullet.team, bullet.damage() * bullet.type().buildingDamageMultiplier);

            hit = 1f;

            //create lightning if necessary
            if(sparking()){
                Lightning.create(team, lightningColor, lightningDamage, x, y, bullet.rotation() + 180f, lightningLength);
                lightningSound.at(tile, Mathf.random(0.9f, 1.1f));
            }

            //deflect bullets if necessary
            if(reflecting()){
                //slow bullets are not deflected
                if(bullet.vel().len() <= 0.1f || !bullet.type.reflectable) return true;

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
        public boolean collide(Bullet other){
            boolean wasDead = health <= 0;

            float damage = other.damage() * other.type().buildingDamageMultiplier;
            if(!other.type.pierceArmor){
                damage = Damage.applyArmor(damage, modes[4]);
            }

            damage(other.team, damage);
            Events.fire(bulletDamageEvent.set(self(), other));

            if(health <= 0 && !wasDead){
                Events.fire(new BuildingBulletDestroyEvent(self(), other));
            }
            return true;
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
            rebuild();

            table.table(t -> {
                t.add(gui);
                t.background(Styles.black6);
            }).top().expandY();
        }

        public void rebuild(){
            gui.clear();

            gui.table(b -> {
                addButton(b, Items.surgeAlloy.fullIcon, 0);
                addButton(b, Items.phaseFabric.fullIcon, 1);
                addButton(b, Items.plastanium.fullIcon, 2);
                addButton(b, Icon.modePvp.getRegion(), 3);
            });

            if(!DPSTesting()) return;
            gui.row();
            gui.table(a -> {
                a.image(new TextureRegionDrawable(Icon.defense.getRegion()), Pal.accent).size(32f);
                a.field(Integer.toString(modes[4]), TextFieldFilter.digitsOnly, text -> configure(Strings.parseFloat(text, 0)));
            });
        }

        public void addButton(Table t, TextureRegion icon, int index){
            ImageButton button = t.button(
                new TextureRegionDrawable(icon),
                Styles.clearNoneTogglei,
                32f, () -> {}
            ).tooltip(buttonLabels[index]).get();
            button.changed(() -> configure(index));
            button.update(() -> button.setChecked(activeMode(index)));
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
            return modes;
        }

        public void toggle(int i){
            modes[i] = (byte)(1 - modes[i]);

            //Reset DPS testing
            if(i == 3 && DPSTesting()){
                total = 0f;
                time = 0f;
                DPS = 0f;
                reset = resetTime;
            }
        }

        public boolean activeMode(int i){
            return modes[i] == 1;
        }

        public boolean sparking(){
            return modes[0] == 1;
        }

        public boolean reflecting(){
            return modes[1] == 1;
        }

        public boolean insulating(){
            return modes[2] == 1;
        }

        public boolean DPSTesting(){
            return modes[3] == 1;
        }

        public boolean armored(){
            return modes[4] > 0;
        }

        public void resetModes(){
            Arrays.fill(modes, 0);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            
            TypeIO.writeInts(write, modes);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 3){
                modes = TypeIO.readInts(read);
                return;
            }

            byte[] oldModes = new byte[4];
            if(revision == 1){
                read.b(oldModes, 0, 3);
            }
            if(revision == 2){
                read.b(oldModes);
            }
            for(int i = 0; i < 4; i++){
                modes[i] = oldModes[i];
            }
        }

        @Override
        public byte version(){
            return 3;
        }
    }
}
