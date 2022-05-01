package progressed.world.blocks.payloads;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.world.meta.*;

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
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, s -> {
            PMStatValues.infoButton(s, unit, 4f * 8f);
        });
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{unit.fullIcon};
    }

    @Override
    public void onUnlock(){
        super.onUnlock();
        unit.unlock();
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
        public Unit unitDrawer;
        public boolean activated;

        @Override
        public Building create(Block block, Team team){
            Building b = super.create(block, team);

            unitDrawer = unit.create(team);
            unitDrawer.elevation = 0;
            unitDrawer.set(x, y);
            unitDrawer.rotation(drawRot() + 90f);

            return b;
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            unitDrawer.rotation(drawRot() + 90f);
        }

        @Override
        public void remove(){
            super.remove();
            if(activated) return;
            unitDrawer.remove();
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(activated) return;
            unitDrawer.set(x, y);
            unitDrawer.rotation(drawRot() + 90f);
            unitDrawer.health(health);
        }

        @Override
        public void draw(){
            Draw.z(Layer.blockUnder - 1f);
            Drawf.shadow(unit.fullIcon, x - elevation, y - elevation, drawRot());
            Draw.z(Layer.block);
            unitDrawer.draw();
        }

        public float drawRot(){
            if(isPayload()) return 0f;
            return rotdeg() - 90f;
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
                unitDrawer.add();
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
