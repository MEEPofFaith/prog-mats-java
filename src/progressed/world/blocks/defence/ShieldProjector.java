package progressed.world.blocks.defence;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import progressed.content.effects.*;
import progressed.util.*;

import static mindustry.Vars.*;

public class ShieldProjector extends ForceProjector{
    public static float maxShieldRange = 0;
    public float chargeTime = 900f, shieldCharge = 300f, phaseShieldCharge = 300f, strikeBlastResistance = 0.35f, phaseStrikeBlastResistance = 0.55f;
    public Effect chargeEffect = OtherFx.squareShieldRecharge;
    public Color lerpColor = Color.gray;
    public float lerpPercent = 0.75f;

    protected static final Cons<Bullet> shieldConsumer = bullet -> {
        if(bullet.team != paramEntity.team && bullet.type.absorbable){
            bullet.absorb();
            paramEffect.at(bullet);
            paramEntity.hit = 1f;
            paramEntity.buildup += bullet.damage;
        }
    };

    public ShieldProjector(String name){
        super(name);

        radius = 80f; //Make it square based because I'm too lazy to do math
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("shield");
        addBar("shield", (ShieldBuild entity) -> new Bar(
            () -> Core.bundle.get("stat.shieldhealth"),
            entity::getColor,
            () -> entity.broken ? 0f : entity.shieldf()
        ).blink(Color.white));

        addBar("charge", (ShieldBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.pm-charge", PMUtls.stringsFixed(entity.charge / chargeTime * 100f)),
            () -> entity.team.color,
            () -> entity.charge / chargeTime
        ));
    }

    @Override
    public void init(){
        clipSize = Math.max(clipSize, PMMathf.cornerDst(radius + phaseRadiusBoost + 3f) * 2f);
        maxShieldRange = Math.max(maxShieldRange, PMMathf.cornerDst(radius + phaseRadiusBoost + 3f) * 2f);

        super.init();
    }

    @Override
    protected TextureRegion[] icons(){
        return teamRegion.found() ? new TextureRegion[]{region, teamRegions[Team.sharded.id]} : new TextureRegion[]{region};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);

        Draw.color(Pal.gray);
        Lines.stroke(3f);
        Lines.square(x * tilesize + offset, y * tilesize + offset, radius + 1f);
        Draw.color(player.team().color);
        Lines.stroke(1f);
        Lines.square(x * tilesize + offset, y * tilesize + offset, radius);
        Draw.color();
    }

    public class ShieldBuild extends ForceBuild{
        public float charge;

        @Override
        public void onRemoved(){
            float radius = realRadius();
            if(!broken && radius > 1f) OtherFx.squareForceShrink.at(x, y, radius, team.color);
        }

        @Override
        public void updateTile(){
            boolean phaseValid = itemConsumer != null && itemConsumer.efficiency(this) > 0;

            phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(phaseValid), 0.1f);

            if(phaseValid && !broken && timer(timerUse, phaseUseTime) && efficiency > 0){
                consume();
            }

            radscl = Mathf.lerpDelta(radscl, broken ? 0f : warmup, 0.05f);

            if(Mathf.chanceDelta(buildup / shieldHealth * 0.1f)){
                Fx.reactorsmoke.at(x + Mathf.range(tilesize / 2f), y + Mathf.range(tilesize / 2f));
            }

            warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);

            if(buildup > 0){
                float scale = !broken ? cooldownNormal : cooldownBrokenBase;

                if(coolantConsumer != null){
                    if(coolantConsumer.efficiency(this) > 0){
                        coolantConsumer.update(this);
                        scale *= (cooldownLiquid * (1f + (liquids.current().heatCapacity - 0.4f) * 0.9f));
                    }
                }


                if(!broken){
                    charge += edelta() * scale;

                    if(charge >= chargeTime){
                        chargeEffect.at(x, y, realRadius(), team.color);
                        buildup -= realCharge() * delta();
                        charge %= chargeTime;
                    }
                }else{
                    buildup -= delta() * scale;
                }

                buildup = buildup < 0 ? 0 : buildup;
            }

            if(broken && buildup <= 0){
                broken = false;
            }

            if(buildup >= shieldHealth + phaseShieldBoost * phaseHeat && !broken){
                broken = true;
                buildup = shieldHealth;
                OtherFx.squareShieldBreak.at(x, y, realRadius(), team.color);
            }

            if(hit > 0f){
                hit -= 1f / 5f * Time.delta;
            }

            deflectBullets();
        }

        @Override
        public void deflectBullets(){
            float realRadius = realRadius();

            if(realRadius > 0 && !broken){
                paramEntity = this;
                paramEffect = absorbEffect;
                Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, shieldConsumer);
            }
        }

        public float realCharge(){
            return shieldCharge + phaseHeat * phaseShieldCharge;
        }

        @Override
        public void drawShield(){
            if(!broken){
                float radius = realRadius();

                Draw.z(Layer.shields);

                Draw.color(getColor());

                if(Core.settings.getBool("animatedshields")){
                    Fill.square(x, y, radius);
                }else{
                    Lines.stroke(1.5f);
                    Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    Fill.square(x, y, radius);
                    Draw.alpha(1f);
                    Lines.square(x, y, radius);
                    Draw.reset();
                }
            }

            Draw.reset();
        }

        public float realStrikeBlastResistance(){
            return 1f - (strikeBlastResistance + phaseHeat * phaseStrikeBlastResistance);
        }

        public float shieldf(){
            return 1f - buildup / (shieldHealth + phaseShieldBoost * phaseHeat);
        }

        public Color getColor(){
            return Tmp.c1.set(team.color).lerp(lerpColor, Mathf.absin(30f, shieldf() * lerpPercent)).lerp(Color.white, Mathf.clamp(hit));
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(charge);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                charge = read.f();
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }
}
