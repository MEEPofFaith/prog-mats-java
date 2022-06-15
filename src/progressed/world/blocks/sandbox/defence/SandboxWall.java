package progressed.world.blocks.sandbox.defence;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.ui.*;

import static mindustry.Vars.*;

public class SandboxWall extends Wall{
    public float rotateSpeed = 6f, rotateRadius = 2.5f, iconSize = 3f;
    public float resetTime = 120f;

    protected String[] buttonLabels = {"Sparking", "Reflecting", "Insulation", "DPS Testing"};
    public TextureRegion colorRegion;
    public TextureRegion[] colorVariantRegions, icons = new TextureRegion[4];

    public SandboxWall(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        lightningDamage = 5000f;
        lightningLength = 10;
        flashHit = insulated = absorbLasers = true;
        schematicPriority = 10;
        configurable = saveConfig = update = noUpdateDisabled = true;

        config(byte[].class, (SandboxWallBuild tile, byte[] b) -> tile.modes.set(b));
        config(Integer.class, (SandboxWallBuild tile, Integer i) -> {
            tile.modes.toggle(i);
            if(i == 1 && tile.modes.active(i)){
                tile.hit = 0f;
            }
        });

        configClear((SandboxWallBuild tile) -> tile.modes.reset());
    }

    @Override
    public void load(){
        super.load();

        colorRegion = Core.atlas.find(name + "-color");
        
        if(variants != 0){
            colorVariantRegions = new TextureRegion[variants];

            for(int i = 0; i < variants; i++){
                colorVariantRegions[i] = Core.atlas.find(name + "-color-" + i);
            }
            colorRegion = colorVariantRegions[0];
        }

        icons[0] = Items.surgeAlloy.fullIcon;
        icons[1] = Items.phaseFabric.fullIcon;
        icons[2] = Items.plastanium.fullIcon;
        icons[3] = Core.atlas.white();
        //Icons are null at this point, load it later.
        Events.on(ClientLoadEvent.class, e -> icons[3] = Icon.modePvp.getRegion());
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
        if(req.config instanceof byte[] b){
            //draw floating items to represent active mode
            int num = 0;
            int amount = b[0] + b[1] + b[2] + b[3];

            for(int i = 0; i < 4; i++){
                if(b[i] == 1){
                    float rot = 90f + 360f / amount * num;
                    Draw.rect(
                        icons[i],
                        req.drawx() + Angles.trnsx(rot, rotateRadius),
                        req.drawy() + Angles.trnsy(rot, rotateRadius),
                        iconSize, iconSize, 0f
                    );
                    num++;
                }
            }
        }
    }

    public class SandboxWallBuild extends WallBuild{
        public float total, reset = resetTime, time;
        WallData modes = new WallData();

        @Override
        public void updateTile(){
            super.updateTile();

            time += Time.delta;
            reset += Time.delta;

            if(reset >= resetTime){
                total = 0f;
                time = 0f;
            }
        }

        @Override
        public void draw(){
            float speed = Core.settings.getInt("pm-strobespeed") / 2f;

            if(variants == 0){
                Draw.rect(region, x, y);
                Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
                Draw.rect(colorRegion, x, y);
            }else{
                int variant = Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1));
                Draw.rect(variantRegions[variant], x, y);
                Draw.color(Tmp.c1.set(Color.red).shiftHue(Time.time * speed), 1f);
                Draw.rect(colorVariantRegions[variant], x, y);
            }
            Draw.reset();

            //draw flashing white overlay if enabled
            if(flashHit && modes.phase && hit >= 0.0001f){
                Draw.color(flashColor);
                Draw.alpha(hit * 0.5f);
                Draw.blend(Blending.additive);
                Fill.rect(x, y, tilesize * size, tilesize * size);
                Draw.blend();
                Draw.reset();

                hit = Mathf.clamp(hit - Time.delta / 10f);
            }

            //draw floating items to represent active mode
            int num = 0;
            int amount = modes.amount();
            for(int i = 0; i < 4; i++){
                if(modes.active(i)){
                    float rot = Time.time * rotateSpeed % 360f + 360f / amount * num;
                    Draw.rect(
                        icons[i],
                        x + Angles.trnsx(rot, rotateRadius),
                        y + Angles.trnsy(rot, rotateRadius),
                        iconSize, iconSize, 0f);
                    num++;
                }
            }

            if(modes.dpsTesting && time > 0){
                Draw.z(Layer.overlayUI);
                String text = Strings.autoFixed((total / time) * 60f, 2) + " DPS";
                PMDrawf.text(x, y + size * tilesize / 2f + 3f, team.color, text);
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            damage(bullet.team, bullet.damage() * bullet.type().buildingDamageMultiplier);

            hit = 1f;

            //create lightning if necessary
            if(modes.surge){
                Lightning.create(team, lightningColor, lightningDamage, x, y, bullet.rotation() + 180f, lightningLength);
                lightningSound.at(tile, Mathf.random(0.9f, 1.1f));
            }

            //deflect bullets if necessary
            if(modes.phase){
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
            return modes.plast;
        }

        @Override
        public boolean isInsulated(){
            return modes.plast;
        }

        @Override
        public void damage(float damage){
            reset = 0f;
            total += damage;
            lastDamageTime = Time.time;
        }

        @Override
        public void kill(){
            //haha no
        }

        @Override
        public void buildConfiguration(Table table){
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            group.setMaxCheckCount(4);
            Table cont = new Table();
            cont.defaults().size(40);

            for(int i = 0; i < 4; i++){
                addButton(cont, group, icons[i], i);
            }

            table.add(cont);
        }

        public void addButton(Table t, ButtonGroup<ImageButton> group, TextureRegion icon, int index){
            ImageButton button = t.button(
                new TextureRegionDrawable(icon),
                Styles.clearTogglei,
                32f, () -> {}
            ).group(group).tooltip(buttonLabels[index]).get();
            button.changed(() -> configure(index));
            button.update(() -> button.setChecked(modes.active(index)));
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(this == other){
                deselect();
                modes.reset();
                return false;
            }

            return true;
        }

        @Override
        public byte[] config(){
            return modes.toByteArray();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            
            write.b(modes.toByteArray());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision == 1){
                modes.set(read.b(), read.b(), read.b(), (byte)0);
            }
            if(revision >= 2){
                modes.set(read.b(), read.b(), read.b(), read.b());
            }
        }

        @Override
        public byte version(){
            return 2;
        }
    }

    public static class WallData{
        public boolean surge, phase, plast, dpsTesting;

        public WallData(){}

        public void set(boolean surge, boolean phase, boolean plast, boolean dpsTesting){
            this.surge = surge;
            this.phase = phase;
            this.plast = plast;
            this.dpsTesting = dpsTesting;
        }

        public void set(byte surge, byte phase, byte plast, byte dpsTesting){
            set(surge == 1, phase == 1, plast == 1, dpsTesting == 1);
        }

        public void set(byte[] data){
            set(data[0], data[1], data[2], data[3]);
        }

        public void toggle(int i){
            if(i == 0){
                surge = !surge;
            }else if(i == 1){
                phase = !phase;
            }else if(i == 2){
                plast = !plast;
            }else if(i == 3){
                dpsTesting = !dpsTesting;
            }
        }

        public void reset(){
            surge = false;
            phase = false;
            plast = false;
            dpsTesting = false;
        }

        public boolean active(int i){
            if(i == 0){
                return surge;
            }else if(i == 1){
                return phase;
            }else if(i == 2){
                return plast;
            }else if(i == 3){
                return dpsTesting;
            }

            return false;
        }

        public int amount(){
            return (surge ? 1 : 0) + (phase ? 1 : 0) + (plast ? 1 : 0) + (dpsTesting ? 1 : 0);
        }

        public byte[] toByteArray(){
            return new byte[]{
                (byte)(surge ? 1 : 0),
                (byte)(phase ? 1 : 0),
                (byte)(plast ? 1 : 0),
                (byte)(dpsTesting ? 1 : 0)
            };
        }
    }
}
