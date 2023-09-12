package progressed.world.blocks.defence;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import progressed.content.effects.*;
import progressed.content.effects.Pseudo3DFx.*;
import progressed.entities.bullet.pseudo3d.*;
import progressed.entities.bullet.pseudo3d.ArcBulletType.*;
import progressed.util.*;

import static mindustry.Vars.*;
import static progressed.graphics.DrawPseudo3D.*;

public class ShieldProjector extends ForceProjector{
    public float chargeTime = 900f, shieldCharge = 300f, phaseShieldCharge = 300f;
    public float height = 5f * tilesize, phaseHeightBoost = 5f * tilesize;
    public Effect chargeEffect = Pseudo3DFx.shieldRecharge;
    public Effect breakEffect = Pseudo3DFx.shieldBreak;
    public Effect shrinkEffect = Pseudo3DFx.shieldShrink;

    public ShieldProjector(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("shield");
        addBar("shield", (ShieldBuild entity) -> new Bar(
            () -> Core.bundle.get("stat.shieldhealth"),
            () -> entity.team.color,
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

        super.init();
    }

    @Override
    protected TextureRegion[] icons(){
        return teamRegion.found() ? new TextureRegion[]{region, teamRegions[Team.sharded.id]} : new TextureRegion[]{region};
    }

    public static float[] getShieldCorners(float x, float y, float radius, int sides, float shieldRotation){
        float[] corners = new float[sides * 2];
        float space = 360f / sides;
        for(int i = 0; i < sides; i++){
            float a = space * i + shieldRotation;
            corners[i * 2] = x + Angles.trnsx(a, radius);
            corners[i * 2 + 1] = y + Angles.trnsy(a, radius);
        }
        return corners;
    }

    public static void drawCorners(float[] corners, float height, int sides, CornersDraw draw){
        for(int i = 0; i < sides; i++){
            float x1 = corners[i * 2];
            float y1 = corners[i * 2 + 1];
            int next = (i + 1) % sides;
            float x2 = corners[next * 2];
            float y2 = corners[next * 2 + 1];
            float x3 = xHeight(x2, height);
            float y3 = yHeight(y2, height);
            float x4 = xHeight(x1, height);
            float y4 = yHeight(y1, height);
            draw.draw(x1, y1, x2, y2, x3, y3, x4, y4);
        }
    }

    public class ShieldBuild extends ForceBuild{
        public float charge;

        @Override
        public void onRemoved(){
            float radius = realRadius();
            if(!broken && radius > 1f) shrinkEffect.at(x, y, realRadius(), team.color, new ShieldSizeData(sides, shieldRotation, realHeight()));
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
                        chargeEffect.at(x, y, 0f, team.color, self());
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
                breakEffect.at(x, y, realRadius(), team.color, new ShieldSizeData(sides, shieldRotation, realHeight()));
                if(team != state.rules.defaultTeam){
                    Events.fire(Trigger.forceProjectorBreak);
                }
            }

            if(hit > 0f){
                hit -= 1f / 5f * Time.delta;
            }

            deflectBullets();
        }

        @Override
        public void deflectBullets(){
            float realRadius = realRadius();
            float realHeight = realHeight();

            if(realRadius > 0 && !broken){
                Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, b -> {
                    if(b.team != team && Intersector.isInRegularPolygon(sides, x, y, realRadius(), shieldRotation, b.x, b.y)){
                        if(b.type instanceof ArcBulletType a){
                            ArcBulletData data = (ArcBulletData)b.data;
                            if(a.zAbsorbable && data.z <= realHeight){
                                b.absorb();
                                a.absorbEffect.at(b.x, b.y, data.z, a.absorbEffectColor);
                                hit = 1f;
                                buildup += b.damage;
                            }
                        }else if(b.type.absorbable){
                            b.absorb();
                            absorbEffect.at(b);
                            hit = 1f;
                            buildup += b.damage;
                        }
                    }
                });
            }
        }

        public float realHeight(){
            return (height + phaseHeat * phaseHeightBoost) * radscl;
        }

        public float realCharge(){
            return shieldCharge + phaseHeat * phaseShieldCharge;
        }

        @Override
        public void drawShield(){
            if(!broken){
                float radius = realRadius();
                if(radius > 0.001f){
                    Draw.color(team.color, Color.white, Mathf.clamp(hit));
                    float[] corners = getShieldCorners(radius);

                    if(renderer.animateShields){
                        Draw.z(Layer.shields + 0.001f * hit);
                    }else{
                        Draw.z(Layer.shields);
                        Lines.stroke(1.5f);
                        Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    }
                    Fill.polyBegin();
                    drawCorners(corners, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
                        Fill.quad(x1, y1, x2, y2, x3, y3, x4, y4);
                        Fill.polyPoint(x4, y4);
                    });
                    Fill.polyEnd();

                    if(renderer.animateShields){
                        Draw.z(Layer.shields + 1.01f);
                    }else{
                        Draw.z(Layer.shields + 0.001f);
                    }
                    Draw.alpha(1f);
                    drawCorners(corners, (x1, y1, x2, y2, x3, y3, x4, y4) -> {
                        //Lines.quad(x1, y1, x2, y2, x3, y3, x4, y4); //Corners expand out wildly for some reason.
                        Lines.line(x1, y1, x2, y2);
                        Lines.line(x1, y1, x4, y4);
                        Lines.line(x2, y2, x3, y3);
                        Lines.line(x4, y4, x3, y3);
                    });
                    Draw.color();
                }
            }

            Draw.reset();
        }

        public float[] getShieldCorners(float radius){
            return ShieldProjector.getShieldCorners(x, y, radius, sides, shieldRotation);
        }

        public float[] getShieldCorners(){
            return getShieldCorners(realRadius());
        }

        public void drawCorners(float[] corners, float height, CornersDraw draw){
            ShieldProjector.drawCorners(corners, height, sides, draw);
        }

        public void drawCorners(float[] corners, CornersDraw draw){
           drawCorners(corners, realHeight(), draw);
        }

        public float shieldf(){
            return 1f - buildup / (shieldHealth + phaseShieldBoost * phaseHeat);
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

    public interface CornersDraw{
        void draw(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);
    }
}
