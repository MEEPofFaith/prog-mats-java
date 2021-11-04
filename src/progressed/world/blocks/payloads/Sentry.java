package progressed.world.blocks.payloads;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class Sentry extends Missile{
    public UnitType unit;

    public Sentry(String name){
        super(name);
        rotate = true;
        configurable = true;
        breakSound = destroySound = Sounds.none;
    }

    @Override
    public void init(){
        super.init();

        health = (int)unit.health;
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{unit.fullIcon};
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(unit.fullIcon, req.drawx(), req.drawy(), req.rotation * 90 - 90f);
    }

    @Override
    public void drawBase(Tile tile){
        if(tile.build != null){
            tile.build.draw();
        }
    }

    public class SentryBuild extends MissileBuild{
        public boolean activated;

        @Override
        public void draw(){
            Draw.z(Layer.blockUnder - 1f);
            Drawf.shadow(unit.fullIcon, x - elevation, y - elevation, drawRot());
            Draw.z(Layer.block);
            drawUnderWeapons();
            if(unit.drawBody && Core.atlas.isFound(unit.outlineRegion)) Draw.rect(unit.outlineRegion, x, y, drawRot());
            drawWeaponOutlines();
            if(unit.drawBody) Draw.rect(unit.region, x, y, drawRot());
            if(unit.drawCell) drawCell();
            drawTopWeapons();
        }

        public void drawCell(){
            float f = Mathf.clamp(healthf());
            Tmp.c1.set(Color.black).lerp(team.color, f + Mathf.absin(Time.time, Math.max(f * 5f, 1f), 1f - f));
            Draw.color(Tmp.c1);
            Draw.rect(unit.cellRegion, x, y, drawRot());
            Draw.color();
        }

        public void drawWeaponOutlines(){
            for(Weapon w : unit.weapons){
                if(!w.top){
                    float
                        wx = x + Angles.trnsx(drawRot(), w.x * Draw.xscl, w.y * Draw.yscl),
                        wy = y + Angles.trnsy(drawRot(), w.x * Draw.xscl, w.y * Draw.yscl);
                    Draw.rect(
                        w.outlineRegion,
                        wx, wy,
                        w.outlineRegion.width * xscl() * -Mathf.sign(w.flipSprite),
                        w.outlineRegion.height * yscl(),
                        drawRot()
                    );
                }
            }
        }

        public void drawUnderWeapons(){
            for(Weapon weapon : unit.weapons){
                if(weapon.layerOffset >= 0) continue;
                drawWeapon(weapon);
            }
        }

        public void drawTopWeapons(){
            for(Weapon weapon : unit.weapons){
                if(weapon.layerOffset < 0) continue;
                drawWeapon(weapon);
            }
        }

        public void drawWeapon(Weapon w){
            float z = Draw.z();
            Draw.z(z + w.layerOffset);
            float
                wx = x + Angles.trnsx(drawRot(), w.x * Draw.xscl, w.y * Draw.yscl),
                wy = y + Angles.trnsy(drawRot(), w.x * Draw.xscl, w.y * Draw.yscl);

            if(w.shadow > 0) Drawf.shadow(wx, wy, w.shadow);
            if(w.top) Draw.rect(w.outlineRegion, wx, wy,
                w.outlineRegion.width * xscl() * -Mathf.sign(w.flipSprite),
                w.outlineRegion.height * yscl(),
                drawRot()
            );
            Draw.rect(
                w.region, wx, wy,
                w.region.width * xscl() * -Mathf.sign(w.flipSprite),
                w.region.height * yscl(),
                drawRot()
            );

            Draw.z(z);
        }

        public float drawRot(){
            return rotdeg() - 90f;
        }

        public float xscl(){
            return Draw.scl * Draw.xscl;
        }

        public float yscl(){
            return Draw.scl * Draw.yscl;
        }

        @Override
        public void drawCracks(){
            //no
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4){
            if(type == LAccess.enabled && !Mathf.zero((float)p1)){
                spawn();
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void buildConfiguration(Table table){
            Table cont = new Table();
            cont.defaults().size(40);

            ImageButton button = cont.button(Tex.whiteui, Styles.clearToggleTransi, 24, this::spawn).get();
            button.getStyle().imageUp = Icon.upload;

            table.add(cont);
        }

        public void spawn(){
            if(!activated){
                Unit spawned = unit.spawn(team, self());
                spawned.rotation(rotdeg());
                spawned.health = unit.health * (health / block.health);
                activated = true;
                kill();
            }
        }

        @Override
        public void onDestroyed(){
            if(!activated){
                Damage.dynamicExplosion(x, y, 0f, block.baseExplosiveness * 3.5f, 0f, tilesize * block.size / 2f, state.rules.damageExplosions, block.destroyEffect);

                if(!floor().solid && !floor().isLiquid){
                    Effect.rubble(x, y, block.size);
                }
            }

            if(explosion != null){
                explode();
            }
        }
    }
}