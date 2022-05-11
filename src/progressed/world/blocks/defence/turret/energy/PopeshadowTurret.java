package progressed.world.blocks.defence.turret.energy;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import progressed.graphics.*;
import progressed.util.*;

import static arc.Core.*;

public class PopeshadowTurret extends PowerTurret{
    public float pullTime = 60f, closeTime = 90f;
    public float baseLightSpacing = 30f, holyLightDelay = 20f, holyLightSpacing = 10f;
    public float xOpen = 2f, yOpen = -3f;
    public Color lightColor = Pal.surge;
    public float lightOpactiy = 0.7f;
    public TextureRegion turretRegion, turretOutline, topRegion;
    public TextureRegion[] sideRegions = new TextureRegion[2], outlineRegions = new TextureRegion[2], cellRegions = new TextureRegion[8];

    public PopeshadowTurret(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        turretRegion = atlas.find(name + "-bottom");
        turretOutline = atlas.find(name + "-outline");
        topRegion = atlas.find(name + "-top");
        for(int i = 0; i < 2; i++){
            sideRegions[i] = atlas.find(name + "-side-" + i);
        }
        for(int i = 0; i < 2; i++){
            outlineRegions[i] = atlas.find(name + "-side-outline-" + i);
        }
        for(int i = 0; i < 8; i++){
            cellRegions[i] = atlas.find(name + "-cell-" + i);
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        Outliner.outlineRegion(packer, turretRegion, outlineColor, name + "-outline");
        Outliner.outlineRegions(packer, sideRegions, outlineColor, name + "-side-outline");
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("pm-reload", (PopeshadowTurretBuild entity) -> new Bar(
            () -> bundle.format("bar.pm-reload", PMUtls.stringsFixed(Mathf.clamp(entity.reloadCounter / reload) * 100f)),
            () -> entity.team.color,
            () -> Mathf.clamp(entity.reloadCounter / reload)
        ));

        addBar("pm-charge", (PopeshadowTurretBuild entity) -> new Bar(
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

            Tmp.v1.trns(rotation - 90, -openX, openY - curRecoil);
            Tmp.v2.trns(rotation - 90, openX, openY - curRecoil);
            float[] sXPre = {Tmp.v1.x, Tmp.v2.x};
            float[] sYPre = {Tmp.v1.y, Tmp.v2.y};
            float[] sX = {sXPre[0] + x, sXPre[1] + x};
            float[] sY = {sYPre[0] + y, sYPre[1] + y};
            
            recoilOffset.trns(rotation, -curRecoil);
            float tx = x + recoilOffset.x, ty = y + recoilOffset.y;

            Draw.rect(baseRegion, x, y);
            
            Draw.z(Layer.turret);

            Drawf.shadow(turretOutline, tx - elevation, ty - elevation, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Drawf.shadow(outlineRegions[i], sX[i] - elevation, sY[i] - elevation, rotation - 90f);
            }

            Draw.rect(turretOutline, tx, ty, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Draw.rect(outlineRegions[i], sX[i], sY[i], rotation - 90f);
            }

            Draw.rect(turretRegion, tx, ty, rotation - 90f);
            for(int i = 0; i < 2; i++){
                Draw.rect(sideRegions[i], sX[i], sY[i], rotation - 90f);
            }

            if(animation){
                //Oh god even more variable spam aaaaaaaa
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
                        PMDrawf.light(tx, ty, cellRegions[i], rotation - 90f, heatColor, cellLights[i] * lightOpactiy, false);
                    }
                }
                //Ending Lights
                for(int i = 5; i < 8; i++){
                    if(cellLights[i - 3] > 0.001f){
                        Draw.alpha(cellLights[i - 3]);
                        for(int j = 0; j < 2; j++){
                            Draw.rect(cellRegions[i], sX[j], sY[j], cellRegions[i].width / 4f * Mathf.signs[j], cellRegions[i].height / 4f, rotation - 90f);
                            PMDrawf.light(sX[j], sY[j], cellRegions[i], rotation - 90f, heatColor, cellLights[i - 3] * lightOpactiy, !Mathf.booleans[j]);
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
