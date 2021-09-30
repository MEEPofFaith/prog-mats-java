package progressed.world.blocks.sandbox;

import arc.*;
import arc.flabel.*;
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
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class SandboxWall extends Wall{
    public float rotateSpeed = 6f, rotateRadius, iconSize;

    protected Item[] iconItems = {Items.surgeAlloy,  Items.phaseFabric, Items.plastanium};
    public TextureRegion colorRegion;
    public TextureRegion[] colorVariantRegions;

    public SandboxWall(String name){
        super(name);
        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.empty);
        alwaysUnlocked = true;

        health = 2147483647;
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
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.health);

        stats.add(Stat.health, t -> t.add(new FLabel("{wave}{rainbow}" + Core.bundle.get("pm-infinity"))));
    }

    @Override
    public void drawRequestConfig(BuildPlan req, Eachable<BuildPlan> list){
        if(req.config instanceof byte[] b){
            //draw floating items to represent active mode
            int num = 0;
            int amount = b[0] + b[1] + b[2];
            for(int i = 0; i < 3; i++){
                if(b[i] == 1){
                    float rot = 90f + 360f / amount * num;
                    Draw.rect(iconItems[i].fullIcon, req.drawx() + Angles.trnsx(rot, rotateRadius), req.drawy() + Angles.trnsy(rot, rotateRadius), iconSize, iconSize, 0f);
                    num++;
                }
            }
        }
    }

    public class SandboxWallBuild extends WallBuild{
        WallData modes = new WallData();

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
            for(int i = 0; i < 3; i++){
                if(modes.active(i)){
                    float rot = Time.time * rotateSpeed % 360f + 360f / amount * num;
                    Draw.rect(iconItems[i].fullIcon, x + Angles.trnsx(rot, rotateRadius), y + Angles.trnsy(rot, rotateRadius), iconSize, iconSize, 0f);
                    num++;
                }
            }
        }

        @Override
        public boolean collision(Bullet bullet){
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
        public void damage(float damage){
            //haha no
        }

        @Override
        public void damage(float amount, boolean withEffect){
            //haha no
        }

        @Override
        public void kill(){
            //haha no
        }

        @Override
        public void buildConfiguration(Table table){
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            group.setMaxCheckCount(3);
            Table cont = new Table();
            cont.defaults().size(40);

            for(int i = 0; i < 3; i++){
                int ii = i;
                ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 40, () -> {}).group(group).get();
                button.changed(() -> configure(ii));
                button.getStyle().imageUp = new TextureRegionDrawable(iconItems[i].fullIcon, 8f * 3f);
                button.update(() -> button.setChecked(modes.active(ii)));
            }

            table.add(cont);
        }

        @Override
        public boolean onConfigureTileTapped(Building other){
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

            if(revision >= 1){
                modes.set(read.b(), read.b(), read.b());
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }

    public static class WallData{
        public boolean surge, phase, plast;

        public WallData(){}

        public void set(boolean surge, boolean phase, boolean plast){
            this.surge = surge;
            this.phase = phase;
            this.plast = plast;
        }

        public void set(byte surge, byte phase, byte plast){
            set(surge == 1, phase == 1, plast == 1);
        }

        public void set(byte[] data){
            set(data[0], data[1], data[2]);
        }

        public void toggle(int i){
            if(i == 0){
                surge = !surge;
            }else if(i == 1){
                phase = !phase;
            }else if(i == 2){
                plast = !plast;
            }
        }

        public void reset(){
            surge = false;
            phase = false;
            plast = false;
        }

        public boolean active(int i){
            if(i == 0){
                return surge;
            }else if(i == 1){
                return phase;
            }else if(i == 2){
                return plast;
            }

            return false;
        }

        public int amount(){
            return (surge ? 1 : 0) + (phase ? 1 : 0) + (plast ? 1 : 0);
        }

        public byte[] toByteArray(){
            return new byte[]{
                (byte)(surge ? 1 : 0),
                (byte)(phase ? 1 : 0),
                (byte)(plast ? 1 : 0)
            };
        }
    }
}
