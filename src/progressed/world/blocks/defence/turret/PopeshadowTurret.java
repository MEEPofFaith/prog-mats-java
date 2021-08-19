package progressed.world.blocks.defence.turret;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.util.*;

import static arc.Core.*;

public class PopeshadowTurret extends PowerTurret{
    public float pullTime = 60f, closeTime = 90f;
    public float baseLightSpacing = 30f, holyLightDelay = 20f, holyLightSpacing = 10f;
    public float xOpen = 2f, yOpen = -3f;
    public Color lightColor = Pal.surge;
    public float lightOpactiy = 0.7f;
    public float[][] lightCoordsSet1 = {
        {21f / 4f, -65f / 4f, 26f / 4f, -60f / 4f, 3f},
        {33f / 4f, -40f / 4f, 38f / 4f, -35f / 4f, 3f},
        {63f / 4f, -55f / 4f, 63f / 4f, -46f / 4f, 3f},
        {71f / 4f, -47f / 4f, 71f / 4f, -38f / 4f, 3f},
        {61f / 4f, -6f / 4f, 61f / 4f, 2f / 4f, 4f}
    };
    public float[][] lightCoordsSet2 = {
        {21f / 4f, 39f / 4f, 21f / 4f, 47f / 4f, 3f},
        {21f / 4f, 52f / 4f, 21f / 4f, 60f / 4f, 3f},
        {21f / 4f, 65f / 4f, 21f / 4f, 73f / 4f, 3f}
    };

    public TextureRegion bottomRegion, topRegion;
    public TextureRegion[] sideRegions = new TextureRegion[2], outlineRegions = new TextureRegion[3], cellRegions = new TextureRegion[8];

    public PopeshadowTurret(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        bottomRegion = atlas.find(name + "-bottom");
        topRegion = atlas.find(name + "-top");
        for(int i = 0; i < 2; i++){
            sideRegions[i] = atlas.find(name + "-side-" + i);
        }
        for(int i = 0; i < 3; i++){
            outlineRegions[i] = atlas.find(name + "-outline-" + i);
        }
        for(int i = 0; i < 8; i++){
            cellRegions[i] = atlas.find(name + "-cell-" + i);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
            baseRegion,
            atlas.find(name + "-icon")
        };
    }

    @Override
    public void setBars(){
        super.setBars();
        
        bars.add("pm-reload", (PopeshadowTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reload / reloadTime) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reload / reloadTime)
        ));

        bars.add("pm-charge", (PopeshadowTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-charge", PMUtls.stringsFixed(Mathf.clamp(entity.charge) * 100f)),
            () -> Color.gold,
            () -> entity.charge
        ));
    }

    public class PopeshadowTurretBuild extends PowerTurretBuild{
        protected float chargeTimer, charge;
        protected boolean animation;

        @Override
        public void draw(){
            float totalTime = chargeTime + closeTime;
            float openAmount = Mathf.curve(chargeTimer, 0f, pullTime);
            float closeAmount = Mathf.curve(chargeTimer, chargeTime, totalTime);
            float openX = xOpen * Interp.pow2Out.apply(openAmount) - xOpen * Interp.pow2In.apply(closeAmount);
            float openY = yOpen * Interp.pow5In.apply(openAmount) - yOpen * Interp.pow5Out.apply(closeAmount);

            Tmp.v1.trns(rotation - 90, -openX, openY - recoil);
            Tmp.v2.trns(rotation - 90, openX, openY - recoil);
            float[] sXPre = {Tmp.v1.x, Tmp.v2.x};
            float[] sYPre = {Tmp.v1.y, Tmp.v2.y};
            float[] sX = {sXPre[0] + x, sXPre[1] + x};
            float[] sY = {sYPre[0] + y, sYPre[1] + y};
            
            tr2.trns(rotation, -recoil);
            float tx = x + tr2.x, ty = y + tr2.y;

            Draw.rect(baseRegion, x, y);
            
            Draw.z(Layer.turret);

            Drawf.shadow(outlineRegions[0], tx - elevation, ty - elevation, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Drawf.shadow(outlineRegions[i], sX[i] - elevation, sY[i] - elevation, rotation - 90f);
            }

            Draw.rect(outlineRegions[0], tx, ty, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Draw.rect(outlineRegions[i + 1], sX[i], sY[i], rotation - 90f);
            }

            Draw.rect(bottomRegion, tx, ty, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Draw.rect(sideRegions[i], sX[i], sY[i], rotation - 90f);
            }

            if(animation){
                //Oh god even more variable aaaaaaaa
                float[] cellLights = {
                    Mathf.curve(chargeTimer, pullTime, (pullTime + baseLightSpacing)) - closeAmount,
                    Mathf.curve(chargeTimer, (pullTime + baseLightSpacing), (pullTime + 2 * baseLightSpacing)) - closeAmount,
                    Mathf.curve(chargeTimer, (chargeTime - holyLightDelay - 4 * holyLightSpacing), (chargeTime - holyLightDelay - 3 * holyLightSpacing)) - closeAmount,
                    Mathf.curve(chargeTimer, (chargeTime - holyLightDelay - 3 * holyLightSpacing), (chargeTime - holyLightDelay - 2 * holyLightSpacing)) - closeAmount,
                    Mathf.curve(chargeTimer, (chargeTime - holyLightDelay - 2 * holyLightSpacing), (chargeTime - holyLightDelay - holyLightSpacing)) - closeAmount
                };

                Draw.color(lightColor);
                //Starting Lights
                for(int i = 0; i < 5; i++){
                    if(cellLights[i] > 0.001f){
                        Draw.alpha(cellLights[i]);
                        Draw.rect(cellRegions[i], tx, ty, rotation - 90f);
                        for(int j = 0; j < 2; j++){
                            Tmp.v1.trns(rotation - 90f, lightCoordsSet1[i][0] * Mathf.signs[j], lightCoordsSet1[i][1]);
                            Tmp.v1.add(tx, ty);
                            Tmp.v2.trns(rotation - 90f, lightCoordsSet1[i][2] * Mathf.signs[j], lightCoordsSet1[i][3]);
                            Tmp.v2.add(tx, ty);
                            Drawf.light(team, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, lightCoordsSet1[i][4], heatColor, cellLights[i] * lightOpactiy);
                        }
                    }
                }
                //Ending Lights
                for(int i = 5; i < 8; i++){
                    if(cellLights[i - 3] > 0.001f){
                        Draw.alpha(cellLights[i - 3]);
                        for(int j = 0; j < 2; j++){
                            Draw.rect(cellRegions[i], sX[j], sY[j], cellRegions[i].width / 4f * Mathf.signs[j], cellRegions[i].height / 4f, rotation - 90f);
                            Tmp.v5.trns(rotation - 90f, lightCoordsSet2[i - 5][0] * Mathf.signs[j], lightCoordsSet2[i - 5][1]);
                            Tmp.v5.add(sX[j], sY[j]);
                            Tmp.v6.trns(rotation - 90f, lightCoordsSet2[i - 5][2] * Mathf.signs[j], lightCoordsSet2[i - 5][3]);
                            Tmp.v6.add(sX[j], sY[j]);
                            Drawf.light(team, Tmp.v5.x, Tmp.v5.y, Tmp.v6.x, Tmp.v6.y, lightCoordsSet2[i - 5][4], heatColor, cellLights[i - 3] * lightOpactiy);
                        }
                    }
                }
                Draw.color();
            }

            Draw.rect(topRegion, tx, ty, rotation - 90f);

            if(atlas.isFound(heatRegion) && heat > 0.00001f){
                Draw.color(heatColor, heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, tx, ty, rotation - 90f);
                Draw.blend();
                Draw.color();
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            float totalTime = chargeTime + closeTime;
            if(animation && chargeTimer < totalTime){
                chargeTimer += Time.delta;
            }else if(animation && chargeTimer >= totalTime){
                chargeTimer = 0;
                animation = false;
            }

            if(charging){
                charge = Mathf.clamp(charge + Time.delta / chargeTime);
            }else{
                charge = 0;
            }
        }

        @Override
        protected void updateCooling(){
            if(!animation) super.updateCooling();
        }

        @Override
        protected void updateShooting(){
            if(!animation) super.updateShooting();
        }

        @Override
        protected void shoot(BulletType type){
            animation = true;
            super.shoot(type);
        }

        @Override
        public boolean shouldTurn(){
            return true;
        }
    }
}