package progressed.type.weapons;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import progressed.graphics.*;

/** @author MEEP */
public class RocketWeapon extends Weapon{
    public RocketWeapon(String name){
        super(name);
        mountType = RocketMount::new;

        rotate = true;
        shootX = shootY = 0f;
        shots = 1;
        inaccuracy = velocityRnd = 0;
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);

        RocketMount rMount = (RocketMount)mount;
        if(loadf(mount) < 0.99f) rMount.total += Time.delta * unit.reloadMultiplier;
        rMount.warmup = Mathf.lerpDelta(rMount.warmup, Mathf.num(mount.reload / reload > 0.001), 0.15f);
    }

    public void drawOutline(Unit unit, WeaponMount mount){
        RocketMount rMount = (RocketMount)mount;

        float
            rotation = unit.rotation - 90,
            weaponRotation  = rotation + (rotate ? mount.rotation : 0),
            wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -mount.recoil),
            wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -mount.recoil);

        if(rMount.warmup > 0.01f || loadf(mount) > 0.01f){
            if(outlineRegion.found() && !top){
                Draw.draw(Draw.z(), () -> {
                    PMDrawf.vertConstruct(wx, wy, outlineRegion, weaponRotation, loadf(mount), rMount.warmup, rMount.total);
                });
            }
        }else{
            if(outlineRegion.found()){
                Draw.rect(outlineRegion,
                    wx, wy,
                    outlineRegion.width * Draw.scl * -Mathf.sign(flipSprite),
                    region.height * Draw.scl,
                    weaponRotation);
            }
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        RocketMount rMount = (RocketMount)mount;

        float rotation = unit.rotation - 90,
            weaponRotation  = rotation + (rotate ? mount.rotation : 0),
            wx = unit.x + Angles.trnsx(rotation, x, y),
            wy = unit.y + Angles.trnsy(rotation, x, y);

        if(shadow > 0){
            Drawf.shadow(wx, wy, shadow, 1f - loadf(mount));
        }

        if(rMount.warmup > 0.01f || loadf(mount) > 0.01f){
            Draw.draw(Draw.z(), () -> {
                if(outlineRegion.found() && top){
                    Draw.draw(Draw.z(), () -> {
                        PMDrawf.vertConstruct(wx, wy, outlineRegion, weaponRotation, loadf(mount), rMount.warmup, rMount.total);
                    });
                }
                PMDrawf.vertConstruct(wx, wy, region, weaponRotation, loadf(mount), rMount.warmup, rMount.total);
            });
        }else{
            if(outlineRegion.found() && top){
                Draw.rect(outlineRegion,
                    wx, wy,
                    outlineRegion.width * Draw.scl * -Mathf.sign(flipSprite),
                    region.height * Draw.scl,
                    weaponRotation);
            }

            Draw.rect(region,
                wx, wy,
                region.width * Draw.scl * -Mathf.sign(flipSprite),
                region.height * Draw.scl,
                weaponRotation);
        }
    }

    public float loadf(WeaponMount mount){
        return 1f - (mount.reload / reload);
    }

    public static class RocketMount extends WeaponMount{
        float total, warmup;

        public RocketMount(Weapon weapon){
            super(weapon);
        }
    }
}