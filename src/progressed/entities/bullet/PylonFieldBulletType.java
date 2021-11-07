package progressed.entities.bullet;

import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.*;
import progressed.entities.*;
import progressed.world.blocks.defence.*;

public class PylonFieldBulletType extends BulletType{
    public MagmaPylon pylon;
    public float radius = 5f * 8f;
    public int amount = 1;

    public PylonFieldBulletType(){
        super();

        absorbable = hittable = false;
        speed = 0;
        collides = false;
        hitEffect = despawnEffect = shootEffect = smokeEffect = Fx.none;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        PylonFieldData data = new PylonFieldData();
        PMDamage.trueEachTile(b.x, b.y, radius, t -> data.tiles.add(t));
        data.tiles.shuffle();
        b.data = data;
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(b.data instanceof PylonFieldData data && b.timer(1, lifetime / amount)){
            for(int i = 0; i < data.tiles.size; i++){
                Tile t = data.tiles.get(i);
                if(t.block() == Blocks.air){
                    boolean occupied = Groups.unit.intersect(t.worldx(), t.worldy(), 1, 1).contains(Flyingc::isGrounded);
                    if(!occupied){
                        data.tiles.remove(t);
                        t.setNet(pylon, b.team, 0);
                        break;
                    }
                }
            }
        }
    }

    public static class PylonFieldData{
        public Seq<Tile> tiles = new Seq<>();
    }
}