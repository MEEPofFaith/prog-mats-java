package progressed.world.blocks.payloads;

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
        Draw.z(Layer.blockUnder - 1f);
        Drawf.shadow(unit.fullIcon, tile.drawx() - elevation, tile.drawy() - elevation, tile.build.rotdeg() - 90f);
        Draw.z(Layer.block);
        Draw.rect(unit.fullIcon, tile.drawx(), tile.drawy(), tile.build.rotdeg() - 90f);
    }

    public class SentryBuild extends MissileBuild{
        public boolean activated;

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